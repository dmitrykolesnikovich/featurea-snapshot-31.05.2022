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
package org.dyn4j.dynamics

import featurea.pop
import featurea.push
import org.dyn4j.DataContainer
import org.dyn4j.Listener
import org.dyn4j.collision.Bounds
import org.dyn4j.collision.BoundsListener
import org.dyn4j.collision.Filter
import org.dyn4j.collision.Fixture
import org.dyn4j.collision.broadphase.*
import org.dyn4j.collision.continuous.ConservativeAdvancement
import org.dyn4j.collision.continuous.TimeOfImpact
import org.dyn4j.collision.continuous.TimeOfImpactDetector
import org.dyn4j.collision.manifold.ClippingManifoldSolver
import org.dyn4j.collision.manifold.Manifold
import org.dyn4j.collision.manifold.ManifoldSolver
import org.dyn4j.collision.narrowphase.*
import org.dyn4j.dynamics.contact.*
import org.dyn4j.dynamics.joint.Joint
import org.dyn4j.geometry.*
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.reflect.KClass

/**
 * Manages the logic of collision detection, resolution, and reporting.
 *
 *
 * Interfacing with dyn4j starts with this class.  Create a new instance of this class
 * and add bodies and joints.  Then call one of the update or step methods in your game
 * loop to move the physics engine forward in time.
 *
 *
 * Via the [.addListener] method, a [World] instance can have multiple listeners for all the listener types.
 * Some listener types return a boolean to indicate continuing or allowing something, like [CollisionListener].  If, for example,
 * there are multiple [CollisionListener]s and **any** one of them returns false for an event, the collision is skipped.  However,
 * all listeners will still be called no matter if the first returned false.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 */
@OptIn(kotlin.ExperimentalStdlibApi::class)
class World : Shiftable, DataContainer {

    companion object {
        /** Identity Transform instance  */
        @JvmField
        val IDENTITY: Transform = Transform()

        /** Earths gravity constant  */
        @JvmField
        val EARTH_GRAVITY: Vector2 = Vector2(0.0, -9.8)

        /** Zero gravity constant  */
        @JvmField
        val ZERO_GRAVITY: Vector2 = Vector2(0.0, 0.0)
    }

    // settings
    /** The dynamics settings for this world  */
    lateinit var settings: Settings

    /** The [Step] used by the dynamics calculations  */
    @JvmField
    var step: Step? = null

    /** The world gravity vector  */
    lateinit var gravity: Vector2

    /** The world [Bounds]  */
    var bounds: Bounds? = null

    // algorithms

    // algorithms
    /** The [BroadphaseDetector]  */
    var broadphaseDetector: BroadphaseDetector<Body, BodyFixture>
        set(value) {
            // set the new broadphase
            field = value
            // re-add all bodies to the broadphase
            val size = bodies.size
            for (i in 0 until size) {
                field.add(bodies[i])
            }
        }

    /** The [BroadphaseFilter] for detection  */
    var detectBroadphaseFilter: BroadphaseFilter<Body, BodyFixture> = DetectBroadphaseFilter()

    /** The [NarrowphaseDetector]  */
    var narrowphaseDetector: NarrowphaseDetector? = null
        set(value) {
            if (value == null) throw NullPointerException(message("dynamics.world.nullNarrowphaseDetector"))
            field = value
        }

    /** The [NarrowphasePostProcessor]  */
    var narrowphasePostProcessor: NarrowphasePostProcessor? = null

    /** The [ManifoldSolver]  */
    var manifoldSolver: ManifoldSolver? = null
        set(value) {
            if (value == null) throw NullPointerException(message("dynamics.world.nullManifoldSolver"))
            field = value
        }

    /** The [TimeOfImpactDetector]  */
    var timeOfImpactDetector: TimeOfImpactDetector? = null
        set(value) {
            if (value == null) throw NullPointerException(message("dynamics.world.nullTimeOfImpactDetector"))
            field = value
        }

    /** The [RaycastDetector]  */
    var raycastDetector: RaycastDetector? = null
        set(value) {
            if (value == null) throw NullPointerException(message("dynamics.world.nullRaycastDetector"))
            field = value
        }

    /** The [ContactManager]  */
    var contactManager: ContactManager? = null
        set(value) {
            field = value
            isUpdateRequired = true
        }

    /** The [CoefficientMixer]  */
    var coefficientMixer: CoefficientMixer? = null
        set(value) {
            if (value == null) throw NullPointerException(message("dynamics.world.nullCoefficientMixer"))
            field = value
        }

    /** The [ContactConstraintSolver]  */
    var contactConstraintSolver: ContactConstraintSolver? = null

    /** The [TimeOfImpactSolver]  */
    lateinit var timeOfImpactSolver: TimeOfImpactSolver

    /** The application data associated  */
    override var userData: Any? = null

    // internal

    // listeners and config

    // internal
    // listeners and config
    /** The list of listeners for this world  */
    private lateinit var listeners: MutableList<Listener>

    // bodies/joints

    // bodies/joints
    /** The [Body] list  */
    lateinit var bodies: MutableList<Body>

    /** The [Joint] list  */
    lateinit var joints: MutableList<Joint>

    // temp data

    // temp data
    /** The reusable island  */
    private var island: Island? = null

    /** The accumulated time  */
    private var time = 0.0

    /** Flag to find new contacts  */
    var isUpdateRequired = false

    /**
     * Default constructor.
     *
     *
     * Builds a simulation [World] without bounds.
     *
     *
     * Defaults to using [.EARTH_GRAVITY], [DynamicAABBTree] broad-phase,
     * [Gjk] narrow-phase, and [ClippingManifoldSolver].
     *
     *
     * Uses the [Capacity.DEFAULT_CAPACITY] capacity object for initialization.
     */
    constructor() : this(Capacity.DEFAULT_CAPACITY, null)

    /**
     * Optional constructor.
     *
     *
     * Defaults to using [.EARTH_GRAVITY], [DynamicAABBTree] broad-phase,
     * [Gjk] narrow-phase, and [ClippingManifoldSolver].
     *
     *
     * The initial capacity specifies the estimated number of bodies that the simulation
     * will have at any one time.  This is used to size internal structures to improve
     * performance.  The internal structures can grow past the initial capacity.
     * @param initialCapacity the initial capacity settings
     * @since 3.1.1
     */
    constructor(initialCapacity: Capacity) : this(initialCapacity, null)

    /**
     * Optional constructor.
     *
     *
     * Defaults to using [.EARTH_GRAVITY], [DynamicAABBTree] broad-phase,
     * [Gjk] narrow-phase, and [ClippingManifoldSolver].
     * @param bounds the bounds of the [World]; can be null
     */
    constructor(bounds: Bounds) : this(Capacity.DEFAULT_CAPACITY, bounds)

    /**
     * Full constructor.
     *
     *
     * Defaults to using [.EARTH_GRAVITY], [DynamicAABBTree] broad-phase,
     * [Gjk] narrow-phase, and [ClippingManifoldSolver].
     *
     *
     * The initial capacity specifies the estimated number of bodies that the simulation
     * will have at any one time.  This is used to size internal structures to improve
     * performance.  The internal structures can grow past the initial capacity.
     * @param initialCapacity the initial capacity settings
     * @param bounds the bounds of the [World]; can be null
     * @throws NullPointerException if initialCapacity is null
     * @since 3.1.1
     */
    constructor(initialCapacity: Capacity?, bounds: Bounds?) {
        // check for null capacity
        var initialCapacity = initialCapacity
        if (initialCapacity == null) initialCapacity = Capacity.DEFAULT_CAPACITY

        // initialize all the classes with default values
        settings = Settings()
        step = Step(settings!!.getStepFrequency())
        gravity = World.EARTH_GRAVITY
        this.bounds = bounds
        detectBroadphaseFilter = DetectBroadphaseFilter()
        narrowphaseDetector = Gjk()
        narrowphasePostProcessor = LinkPostProcessor()
        manifoldSolver = ClippingManifoldSolver()
        timeOfImpactDetector = ConservativeAdvancement()
        raycastDetector = Gjk()
        coefficientMixer = CoefficientMixer.DEFAULT_MIXER
        contactManager = DefaultContactManager(initialCapacity)
        contactConstraintSolver = SequentialImpulses()
        timeOfImpactSolver = TimeOfImpactSolver()
        bodies = ArrayList(initialCapacity.bodyCount)
        broadphaseDetector = DynamicAABBTree<Body, BodyFixture>(initialCapacity.bodyCount)
        joints = ArrayList(initialCapacity.jointCount)
        listeners = ArrayList(initialCapacity.listenerCount)
        island = Island(initialCapacity)
        time = 0.0
        isUpdateRequired = true
    }

    /**
     * Updates the [World].
     *
     *
     * This method will only update the world given the step frequency contained
     * in the [Settings] object.  You can use the [StepListener] interface
     * to listen for when a step is actually performed.  In addition, this method will
     * return true if a step was performed.
     *
     *
     * This method performs, at maximum, one simulation step.  Any remaining time from
     * the previous call of this method is added to the given elapsed time to determine
     * if a step needs to be performed.  If the given elapsed time is usually greater
     * than the step frequency, consider using the [.update] method
     * instead.
     *
     *
     * Alternatively you can call the [.updatev] method to use a variable
     * time step.
     * @see .update
     * @see .updatev
     * @see .getAccumulatedTime
     * @param elapsedTime the elapsed time in seconds
     * @return boolean true if the [World] performed a simulation step
     */
    fun update(elapsedTime: Double): Boolean {
        return update(elapsedTime, -1.0, 1)
    }

    /**
     * Updates the [World].
     *
     *
     * This method will only update the world given the step frequency contained
     * in the [Settings] object.  You can use the [StepListener] interface
     * to listen for when a step is actually performed.
     *
     *
     * Unlike the [.update] method, this method will perform more than one
     * step based on the given elapsed time.  For example, if the given elapsed time + the
     * remaining time from the last call of this method is 2 * step frequency, then 2 steps
     * will be performed.  Use the maximumSteps parameter to put an upper bound on the
     * number of steps performed.
     *
     *
     * Alternatively you can call the [.updatev] method to use a variable
     * time step.
     * @see .update
     * @see .updatev
     * @see .getAccumulatedTime
     * @param elapsedTime the elapsed time in seconds
     * @param maximumSteps the maximum number of steps to perform
     * @return boolean true if the [World] performed at least one simulation step
     * @since 3.1.10
     */
    fun update(elapsedTime: Double, maximumSteps: Int): Boolean {
        return this.update(elapsedTime, -1.0, maximumSteps)
    }

    /**
     * Updates the [World].
     *
     *
     * This method will only update the world given the step frequency contained
     * in the [Settings] object.  You can use the [StepListener] interface
     * to listen for when a step is actually performed.  In addition, this method will
     * return true if a step was performed.
     *
     *
     * This method performs, at maximum, one simulation step.  Any remaining time from
     * the previous call of this method is added to the given elapsed time to determine
     * if a step needs to be performed.  If the given elapsed time is usually greater
     * than the step frequency, consider using the [.update] method
     * instead.
     *
     *
     * The stepElapsedTime parameter provides a way for the [World] to continue to
     * update at the frequency defined in the [Settings] object, but advance the
     * simulation by the given time.
     *
     *
     * Alternatively you can call the [.updatev] method to use a variable
     * time step.
     * @see .update
     * @see .updatev
     * @see .getAccumulatedTime
     * @param elapsedTime the elapsed time in seconds
     * @param stepElapsedTime the time, in seconds, that the simulation should be advanced
     * @return boolean true if the [World] performed at least one simulation step
     * @since 3.2.4
     */
    fun update(elapsedTime: Double, stepElapsedTime: Double): Boolean {
        return this.update(elapsedTime, stepElapsedTime, 1)
    }

    /**
     * Updates the [World].
     *
     *
     * This method will only update the world given the step frequency contained
     * in the [Settings] object.  You can use the [StepListener] interface
     * to listen for when a step is actually performed.
     *
     *
     * Unlike the [.update] method, this method will perform more than one
     * step based on the given elapsed time.  For example, if the given elapsed time + the
     * remaining time from the last call of this method is 2 * step frequency, then 2 steps
     * will be performed.  Use the maximumSteps parameter to put an upper bound on the
     * number of steps performed.
     *
     *
     * The stepElapsedTime parameter provides a way for the [World] to continue to
     * update at the frequency defined in the [Settings] object, but advance the
     * simulation by the given time.
     *
     *
     * Alternatively you can call the [.updatev] method to use a variable
     * time step.
     * @see .update
     * @see .updatev
     * @see .getAccumulatedTime
     * @param elapsedTime the elapsed time in seconds
     * @param stepElapsedTime the time, in seconds, that the simulation should be advanced for each step; if less than or equal to zero [Settings.getStepFrequency] will be used
     * @param maximumSteps the maximum number of steps to perform
     * @return boolean true if the [World] performed at least one simulation step
     * @since 3.2.4
     */
    fun update(elapsedTime: Double, stepElapsedTime: Double, maximumSteps: Int): Boolean {
        // make sure the update time is greater than zero
        var elapsedTime = elapsedTime
        if (elapsedTime < 0.0) elapsedTime = 0.0
        // update the time
        time += elapsedTime
        // check the frequency in settings
        val invhz = settings!!.getStepFrequency()
        // see if we should update or not
        var steps = 0
        while (time >= invhz && steps < maximumSteps) {
            // update the step
            step!!.update(if (stepElapsedTime <= 0) invhz else stepElapsedTime)
            // reset the time
            time = time - invhz
            // step the world
            this.step()
            // increment the number of steps
            steps++
        }
        return steps > 0
    }

