/*
 * Copyright (c) 2010-2016 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of dyn4j nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dyn4j.collision.broadphase

import org.dyn4j.BinarySearchTree
import org.dyn4j.collision.Collidable
import org.dyn4j.collision.Collisions
import org.dyn4j.collision.Collisions.getEstimatedCollisionsPerObject
import org.dyn4j.collision.Collisions.getEstimatedRaycastCollisions
import org.dyn4j.collision.Fixture
import org.dyn4j.geometry.AABB
import org.dyn4j.geometry.Ray
import org.dyn4j.geometry.Transform
import org.dyn4j.geometry.Vector2

/**
 * Implementation of the Sweep and Prune broad-phase collision detection algorithm.
 *
 *
 * This implementation maintains a red-black tree of [Collidable] [Fixture]s where each update
 * will reposition the respective [Collidable] [Fixture] in the tree.
 *
 *
 * Projects all [Collidable] [Fixture]s on both the x and y axes and performs overlap checks
 * on all the projections to test for possible collisions (AABB tests).
 *
 *
 * This algorithm is O(n) for all [.detect] and [.raycast] methods.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
</T></E> */
class Sap<E : Collidable<T>, T : Fixture> : AbstractBroadphaseDetector<E, T>, BroadphaseDetector<E, T> {
    /** Sorted tree set of proxies  */
    lateinit var tree: BinarySearchTree<SapProxy<E, T>>

    /** Id to proxy map for fast lookup  */
    var map: MutableMap<BroadphaseKey?, SapProxy<E, T>>? = null

    /** Default constructor.  */
    constructor() : this(BroadphaseDetector.DEFAULT_INITIAL_CAPACITY)

    /**
     * Full constructor.
     *
     *
     * Allows fine tuning of the initial capacity of local storage for faster running times.
     * @param initialCapacity the initial capacity of local storage
     * @throws IllegalArgumentException if initialCapacity is less than zero
     * @since 3.1.1
     */
    constructor(initialCapacity: Int) {
        tree = BinarySearchTree<SapProxy<E, T>>(true)
        // 0.75 = 3/4, we can garuantee that the hashmap will not need to be rehashed
        // if we take capacity / load factor
        // the default load factor is 0.75 according to the javadocs, but lets assign it to be sure
        map = HashMap(initialCapacity * 4 / 3 + 1, 0.75f)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#add(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun add(collidable: E, fixture: T) {
        val key = BroadphaseKey[collidable, fixture]
        val proxy = map!![key]
        if (proxy == null) {
            this.add(key, collidable, fixture)
        } else {
            this.update(key, proxy, collidable, fixture)
        }
    }

    /**
     * Internal add method.
     *
     *
     * This method assumes the given arguments are all non-null and that the
     * [Collidable] [Fixture] is not currently in this broad-phase.
     * @param key the key for the collidable-fixture pair
     * @param collidable the collidable
     * @param fixture the fixture
     */
    fun add(key: BroadphaseKey, collidable: E, fixture: T) {
        val tx: Transform = collidable.transform
        val aabb: AABB = fixture.shape.createAABB(tx)
        // expand the aabb
        aabb.expand(this.expansion)
        // create a new node for the collidable
        val proxy = SapProxy(collidable, fixture, aabb)
        // add the proxy to the map
        map!![key] = proxy
        // insert the node into the tree
        tree.insert(proxy)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#remove(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun remove(collidable: E, fixture: T?): Boolean {
        val key = BroadphaseKey[collidable, fixture]
        // find the proxy in the map
        val proxy = map!!.remove(key)
        // make sure it was found
        if (proxy != null) {
            // remove the proxy from the tree
            tree.remove(proxy)
            return true
        }
        return false
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#update(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun update(collidable: E, fixture: T) {
        val key = BroadphaseKey[collidable, fixture]
        val proxy = map!![key]
        if (proxy != null) {
            this.update(key, proxy, collidable, fixture)
        } else {
            this.add(key, collidable, fixture)
        }
    }


