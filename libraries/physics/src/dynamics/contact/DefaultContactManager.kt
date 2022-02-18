package org.dyn4j.dynamics.contact

import org.dyn4j.collision.Collisions.getEstimatedCollisionPairs
import org.dyn4j.collision.manifold.ManifoldPointId
import org.dyn4j.dynamics.Capacity
import org.dyn4j.dynamics.Settings
import org.dyn4j.geometry.Shiftable
import org.dyn4j.geometry.Vector2

open class DefaultContactManager : ContactManager, Shiftable {

    private var constraintQueue: MutableList<ContactConstraint>
    private var constraints: MutableMap<ContactConstraintId, ContactConstraint>
    private var constraints1: MutableMap<ContactConstraintId, ContactConstraint>
    var isWarmStartingEnabled = false

    constructor() : this(Capacity.DEFAULT_CAPACITY)

    constructor(initialCapacity: Capacity) {
        // estimate the number of contact constraints
        val eSize = getEstimatedCollisionPairs(initialCapacity.bodyCount)
        // initialize the members
        constraintQueue = ArrayList(eSize)
        // 0.75 = 3/4, we can garuantee that the hashmap will not need to be rehashed
        // if we take capacity / load factor
        // the default load factor is 0.75 according to the javadocs, but lets assign it to be sure
        constraints = HashMap(eSize * 4 / 3 + 1, 0.75f)
        constraints1 = HashMap(eSize * 4 / 3 + 1, 0.75f)
        // enabled by default
        isWarmStartingEnabled = true
    }

    override fun queue(constraint: ContactConstraint) {
        constraintQueue.add(constraint)
    }

    override fun clear() {
        constraintQueue.clear()
        constraints.clear()
    }

    override fun end(constraint: ContactConstraint): Boolean {
        return constraints.remove(constraint.id) != null
    }

    override fun shift(shift: Vector2) {
        val iterator = constraints.values.iterator()
        while (iterator.hasNext()) {
            val cc = iterator.next()
            cc.shift(shift)
        }
    }

    override fun updateAndNotify(listeners: List<ContactListener>?, settings: Settings) {
        // get the size of the list
        val size = constraintQueue.size
        val lsize = listeners?.size ?: 0

        // get the warm start distance from the settings
        val warmStartDistanceSquared = settings.warmStartDistanceSquared

        // create a new map for the new contacts constraints
        val newMap = constraints1

        // loop over the new contact constraints
        // and attempt to persist contacts
        for (i in 0 until size) {
            // get the new contact constraint
            val newContactConstraint = constraintQueue[i]
            // define the old contact constraint
            var oldContactConstraint: ContactConstraint? = null
            val contacts: List<Contact> = newContactConstraint.contacts
            val nsize = contacts.size

            // get the old contact constraint
            // doing a remove here will ensure that the remaining contact
            // constraints in the map will be contacts that need to be notified of
            // removal
            oldContactConstraint = constraints.remove(newContactConstraint.id)

            // check if the contact constraint exists
            if (oldContactConstraint != null) {
                val ocontacts: List<Contact> = oldContactConstraint.contacts
                val osize = ocontacts.size
                // create an array for removed contacts
                val persisted = BooleanArray(osize)
                // warm start the constraint
                for (j in nsize - 1 downTo 0) {
                    // get the new contact
                    val newContact = contacts[j]
                    // loop over the old contacts
                    var found = false
                    for (k in 0 until osize) {
                        // get the old contact
                        val oldContact = ocontacts[k]
                        // check if the id type is distance, if so perform a distance check using the warm start distance
                        // else just compare the ids
                        if (newContact.id === ManifoldPointId.DISTANCE && newContact.p.distanceSquared(oldContact.p) <= warmStartDistanceSquared || newContact.id == oldContact.id) {
                            // warm start by setting the new contact constraint
                            // accumulated impulses to the old contact constraint
                            if (isWarmStartingEnabled) {
                                newContact.jn = oldContact.jn
                                newContact.jt = oldContact.jt
                            }
                            // notify of a persisted contact
                            val point = PersistedContactPoint(newContactConstraint, newContact, oldContactConstraint, oldContact)
                            // call the listeners and set the enabled flag to the result
                            var allow = true
                            for (l in 0 until lsize) {
                                val listener = listeners!![l]
                                if (!listener.persist(point)) {
                                    allow = false
                                }
                            }
                            if (!allow) {
                                newContactConstraint.isEnabled = false
                            }
                            // flag that the contact was persisted
                            persisted[k] = true
                            found = true
                            break
                        }
                    }
                    // check for persistence, if it wasn't persisted its a new contact
                    if (!found) {
                        // notify of new contact (begin of contact)
                        val point = ContactPoint(newContactConstraint, newContact)
                        // call the listeners and set the enabled flag to the result
                        var allow = true
                        for (l in 0 until lsize) {
                            val listener = listeners!![l]
                            if (!listener.begin(point)) {
                                allow = false
                            }
                        }
                        if (!allow) {
                            newContactConstraint.isEnabled = false
                        }
                    }
                }

                // check for removed contacts
                // if the contact was not persisted then it was removed
                val rsize = persisted.size
                for (j in 0 until rsize) {
                    // check the boolean array
                    if (!persisted[j]) {
                        // get the contact
                        val contact = ocontacts[j]
                        // notify of new contact (begin of contact)
                        val point = ContactPoint(newContactConstraint, contact)
                        // call the listeners
                        for (l in 0 until lsize) {
                            val listener = listeners!![l]
                            listener.end(point)
                        }
                    }
                }
            } else {
                // notify new contacts
                // if the old contact point was not found notify of the new contact
                for (j in nsize - 1 downTo 0) {
                    // get the contact
                    val contact = contacts[j]
                    // notify of new contact (begin of contact)
                    val point = ContactPoint(newContactConstraint, contact)
                    // call the listeners and set the enabled flag to the result
                    var allow = true
                    for (l in 0 until lsize) {
                        val listener = listeners!![l]
                        if (!listener.begin(point)) {
                            allow = false
                        }
                    }
                    if (!allow) {
                        newContactConstraint.isEnabled = false
                    }
                }
            }
            // add the contact constraint to the map
            if (newContactConstraint.contacts.size > 0) {
                newMap[newContactConstraint.id] = newContactConstraint
            }
        }

        // check the map and its size
        if (constraints.isNotEmpty()) {
            // now loop over the remaining contacts in the map to notify of any removed contacts
            val iterator: Iterator<ContactConstraint> = constraints.values.iterator()
            while (iterator.hasNext()) {
                val contactConstraint = iterator.next()
                // loop over the contact points
                val rsize = contactConstraint.contacts.size
                for (i in 0 until rsize) {
                    // get the contact
                    val contact = contactConstraint.contacts[i]
                    // set the contact point values
                    val point = ContactPoint(contactConstraint, contact)
                    // call the listeners
                    for (l in 0 until lsize) {
                        val listener = listeners!![l]
                        listener.end(point)
                    }
                }
            }
        }

        // finally overwrite the contact constraint map with the new map
        if (size > 0) {
            // swap the maps so we can reuse
            constraints.clear()
            constraints1 = constraints
            constraints = newMap
        } else {
            // if no contact constraints exist, just clear the old map
            constraints.clear()
        }
        constraintQueue.clear()
    }