    /**
     * Updates the [World].
     *
     *
     * This method will update the world on every call.  Unlike the [.update]
     * method, this method uses the given elapsed time and does not attempt to update the world
     * on a set interval.
     *
     *
     * This method immediately returns if the given elapsedTime is less than or equal to
     * zero.
     * @see .update
     * @see .update
     * @param elapsedTime the elapsed time in seconds
     */
    fun updatev(elapsedTime: Double) {
        // make sure the update time is greater than zero
        if (elapsedTime <= 0.0) return
        // update the step
        step!!.update(elapsedTime)
        // step the world
        this.step()
    }

    /**
     * Performs the given number of simulation steps using the step frequency in [Settings].
     *
     *
     * This method immediately returns if the given step count is less than or equal to
     * zero.
     * @param steps the number of simulation steps to perform
     */
    fun step(steps: Int) {
        // get the frequency from settings
        val invhz = settings!!.getStepFrequency()
        // perform the steps
        this.step(steps, invhz)
    }

    /**
     * Performs the given number of simulation steps using the given elapsed time for each step.
     *
     *
     * This method immediately returns if the given elapsedTime or step count is less than or equal to
     * zero.
     * @param steps the number of simulation steps to perform
     * @param elapsedTime the elapsed time for each step
     */
    fun step(steps: Int, elapsedTime: Double) {
        // make sure the number of steps is greather than zero
        if (steps <= 0) return
        // make sure the update time is greater than zero
        if (elapsedTime <= 0.0) return
        // perform the steps
        for (i in 0 until steps) {
            // update the step object
            step!!.update(elapsedTime)
            // step the world
            this.step()
        }
    }

    /**
     * Performs one time step of the [World] using the current [Step].
     *
     *
     * This method advances the world by the elapsed time in the [Step] object
     * and performs collision resolution and constraint solving.
     *
     *
     * This method will perform a collision detection sweep at the end to ensure that
     * callers of the world have the latest collision information. If the [.isUpdateRequired]
     * method returns true, a collision detection sweep will be performed before doing
     * collision resolution.  See the [.setUpdateRequired] method for details
     * on when this flag should be set.
     *
     *
     * Use the various listeners to listen for events during the execution of
     * this method.
     *
     *
     * If possible use the [StepListener.postSolve] method to update any
     * bodies or joints to increase performance.
     *
     *
     * Most [Listener]s do not allow modification of the world, bodies, joints, etc in
     * there methods. It's recommended that any of modification be performed in a [StepListener]
     * or after this method has returned.
     */
    protected fun step() {
        // get all the step listeners
        val stepListeners = this.getListeners(StepListener::class)!!
        val contactListeners = this.getListeners(ContactListener::class)!!
        val sSize = stepListeners.size

        // notify the step listeners
        for (i in 0 until sSize) {
            val sl = stepListeners[i]
            sl.begin(step, this)
        }

        // check if we need to update the contacts first
        if (isUpdateRequired) {
            // if so then update the contacts
            this.detect()
            // notify that an update was performed
            for (i in 0 until sSize) {
                val sl = stepListeners[i]
                sl.updatePerformed(step, this)
            }
            // set the update required flag to false
            isUpdateRequired = false
        }

        // notify of all the contacts that will be solved and all the sensed contacts
        contactManager!!.preSolveNotify(contactListeners)

        // check for CCD
        val continuousDetectionMode = settings.getContinuousDetectionMode()

        // get the number of bodies
        val size = bodies.size

        // test for out of bounds objects
        // clear the body contacts
        // clear the island flag
        // save the current transform for CCD
        for (i in 0 until size) {
            val body = bodies[i]
            // remove the island flag
            body.setOnIsland(false)
            // save the current transform into the previous transform
            body.transform0.set(body.transform)
        }

        // clear the joint island flags
        val jSize = joints.size
        for (i in 0 until jSize) {
            // get the joint
            val joint: Constraint = joints[i]
            // set the island flag to false
            joint.isOnIsland = false
        }

        // perform a depth first search of the contact graph
        // to create islands for constraint solving
        val stack = ArrayDeque<Body>(size)

        // temp storage
        // we put these here so we can implicitly convert from joint and
        // contact constraint to constraint so that we have package private
        // access to the isOnIsland and setOnIsland methods
        var joint: Joint
        var contactConstraint: ContactConstraint
        var constraint: Constraint

        // loop over the bodies and their contact edges to create the islands
        for (i in 0 until size) {
            val seed = bodies!![i]
            // skip if asleep, in active, static, or already on an island
            if (seed.isOnIsland() || seed.isAsleep() || !seed.isActive() || seed.isStatic()) continue

            // set the island to the reusable island
            val island = island
            island!!.clear()
            stack.clear()
            stack.push(seed)
            while (stack.size > 0) {
                // get the next body
                val body: Body = stack.pop()
                // add it to the island
                island.add(body)
                // flag that it has been added
                body.setOnIsland(true)
                // make sure the body is awake
                body.setAsleep(false)
                // if its static then continue since we dont want the
                // island to span more than one static object
                // this keeps the size of the islands small
                if (body.isStatic()) continue
                // loop over the contact edges of this body
                val ceSize = body.contacts.size
                for (j in 0 until ceSize) {
                    val contactEdge = body.contacts[j]
                    // get the contact constraint
                    contactConstraint = contactEdge.interaction
                    constraint = contactConstraint
                    // skip sensor contacts
                    // check if the contact constraint has already been added to an island
                    if (!contactConstraint.isEnabled || contactConstraint.isSensor || constraint.isOnIsland) continue
                    // get the other body
                    val other = contactEdge.other
                    // add the contact constraint to the island list
                    island.add(contactConstraint)
                    // set the island flag on the contact constraint
                    constraint.isOnIsland = true
                    // has the other body been added to an island yet?
                    if (!other.isOnIsland()) {
                        // if not then add this body to the stack
                        stack.push(other)
                        other.setOnIsland(true)
                    }
                }
                // loop over the joint edges of this body
                val jeSize = body.joints.size
                for (j in 0 until jeSize) {
                    // get the joint edge
                    val jointEdge: JointEdge = body.joints[j]
                    // get the joint
                    joint = jointEdge.interaction!!
                    constraint = joint
                    // check if the joint is inactive
                    if (!joint.isActive || constraint.isOnIsland) continue
                    // get the other body
                    val other: Body = jointEdge.other
                    // check if the joint has already been added to an island
                    // or if the other body is not active
                    if (!other.isActive()) continue
                    // add the joint to the island
                    island.add(joint)
                    // set the island flag on the joint
                    constraint.isOnIsland = true
                    // check if the other body has been added to an island
                    if (!other.isOnIsland()) {
                        // if not then add the body to the stack
                        stack.push(other)
                        other.setOnIsland(true)
                    }
                }
            }

            // solve the island
            island.solve(contactConstraintSolver!!, gravity, step!!, settings!!)

            // allow static bodies to participate in other islands
            val isize = island.bodies.size
            for (j in 0 until isize) {
                val body = island.bodies[j]
                if (body.isStatic()) {
                    body.setOnIsland(false)
                }
            }
        }

        // allow memory to be reclaimed
        stack.clear()
        island!!.clear()

        // notify of the all solved contacts
        contactManager!!.postSolveNotify(contactListeners)

        // make sure CCD is enabled
        if (continuousDetectionMode !== ContinuousDetectionMode.NONE) {
            // solve time of impact
            this.solveTOI(continuousDetectionMode)
        }

        // notify the step listener
        for (i in 0 until sSize) {
            val sl = stepListeners[i]
            sl.postSolve(step, this)
        }

        // after all has been updated find new contacts
        // this is done so that the user has the latest contacts
        // and the broadphase has the latest AABBs, etc.
        this.detect()

        // set the update required flag to false
        isUpdateRequired = false

        // notify the step listener
        for (i in 0 until sSize) {
            val sl = stepListeners[i]
            sl.end(step, this)
        }
    }

    /**
     * Finds new contacts for all bodies in this world.
     *
     *
     * This method performs the following:
     *
     *  1. Checks for out of bound bodies
     *  1. Updates the broad-phase using the current body positions
     *  1. Performs broad-phase collision detection
     *  1. Performs narrow-phase collision detection
     *  1. Performs manifold solving
     *  1. Adds contacts to the contact manager
     *  1. Warm starts the contacts
     *
     *
     *
     * This method will notify all bounds and collision listeners.  If any [CollisionListener]
     * returns false, the collision is ignored.
     *
     *
     * This method also notifies any [ContactListener]s.
     * @since 3.0.0
     */
    protected fun detect() {
        // get the bounds listeners
        val boundsListeners = this.getListeners(BoundsListener::class)!!
        val collisionListeners = this.getListeners(CollisionListener::class)!!

        // get the number of bodies
        val size = bodies.size
        val blSize = boundsListeners.size
        val clSize = collisionListeners.size

        // test for out of bounds objects
        // clear the body contacts
        // update the broadphase

        // Check if the current broad-phase detector support batch updates, and use it if so
        if (broadphaseDetector is BatchBroadphaseDetector<*, *>) {
            for (i in 0 until size) {
                val body = bodies!![i]
                // skip if already not active
                if (!body.isActive()) continue
                // clear all the old contacts
                body.contacts.clear()
                // check if bounds have been set
                // check if the body is out of bounds
                val bounds = bounds
                if (bounds != null && bounds.isOutside(body)) {
                    // set the body to inactive
                    body.setActive(false)
                    // if so, notify via the listeners
                    for (j in 0 until blSize) {
                        val bl = boundsListeners[j]
                        bl.outside(body)
                    }
                }
            }
            (broadphaseDetector as BatchBroadphaseDetector<*, *>?)!!.batchUpdate()
        } else {
            // Else update each body separately
            for (i in 0 until size) {
                val body = bodies!![i]
                // skip if already not active
                if (!body.isActive()) continue
                // clear all the old contacts
                body.contacts.clear()
                // check if bounds have been set
                // check if the body is out of bounds
                val bounds = bounds
                if (bounds != null && bounds.isOutside(body)) {
                    // set the body to inactive
                    body.setActive(false)
                    // if so, notify via the listeners
                    for (j in 0 until blSize) {
                        val bl = boundsListeners[j]
                        bl.outside(body)
                    }
                }
                // update the broadphase with the new position/orientation
                broadphaseDetector!!.update(body)
            }
        }

        // make sure there are some bodies
        if (size > 0) {
            // test for collisions via the broad-phase
            val pairs =
                broadphaseDetector.detect(detectBroadphaseFilter)
            val pSize = pairs.size
            var allow = true

            // using the broad-phase results, test for narrow-phase
            for (i in 0 until pSize) {
                val pair = pairs[i]

                // get the bodies
                val body1 = pair.collidable1!!
                val body2 = pair.collidable2!!
                val fixture1 = pair.fixture1!!
                val fixture2 = pair.fixture2!!
                allow = true
                for (j in 0 until clSize) {
                    val cl = collisionListeners[j]
                    if (!cl.collision(body1, fixture1, body2, fixture2)) {
                        // if any collision listener returned false then skip this collision
                        // we must allow all the listeners to get notified first, then skip
                        // the collision
                        allow = false
                    }
                }
                if (!allow) continue

                // get their transforms
                val transform1: Transform = body1.transform!!
                val transform2: Transform = body2.transform!!
                val convex2: Convex = fixture2.shape!!
                val convex1: Convex = fixture1.shape!!
                val penetration = Penetration()
                // test the two convex shapes
                if (narrowphaseDetector!!.detect(convex1, transform1, convex2, transform2, penetration)) {
                    // check for zero penetration
                    if (penetration.depth == 0.0) {
                        // this should only happen if numerical error occurs
                        continue
                    }
                    // perform post processing
                    if (narrowphasePostProcessor != null) {
                        narrowphasePostProcessor!!.process(convex1, transform1, convex2, transform2, penetration)
                    }
                    // notify of the narrow-phase collision
                    allow = true
                    for (j in 0 until clSize) {
                        val cl = collisionListeners[j]
                        if (!cl.collision(body1, fixture1, body2, fixture2, penetration)) {
                            // if any collision listener returned false then skip this collision
                            // we must allow all the listeners to get notified first, then skip
                            // the collision
                            allow = false
                        }
                    }
                    if (!allow) continue
                    val manifold = Manifold()
                    // if there is penetration then find a contact manifold
                    // using the filled in penetration object
                    if (manifoldSolver!!.getManifold(penetration, convex1, transform1, convex2, transform2, manifold)) {
                        // check for zero points
                        if (manifold.points.size == 0) {
                            // this should only happen if numerical error occurs
                            continue
                        }
                        // notify of the manifold solving result
                        allow = true
                        for (j in 0 until clSize) {
                            val cl = collisionListeners[j]
                            if (!cl.collision(body1, fixture1, body2, fixture2, manifold)) {
                                // if any collision listener returned false then skip this collision
                                // we must allow all the listeners to get notified first, then skip
                                // the collision
                                allow = false
                            }
                        }
                        if (!allow) continue
                        // create a contact constraint
                        val contactConstraint = ContactConstraint(body1, fixture1, body2, fixture2, manifold,
                            coefficientMixer!!.mixFriction(fixture1.friction, fixture2.friction),
                            coefficientMixer!!.mixRestitution(fixture1.restitution, fixture2.restitution)
                        )
                        allow = true
                        // notify of the created contact constraint
                        for (j in 0 until clSize) {
                            val cl = collisionListeners[j]
                            if (!cl.collision(contactConstraint)) {
                                // if any collision listener returned false then skip this collision
                                // we must allow all the listeners to get notified first, then skip
                                // the collision
                                allow = false
                            }
                        }
                        if (!allow) continue

                        // add a contact edge to both bodies
                        val contactEdge1 = ContactEdge(body2, contactConstraint)
                        val contactEdge2 = ContactEdge(body1, contactConstraint)
                        body1.contacts.add(contactEdge1)
                        body2.contacts.add(contactEdge2)
                        // add the contact constraint to the contact manager
                        contactManager!!.queue(contactConstraint)
                    }
                }
            }
        }

        // warm start the contact constraints
        contactManager!!.updateAndNotify(this.getListeners(ContactListener::class)!!, settings)
    }