    /**
     * Internal update method.
     *
     *
     * This method assumes the given arguments are all non-null.
     * @param key the key for the collidable-fixture pair
     * @param proxy the current node in the tree
     * @param collidable the collidable
     * @param fixture the fixture
     */
    fun update(key: BroadphaseKey, proxy: SapProxy<E, T>, collidable: E, fixture: T) {
        val tx = collidable.transform
        // create the new aabb
        val aabb = fixture.shape.createAABB(tx)
        // see if the old aabb contains the new one
        if (proxy.aabb.contains(aabb)) {
            // if so, don't do anything
            return
        }
        // otherwise expand the new aabb
        aabb.expand(expansion)
        // remove the current proxy from the tree
        tree.remove(proxy)
        // set the new aabb
        proxy.aabb = aabb
        // reinsert the proxy
        tree.insert(proxy)
    }


    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#getAABB(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun getAABB(collidable: E, fixture: T): AABB? {
        val key = BroadphaseKey[collidable, fixture]
        val proxy = map!![key]
        return proxy?.aabb ?: fixture.shape.createAABB(collidable.transform)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#contains(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun contains(collidable: E, fixture: T): Boolean {
        val key = BroadphaseKey[collidable, fixture]
        return map!!.containsKey(key)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#clear()
	 */
    override fun clear() {
        map!!.clear()
        tree.clear()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#size()
	 */
    override fun size(): Int {
        return map!!.size
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#detect(org.dyn4j.collision.broadphase.BroadphaseFilter)
	 */
    override fun detect(filter: BroadphaseFilter<E, T>): List<BroadphasePair<E, T>> {
        // get the number of proxies
        val size: Int = tree.size

        // check the size
        if (size == 0) {
            // return the empty list
            return emptyList()
        }

        // the estimated size of the pair list
        val eSize: Int = Collisions.getEstimatedCollisionPairs(size)
        val pairs: MutableList<BroadphasePair<E, T>> = ArrayList(eSize)

        // clear the tested flags
        val itp = tree.iterator()
        while (itp.hasNext()) {
            val p = itp.next()
            p.tested = false
        }

        // find all the possible pairs O(n*log(n))
        val ito = tree.iterator()
        while (ito.hasNext()) {
            // get the current proxy
            val current = ito.next()
            val iti: MutableIterator<SapProxy<E, T>> = tree.tailIterator(current)
            while (iti.hasNext()) {
                val test = iti.next()
                // dont compare objects against themselves
                if (test.collidable === current.collidable) continue
                // dont compare object that have already been compared
                if (test.tested) continue
                // test overlap
                // the >= is to support degenerate intervals created by vertical segments
                if (current.aabb.maxX >= test.aabb.minX) {
                    if (current.aabb.overlaps(test.aabb)) {
                        if (filter.isAllowed(current.collidable, current.fixture, test.collidable, test.fixture)) {
                            pairs.add(
                                BroadphasePair(
                                    current.collidable,
                                    current.fixture,
                                    test.collidable,
                                    test.fixture
                                )
                            )
                        }
                    }
                } else {
                    // otherwise we can break from the loop
                    break
                }
            }
            current.tested = true
        }
        return pairs
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#detect(org.dyn4j.geometry.AABB, org.dyn4j.collision.broadphase.BroadphaseFilter)
	 */
    override fun detect(aabb: AABB, filter: BroadphaseFilter<E, T>): List<BroadphaseItem<E, T>> {
        // get the size of the proxy list
        val size = tree.size

        // check the size of the proxy list
        if (size == 0) {
            // return the empty list
            return emptyList()
        }
        val list: MutableList<BroadphaseItem<E, T>> = ArrayList(getEstimatedCollisionsPerObject())

        // we must check all aabbs starting at the root
        // from which point the first aabb to not intersect
        // flags us to stop O(n)
        val it: Iterator<SapProxy<E, T>> = tree.inOrderIterator()
        while (it.hasNext()) {
            val proxy = it.next()
            // check for overlap
            if (proxy.aabb.maxX > aabb.minX) {
                if (proxy.aabb.overlaps(aabb)) {
                    if (filter.isAllowed(aabb, proxy.collidable, proxy.fixture)) {
                        list.add(
                            BroadphaseItem(
                                proxy.collidable,
                                proxy.fixture
                            )
                        )
                    }
                }
            } else if (aabb.maxX < proxy.aabb.minX) {
                // if not overlapping, then nothing after this
                // node will overlap either so we can exit the loop
                break
            }
        }
        return list
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#raycast(org.dyn4j.geometry.Ray, double)
	 */
    override fun raycast(ray: Ray, length: Double, filter: BroadphaseFilter<E, T>): List<BroadphaseItem<E, T>> {
        // check the size of the proxy list
        if (tree.size == 0) {
            // return an empty list
            return emptyList()
        }

        // create an aabb from the ray
        val s = ray.start!!
        val d = ray.directionVector

        // get the length
        var l = length
        if (length <= 0.0) l = Double.MAX_VALUE

        // compute the coordinates
        val x1 = s.x
        val x2 = s.x + d.x * l
        val y1 = s.y
        val y2 = s.y + d.y * l

        // create the aabb
        val aabb = AABB.createAABBFromPoints(x1, y1, x2, y2)
        val invDx = 1.0 / d.x
        val invDy = 1.0 / d.y

        // get the size of the proxy list
        val size = tree.size

        // check the size of the proxy list
        if (size == 0) {
            // return the empty list
            return emptyList()
        }
        val eSize = getEstimatedRaycastCollisions(map!!.size)
        val list: MutableList<BroadphaseItem<E, T>> = ArrayList(eSize)

        // we must check all aabbs starting with the root
        // from which point the first aabb to not intersect
        // flags us to stop O(n)
        val it: Iterator<SapProxy<E, T>> = tree.inOrderIterator()
        while (it.hasNext()) {
            val proxy = it.next()
            // check for overlap
            if (proxy.aabb.maxX > aabb.minX) {
                if (proxy.aabb.overlaps(aabb)) {
                    if (this.raycast(s, l, invDx, invDy, proxy.aabb)) {
                        if (filter.isAllowed(ray, length, proxy.collidable, proxy.fixture)) {
                            list.add(
                                BroadphaseItem(
                                    proxy.collidable,
                                    proxy.fixture
                                )
                            )
                        }
                    }
                }
            } else if (aabb.maxX < proxy.aabb.minX) {
                // if not overlapping, then nothing after this
                // node will overlap either so we can exit the loop
                break
            }
        }
        return list
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shiftable#shift(org.dyn4j.geometry.Vector2)
	 */
    override fun shift(shift: Vector2) {
        // loop over all the proxies and translate their aabb
        val it: Iterator<SapProxy<E, T>> = tree.iterator()
        while (it.hasNext()) {
            val proxy = it.next()
            proxy.aabb.translate(shift)
        }
    }



}