    override fun preSolveNotify(listeners: List<ContactListener>?) {
        val lsize = listeners?.size ?: 0

        // loop through the list of contacts that were solved
        val iterator = constraints.values.iterator()
        while (iterator.hasNext()) {
            // get the contact constraint
            val contactConstraint = iterator.next()
            // don't report preSolve of disabled contact constraints
            if (!contactConstraint.isEnabled || contactConstraint.isSensor) continue
            // loop over the contacts
            val csize = contactConstraint.contacts.size
            // iterate backwards so we can remove
            for (j in csize - 1 downTo 0) {
                // get the contact
                val contact = contactConstraint.contacts[j]
                // notify of the contact that will be solved
                val point = ContactPoint(contactConstraint, contact)
                // call the listeners and set the enabled flag to the result
                var allow = true
                for (l in 0 until lsize) {
                    val listener = listeners!![l]
                    if (!listener.preSolve(point)) {
                        allow = false
                    }
                }
                // if any of the listeners flagged it as not allowed then
                // remove the contact from the list
                if (!allow) {
                    contactConstraint.contacts.removeAt(j)
                }
            }
            // check if all the contacts were not allowed
            if (contactConstraint.contacts.size == 0) {
                // remove the constraint
                iterator.remove()
            }
        }
    }

    override fun postSolveNotify(listeners: List<ContactListener>?) {
        val lsize = listeners?.size ?: 0

        // loop through the list of contacts that were solved
        for (contactConstraint in constraints.values) {
            // don't report postSolve of disabled contact constraints
            if (!contactConstraint.isEnabled || contactConstraint.isSensor) continue
            // loop over the contacts
            val rsize = contactConstraint.contacts.size
            for (j in 0 until rsize) {
                // get the contact
                val contact = contactConstraint.contacts[j]
                // set the contact point values
                val point = SolvedContactPoint(contactConstraint, contact)
                // notify of them being solved
                for (l in 0 until lsize) {
                    val listener = listeners!![l]
                    listener.postSolve(point)
                }
            }
        }
    }

    override val queueCount: Int get() = constraintQueue.size
    override val contactCount: Int get() = constraints.size

}