    /**
     * Solves the time of impact for all the [Body]s in this [World].
     *
     *
     * This method solves for the time of impact for each [Body] iteratively
     * and pairwise.
     *
     *
     * The cases considered are dependent on the given collision detection mode.
     *
     *
     * Cases skipped (including the converse of the above):
     *
     *  * Inactive, asleep, or non-moving bodies
     *  * Bodies connected via a joint with the collision flag set to false
     *  * Bodies already in contact
     *  * Fixtures whose filters return false
     *  * Sensor fixtures
     *
     * @param mode the continuous collision detection mode
     * @see ContinuousDetectionMode
     *
     * @since 1.2.0
     */
    protected fun solveTOI(mode: ContinuousDetectionMode) {
        val listeners: List<TimeOfImpactListener> = this.getListeners(TimeOfImpactListener::class)!!
        // get the number of bodies
        val size = bodies.size

        // check the CCD mode
        val bulletsOnly = mode === ContinuousDetectionMode.BULLETS_ONLY

        // loop over all the bodies and find the minimum TOI for each
        // dynamic body
        for (i in 0 until size) {
            // get the body
            val body = bodies!![i]

            // if we are only doing CCD on bullets only, then check
            // to make sure that the current body is a bullet
            if (bulletsOnly && !body.isBullet()) continue

            // otherwise we process all dynamic bodies

            // we don't process kinematic or static bodies except with
            // dynamic bodies (in other words b1 must always be a dynamic
            // body)
            if (body.mass!!.isInfinite) continue

            // don't bother with bodies that did not have their
            // positions integrated, if they were not added to an island then
            // that means they didn't move

            // we can also check for sleeping bodies and skip those since
            // they will only be asleep after being stationary for a set
            // time period
            if (!body.isOnIsland() || body.isAsleep()) continue

            // solve for time of impact
            this.solveTOI(body, listeners)
        }
    }

    /**
     * Solves the time of impact for the given [Body].
     *
     *
     * This method will find the first [Body] that the given [Body]
     * collides with unless ignored via the [TimeOfImpactListener].
     *
     *
     * If any [TimeOfImpactListener] doesn't allow the collision then the collision
     * is ignored.
     *
     *
     * After the first [Body] is found the two [Body]s are interpolated
     * to the time of impact.
     *
     *
     * Then the [Body]s are position solved using the [TimeOfImpactSolver]
     * to force the [Body]s into collision.  This causes the discrete collision
     * detector to detect the collision on the next time step.
     * @param body1 the [Body]
     * @param listeners the list of [TimeOfImpactListener]s
     * @since 3.1.0
     */
    protected fun solveTOI(body1: Body, listeners: List<TimeOfImpactListener>) {
        val size = bodies.size

        // generate a swept AABB for this body
        val aabb1 = body1.createSweptAABB()
        val bullet = body1.isBullet()

        // setup the initial time bounds [0, 1]
        val t1 = 0.0
        var t2 = 1.0

        // save the minimum time of impact and body
        var minToi: TimeOfImpact? = null
        var minBody: Body? = null

        // loop over all the other bodies to find the minimum TOI
        for (i in 0 until size) {
            // get the other body
            val body2 = bodies[i]

            // skip this test if they are the same body
            if (body1 == body2) continue

            // make sure the other body is active
            if (!body2.isActive()) continue

            // skip other dynamic bodies; we only do TOI for
            // dynamic vs. static/kinematic unless its a bullet
            if (body2.isDynamic() && !bullet) continue

            // check for connected pairs who's collision is not allowed
            if (body1.isConnected(body2, false)) continue

            // check for bodies already in collision
            if (body1.isInContact(body2)) continue

            // create a swept AABB for the other body
            val aabb2 = body2.createSweptAABB()
            // if the swept AABBs don't overlap then don't bother testing them
            if (!aabb1!!.overlaps(aabb2!!)) continue
            val toi = TimeOfImpact()
            val fc1 = body1.fixtureCount
            val fc2 = body2.fixtureCount

            // get the velocities for the time step since we want
            // [t1, t2] to be bound to this time step
            val dt = step!!.deltaTime
            // the linear and angular velocities should match what
            // we did when we advanced the position. alternatively
            // we could calculate these from the start and end transforms
            // but this has the problem of not knowing which direction
            // the angular velocity is going (clockwise or anti-clockwise).
            // however, this also has the problem of being different that
            // the way the bodies are advanced in the Island solving
            // (for now they are the same, but could be changed in the
            // future).
            val v1: Vector2 = body1.getLinearVelocity().product(dt)
            val v2: Vector2 = body2.getLinearVelocity().product(dt)
            val av1 = body1.angularVelocity * dt
            val av2 = body2.angularVelocity * dt
            val tx1 = body1.getInitialTransform()
            val tx2 = body2.getInitialTransform()

            // test against all fixture pairs taking the fixture
            // with the smallest time of impact
            for (j in 0 until fc1) {
                val f1 = body1.getFixture(j)!!

                // skip sensor fixtures
                if (f1.isSensor) continue
                for (k in 0 until fc2) {
                    val f2 = body2.getFixture(k)!!

                    // skip sensor fixtures
                    if (f2.isSensor) continue
                    val filter1: Filter = f1.filter!!
                    val filter2: Filter = f2.filter!!

                    // make sure the fixture filters allow the collision
                    if (!filter1.isAllowed(filter2)) {
                        continue
                    }
                    val c1: Convex = f1.shape
                    val c2: Convex = f2.shape

                    // get the time of impact for the fixture pair
                    if (timeOfImpactDetector!!.getTimeOfImpact(c1, tx1, v1, av1, c2, tx2, v2, av2, t1, t2, toi)) {
                        // get the time of impact
                        val t = toi.time
                        // check if the time of impact is less than
                        // the current time of impact
                        if (t < t2) {
                            // if it is then ask the listeners if we should use this collision
                            var allow = true
                            for (tl in listeners) {
                                if (!tl.collision(body1, f1, body2, f2, toi)) {
                                    // if any toi listener doesnt allow it, then don't allow it
                                    // we need to allow all listeners to be notified before we continue
                                    allow = false
                                }
                            }
                            if (allow) {
                                // set the new upper bound
                                t2 = t
                                // save the minimum toi and body
                                minToi = toi
                                minBody = body2
                            }
                        }
                    }
                }
            }
            // if the bodies are intersecting or do not intersect
            // within the range of motion then skip this body
            // and move to the next
        }

        // make sure the time of impact is not null
        if (minToi != null) {
            // get the time of impact info
            val t = minToi.time

            // move the dynamic body to the time of impact
            body1.transform0.lerp(body1.transform, t, body1.transform)
            // check if the other body is dynamic
            if (minBody!!.isDynamic()) {
                // if the other body is dynamic then interpolate its transform also
                minBody.transform0.lerp(minBody.transform, t, minBody.transform)
            }
            // this should bring the bodies within d distance from one another
            // we need to move the bodies more so that they are in collision
            // so that on the next time step they are solved by the discrete
            // collision detector

            // performs position correction on the body/bodies so that they are
            // in collision and will be detected in the next time step
            timeOfImpactSolver.solve(body1, minBody, minToi, settings)

            // this method does not conserve time
        }
    }

    /**
     * Performs a raycast against all the [Body]s in the [World].
     *
     *
     * The given [RaycastResult] list, results, will be filled with the raycast results
     * if the given ray intersected any bodies.
     *
     *
     * The [RaycastResult] class implements the Comparable interface to allow sorting by
     * distance from the ray's origin.
     *
     *
     * If the all flag is false, the results list will only contain the closest result (if any).
     *
     *
     * All raycasts pass through the [RaycastListener]s before being tested.  If **any**
     * [RaycastListener] doesn't allow the raycast then the body will not be tested.
     *
     *
     * Bodies that contain the start of the ray will not be included in the results.
     *
     *
     * Inactive bodies are ignored in this test.
     * @param start the start point
     * @param end the end point
     * @param ignoreSensors true if sensor [BodyFixture]s should be ignored
     * @param all true if all intersected [Body]s should be returned; false if only the closest [Body] should be returned
     * @param results a list to contain the results of the raycast
     * @return boolean true if at least one [Body] was intersected by the [Ray]
     * @throws NullPointerException if start, end, or results is null
     * @see .raycast
     * @see RaycastListener.allow
     * @since 2.0.0
     */
    fun raycast(start: Vector2, end: Vector2, ignoreSensors: Boolean, all: Boolean,
                results: MutableList<RaycastResult>): Boolean {
        return this.raycast(start, end, null, ignoreSensors, true, all, results)
    }

    /**
     * Performs a raycast against all the [Body]s in the [World].
     *
     *
     * The given [RaycastResult] list, results, will be filled with the raycast results
     * if the given ray intersected any bodies.
     *
     *
     * The [RaycastResult] class implements the Comparable interface to allow sorting by
     * distance from the ray's origin.
     *
     *
     * If the all flag is false, the results list will only contain the closest result (if any).
     *
     *
     * All raycasts pass through the [RaycastListener]s before being tested.  If **any**
     * [RaycastListener] doesn't allow the raycast then the body will not be tested.
     *
     *
     * Bodies that contain the start of the ray will not be included in the results.
     * @param start the start point
     * @param end the end point
     * @param ignoreSensors true if sensor [BodyFixture]s should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param all true if all intersected [Body]s should be returned; false if only the closest [Body] should be returned
     * @param results a list to contain the results of the raycast
     * @return boolean true if at least one [Body] was intersected by the [Ray]
     * @throws NullPointerException if start, end, or results is null
     * @see .raycast
     * @see RaycastListener.allow
     * @since 3.1.9
     */
    fun raycast(start: Vector2, end: Vector2, ignoreSensors: Boolean, ignoreInactive: Boolean, all: Boolean,
        results: MutableList<RaycastResult>): Boolean {
        return this.raycast(start, end, null, ignoreSensors, ignoreInactive, all, results)
    }

    /**
     * Performs a raycast against all the [Body]s in the [World].
     *
     *
     * The given [RaycastResult] list, results, will be filled with the raycast results
     * if the given ray intersected any bodies.
     *
     *
     * The [RaycastResult] class implements the Comparable interface to allow sorting by
     * distance from the ray's origin.
     *
     *
     * If the all flag is false, the results list will only contain the closest result (if any).
     *
     *
     * All raycasts pass through the [RaycastListener]s before being tested.  If **any**
     * [RaycastListener] doesn't allow the raycast then the body will not be tested.
     *
     *
     * Bodies that contain the start of the ray will not be included in the results.
     * @param start the start point
     * @param end the end point
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor [BodyFixture]s should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param all true if all intersected [Body]s should be returned; false if only the closest [Body] should be returned
     * @param results a list to contain the results of the raycast
     * @return boolean true if at least one [Body] was intersected by the [Ray]
     * @throws NullPointerException if start, end, or results is null
     * @see .raycast
     * @see RaycastListener.allow
     * @since 3.1.9
     */
    fun raycast(start: Vector2, end: Vector2, filter: Filter?, ignoreSensors: Boolean, ignoreInactive: Boolean,
                all: Boolean, results: MutableList<RaycastResult>): Boolean {
        // create the ray and obtain the maximum length
        val d: Vector2 = start.to(end)
        val maxLength: Double = d.normalize()
        val ray = Ray(start, d)
        // call the raycast method
        return this.raycast(ray, maxLength, filter, ignoreSensors, ignoreInactive, all, results)
    }

    /**
     * Performs a raycast against all the [Body]s in the [World].
     *
     *
     * The given [RaycastResult] list, results, will be filled with the raycast results
     * if the given ray intersected any bodies.
     *
     *
     * The [RaycastResult] class implements the Comparable interface to allow sorting by
     * distance from the ray's origin.
     *
     *
     * If the all flag is false, the results list will only contain the closest result (if any).
     *
     *
     * Pass 0 into the maxLength field to specify an infinite length [Ray].
     *
     *
     * All raycasts pass through the [RaycastListener]s before being tested.  If **any**
     * [RaycastListener] doesn't allow the raycast then the body will not be tested.
     *
     *
     * Bodies that contain the start of the ray will not be included in the results.
     *
     *
     * Inactive bodies are ignored in this test.
     * @param ray the [Ray]
     * @param maxLength the maximum length of the ray; 0 for infinite length
     * @param ignoreSensors true if sensor [BodyFixture]s should be ignored
     * @param all true if all intersected [Body]s should be returned; false if only the closest [Body] should be returned
     * @param results a list to contain the results of the raycast
     * @return boolean true if at least one [Body] was intersected by the given [Ray]
     * @throws NullPointerException if ray or results is null
     * @see .raycast
     * @see RaycastListener.allow
     * @since 2.0.0
     */
    fun raycast(ray: Ray, maxLength: Double, ignoreSensors: Boolean, all: Boolean, results: MutableList<RaycastResult>): Boolean {
        return this.raycast(ray, maxLength, null, ignoreSensors, true, all, results)
    }

    /**
     * Performs a raycast against all the [Body]s in the [World].
     *
     *
     * The given [RaycastResult] list, results, will be filled with the raycast results
     * if the given ray intersected any bodies.
     *
     *
     * The [RaycastResult] class implements the Comparable interface to allow sorting by
     * distance from the ray's origin.
     *
     *
     * If the all flag is false, the results list will only contain the closest result (if any).
     *
     *
     * Pass 0 into the maxLength field to specify an infinite length [Ray].
     *
     *
     * All raycasts pass through the [RaycastListener]s before being tested.  If **any**
     * [RaycastListener] doesn't allow the raycast then the body will not be tested.
     *
     *
     * Bodies that contain the start of the ray will not be included in the results.
     * @param ray the [Ray]
     * @param maxLength the maximum length of the ray; 0 for infinite length
     * @param ignoreSensors true if sensor [BodyFixture]s should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param all true if all intersected [Body]s should be returned; false if only the closest [Body] should be returned
     * @param results a list to contain the results of the raycast
     * @return boolean true if at least one [Body] was intersected by the given [Ray]
     * @throws NullPointerException if ray or results is null
     * @see .raycast
     * @see RaycastListener.allow
     * @since 3.1.9
     */
    fun raycast(ray: Ray, maxLength: Double, ignoreSensors: Boolean, ignoreInactive: Boolean, all: Boolean,
                results: MutableList<RaycastResult>): Boolean {
        return this.raycast(ray, maxLength, null, ignoreSensors, ignoreInactive, all, results)
    }

    /**
     * Performs a raycast against all the [Body]s in the [World].
     *
     *
     * The given [RaycastResult] list, results, will be filled with the raycast results
     * if the given ray intersected any bodies.
     *
     *
     * The [RaycastResult] class implements the Comparable interface to allow sorting by
     * distance from the ray's origin.
     *
     *
     * If the all flag is false, the results list will only contain the closest result (if any).
     *
     *
     * Pass 0 into the maxLength field to specify an infinite length [Ray].
     *
     *
     * All raycasts pass through the [RaycastListener]s before being tested.  If **any**
     * [RaycastListener] doesn't allow the raycast then the body will not be tested.
     *
     *
     * Bodies that contain the start of the ray will not be included in the results.
     * @param ray the [Ray]
     * @param maxLength the maximum length of the ray; 0 for infinite length
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor [BodyFixture]s should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param all true if all intersected [Body]s should be returned; false if only the closest [Body] should be returned
     * @param results a list to contain the results of the raycast
     * @return boolean true if at least one [Body] was intersected by the given [Ray]
     * @throws NullPointerException if ray or results is null
     * @see .raycast
     * @see RaycastListener.allow
     * @since 3.1.9
     */
    fun raycast(ray: Ray, maxLength: Double, filter: Filter?, ignoreSensors: Boolean, ignoreInactive: Boolean,
        all: Boolean, results: MutableList<RaycastResult>): Boolean {
        val listeners: List<RaycastListener> = this.getListeners(RaycastListener::class)!!
        val rlSize = listeners.size
        // check for the desired length
        var max = 0.0
        if (maxLength > 0.0) {
            max = maxLength
        }
        // create a raycast result
        var result: RaycastResult? = null
        val bpFilter = RaycastBroadphaseFilter(ignoreInactive, ignoreSensors, filter)
        // filter using the broadphase first
        val items = broadphaseDetector!!.raycast(ray, maxLength, bpFilter)
        // loop over the list of bodies testing each one
        val size = items!!.size
        var found = false
        var allow = true
        for (i in 0 until size) {
            // get a body to test
            val item = items[i]
            val body = item.collidable
            val fixture = item.fixture
            val transform: Transform = body.transform

            // create a raycast object to store the result
            val raycast = Raycast()

            // notify the listeners to see if we should test this fixture
            allow = true
            for (j in 0 until rlSize) {
                val rl = listeners[j]
                // see if we should test this fixture
                if (!rl.allow(ray, body, fixture)) {
                    allow = false
                }
            }
            if (!allow) continue
            // get the convex shape
            val convex: Convex = fixture.shape
            // perform the raycast
            if (raycastDetector!!.raycast(ray, max, convex, transform, raycast)) {
                // notify the listeners to see if we should allow this result
                allow = true
                for (j in 0 until rlSize) {
                    val rl = listeners[j]
                    // see if we should test this fixture
                    if (!rl.allow(ray, body, fixture, raycast)) {
                        allow = false
                    }
                }
                if (!allow) continue
                if (!all) {
                    if (result == null) {
                        result = RaycastResult(body, fixture, raycast)
                        results.add(result)
                        found = true
                    } else {
                        result.body = body
                        result.fixture = fixture
                        result.raycast = raycast
                    }
                    // we are only looking for the closest so
                    // set the new maximum
                    max = result.raycast!!.distance
                } else {
                    // add this result to the results
                    results.add(RaycastResult(body, fixture, raycast))
                    found = true
                }
            }
        }
        return found
    }

    /**
     * Performs a raycast against the given [Body] and returns true
     * if the ray intersects the body.
     *
     *
     * The given [RaycastResult] object, result, will be filled with the raycast result
     * if the given ray intersected the given body.
     *
     *
     * All raycasts pass through the [RaycastListener]s before being tested.  If **any**
     * [RaycastListener] doesn't allow the raycast then the body will not be tested.
     *
     *
     * Returns false if the start position of the ray lies inside the given body.
     * @param start the start point
     * @param end the end point
     * @param body the [Body] to test
     * @param ignoreSensors whether or not to ignore sensor [BodyFixture]s
     * @param result the raycast result
     * @return boolean true if the [Ray] intersects the [Body]
     * @throws NullPointerException if start, end, body, or result is null
     * @see .raycast
     * @see RaycastListener.allow
     * @since 2.0.0
     */
    fun raycast(start: Vector2, end: Vector2, body: Body, ignoreSensors: Boolean, result: RaycastResult): Boolean {
        return this.raycast(start, end, body, null, ignoreSensors, result)
    }

    /**
     * Performs a raycast against the given [Body] and returns true
     * if the ray intersects the body.
     *
     *
     * The given [RaycastResult] object, result, will be filled with the raycast result
     * if the given ray intersected the given body.
     *
     *
     * All raycasts pass through the [RaycastListener]s before being tested.  If **any**
     * [RaycastListener] doesn't allow the raycast then the body will not be tested.
     *
     *
     * Returns false if the start position of the ray lies inside the given body.
     * @param start the start point
     * @param end the end point
     * @param body the [Body] to test
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors whether or not to ignore sensor [BodyFixture]s
     * @param result the raycast result
     * @return boolean true if the [Ray] intersects the [Body]
     * @throws NullPointerException if start, end, body, or result is null
     * @see .raycast
     * @see RaycastListener.allow
     * @since 3.1.9
     */
    fun raycast(start: Vector2, end: Vector2, body: Body, filter: Filter?, ignoreSensors: Boolean, result: RaycastResult): Boolean {
        // create the ray and obtain the maximum length
        val d: Vector2 = start.to(end)
        val maxLength: Double = d.normalize()
        val ray = Ray(start, d)
        // call the raycast method
        return this.raycast(ray, body, maxLength, filter, ignoreSensors, result)
    }

    /**
     * Performs a raycast against the given [Body] and returns true
     * if the ray intersects the body.
     *
     *
     * The given [RaycastResult] object, result, will be filled with the raycast result
     * if the given ray intersected the given body.
     *
     *
     * Pass 0 into the maxLength field to specify an infinite length [Ray].
     *
     *
     * All raycasts pass through the [RaycastListener]s before being tested.  If **any**
     * [RaycastListener] doesn't allow the raycast then the body will not be tested.
     *
     *
     * Returns false if the start position of the ray lies inside the given body.
     * @param ray the [Ray] to cast
     * @param body the [Body] to test
     * @param maxLength the maximum length of the ray; 0 for infinite length
     * @param ignoreSensors whether or not to ignore sensor [BodyFixture]s
     * @param result the raycast result
     * @return boolean true if the [Ray] intersects the [Body]
     * @throws NullPointerException if ray, body, or result is null
     * @see .raycast
     * @see RaycastListener.allow
     * @since 2.0.0
     */
    fun raycast(ray: Ray, body: Body, maxLength: Double, ignoreSensors: Boolean, result: RaycastResult): Boolean {
        return this.raycast(ray, body, maxLength, null, ignoreSensors, result)
    }

    /**
     * Performs a raycast against the given [Body] and returns true
     * if the ray intersects the body.
     *
     *
     * The given [RaycastResult] object, result, will be filled with the raycast result
     * if the given ray intersected the given body.
     *
     *
     * Pass 0 into the maxLength field to specify an infinite length [Ray].
     *
     *
     * All raycasts pass through the [RaycastListener]s before being tested.  If **any**
     * [RaycastListener] doesn't allow the raycast then the body will not be tested.
     *
     *
     * Returns false if the start position of the ray lies inside the given body.
     * @param ray the [Ray] to cast
     * @param body the [Body] to test
     * @param maxLength the maximum length of the ray; 0 for infinite length
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors whether or not to ignore sensor [BodyFixture]s
     * @param result the raycast result
     * @return boolean true if the [Ray] intersects the [Body]
     * @throws NullPointerException if ray, body, or result is null
     * @see .raycast
     * @see RaycastListener.allow
     * @since 3.1.9
     */
    fun raycast(ray: Ray, body: Body, maxLength: Double, filter: Filter?, ignoreSensors: Boolean, result: RaycastResult): Boolean {
        val listeners: List<RaycastListener> = this.getListeners(RaycastListener::class)!!
        val rlSize = listeners.size
        var allow = true
        // get the number of fixtures
        val size = body.fixtureCount
        // get the body transform
        val transform: Transform = body.transform
        // set the maximum length
        var max = 0.0
        if (maxLength > 0.0) {
            max = maxLength
        }
        // create a raycast object to store the result
        val raycast = Raycast()
        // loop over the fixtures finding the closest one
        var found = false
        for (i in 0 until size) {
            // get the fixture
            val fixture = body.getFixture(i)!!
            // check for sensor
            if (ignoreSensors && fixture.isSensor) {
                // skip this fixture
                continue
            }
            // check against the filter
            if (filter != null && !filter.isAllowed(fixture.filter)) {
                continue
            }
            // notify the listeners to see if we should test this fixture
            allow = true
            for (j in 0 until rlSize) {
                val rl = listeners[j]
                // see if we should test this fixture
                if (!rl.allow(ray, body, fixture)) {
                    allow = false
                }
            }
            if (!allow) continue
            // get the convex shape
            val convex: Convex = fixture.shape
            // perform the raycast
            if (raycastDetector!!.raycast(ray, max, convex, transform, raycast)) {
                // notify the listeners to see if we should allow this result
                allow = true
                for (j in 0 until rlSize) {
                    val rl = listeners[j]
                    // see if we should test this fixture
                    if (!rl.allow(ray, body, fixture, raycast)) {
                        allow = false
                    }
                }
                if (!allow) continue
                // if the raycast detected a collision then set the new
                // maximum distance
                max = raycast.distance
                // assign the fixture
                result.fixture = fixture
                // the last raycast will always be the minimum raycast
                // flag that we did get a successful raycast
                found = true
            }
        }

        // we only want to populate the
        // result object if a result was found
        if (found) {
            result.body = body
            result.raycast = raycast
        }
        return found
    }

    /**
     * Performs a linear convex cast on the world, placing any detected collisions into the given results list.
     *
     *
     * This method does a static test of bodies (in other words, does not take into account the bodies linear
     * or angular velocity, but rather assumes they are stationary).
     *
     *
     * The `deltaPosition` parameter is the linear cast vector determining the direction and magnitude of the cast.
     *
     *
     * The [ConvexCastResult] class implements the Comparable interface to allow sorting by
     * the time of impact.
     *
     *
     * If the all flag is false, the results list will only contain the closest result (if any).
     *
     *
     * All convex casts pass through the [ConvexCastListener]s before being tested.  If **any**
     * [ConvexCastListener] doesn't allow the convex cast, then the body will not be tested.
     *
     *
     * For multi-fixtured bodies, only the fixture that has the minimum time of impact will be added to the
     * results list.
     *
     *
     * Bodies in collision with the given convex at the beginning of the cast are not included in the results.
     *
     *
     * Inactive bodies are ignored in this test.
     * @param convex the convex to cast
     * @param transform the initial position and orientation of the convex
     * @param deltaPosition position; the change in position (the cast length and direction basically)
     * @param ignoreSensors true if sensor fixtures should be ignored in the tests
     * @param all true if all hits should be returned; false if only the first should be returned
     * @param results the list to add the results to
     * @return boolean true if a collision was found
     * @since 3.1.5
     * @see .convexCast
     */
    fun convexCast(convex: Convex, transform: Transform, deltaPosition: Vector2, ignoreSensors: Boolean,
                   all: Boolean, results: MutableList<ConvexCastResult>): Boolean {
        return this.convexCast(convex, transform, deltaPosition, 0.0, null, ignoreSensors, true, all, results)
    }

    /**
     * Performs a linear convex cast on the world, placing any detected collisions into the given results list.
     *
     *
     * This method does a static test of bodies (in other words, does not take into account the bodies linear
     * or angular velocity, but rather assumes they are stationary).
     *
     *
     * The `deltaPosition` parameter is the linear cast vector determining the direction and magnitude of the cast.
     *
     *
     * The [ConvexCastResult] class implements the Comparable interface to allow sorting by
     * the time of impact.
     *
     *
     * If the all flag is false, the results list will only contain the closest result (if any).
     *
     *
     * All convex casts pass through the [ConvexCastListener]s before being tested.  If **any**
     * [ConvexCastListener] doesn't allow the convex cast, then the body will not be tested.
     *
     *
     * For multi-fixtured bodies, only the fixture that has the minimum time of impact will be added to the
     * results list.
     *
     *
     * Bodies in collision with the given convex at the beginning of the cast are not included in the results.
     * @param convex the convex to cast
     * @param transform the initial position and orientation of the convex
     * @param deltaPosition position; the change in position (the cast length and direction basically)
     * @param ignoreSensors true if sensor fixtures should be ignored in the tests
     * @param ignoreInactive true if inactive bodies should be ignored in the tests
     * @param all true if all hits should be returned; false if only the first should be returned
     * @param results the list to add the results to
     * @return boolean true if a collision was found
     * @since 3.1.9
     * @see .convexCast
     */
    fun convexCast(convex: Convex, transform: Transform, deltaPosition: Vector2, ignoreSensors: Boolean, ignoreInactive: Boolean,
        all: Boolean, results: MutableList<ConvexCastResult>): Boolean {
        return this.convexCast(convex, transform, deltaPosition, 0.0, null, ignoreSensors, ignoreInactive, all, results)
    }

    /**
     * Performs a linear convex cast on the world, placing any detected collisions into the given results list.
     *
     *
     * This method does a static test of bodies (in other words, does not take into account the bodies linear
     * or angular velocity, but rather assumes they are stationary).
     *
     *
     * The `deltaPosition` parameter is the linear cast vector determining the direction and magnitude of the cast.
     * The `deltaAngle` parameter is the change in angle over the linear cast and is interpolated linearly
     * during detection.
     *
     *
     * The [ConvexCastResult] class implements the Comparable interface to allow sorting by
     * the time of impact.
     *
     *
     * If the all flag is false, the results list will only contain the closest result (if any).
     *
     *
     * All convex casts pass through the [ConvexCastListener]s before being tested.  If **any**
     * [ConvexCastListener] doesn't allow the convex cast, then the body will not be tested.
     *
     *
     * For multi-fixtured bodies, only the fixture that has the minimum time of impact will be added to the
     * results list.
     *
     *
     * Bodies in collision with the given convex at the beginning of the cast are not included in the results.
     *
     *
     * Inactive bodies are ignored in this test.
     * @param convex the convex to cast
     * @param transform the initial position and orientation of the convex
     * @param deltaPosition position; the change in position (the cast length and direction basically)
     * @param deltaAngle angle; the change in the angle; this is the change in the angle over the linear period
     * @param ignoreSensors true if sensor fixtures should be ignored in the tests
     * @param all true if all hits should be returned; false if only the first should be returned
     * @param results the list to add the results to
     * @return boolean true if a collision was found
     * @see .convexCast
     * @since 3.1.5
     */
    fun convexCast(convex: Convex, transform: Transform, deltaPosition: Vector2, deltaAngle: Double, ignoreSensors: Boolean,
        all: Boolean, results: MutableList<ConvexCastResult>): Boolean {
        return this.convexCast(convex, transform, deltaPosition, deltaAngle, null, ignoreSensors, true, all, results)
    }

    /**
     * Performs a linear convex cast on the world, placing any detected collisions into the given results list.
     *
     *
     * This method does a static test of bodies (in other words, does not take into account the bodies linear
     * or angular velocity, but rather assumes they are stationary).
     *
     *
     * The `deltaPosition` parameter is the linear cast vector determining the direction and magnitude of the cast.
     * The `deltaAngle` parameter is the change in angle over the linear cast and is interpolated linearly
     * during detection.
     *
     *
     * The [ConvexCastResult] class implements the Comparable interface to allow sorting by
     * the time of impact.
     *
     *
     * If the all flag is false, the results list will only contain the closest result (if any).
     *
     *
     * All convex casts pass through the [ConvexCastListener]s before being tested.  If **any**
     * [ConvexCastListener] doesn't allow the convex cast, then the body will not be tested.
     *
     *
     * For multi-fixtured bodies, only the fixture that has the minimum time of impact will be added to the
     * results list.
     *
     *
     * Bodies in collision with the given convex at the beginning of the cast are not included in the results.
     * @param convex the convex to cast
     * @param transform the initial position and orientation of the convex
     * @param deltaPosition position; the change in position (the cast length and direction basically)
     * @param deltaAngle angle; the change in the angle; this is the change in the angle over the linear period
     * @param ignoreSensors true if sensor fixtures should be ignored in the tests
     * @param ignoreInactive true if inactive bodies should be ignored in the tests
     * @param all true if all hits should be returned; false if only the first should be returned
     * @param results the list to add the results to
     * @return boolean true if a collision was found
     * @since 3.1.9
     */
    fun convexCast(convex: Convex, transform: Transform, deltaPosition: Vector2, deltaAngle: Double, ignoreSensors: Boolean,
        ignoreInactive: Boolean, all: Boolean, results: MutableList<ConvexCastResult>): Boolean {
        return this.convexCast(convex, transform, deltaPosition, deltaAngle, null, ignoreSensors, ignoreInactive, all, results)
    }

    /**
     * Performs a linear convex cast on the world, placing any detected collisions into the given results list.
     *
     *
     * This method does a static test of bodies (in other words, does not take into account the bodies linear
     * or angular velocity, but rather assumes they are stationary).
     *
     *
     * The `deltaPosition` parameter is the linear cast vector determining the direction and magnitude of the cast.
     * The `deltaAngle` parameter is the change in angle over the linear cast and is interpolated linearly
     * during detection.
     *
     *
     * The [ConvexCastResult] class implements the Comparable interface to allow sorting by
     * the time of impact.
     *
     *
     * If the all flag is false, the results list will only contain the closest result (if any).
     *
     *
     * All convex casts pass through the [ConvexCastListener]s before being tested.  If **any**
     * [ConvexCastListener] doesn't allow the convex cast, then the body will not be tested.
     *
     *
     * For multi-fixtured bodies, only the fixture that has the minimum time of impact will be added to the
     * results list.
     *
     *
     * Bodies in collision with the given convex at the beginning of the cast are not included in the results.
     * @param convex the convex to cast
     * @param transform the initial position and orientation of the convex
     * @param deltaPosition position; the change in position (the cast length and direction basically)
     * @param deltaAngle angle; the change in the angle; this is the change in the angle over the linear period
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor fixtures should be ignored in the tests
     * @param ignoreInactive true if inactive bodies should be ignored in the tests
     * @param all true if all hits should be returned; false if only the first should be returned
     * @param results the list to add the results to
     * @return boolean true if a collision was found
     * @since 3.1.9
     */
    fun convexCast(
        convex: Convex, transform: Transform, deltaPosition: Vector2, deltaAngle: Double, filter: Filter?,
        ignoreSensors: Boolean, ignoreInactive: Boolean, all: Boolean, results: MutableList<ConvexCastResult>
    ): Boolean {
        // get the listeners
        val listeners: List<ConvexCastListener> = this.getListeners(ConvexCastListener::class)!!
        val clSize = listeners.size

        // compute a conservative AABB for the motion of the convex
        val radius = convex.radius
        val startWorldCenter: Vector2 = transform.getTransformed(convex.center)
        val startAABB = AABB(startWorldCenter, radius)
        // linearlly interpolate to get the final transform given the
        // change in position and angle
        val finalTransform: Transform = transform.lerped(deltaPosition, deltaAngle, 1.0)
        // get the end AABB
        val endWorldCenter: Vector2 = finalTransform.getTransformed(convex.center)
        val endAABB = AABB(endWorldCenter, radius)
        // union the AABBs to get the swept AABB
        val aabb = startAABB.getUnion(endAABB)
        var min: ConvexCastResult? = null
        val dp2 = Vector2()
        var t2 = 1.0
        var found = false
        var allow = true
        val bpFilter = AABBBroadphaseFilter(ignoreInactive, ignoreSensors, filter)
        // use the broadphase to filter first
        val items: List<BroadphaseItem<Body, BodyFixture>> =
            broadphaseDetector.detect(aabb, bpFilter)
        // loop over the potential collisions
        for (item in items) {
            val body = item.collidable
            val fixture = item.fixture

            // only get the minimum fixture
            var ft2 = t2
            // find the minimum time of impact for the given convex
            // and the current body
            var bodyMinToi: TimeOfImpact? = null
            var bodyMinFixture: BodyFixture? = null
            val bodyTransform: Transform = body.transform

            // notify the listeners to see if we should test this fixture
            allow = true
            for (j in 0 until clSize) {
                val ccl = listeners[j]
                // see if we should test this fixture
                if (!ccl.allow(convex, body, fixture)) {
                    allow = false
                }
            }
            if (!allow) continue

            // get the time of impact
            val c: Convex = fixture.shape
            val timeOfImpact = TimeOfImpact()
            // we pass the zero vector and 0 for the change in position and angle for the body
            // since we assume that it is not moving since this is a static test
            if (timeOfImpactDetector!!.getTimeOfImpact(
                    convex,
                    transform,
                    deltaPosition,
                    deltaAngle,
                    c,
                    bodyTransform,
                    dp2,
                    0.0,
                    0.0,
                    ft2,
                    timeOfImpact
                )
            ) {
                // notify the listeners to see if we should test this fixture
                allow = true
                for (j in 0 until clSize) {
                    val ccl = listeners[j]
                    // see if we should test this fixture
                    if (!ccl.allow(convex, body, fixture, timeOfImpact)) {
                        allow = false
                    }
                }
                if (!allow) continue

                // only save the minimum for the body
                if (bodyMinToi == null || timeOfImpact.time < bodyMinToi.time) {
                    ft2 = timeOfImpact.time
                    bodyMinToi = timeOfImpact
                    bodyMinFixture = fixture
                }
            }
            if (bodyMinToi != null) {
                if (!all) {
                    t2 = bodyMinToi.time
                    if (min == null || bodyMinToi.time < min.timeOfImpact!!.time) {
                        min = ConvexCastResult(body, bodyMinFixture, bodyMinToi)
                    }
                } else {
                    val result = ConvexCastResult(body, fixture, timeOfImpact)
                    results.add(result)
                }
                found = true
            }
        }
        if (min != null) {
            results.add(min)
        }

        // if something is in the list then we know we found a collision
        return found
    }

    /**
     * Performs a linear convex cast on the given body, placing a detected collision into the given result object.
     *
     *
     * This method does a static test of the body (in other words, does not take into account the body's linear
     * or angular velocity, but rather assumes it is stationary).
     *
     *
     * The `deltaPosition` parameter is the linear cast vector determining the direction and magnitude of the cast.
     *
     *
     * All convex casts pass through the [ConvexCastListener]s before being tested.  If **any**
     * [ConvexCastListener] doesn't allow the convex cast, then the body will not be tested.
     *
     *
     * For multi-fixtured bodies, the fixture that has the minimum time of impact will be the result.
     *
     *
     * Returns false if the given body and convex are in collision at the beginning of the cast.
     * @param convex the convex to cast
     * @param transform the initial position and orientation of the convex
     * @param deltaPosition position; the change in position (the cast length and direction basically)
     * @param body the body to cast against
     * @param ignoreSensors true if sensor fixtures should be ignored in the tests
     * @param result the convex cast result
     * @return boolean true if a collision was found
     * @since 3.1.5
     */
    fun convexCast(convex: Convex, transform: Transform, deltaPosition: Vector2, body: Body,
        ignoreSensors: Boolean, result: ConvexCastResult): Boolean {
        return this.convexCast(convex, transform, deltaPosition, 0.0, body, null, ignoreSensors, result)
    }

    /**
     * Performs a linear convex cast on the given body, placing a detected collision into the given result object.
     *
     *
     * This method does a static test of the body (in other words, does not take into account the body's linear
     * or angular velocity, but rather assumes it is stationary).
     *
     *
     * The `deltaPosition` parameter is the linear cast vector determining the direction and magnitude of the cast.
     * The `deltaAngle` parameter is the change in angle over the linear cast and is interpolated linearly
     * during detection.
     *
     *
     * All convex casts pass through the [ConvexCastListener]s before being tested.  If **any**
     * [ConvexCastListener] doesn't allow the convex cast, then the body will not be tested.
     *
     *
     * For multi-fixtured bodies, the fixture that has the minimum time of impact will be the result.
     *
     *
     * Returns false if the given body and convex are in collision at the beginning of the cast.
     * @param convex the convex to cast
     * @param transform the initial position and orientation of the convex
     * @param deltaPosition position; the change in position (the cast length and direction basically)
     * @param deltaAngle angle; the change in the angle; this is the change in the angle over the linear period
     * @param body the body to cast against
     * @param ignoreSensors true if sensor fixtures should be ignored in the tests
     * @param result the convex cast result
     * @return boolean true if a collision was found
     * @since 3.1.5
     */
    fun convexCast(convex: Convex, transform: Transform, deltaPosition: Vector2, deltaAngle: Double, body: Body,
        ignoreSensors: Boolean, result: ConvexCastResult): Boolean {
        return this.convexCast(convex, transform, deltaPosition, deltaAngle, body, null, ignoreSensors, result)
    }

    /**
     * Performs a linear convex cast on the given body, placing a detected collision into the given result object.
     *
     *
     * This method does a static test of the body (in other words, does not take into account the body's linear
     * or angular velocity, but rather assumes it is stationary).
     *
     *
     * The `deltaPosition` parameter is the linear cast vector determining the direction and magnitude of the cast.
     * The `deltaAngle` parameter is the change in angle over the linear cast and is interpolated linearly
     * during detection.
     *
     *
     * All convex casts pass through the [ConvexCastListener]s before being tested.  If **any**
     * [ConvexCastListener] doesn't allow the convex cast, then the body will not be tested.
     *
     *
     * For multi-fixtured bodies, the fixture that has the minimum time of impact will be the result.
     *
     *
     * Returns false if the given body and convex are in collision at the beginning of the cast.
     * @param convex the convex to cast
     * @param transform the initial position and orientation of the convex
     * @param deltaPosition position; the change in position (the cast length and direction basically)
     * @param deltaAngle angle; the change in the angle; this is the change in the angle over the linear period
     * @param body the body to cast against
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor fixtures should be ignored in the tests
     * @param result the convex cast result
     * @return boolean true if a collision was found
     * @since 3.1.9
     */
    fun convexCast(convex: Convex, transform: Transform, deltaPosition: Vector2, deltaAngle: Double, body: Body,
        filter: Filter?, ignoreSensors: Boolean, result: ConvexCastResult): Boolean {
        // get the listeners
        val listeners: List<ConvexCastListener> = this.getListeners(ConvexCastListener::class)!!
        val clSize = listeners.size
        var allow = true
        var found = false
        val dp2 = Vector2()
        var t2 = 1.0

        // find the minimum time of impact for the given convex
        // and the current body
        val bSize = body.fixtureCount
        val bodyTransform: Transform = body.transform

        // loop through all the body fixtures until we find
        // a the fixture that has the smallest time of impact
        for (i in 0 until bSize) {
            val bodyFixture = body.getFixture(i)!!
            // filter out sensors if desired
            if (ignoreSensors && bodyFixture.isSensor) continue
            // check the filter
            if (filter != null && !filter.isAllowed(bodyFixture.filter)) continue
            allow = true
            for (j in 0 until clSize) {
                val ccl = listeners[j]
                // see if we should test this body
                if (!ccl.allow(convex, body, bodyFixture)) {
                    allow = false
                }
            }
            if (!allow) return false

            // get the time of impact
            val c: Convex = bodyFixture.shape
            val toi = TimeOfImpact()
            // we pass the zero vector and 0 for the change in position and angle for the body
            // since we assume that it is not moving since this is a static test
            if (timeOfImpactDetector!!.getTimeOfImpact(
                    convex,
                    transform,
                    deltaPosition,
                    deltaAngle,
                    c,
                    bodyTransform,
                    dp2,
                    0.0,
                    0.0,
                    t2,
                    toi
                )
            ) {
                // notify the listeners to see if we should test this fixture
                allow = true
                for (j in 0 until clSize) {
                    val ccl = listeners[j]
                    // see if we should test this fixture
                    if (!ccl.allow(convex, body, bodyFixture, toi)) {
                        allow = false
                    }
                }
                if (!allow) continue

                // set the new maximum time
                t2 = toi.time
                // save the min time of impact
                result.fixture = bodyFixture
                result.timeOfImpact = toi
                result.body = body
                // set the found flag
                found = true
            }
        }
        return found
    }

    /**
     * Returns true if the given AABB overlaps a [Body] in this [World].
     *
     *
     * If any part of a body is overlaping the AABB, the body is added to the list.
     *
     *
     * This performs a static collision test of the world using the [BroadphaseDetector].
     *
     *
     * This may return bodies who only have sensor fixtures overlapping.
     *
     *
     * Inactive bodies are ignored in this test.
     * @param aabb the world space [AABB]
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if the AABB overlaps any body
     * @since 3.1.9
     */
    fun detect(aabb: AABB, results: MutableList<DetectResult>): Boolean {
        return this.detect(aabb, null, false, true, results)
    }

    /**
     * Returns true if the given AABB overlaps a [Body] [Fixture] in this [World].
     *
     *
     * If any part of a body is overlaping the AABB, the body and that respective fixture is added
     * to the returned list.
     *
     *
     * This performs a static collision test of the world using the [BroadphaseDetector].
     *
     *
     * This may return bodies who only have sensor fixtures overlapping.
     * @param aabb the world space [AABB]
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if the AABB overlaps any body
     * @since 3.1.9
     */
    fun detect(aabb: AABB, ignoreInactive: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(aabb, null, false, ignoreInactive, results)
    }

    /**
     * Returns true if the given AABB overlaps a [Body] in this [World].
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     * @param aabb the world space [AABB]
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if the AABB overlaps any fixture
     * @since 3.1.9
     */
    fun detect(aabb: AABB, ignoreSensors: Boolean, ignoreInactive: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(aabb, null, ignoreSensors, ignoreInactive, results)
    }

    /**
     * Returns true if the given AABB overlaps a [Body] in this [World].
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     * @param aabb the world space [AABB]
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if the AABB overlaps any fixture
     * @since 3.1.9
     */
    fun detect(aabb: AABB, filter: Filter?, ignoreSensors: Boolean, ignoreInactive: Boolean, results: MutableList<DetectResult>): Boolean {
        val listeners: List<DetectListener> = this.getListeners(DetectListener::class)!!
        val dlSize = listeners.size
        val bpFilter = AABBBroadphaseFilter(ignoreInactive, ignoreSensors, filter)
        val collisions: List<BroadphaseItem<Body, BodyFixture>> = broadphaseDetector.detect(aabb, bpFilter)
        var found = false
        val bSize = collisions.size
        var allow: Boolean
        for (i in 0 until bSize) {
            val item = collisions[i]
            val body = item.collidable
            val fixture = item.fixture
            // check body's fixtures next
            val transform: Transform = body.transform
            // pass through the listeners
            allow = true
            for (j in 0 until dlSize) {
                val dl = listeners[j]
                if (!dl.allow(aabb, body, fixture)) {
                    allow = false
                }
            }
            if (!allow) {
                continue
            }
            // create an AABB for the fixture
            val faabb: AABB = fixture.shape.createAABB(transform)
            // test the aabbs
            if (aabb.overlaps(faabb)) {
                // add this fixture to the results list
                val result = DetectResult(body, fixture)
                results.add(result)
                found = true
            }
        }
        return found
    }

    /**
     * Returns true if the given [Convex] overlaps a body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * The returned results may include sensor fixutres.
     *
     *
     * Inactive bodies are ignored in this test.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the convex shape in world coordinates
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     * @see .detect
     */
    fun detect(convex: Convex, results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, IDENTITY, null, false, true, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps a body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * Inactive bodies are ignored in this test.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the convex shape in world coordinates
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     * @see .detect
     */
    fun detect(convex: Convex, ignoreSensors: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, IDENTITY, null, ignoreSensors, true, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps a body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the convex shape in world coordinates
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     * @see .detect
     */
    fun detect(convex: Convex, ignoreSensors: Boolean, ignoreInactive: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, IDENTITY, null, ignoreSensors, ignoreInactive, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps a body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the convex shape in world coordinates
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     * @see .detect
     */
    fun detect(convex: Convex, filter: Filter, ignoreSensors: Boolean, ignoreInactive: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, IDENTITY, filter, ignoreSensors, ignoreInactive, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps a body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * Use the `includeCollisionData` parameter to have the [Penetration] object
     * filled in the [DetectResult]s.  Including this information will have a performance impact.
     * @param convex the convex shape in world coordinates
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param includeCollisionData true if the overlap [Penetration] should be returned
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     */
    fun detect(convex: Convex, filter: Filter?, ignoreSensors: Boolean, ignoreInactive: Boolean,
               includeCollisionData: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, IDENTITY, filter, ignoreSensors, ignoreInactive, includeCollisionData, results)
    }

    /**
     * Returns true if the given [Convex] overlaps a body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * The returned results may include sensor fixutres.
     *
     *
     * Inactive bodies are ignored in this test.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the convex shape in local coordinates
     * @param transform the convex shape's world transform
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     * @see .detect
     */
    fun detect(convex: Convex, transform: Transform, results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, transform, null, false, true, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps a body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * Inactive bodies are ignored in this test.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the convex shape in local coordinates
     * @param transform the convex shape's world transform
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     * @see .detect
     */
    fun detect(convex: Convex, transform: Transform, ignoreSensors: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, transform, null, ignoreSensors, true, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps a body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the convex shape in local coordinates
     * @param transform the convex shape's world transform
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     * @see .detect
     */
    fun detect(convex: Convex, transform: Transform, ignoreSensors: Boolean, ignoreInactive: Boolean,
               results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, transform, null, ignoreSensors, ignoreInactive, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps a body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the convex shape in local coordinates
     * @param transform the convex shape's world transform
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     * @see .detect
     */
    fun detect(convex: Convex, transform: Transform, filter: Filter?, ignoreSensors: Boolean, ignoreInactive: Boolean,
        results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, transform, filter, ignoreSensors, ignoreInactive, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps a body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * Use the `includeCollisionData` parameter to have the [Penetration] object
     * filled in the [DetectResult]s.  Including this information will have a performance impact.
     * @param convex the convex shape in local coordinates
     * @param transform the convex shape's world transform
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param ignoreInactive true if inactive bodies should be ignored
     * @param includeCollisionData true if the overlap [Penetration] should be returned
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     */
    fun detect(convex: Convex, transform: Transform, filter: Filter?, ignoreSensors: Boolean, ignoreInactive: Boolean,
        includeCollisionData: Boolean, results: MutableList<DetectResult>): Boolean {
        val listeners: List<DetectListener> = this.getListeners(DetectListener::class)!!
        val dlSize = listeners.size
        var allow = true

        // create an aabb for the given convex
        val aabb = convex.createAABB(transform)
        val bpFilter = AABBBroadphaseFilter(ignoreInactive, ignoreSensors, filter)
        // test using the broadphase to rule out as many bodies as we can
        val items: List<BroadphaseItem<Body, BodyFixture>> = broadphaseDetector.detect(aabb, bpFilter)
        // now perform a more accurate test
        val bSize = items.size
        var found = false
        for (i in 0 until bSize) {
            val item = items[i]
            val body = item.collidable
            val fixture = item.fixture
            // get the body transform
            val bt: Transform = body.transform

            // pass through the listeners
            allow = true
            for (j in 0 until dlSize) {
                val dl = listeners[j]
                if (!dl.allow(convex, transform, body, fixture)) {
                    allow = false
                }
            }
            if (!allow) {
                continue
            }

            // just perform a boolean test since its typically faster
            val bc: Convex = fixture.shape
            var collision = false
            // should we use the fast method or the one that returns the collision info
            val penetration = if (includeCollisionData) Penetration() else null
            collision = if (includeCollisionData) {
                narrowphaseDetector!!.detect(convex, transform, bc, bt, penetration)
            } else {
                narrowphaseDetector!!.detect(convex, transform, bc, bt)
            }
            if (collision) {
                // add this fixture to the results list
                val result = DetectResult(body, fixture, penetration)
                results.add(result)
                found = true
            }
        }
        // return the bodies in collision
        return found
    }

    /**
     * Returns true if the given [AABB] overlaps the given body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the AABB overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     * @param aabb the [AABB] in world coordinates
     * @param body the [Body] to test against
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     */
    fun detect(aabb: AABB, body: Body, ignoreSensors: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(aabb, body, null, ignoreSensors, results)
    }

    /**
     * Returns true if the given [AABB] overlaps the given body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the AABB overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     * @param aabb the [AABB] in world coordinates
     * @param body the [Body] to test against
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     */
    fun detect(aabb: AABB, body: Body, filter: Filter?, ignoreSensors: Boolean, results: MutableList<DetectResult>): Boolean {
        val listeners: List<DetectListener> = this.getListeners(DetectListener::class)!!
        val dlSize = listeners.size
        var allow = true
        // test the AABBs
        var found = false
        var baabb = broadphaseDetector!!.getAABB(body)
        if (baabb == null) {
            baabb = body.createAABB()
        }
        if (aabb.overlaps(baabb)) {
            // check body's fixtures next
            val transform: Transform = body.transform
            val fSize = body.fixtureCount
            for (j in 0 until fSize) {
                val fixture = body.getFixture(j)!!
                // test for sensors
                if (ignoreSensors && fixture.isSensor) continue
                // test the filter
                if (filter != null && !filter.isAllowed(fixture.filter)) continue
                // pass through the listeners
                allow = true
                for (k in 0 until dlSize) {
                    val dl = listeners[k]
                    if (!dl.allow(aabb, body, fixture)) {
                        allow = false
                    }
                }
                if (!allow) {
                    continue
                }
                // create an AABB for the fixture
                val faabb: AABB = fixture.shape.createAABB(transform)
                // test the aabbs
                if (aabb.overlaps(faabb)) {
                    // add this fixture to the results list
                    val result = DetectResult(body, fixture)
                    results.add(result)
                    found = true
                }
            }
        }
        return found
    }

    /**
     * Returns true if the given [Convex] overlaps the given body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the [Convex] in world coordinates
     * @param body the [Body] to test against
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     */
    fun detect(convex: Convex, body: Body, ignoreSensors: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, IDENTITY, body, null, ignoreSensors, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps the given body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the [Convex] in world coordinates
     * @param body the [Body] to test against
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     */
    fun detect(convex: Convex, body: Body, filter: Filter?, ignoreSensors: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, IDENTITY, body, filter, ignoreSensors, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps the given body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * Use the `includeCollisionData` parameter to have the [Penetration] object
     * filled in the [DetectResult]s.  Including this information negatively impacts performance.
     * @param convex the [Convex] in world coordinates
     * @param body the [Body] to test against
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param includeCollisionData true if the overlap [Penetration] should be returned
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     */
    fun detect(convex: Convex, body: Body, filter: Filter?, ignoreSensors: Boolean, includeCollisionData: Boolean,
               results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, IDENTITY, body, filter, ignoreSensors, includeCollisionData, results)
    }

    /**
     * Returns true if the given [Convex] overlaps the given body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the [Convex] in local coordinates
     * @param transform the convex shape's world [Transform]
     * @param body the [Body] to test against
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     */
    fun detect(convex: Convex, transform: Transform, body: Body, ignoreSensors: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, transform, body, null, ignoreSensors, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps the given body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * The results from this test will not include [Penetration] objects.
     * @param convex the [Convex] in local coordinates
     * @param transform the convex shape's world [Transform]
     * @param body the [Body] to test against
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     */
    fun detect(convex: Convex, transform: Transform, body: Body, filter: Filter, ignoreSensors: Boolean, results: MutableList<DetectResult>): Boolean {
        return this.detect(convex, transform, body, filter, ignoreSensors, false, results)
    }

    /**
     * Returns true if the given [Convex] overlaps the given body in the world.
     *
     *
     * If this method returns true, the results list will contain the bodies and
     * fixtures that the convex overlaps.
     *
     *
     * Use the [Body.isInContact] method instead if you want to test if two bodies
     * are colliding.
     *
     *
     * Use the `includeCollisionData` parameter to have the [Penetration] object
     * filled in the [DetectResult]s.  Including this information negatively impacts performance.
     * @param convex the [Convex] in local coordinates
     * @param transform the convex shape's world [Transform]
     * @param body the [Body] to test against
     * @param filter the [Filter] to use against the fixtures; can be null
     * @param includeCollisionData true if the overlap [Penetration] should be returned
     * @param ignoreSensors true if sensor fixtures should be ignored
     * @param results the list of overlapping bodies and fixtures
     * @return boolean true if an overlap was found
     * @since 3.1.9
     */
    fun detect(convex: Convex, transform: Transform, body: Body, filter: Filter?, ignoreSensors: Boolean,
               includeCollisionData: Boolean, results: MutableList<DetectResult>): Boolean {
        val listeners: List<DetectListener> = this.getListeners(DetectListener::class)!!
        val dlSize = listeners.size
        // make sure we can test the body
        var allow = true
        for (i in 0 until dlSize) {
            val dl = listeners[i]
            if (!dl.allow(convex, transform, body)) {
                allow = false
            }
        }
        if (!allow) return false
        // create an aabb for the given convex
        val aabb = convex.createAABB(transform)
        // test using the broadphase to rule out as many bodies as we can
        var baabb = broadphaseDetector!!.getAABB(body)
        if (baabb == null) {
            baabb = body.createAABB()
        }
        // now perform an AABB test first
        var found = false
        if (aabb!!.overlaps(baabb)) {
            // get the body transform
            val bt: Transform = body.transform
            // test all the fixtures
            val fSize = body.fixtureCount
            for (i in 0 until fSize) {
                val fixture = body.getFixture(i)!!
                // check against the sensor flag
                if (ignoreSensors && fixture.isSensor) continue
                // check against the filter if given
                val ff = fixture.filter!!
                if (filter != null && !ff.isAllowed(filter)) continue

                // pass through the listeners
                allow = true
                for (j in 0 until dlSize) {
                    val dl = listeners[j]
                    if (!dl.allow(convex, transform, body, fixture)) {
                        allow = false
                    }
                }
                if (!allow) {
                    continue
                }

                // just perform a boolean test since its typically faster
                val bc: Convex = fixture.shape
                var collision = false
                // should we use the fast method or the one that returns the collision info
                val penetration = if (includeCollisionData) Penetration() else null
                collision = if (includeCollisionData) {
                    narrowphaseDetector!!.detect(convex, transform, bc, bt, penetration)
                } else {
                    narrowphaseDetector!!.detect(convex, transform, bc, bt)
                }
                if (collision) {
                    // add this fixture to the results list
                    val result = DetectResult(body, fixture, penetration)
                    results.add(result)
                    found = true
                }
            }
        }
        // return the bodies in collision
        return found
    }

    /**
     * Shifts the coordinates of the entire world by the given amount.
     * <pre>
     * NewPosition = OldPosition + shift
    </pre> *
     * This method is useful in situations where the world is very large
     * causing very large numbers to be used in the computations.  Shifting
     * the coordinate system allows the computations to be localized and
     * retain accuracy.
     *
     *
     * This method modifies the coordinates of every body and joint in the world.
     *
     *
     * Adding joints or bodies after this method is called should consider that
     * everything has been shifted.
     *
     *
     * This method does **NOT** require a call to [.setUpdateRequired].
     * @param shift the distance to shift along the x and y axes
     * @since 3.2.0
     */
    override fun shift(shift: Vector2) {
        // update the bodies
        val bSize = bodies!!.size
        for (i in 0 until bSize) {
            val body = bodies!![i]
            body.shift(shift)
        }
        // update the joints
        val jSize = joints!!.size
        for (i in 0 until jSize) {
            val joint = joints!![i]
            joint.shift(shift)
        }
        // update the broadphase
        broadphaseDetector!!.shift(shift)
        // update the bounds
        if (bounds != null) {
            bounds!!.shift(shift)
        }
        // update contact manager
        contactManager!!.shift(shift)
    }

    /**
     * Adds the given [Body] to the [World].
     * @param body the [Body] to add
     * @throws NullPointerException if body is null
     * @throws IllegalArgumentException if body has already been added to this world or if its a member of another world instance
     * @since 3.1.1
     */
    fun addBody(body: Body?) {
        // check for null body
        if (body == null) throw NullPointerException(message("dynamics.world.addNullBody"))
        // dont allow adding it twice
        if (body.world == this) throw IllegalArgumentException(message("dynamics.world.addExistingBody"))
        // dont allow a body that already is assigned to another world
        if (body.world != null) throw IllegalArgumentException(message("dynamics.world.addOtherWorldBody"))
        // add it to the world
        bodies.add(body)
        // set the world property on the body
        body.world = this
        // add it to the broadphase
        broadphaseDetector!!.add(body)
    }

    /**
     * Adds the given [Joint] to the [World].
     * @param joint the [Joint] to add
     * @throws NullPointerException if joint is null
     * @throws IllegalArgumentException if joint has already been added to this world or if its a member of another world instance
     * @since 3.1.1
     */
    fun addJoint(joint: Joint?) {
        // check for null joint
        if (joint == null) throw NullPointerException(message("dynamics.world.addNullJoint"))
        // implicitly cast to constraint
        val constraint: Constraint = joint
        // dont allow adding it twice
        if (constraint.world == this) throw IllegalArgumentException(message("dynamics.world.addExistingBody"))
        // dont allow a joint that already is assigned to another world
        if (constraint.world != null) throw IllegalArgumentException(message("dynamics.world.addOtherWorldBody"))
        // add the joint to the joint list
        joints.add(joint)
        // set that its attached to this world
        constraint.world = this
        // get the associated bodies
        val body1 = joint.body1
        val body2 = joint.body2
        // create a joint edge from the first body to the second
        val jointEdge1 = JointEdge(body2, joint)
        // add the edge to the body
        body1!!.joints.add(jointEdge1)
        // create a joint edge from the second body to the first
        val jointEdge2 = JointEdge(body1, joint)
        // add the edge to the body
        body2!!.joints.add(jointEdge2)
    }

    /**
     * Returns true if this world contains the given body.
     * @param body the [Body] to test for
     * @return boolean true if the body is contained in this world
     * @since 3.1.1
     */
    fun containsBody(body: Body?): Boolean {
        return bodies!!.contains(body!!)
    }

    /**
     * Returns true if this world contains the given joint.
     * @param joint the [Joint] to test for
     * @return boolean true if the joint is contained in this world
     * @since 3.1.1
     */
    fun containsJoint(joint: Joint?): Boolean {
        return joints!!.contains(joint!!)
    }

    /**
     * Removes the [Body] at the given index from this [World].
     *
     *
     * Use the [.removeBody] method to enable implicit
     * destruction notification.
     * @param index the index of the body to remove.
     * @return boolean true if the body was removed
     * @since 3.2.0
     */
    fun removeBody(index: Int): Boolean {
        return removeBody(index, false)
    }

    /**
     * Removes the [Body] at the given index from this [World].
     *
     *
     * When a body is removed, joints and contacts may be implicitly destroyed.
     * Pass true to the notify parameter to be notified of the destruction of these objects
     * via the [DestructionListener]s.
     *
     *
     * This method does not trigger [ContactListener.end] events
     * for the contacts that are being removed.
     * @param index the index of the body to remove.
     * @param notify true if implicit destruction should be notified
     * @return boolean true if the body was removed
     * @since 3.2.0
     */
    fun removeBody(index: Int, notify: Boolean): Boolean {
        val body = bodies!![index]
        return removeBody(body, notify)
    }

    /**
     * Removes the given [Body] from this [World].
     *
     *
     * Use the [.removeBody] method to enable implicit
     * destruction notification.
     * @param body the [Body] to remove.
     * @return boolean true if the body was removed
     */
    fun removeBody(body: Body?): Boolean {
        return removeBody(body, false)
    }

    /**
     * Removes the given [Body] from this [World].
     *
     *
     * When a body is removed, joints and contacts may be implicitly destroyed.
     * Pass true to the notify parameter to be notified of the destruction of these objects
     * via the [DestructionListener]s.
     *
     *
     * This method does not trigger [ContactListener.end] events
     * for the contacts that are being removed.
     * @param body the [Body] to remove
     * @param notify true if implicit destruction should be notified
     * @return boolean true if the body was removed
     * @since 3.1.1
     */
    fun removeBody(body: Body?, notify: Boolean): Boolean {
        var listeners: List<DestructionListener>? = null
        if (notify) {
            listeners = this.getListeners(DestructionListener::class)!!
        }
        // check for null body
        if (body == null) return false
        // remove the body from the list
        val removed: Boolean = bodies.remove(body)

        // only remove joints and contacts if the body was removed
        if (removed) {
            // set the world property to null
            body.world = null

            // remove the body from the broadphase
            broadphaseDetector!!.remove(body)

            // wake up any bodies connected to this body by a joint
            // and destroy the joints and remove the edges
            val aIterator = body.joints!!.iterator()
            while (aIterator.hasNext()) {
                // get the joint edge
                val jointEdge: JointEdge = aIterator.next()
                // remove the joint edge from the given body
                aIterator.remove()
                // get the joint
                val joint = jointEdge.interaction!!
                // set the world property to null
                val constraint: Constraint = joint
                constraint.world = null
                // get the other body
                val other: Body = jointEdge.other
                // wake up the other body
                other.setAsleep(false)
                // remove the joint edge from the other body
                val bIterator = other.joints!!.iterator()
                while (bIterator.hasNext()) {
                    // get the joint edge
                    val otherJointEdge: JointEdge = bIterator.next()
                    // get the joint
                    val otherJoint: Joint = otherJointEdge.interaction!!
                    // are the joints the same object reference
                    if (otherJoint === joint) {
                        // remove the joint edge
                        bIterator.remove()
                        // we can break from the loop since there should
                        // not be more than one joint edge per joint per body
                        break
                    }
                }
                // notify of the destroyed joint
                if (notify) {
                    for (dl in listeners!!) {
                        dl.destroyed(joint)
                    }
                }
                // remove the joint from the world
                joints.remove(joint)
            }

            // remove any contacts this body had with any other body
            val acIterator = body.contacts!!.iterator()
            while (acIterator.hasNext()) {
                // get the contact edge
                val contactEdge = acIterator.next()
                // remove the contact edge from the given body
                acIterator.remove()
                // get the contact constraint
                val contactConstraint = contactEdge.interaction
                // get the other body
                val other = contactEdge.other
                // wake up the other body
                other.setAsleep(false)
                // remove the contact edge connected from the other body
                // to this body
                val iterator = other.contacts!!.iterator()
                while (iterator.hasNext()) {
                    val otherContactEdge = iterator.next()
                    // get the contact constraint
                    val otherContactConstraint = otherContactEdge.interaction
                    // check if the contact constraint is the same reference
                    if (otherContactConstraint == contactConstraint) {
                        // remove the contact edge
                        iterator.remove()
                        // break from the loop since there should only be
                        // one contact edge per body pair
                        break
                    }
                }
                // remove the contact constraint from the contact manager
                contactManager!!.end(contactConstraint)
                // loop over the contact points
                val contacts = contactConstraint!!.contacts
                val size = contacts!!.size
                for (j in 0 until size) {
                    // get the contact
                    val contact = contacts[j]!!
                    // create a contact point for notification
                    val contactPoint = ContactPoint(contactConstraint, contact)
                    // call the destruction listeners
                    if (notify) {
                        for (dl in listeners!!) {
                            dl.destroyed(contactPoint)
                        }
                    }
                }
            }
        }
        return removed
    }

    /**
     * Removes the [Joint] at the given index from this [World].
     *
     *
     * No other objects are implicitly destroyed with joints are removed.
     * @param index the index of the [Joint] to remove
     * @return boolean true if the [Joint] was removed
     * @since 3.2.0
     */
    fun removeJoint(index: Int): Boolean {
        val joint = joints!![index]
        return removeJoint(joint)
    }

    /**
     * Removes the given [Joint] from this [World].
     *
     *
     * No other objects are implicitly destroyed with joints are removed.
     * @param joint the [Joint] to remove
     * @return boolean true if the [Joint] was removed
     */
    fun removeJoint(joint: Joint?): Boolean {
        // check for null joint
        if (joint == null) return false
        // remove the joint from the joint list
        val removed: Boolean = joints.remove(joint)

        // see if the given joint was removed
        if (removed) {
            // set the world property to null
            val constraint: Constraint = joint
            constraint.world = null

            // get the involved bodies
            val body1 = joint.body1
            val body2 = joint.body2

            // remove the joint edges from body1
            var iterator = body1!!.joints!!.iterator()
            while (iterator.hasNext()) {
                // see if this is the edge we want to remove
                val jointEdge: JointEdge = iterator.next()
                if (jointEdge.interaction === joint) {
                    // then remove this joint edge
                    iterator.remove()
                    // joints should only have one joint edge
                    // per body
                    break
                }
            }
            // remove the joint edges from body2
            iterator = body2!!.joints!!.iterator()
            while (iterator.hasNext()) {
                // see if this is the edge we want to remove
                val jointEdge: JointEdge = iterator.next()
                if (jointEdge.interaction === joint) {
                    // then remove this joint edge
                    iterator.remove()
                    // joints should only have one joint edge
                    // per body
                    break
                }
            }

            // finally wake both bodies
            body1.setAsleep(false)
            body2.setAsleep(false)
        }
        return removed
    }

    /**
     * Removes all the joints and bodies from this world.
     *
     *
     * This method does **not** notify of destroyed objects.
     * @see .removeAllBodiesAndJoints
     * @since 3.1.1
     */
    fun removeAllBodiesAndJoints() {
        this.removeAllBodiesAndJoints(false)
    }

    /**
     * Removes all the joints and bodies from this world.
     * @param notify true if destruction of joints and contacts should be notified of by the [DestructionListener]
     * @since 3.1.1
     */
    fun removeAllBodiesAndJoints(notify: Boolean) {
        var listeners: List<DestructionListener>? = null
        if (notify) {
            listeners = this.getListeners(DestructionListener::class)!!
        }
        // loop over the bodies and clear the
        // joints and contacts
        val bsize = bodies!!.size
        for (i in 0 until bsize) {
            // get the body
            val body = bodies!![i]
            // clear the joint edges
            body.joints.clear()
            // do we need to notify?
            if (notify) {
                // notify of all the destroyed contacts
                val aIterator = body.contacts!!.iterator()
                while (aIterator.hasNext()) {
                    // get the contact edge
                    val contactEdge = aIterator.next()
                    // get the other body involved
                    val other = contactEdge.other
                    // get the contact constraint
                    val contactConstraint = contactEdge.interaction
                    // find the other contact edge
                    val bIterator = other.contacts!!.iterator()
                    while (bIterator.hasNext()) {
                        // get the contact edge
                        val otherContactEdge = bIterator.next()
                        // get the contact constraint on the edge
                        val otherContactConstraint = otherContactEdge.interaction
                        // are the constraints the same object reference
                        if (otherContactConstraint == contactConstraint) {
                            // if so then remove it
                            bIterator.remove()
                            // there should only be one contact edge
                            // for each body-body pair
                            break
                        }
                    }
                    // notify of all the contacts on the contact constraint
                    val contacts = contactConstraint!!.contacts
                    val csize = contacts!!.size
                    for (j in 0 until csize) {
                        val contact = contacts[j]!!
                        // create a contact point for notification
                        val contactPoint = ContactPoint(contactConstraint, contact)
                        // call the destruction listeners
                        for (dl in listeners!!) {
                            dl.destroyed(contactPoint)
                        }
                    }
                }

                // notify of the destroyed body
                for (dl in listeners!!) {
                    dl.destroyed(body)
                }
            }
            // clear all the contacts
            body.contacts.clear()
            // set the world to null
            body.world = null
        }
        // do we need to notify?
        if (notify) {
            // notify of all the destroyed joints
            val jsize = joints!!.size
            for (i in 0 until jsize) {
                // get the joint
                val joint = joints!![i]
                // set the world property to null
                val constraint: Constraint = joint
                constraint.world = null
                // call the destruction listeners
                for (dl in listeners!!) {
                    dl.destroyed(joint)
                }
            }
        }
        // clear all the broadphase bodies
        broadphaseDetector!!.clear()
        // clear all the joints
        joints.clear()
        // clear all the bodies
        bodies.clear()
        // clear the contact manager of cached contacts
        contactManager!!.clear()
    }

    /**
     * This is a convenience method for the [.removeAllBodiesAndJoints] method since all joints will be removed
     * when all bodies are removed anyway.
     *
     *
     * This method does not notify of the destroyed contacts, joints, etc.
     * @see .removeAllBodies
     * @since 3.0.1
     */
    fun removeAllBodies() {
        this.removeAllBodiesAndJoints(false)
    }

    /**
     * This is a convenience method for the [.removeAllBodiesAndJoints] method since all joints will be removed
     * when all bodies are removed anyway.
     * @param notify true if destruction of joints and contacts should be notified of by the [DestructionListener]
     * @since 3.0.1
     */
    fun removeAllBodies(notify: Boolean) {
        this.removeAllBodiesAndJoints(notify)
    }

    /**
     * Removes all [Joint]s from this [World].
     *
     *
     * This method does not notify of the joints removed.
     * @see .removeAllJoints
     * @since 3.0.1
     */
    fun removeAllJoints() {
        this.removeAllJoints(false)
    }

    /**
     * Removes all [Joint]s from this [World].
     * @param notify true if destruction of joints should be notified of by the [DestructionListener]
     * @since 3.0.1
     */
    fun removeAllJoints(notify: Boolean) {
        var listeners: List<DestructionListener>? = null
        if (notify) {
            listeners = this.getListeners(DestructionListener::class)
        }
        // get the number of joints
        val jSize = joints.size
        // remove all the joints
        for (i in 0 until jSize) {
            // remove the joint from the joint list
            val joint = joints[i]
            // set the world property to null
            val constraint: Constraint = joint
            constraint.world = null

            // get the involved bodies
            val body1 = joint.body1
            val body2 = joint.body2

            // remove the joint edges from body1
            var iterator: MutableIterator<JointEdge> = body1!!.joints!!.iterator()
            while (iterator.hasNext()) {
                // see if this is the edge we want to remove
                val jointEdge: JointEdge = iterator.next()
                if (jointEdge.interaction === joint) {
                    // then remove this joint edge
                    iterator.remove()
                    // joints should only have one joint edge
                    // per body
                    break
                }
            }
            // remove the joint edges from body2
            iterator = body2!!.joints.iterator()
            while (iterator.hasNext()) {
                // see if this is the edge we want to remove
                val jointEdge: JointEdge = iterator.next()
                if (jointEdge.interaction === joint) {
                    // then remove this joint edge
                    iterator.remove()
                    // joints should only have one joint edge
                    // per body
                    break
                }
            }

            // finally wake both bodies
            body1.setAsleep(false)
            body2.setAsleep(false)

            // notify of the destruction if required
            if (notify) {
                for (dl in listeners!!) {
                    dl.destroyed(joint)
                }
            }
        }

        // remove all the joints from the joint list
        joints.clear()
    }

    /**
     * Returns the listeners that are of the given type (or sub types)
     * of the given type.
     *
     *
     * Returns an empty list if no listeners for the given type are found.
     *
     *
     * Returns null if clazz is null.
     *
     *
     * Example usage:
     * <pre>
     * world.getListeners(ContactListener.class);
    </pre> *
     * @param <T> the listener type
     * @param clazz the type of listener to get
     * @return List&lt;T&gt;
     * @since 3.1.0
    </T> */
    fun <T : Listener> getListeners(clazz: KClass<T>?): MutableList<T>? {
        // check for null
        if (clazz == null) return null
        // create a new list and loop over the listeners
        val listeners: MutableList<T> = ArrayList()
        getListeners(clazz, listeners)
        // return the new list
        return listeners
    }

    /**
     * Returns the listeners of the given type (or sub types) in the given list.
     *
     *
     * This method does **not** clear the given listeners list before
     * adding the listeners.
     *
     *
     * If clazz or listeners is null, this method immediately returns.
     *
     *
     * Example usage:
     * <pre>
     * List&lt;ContactListener&gt; list = ...;
     * world.getListeners(ContactListener.class, list);
    </pre> *
     * @param <T> the listener type
     * @param clazz the type of listener to get
     * @param listeners the list to add the listeners to
     * @since 3.1.1
    </T> */
    fun <T : Listener> getListeners(clazz: KClass<T>?, listeners: MutableList<T>?) {
        // check for null
        if (clazz == null || listeners == null) return
        // create a new list and loop over the listeners
        val lSize = this.listeners.size
        for (i in 0 until lSize) {
            val listener: Listener = this.listeners[i]
            // check if the listener is of the given type
            if (clazz.isInstance(listener)) {
                // if so, add it to the new list
                listeners.add(listener as T)
            }
        }
    }

    /**
     * Adds the given listener to the list of listeners.
     * @param listener the listener
     * @throws NullPointerException if the given listener is null
     * @throws IllegalArgumentException if the given listener has already been added to this world
     * @since 3.1.0
     */
    fun addListener(listener: Listener?) {
        // make sure its not null
        if (listener == null) throw NullPointerException(message("dynamics.world.nullListener"))
        // make sure its not already been added
        if (listeners!!.contains(listener)) throw IllegalArgumentException("dynamics.world.addExistingListener")
        // then add the listener
        listeners.add(listener)
    }

    /**
     * Returns true if the given listener is already attached to this world.
     * @param listener the listener
     * @return boolean
     * @since 3.1.1
     */
    fun containsListener(listener: Listener?): Boolean {
        return listeners!!.contains(listener)
    }

    /**
     * Removes the given listener from this world.
     * @param listener the listener to remove
     * @return boolean true if the listener was removed
     * @since 3.1.0
     */
    fun removeListener(listener: Listener?): Boolean {
        return listeners.remove(listener)
    }

    /**
     * Removes all the listeners.
     * @return int the number of listeners removed
     * @since 3.1.1
     */
    fun removeAllListeners(): Int {
        val count = listeners!!.size
        listeners.clear()
        return count
    }

    /**
     * Removes all the listeners of the specified type (or sub types).
     *
     *
     * Returns zero if the given type is null or there are zero listeners
     * attached.
     *
     *
     * Example usage:
     * <pre>
     * world.removeAllListeners(ContactListener.class);
    </pre> *
     * @param <T> the listener type
     * @param clazz the listener type
     * @return int the number of listeners removed
     * @since 3.1.1
    </T> */
    fun <T : Listener> removeAllListeners(clazz: KClass<T>?): Int {
        // if null, just return
        if (clazz == null) return 0
        // if empty list, return
        if (listeners!!.isEmpty()) return 0
        // loop over the list of listeners
        var count = 0
        val listenerIterator = listeners!!.iterator()
        while (listenerIterator.hasNext()) {
            val listener: Listener = listenerIterator.next()
            if (clazz.isInstance(listener)) {
                listenerIterator.remove()
                count++
            }
        }
        return count
    }

    /**
     * Returns the total number of listeners attached to this world.
     * @return int
     * @since 3.1.1
     */
    fun getListenerCount(): Int {
        return listeners!!.size
    }

    /**
     * Returns the total number of listeners of the given type (or sub types)
     * attached to this world.
     *
     *
     * Returns zero if the given class type is null.
     *
     *
     * Example usage:
     * <pre>
     * world.getListenerCount(BoundsListener.class);
    </pre> *
     * @param <T> the listener type
     * @param clazz the listener type
     * @return int
     * @since 3.1.1
    </T> */
    fun <T : Listener> getListenerCount(clazz: KClass<T>?): Int {
        // check for null
        if (clazz == null) return 0
        // loop over the listeners
        var count = 0
        val lSize = listeners!!.size
        for (i in 0 until lSize) {
            val listener: Listener = listeners!![i]
            // check if the listener is of the given type
            if (clazz.isInstance(listener)) {
                // if so, increment
                count++
            }
        }
        // return the count
        return count
    }

    /**
     * Returns the number of [Body]s in this [World].
     * @return int the number of bodies
     */
    val bodyCount: Int
        get() = bodies!!.size

    /**
     * Returns the [Body] at the given index.
     * @param index the index
     * @return [Body]
     */
    fun getBody(index: Int): Body? {
        return bodies!![index]
    }

    /**
     * Returns an iterator for iterating over the bodies in this world.
     *
     *
     * The returned iterator supports the `remove` method.
     * @return Iterator&lt;[Body]&gt;
     * @since 3.2.0
     */
    val bodyIterator: Iterator<Body?>
        get() = BodyIterator(this)

    /**
     * Returns the number of [Joint]s in this [World].
     * @return int the number of joints
     */
    val jointCount: Int
        get() = joints!!.size

    /**
     * Returns the [Joint] at the given index.
     * @param index the index
     * @return [Joint]
     */
    fun getJoint(index: Int): Joint? {
        return joints!![index]
    }

    /**
     * Returns an iterator for iterating over the joints in this world.
     *
     *
     * The returned iterator supports the `remove` method.
     * @return Iterator&lt;[Joint]&gt;
     * @since 3.2.0
     */
    val jointIterator: Iterator<Joint?>
        get() = JointIterator(this)

    /**
     * Returns true if this world doesn't contain any
     * bodies or joints.
     * @return boolean
     * @since 3.0.1
     */
    fun isEmpty(): Boolean {
        val bSize = bodies!!.size
        val jSize = joints!!.size
        return bSize == 0 && jSize == 0
    }

    /**
     * Returns the current accumulated time.
     *
     *
     * This is the time that has elapsed since the last step
     * of the engine.
     *
     *
     * This time is used and/or accumulated on each call of the
     * [.update] and [.update] methods.
     *
     *
     * This time is reduced by the step frequency for each step
     * of the engine.
     * @return double
     * @since 3.1.10
     */
    fun getAccumulatedTime(): Double {
        return time
    }

    /**
     * Sets the current accumulated time.
     *
     *
     * A typical use case would be to throw away any remaining time
     * that the [.update] or [.update]
     * methods didn't use:
     * <pre>
     * boolean updated = world.update(elapsedTime);
     * // the check if the world actually updated is crutial in this example
     * if (updated) {
     * // throw away any remaining time we didnt use
     * world.setAccumulatedTime(0);
     * }
    </pre> *
     * Or, in the case of reusing the same World object, you could use this
     * method to clear any accumulated time.
     *
     *
     * If elapsedTime is less than zero, this method immediately returns.
     * @see .getAccumulatedTime
     * @param elapsedTime the desired elapsed time
     * @since 3.1.10
     */
    fun setAccumulatedTime(elapsedTime: Double) {
        if (elapsedTime < 0.0) return
        time = elapsedTime
    }

}