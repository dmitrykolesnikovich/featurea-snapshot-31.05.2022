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

import org.dyn4j.DataContainer
import org.dyn4j.Epsilon
import org.dyn4j.collision.Collidable
import org.dyn4j.collision.Collidable.Companion.TYPICAL_FIXTURE_COUNT
import org.dyn4j.collision.Collisions
import org.dyn4j.dynamics.contact.ContactListener
import org.dyn4j.dynamics.contact.ContactPoint
import org.dyn4j.dynamics.joint.Joint
import org.dyn4j.geometry.*
import featurea.math.max
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.math.PI
import kotlin.math.abs
import org.dyn4j.collision.AbstractCollidable as AbstractCollidable1

/**
 * Represents a physical [Body].
 *
 *
 * A [Body] typically has at least one [BodyFixture] attached to it.
 * the [BodyFixture]s represent the shape of the body.  When a body
 * is first created the body is a shapeless infinite mass body.  Add fixtures to
 * the body using the `addFixture` methods.
 *
 *
 * Use the [.setMass] methods to calculate the
 * mass of the entire [Body] given the currently attached
 * [BodyFixture]s.  The [.setMass] method can be used to set
 * the mass directly.  Use the [.setMassType]
 * method to toggle the mass type between the special types.
 *
 *
 * The coefficient of friction and restitution and the linear and angular damping
 * are all defaulted but can be changed via the accessor and mutator methods.
 *
 *
 * By default [Body]s are allowed to be put to sleep automatically. [Body]s are
 * put to sleep when they come to rest for a certain amount of time.  Applying any force,
 * torque, or impulse will wake the [Body].
 *
 *
 * A [Body] becomes inactive when the [Body] has left the boundary of
 * the world.
 *
 *
 * A [Body] is dynamic if either its inertia or mass is greater than zero.
 * A [Body] is static if both its inertia and mass are zero.
 *
 *
 * A [Body] flagged as a Bullet will be checked for tunneling depending on the CCD
 * setting in the world's [Settings].  Use this if the body is a fast moving
 * body, but be careful as this will incur a performance hit.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 */
open class Body : AbstractCollidable1<BodyFixture>, Collidable<BodyFixture>, Transformable, DataContainer {

    companion object {
        /** The default linear damping; value = [.DEFAULT_LINEAR_DAMPING]  */
        @JvmField
        val DEFAULT_LINEAR_DAMPING = 0.0

        /** The default angular damping; value = [.DEFAULT_ANGULAR_DAMPING]  */
        @JvmField
        val DEFAULT_ANGULAR_DAMPING = 0.01

        /** The state flag for allowing automatic sleeping  */
        @JvmField
        val AUTO_SLEEP = 1

        /** The state flag for the [Body] being asleep  */
        @JvmField
        val ASLEEP = 2

        /** The state flag for the [Body] being active (out of bounds for example)  */
        @JvmField
        val ACTIVE = 4

        /** The state flag indicating the [Body] has been added to an [Island]  */
        @JvmField
        val ISLAND = 8

        /** The state flag indicating the [Body] is a really fast object and requires CCD  */
        @JvmField
        val BULLET = 16
    }


    /** The [Mass] information  */
    var mass: Mass? = null
        set(value) {
            // make sure the mass is not null
            if (value == null) throw NullPointerException(message("dynamics.body.nullMass"))
            // set the mass
            field = value
            // compute the rotation disc radius
            setRotationDiscRadius()
        }

    /** The current linear velocity  */
    lateinit var velocity: Vector2

    /** The current angular velocity  */
    @JvmField
    var angularVelocity = 0.0

    /** The [Body]'s linear damping  */
    var linearDamping = 0.0
        set(value) {
            if (value < 0) throw IllegalArgumentException(message("dynamics.body.invalidLinearDamping"))
            field = value
        }

    /** The [Body]'s angular damping  */
    var angularDamping = 0.0
        set(value) {
            if (value < 0) throw IllegalArgumentException(message("dynamics.body.invalidAngularDamping"))
            field = value
        }

    /** The per body gravity scale factor  */
    var gravityScale = 0.0

    // internal

    // internal
    /** The beginning transform for CCD  */
    lateinit var transform0: Transform

    /** The [Body]'s state  */
    private var state = 0

    /** The world this body belongs to  */
    @JvmField
    var world: World? = null

    /** The time that the [Body] has been waiting to be put sleep  */
    var sleepTime = 0.0

    // last iteration accumulated force/torque

    // last iteration accumulated force/torque
    /** The current force  */
    @JvmField
    var force: Vector2? = null

    /** The current torque  */
    @JvmField
    var torque = 0.0

    // force/torque accumulators

    // force/torque accumulators
    /** The force accumulator  */
    @JvmField
    var forces: MutableList<Force>

    /** The torque accumulator  */
    @JvmField
    var torques: MutableList<Torque>

    // interaction graph

    // interaction graph
    /** The [Body]'s contacts  */
    lateinit var contacts: MutableList<ContactEdge>

    /** The [Body]'s joints  */
    @JvmField
    var joints: MutableList<JointEdge>

    /**
     * Default constructor.
     */
    constructor() : this(TYPICAL_FIXTURE_COUNT)

    /**
     * Optional constructor.
     *
     *
     * Creates a new [Body] using the given estimated fixture count.
     * Assignment of the initial fixture count allows sizing of internal structures
     * for optimal memory/performance.  This estimated fixture count is **not** a
     * limit on the number of fixtures.
     * @param fixtureCount the estimated number of fixtures
     * @throws IllegalArgumentException if fixtureCount less than zero
     * @since 3.1.1
     */
    constructor(fixtureCount: Int) : super(fixtureCount){
        world = null
        this.radius = 0.0
        mass = Mass()
        transform0 = Transform()
        velocity = Vector2()
        angularVelocity = 0.0
        force = Vector2()
        torque = 0.0
        // its common to apply a force or two to a body during a timestep
        // so 1 is a good trade off
        forces = ArrayList(1)
        torques = ArrayList(1)
        // initialize the state (allow sleeping and start off active)
        state = Body.AUTO_SLEEP or Body.ACTIVE
        sleepTime = 0.0
        linearDamping = Body.DEFAULT_LINEAR_DAMPING
        angularDamping = Body.DEFAULT_ANGULAR_DAMPING
        gravityScale = 1.0
        contacts = ArrayList(Collisions.estimatedCollisionsPerObject)
        // its more common that bodies do not have joints attached
        // then they do, so by default don't allocate anything
        // for the joints list
        joints = ArrayList(0)
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Body[HashCode=").append(this.hashCode()).append("|Fixtures={")
        // append all the shapes
        val size = fixtures.size
        for (i in 0 until size) {
            if (i != 0) sb.append(",")
            sb.append(fixtures[i])
        }
        sb.append("}|InitialTransform=").append(transform0)
            .append("|Transform=").append(transform)
            .append("|RotationDiscRadius=").append(this.radius)
            .append("|Mass=").append(mass)
            .append("|Velocity=").append(velocity)
            .append("|AngularVelocity=").append(angularVelocity)
            .append("|Force=").append(force)
            .append("|Torque=").append(torque)
            .append("|AccumulatedForce=").append(this.getAccumulatedForce())
            .append("|AccumulatedTorque=").append(this.getAccumulatedTorque())
            .append("|IsAutoSleepingEnabled=").append(this.isAutoSleepingEnabled())
            .append("|IsAsleep=").append(this.isAsleep())
            .append("|IsActive=").append(this.isActive())
            .append("|IsBullet=").append(this.isBullet())
            .append("|LinearDamping=").append(linearDamping)
            .append("|AngularDamping").append(angularDamping)
            .append("|GravityScale=").append(gravityScale)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#addFixture(org.dyn4j.geometry.Convex)
	 */
    override fun addFixture(convex: Convex): BodyFixture {
        return this.addFixture(
            convex,
            BodyFixture.DEFAULT_DENSITY,
            BodyFixture.DEFAULT_FRICTION,
            BodyFixture.DEFAULT_RESTITUTION
        )
    }

    /**
     * Creates a [BodyFixture] for the given [Convex] [Shape],
     * adds it to the [Body], and returns it for configuration.
     *
     *
     * After adding or removing fixtures make sure to call the [.updateMass]
     * or [.setMass] method to compute the new total
     * [Mass] for the body.
     *
     *
     * This is a convenience method for setting the density of a [BodyFixture].
     * @param convex the [Convex] [Shape] to add to the [Body]
     * @param density the density of the shape in kg/m<sup>2</sup>; in the range (0.0, ]
     * @return [BodyFixture] the fixture created using the given [Shape] and added to the [Body]
     * @throws NullPointerException if convex is null
     * @throws IllegalArgumentException if density is less than or equal to zero; if friction or restitution is less than zero
     * @see .addFixture
     * @see .addFixture
     * @since 3.1.5
     */
    fun addFixture(convex: Convex, density: Double): BodyFixture {
        return this.addFixture(convex, density, BodyFixture.DEFAULT_FRICTION, BodyFixture.DEFAULT_RESTITUTION)
    }

    /**
     * Creates a [BodyFixture] for the given [Convex] [Shape],
     * adds it to the [Body], and returns it for configuration.
     *
     *
     * After adding or removing fixtures make sure to call the [.updateMass]
     * or [.setMass] method to compute the new total
     * [Mass] for the body.
     *
     *
     * This is a convenience method for setting the properties of a [BodyFixture].
     * Use the [BodyFixture.DEFAULT_DENSITY], [BodyFixture.DEFAULT_FRICTION],
     * and [BodyFixture.DEFAULT_RESTITUTION] values if you need to only set one
     * of these properties.
     * @param convex the [Convex] [Shape] to add to the [Body]
     * @param density the density of the shape in kg/m<sup>2</sup>; in the range (0.0, ]
     * @param friction the coefficient of friction; in the range [0.0, ]
     * @param restitution the coefficient of restitution; in the range [0.0, ]
     * @return [BodyFixture] the fixture created using the given [Shape] and added to the [Body]
     * @throws NullPointerException if convex is null
     * @throws IllegalArgumentException if density is less than or equal to zero; if friction or restitution is less than zero
     * @see .addFixture
     * @see .addFixture
     * @since 3.1.1
     */
    fun addFixture(convex: Convex, density: Double, friction: Double, restitution: Double): BodyFixture {
        // create the fixture
        val fixture = BodyFixture(convex)
        // set the properties
        fixture.density = density
        fixture.friction = friction
        fixture.restitution = restitution
        // add the fixture to the body
        fixtures.add(fixture)
        // add the fixture to the broadphase
        if (world != null) {
            world!!.broadphaseDetector.add(this, fixture)
        }
        // return the fixture so the caller can configure it
        return fixture
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#addFixture(org.dyn4j.collision.Fixture)
	 */
    override fun addFixture(fixture: BodyFixture): Body {
        // add the shape and mass to the respective lists
        fixtures.add(fixture)
        // add the fixture to the broadphase
        if (world != null) {
            world!!.broadphaseDetector.add(this, fixture)
        }
        // return this body to facilitate chaining
        return this
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.AbstractCollidable#removeFixture(org.dyn4j.collision.Fixture)
	 */
    override fun removeFixture(fixture: BodyFixture?): Boolean {
        if (world != null) {
            world!!.broadphaseDetector.remove(this, fixture)
        }
        return super.removeFixture(fixture)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.AbstractCollidable#removeFixture(int)
	 */
    override fun removeFixture(index: Int): BodyFixture {
        val fixture = super.removeFixture(index)
        if (world != null) {
            world!!.broadphaseDetector.remove(this, fixture)
        }
        return fixture
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.AbstractCollidable#removeFixture(org.dyn4j.geometry.Vector2)
	 */
    override fun removeFixture(point: Vector2): BodyFixture? {
        val fixture: BodyFixture? = super.removeFixture(point)
        if (world != null) {
            world!!.broadphaseDetector.remove(this, fixture)
        }
        return fixture
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.AbstractCollidable#removeAllFixtures()
	 */
    override fun removeAllFixtures(): List<BodyFixture> {
        val fixtures = super.removeAllFixtures()
        if (world != null) {
            world!!.broadphaseDetector.remove(this)
        }
        return fixtures
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.AbstractCollidable#removeFixtures(org.dyn4j.geometry.Vector2)
	 */
    override fun removeFixtures(point: Vector2): List<BodyFixture> {
        val fixtures: List<BodyFixture> = super.removeFixtures(point)
        if (world != null) {
            val size = fixtures.size
            for (i in 0 until size) {
                world!!.broadphaseDetector.remove(this, fixtures[i])
            }
        }
        return fixtures
    }

    /**
     * This is a shortcut method for the [.setMass]
     * method that will use the current mass type as the mass type and
     * then recompute the mass from the body's fixtures.
     * @return [Body] this body
     * @since 3.2.0
     * @see .setMass
     */
    fun updateMass(): Body? {
        return this.setMass(mass!!.type)
    }

    /**
     * This method should be called after fixture modification
     * is complete.
     *
     *
     * This method will calculate a total mass for the body
     * given the masses of the attached fixtures.
     *
     *
     * A [org.dyn4j.geometry.MassType] can be used to create special mass
     * types.
     * @param type the mass type
     * @return [Body] this body
     */
    fun setMass(type: MassType?): Body? {
        // check for null
        var type = type
        if (type == null) {
            type = mass!!.type
        }
        // get the size
        val size = fixtures!!.size
        // check the size
        if (size == 0) {
            // set the mass to an infinite point mass at (0, 0)
            mass = Mass()
        } else if (size == 1) {
            // then just use the mass for the first shape
            mass = fixtures[0]!!.createMass()
        } else {
            // create a list of mass objects
            val masses: MutableList<Mass> = ArrayList(size)
            // create a mass object for each shape
            for (i in 0 until size) {
                val mass = fixtures[i]!!.createMass()
                masses.add(mass)
            }
            mass = Mass.create(masses)
        }
        // set the type
        mass!!.type =(type)
        // compute the rotation disc radius
        setRotationDiscRadius()
        // return this body to facilitate chaining
        return this
    }

    /**
     * Sets the [org.dyn4j.geometry.MassType] of this [Body].
     *
     *
     * This method does not compute/recompute the mass of the body but solely
     * sets the mass type to one of the special types.
     *
     *
     * Since its possible to create a [Mass] object with zero mass and/or
     * zero inertia (`Mass m = new Mass(new Vector2(), 0, 0);` for example), setting the type
     * to something other than MassType.INFINITE can have undefined results.
     * @param type the desired type
     * @return [Body] this body
     * @throws NullPointerException if the given mass type is null
     * @since 2.2.3
     */
    fun setMassType(type: MassType?): Body? {
        // check for null type
        if (type == null) throw NullPointerException(message("dynamics.body.nullMassType"))
        // otherwise just set the type
        mass!!.type =(type)
        // return this body
        return this
    }

    /**
     * Computes the rotation disc for this [Body].
     *
     *
     * This method requires that the center of mass be computed first.
     *
     *
     * The rotation disc radius is the radius, from the center of mass,
     * of the disc that encompasses the entire body as if it was rotated
     * 360 degrees.
     * @since 2.0.0
     * @see .getRotationDiscRadius
     */
    protected fun setRotationDiscRadius() {
        var r = 0.0
        // get the number of fixtures
        val size = fixtures.size
        // check for zero fixtures
        if (size == 0) {
            // set the radius to zero
            this.radius = 0.0
            return
        }
        // get the body's center of mass
        val c: Vector2 = mass!!.center!!
        // loop over the fixtures
        for (i in 0 until size) {
            // get the fixture and convex
            val fixture: BodyFixture = fixtures[i]
            val convex: Convex = fixture.shape
            // get the convex's radius using the
            // body's center of mass
            val cr = convex.getRadius(c)
            // keep the maximum
            r = max(r, cr)
        }
        // return the max
        this.radius = r
    }

    /**
     * Applies the given force to this [Body].
     *
     *
     * This method will wake-up the body if its sleeping.
     *
     *
     * This method does not apply the force if this body
     * returns zero from the [Mass.getMass] method.
     *
     *
     * The force is not applied immediately, but instead stored in the
     * force accumulator ([.getAccumulatedForce]).  This is to
     * preserve the last time step's computed force ([.getForce].
     *
     *
     * The force is assumed to be in world space coordinates.
     * @param force the force
     * @return [Body] this body
     * @throws NullPointerException if force is null
     * @since 3.1.1
     */
    fun applyForce(force: Vector2?): Body? {
        // check for null
        if (force == null) throw NullPointerException(message("dynamics.body.nullForce"))
        // check the linear mass of the body
        if (mass!!.mass == 0.0) {
            // this means that applying a force will do nothing
            // so, just return
            return this
        }
        // apply the force
        forces.add(Force(force))
        // wake up the body
        setAsleep(false)
        // return this body to facilitate chaining
        return this
    }

    /**
     * Applies the given [Force] to this [Body].
     *
     *
     * This method will wake-up the body if its sleeping.
     *
     *
     * This method does not apply the force if this body
     * returns zero from the [Mass.getMass] method.
     *
     *
     * The force is not applied immediately, but instead stored in the
     * force accumulator ([.getAccumulatedForce]).  This is to
     * preserve the last time step's computed force ([.getForce].
     *
     *
     * The force is assumed to be in world space coordinates.
     * @param force the force
     * @return [Body] this body
     * @throws NullPointerException if force is null
     * @since 3.1.1
     */
    fun applyForce(force: Force?): Body? {
        // check for null
        if (force == null) throw NullPointerException(message("dynamics.body.nullForce"))
        // check the linear mass of the body
        if (mass!!.mass == 0.0) {
            // this means that applying a force will do nothing
            // so, just return
            return this
        }
        // add the force to the list
        forces.add(force)
        // wake up the body
        setAsleep(false)
        // return this body to facilitate chaining
        return this
    }

    /**
     * Applies the given torque about the center of this [Body].
     *
     *
     * This method will wake-up the body if its sleeping.
     *
     *
     * This method does not apply the torque if this body returns
     * zero from the [Mass.getInertia] method.
     *
     *
     * The torque is not applied immediately, but instead stored in the
     * torque accumulator ([.getAccumulatedTorque]).  This is to
     * preserve the last time step's computed torque ([.getTorque].
     * @param torque the torque about the center
     * @return [Body] this body
     * @since 3.1.1
     */
    fun applyTorque(torque: Double): Body? {
        // check the angular mass of the body
        if (mass!!.inertia == 0.0) {
            // this means that applying a torque will do nothing
            // so, just return
            return this
        }
        // apply the torque
        torques.add(Torque(torque))
        // wake up the body
        setAsleep(false)
        // return this body
        return this
    }

    /**
     * Applies the given [Torque] to this [Body].
     *
     *
     * This method will wake-up the body if its sleeping.
     *
     *
     * This method does not apply the torque if this body returns
     * zero from the [Mass.getInertia] method.
     *
     *
     * The torque is not applied immediately, but instead stored in the
     * torque accumulator ([.getAccumulatedTorque]).  This is to
     * preserve the last time step's computed torque ([.getTorque].
     * @param torque the torque
     * @return [Body] this body
     * @throws NullPointerException if torque is null
     * @since 3.1.1
     */
    fun applyTorque(torque: Torque?): Body? {
        // check for null
        if (torque == null) throw NullPointerException(message("dynamics.body.nullTorque"))
        // check the angular mass of the body
        if (mass!!.inertia == 0.0) {
            // this means that applying a torque will do nothing
            // so, just return
            return this
        }
        // add the torque to the list
        torques.add(torque)
        // wake up the body
        setAsleep(false)
        // return this body to facilitate chaining
        return this
    }

    /**
     * Applies the given force to this [Body] at the
     * given point (torque).
     *
     *
     * This method will wake-up the body if its sleeping.
     *
     *
     * This method does not apply the force if this body
     * returns zero from the [Mass.getMass] method nor
     * will it apply the torque if this body returns
     * zero from the [Mass.getInertia] method.
     *
     *
     * The force/torque is not applied immediately, but instead stored in the
     * force/torque accumulators ([.getAccumulatedForce] and
     * [.getAccumulatedTorque]).  This is to preserve the last time
     * step's computed force ([.getForce] and torque ([.getTorque]).
     *
     *
     * The force and point are assumed to be in world space coordinates.
     * @param force the force
     * @param point the application point in world coordinates
     * @return [Body] this body
     * @throws NullPointerException if force or point is null
     * @since 3.1.1
     */
    fun applyForce(force: Vector2?, point: Vector2?): Body? {
        // check for null
        if (force == null) throw NullPointerException(message("dynamics.body.nullForceForTorque"))
        if (point == null) throw NullPointerException(message("dynamics.body.nullPointForTorque"))
        var awaken = false
        // check the linear mass of the body
        if (mass!!.mass != 0.0) {
            // apply the force
            forces.add(Force(force))
            awaken = true
        }
        // check the angular mass of the body
        if (mass!!.inertia != 0.0) {
            // compute the moment r
            val r: Vector2 = worldCenter.to(point)
            // check for the zero vector
            if (!r.isZero) {
                // find the torque about the given point
                val tao: Double = r.cross(force)
                // apply the torque
                torques.add(Torque(tao))
                awaken = true
            }
        }
        // see if we applied either
        if (awaken) {
            // wake up the body
            setAsleep(false)
        }
        // return this body to facilitate chaining
        return this
    }

    /**
     * Applies a linear impulse to this [Body] at its center of mass.
     *
     *
     * This method will wake-up the body if its sleeping.
     *
     *
     * This method does not apply the impulse if this body's mass
     * returns zero from the [Mass.getInertia] method.
     *
     *
     * **NOTE:** Applying an impulse differs from applying a force and/or torque. Forces
     * and torques are stored in accumulators, but impulses are applied to the
     * velocities of the body immediately.
     *
     *
     * The impulse is assumed to be in world space coordinates.
     * @param impulse the impulse to apply
     * @return [Body] this body
     * @throws NullPointerException if impulse is null
     * @since 3.1.1
     */
    fun applyImpulse(impulse: Vector2?): Body? {
        // check for null
        if (impulse == null) throw NullPointerException(message("dynamics.body.nullImpulse"))
        // get the inverse linear mass
        val invM = mass!!.inverseMass
        // check the linear mass
        if (invM == 0.0) {
            // this means that applying an impulse will do nothing
            // so, just return
            return this
        }
        // apply the impulse immediately
        velocity.add(impulse.x * invM, impulse.y * invM)
        // wake up the body
        setAsleep(false)
        // return this body
        return this
    }

    /**
     * Applies an angular impulse to this [Body] about its center of mass.
     *
     *
     * This method will wake-up the body if its sleeping.
     *
     *
     * This method does not apply the impulse if this body's inertia
     * returns zero from the [Mass.getInertia] method.
     *
     *
     * **NOTE:** Applying an impulse differs from applying a force and/or torque. Forces
     * and torques are stored in accumulators, but impulses are applied to the
     * velocities of the body immediately.
     * @param impulse the impulse to apply
     * @return [Body] this body
     * @since 3.1.1
     */
    fun applyImpulse(impulse: Double): Body? {
        val invI = mass!!.inverseInertia
        // check the angular mass
        if (invI == 0.0) {
            // this means that applying an impulse will do nothing
            // so, just return
            return this
        }
        // apply the impulse immediately
        angularVelocity += invI * impulse
        // wake up the body
        setAsleep(false)
        // return this body
        return this
    }

    /**
     * Applies an impulse to this [Body] at the given point.
     *
     *
     * This method will wake-up the body if its sleeping.
     *
     *
     * This method does not apply the linear impulse if this body
     * returns zero from the [Mass.getMass] method nor
     * will it apply the angular impulse if this body returns
     * zero from the [Mass.getInertia] method.
     *
     *
     * **NOTE:** Applying an impulse differs from applying a force and/or torque. Forces
     * and torques are stored in accumulators, but impulses are applied to the
     * velocities of the body immediately.
     *
     *
     * The impulse and point are assumed to be in world space coordinates.
     * @param impulse the impulse to apply
     * @param point the world space point to apply the impulse
     * @return [Body] this body
     * @throws NullPointerException if impulse or point is null
     * @since 3.1.1
     */
    fun applyImpulse(impulse: Vector2?, point: Vector2?): Body? {
        // check for null
        if (impulse == null) throw NullPointerException(message("dynamics.body.nullImpulse"))
        if (point == null) throw NullPointerException(message("dynamics.body.nullPointForImpulse"))
        var awaken = false
        // get the inverse mass
        val invM = mass!!.inverseMass
        val invI = mass!!.inverseInertia
        // check the linear mass
        if (invM != 0.0) {
            // apply the impulse immediately
            velocity.add(impulse.x * invM, impulse.y * invM)
            awaken = true
        }
        if (invI != 0.0) {
            // apply the impulse immediately
            val r: Vector2 = worldCenter.to(point)
            angularVelocity += invI * r.cross(impulse)
            awaken = true
        }
        if (awaken) {
            // wake up the body
            setAsleep(false)
        }
        // return this body
        return this
    }

    /**
     * Clears the last time step's force on the [Body].
     */
    fun clearForce() {
        force!!.zero()
    }

    /**
     * Clears the forces stored in the force accumulator.
     *
     *
     * Renamed from clearForces (3.0.0 and below).
     * @since 3.0.1
     */
    fun clearAccumulatedForce() {
        forces.clear()
    }

    /**
     * Clears the last time step's torque on the [Body].
     */
    fun clearTorque() {
        torque = 0.0
    }

    /**
     * Clears the torques stored in the torque accumulator.
     *
     *
     * Renamed from clearTorques (3.0.0 and below).
     * @since 3.0.1
     */
    fun clearAccumulatedTorque() {
        torques.clear()
    }

    /**
     * Accumulates the forces and torques.
     * @param elapsedTime the elapsed time since the last call
     * @since 3.1.0
     */
    fun accumulate(elapsedTime: Double) {
        // set the current force to zero
        force!!.zero()
        // get the number of forces
        var size = forces!!.size
        // check if the size is greater than zero
        if (size > 0) {
            // apply all the forces
            val it = forces!!.iterator()
            while (it.hasNext()) {
                val force = it.next()
                this.force!!.add(force.force)
                // see if we should remove the force
                if (force.isComplete(elapsedTime)) {
                    it.remove()
                }
            }
        }
        // set the current torque to zero
        torque = 0.0
        // get the number of torques
        size = torques!!.size
        // check the size
        if (size > 0) {
            // apply all the torques
            val it = torques!!.iterator()
            while (it.hasNext()) {
                val torque = it.next()
                this.torque += torque.torque
                // see if we should remove the torque
                if (torque.isComplete(elapsedTime)) {
                    it.remove()
                }
            }
        }
    }

    /**
     * Returns true if this body has infinite mass and
     * the velocity and angular velocity is zero.
     * @return boolean
     */
    fun isStatic(): Boolean {
//		return this.mass.isInfinite() && this.velocity.isZero && Math.abs(this.angularVelocity) <= Epsilon.E;
        return mass!!.type === MassType.INFINITE && abs(velocity.x) <= Epsilon.E && abs(
            velocity.y
        ) <= Epsilon.E && abs(angularVelocity) <= Epsilon.E
    }

    /**
     * Returns true if this body has infinite mass and
     * the velocity or angular velocity are NOT zero.
     * @return boolean
     */
    fun isKinematic(): Boolean {
//		return this.mass.isInfinite() && (!this.velocity.isZero || Math.abs(this.angularVelocity) > Epsilon.E);
        return mass!!.type === MassType.INFINITE &&
                (abs(velocity.x) > Epsilon.E || abs(velocity.y) > Epsilon.E || abs(
                    angularVelocity
                ) > Epsilon.E)
    }

    /**
     * Returns true if this body does not have infinite mass.
     * @return boolean
     */
    fun isDynamic(): Boolean {
        return mass!!.type !== MassType.INFINITE
    }

    /**
     * Returns whether 'state' has all the bits indicated by the binary mask 'mask' set.
     *
     * @param mask The binary mask to select bits
     * @return boolean
     */
    fun getState(mask: Int): Boolean {
        return state and mask == mask
    }

    /**
     * Sets (if flag == true) or clears (if flag == false) the bits indicated by the binary mask 'mask' in the 'state' field.
     * @param mask the binary mask to select bits
     * @param flag true to set, false to clear bits
     */
    fun setState(mask: Int, flag: Boolean) {
        if (flag) {
            state = state or mask
        } else {
            state = state and mask.inv()
        }
    }

    /**
     * Sets the [Body] to allow or disallow automatic sleeping.
     * @param flag true if the [Body] is allowed to sleep
     * @since 1.2.0
     */
    fun setAutoSleepingEnabled(flag: Boolean) {
        setState(Body.AUTO_SLEEP, flag)
    }

    /**
     * Returns true if this [Body] is allowed to be
     * put to sleep automatically.
     * @return boolean
     * @since 1.2.0
     */
    fun isAutoSleepingEnabled(): Boolean {
        return getState(Body.AUTO_SLEEP)
    }

    /**
     * Returns true if this [Body] is sleeping.
     * @return boolean
     */
    fun isAsleep(): Boolean {
        return getState(Body.ASLEEP)
    }

    /**
     * Sets whether this [Body] is awake or not.
     *
     *
     * If flag is true, this body's velocity, angular velocity,
     * force, torque, and accumulators are cleared.
     * @param flag true if the body should be put to sleep
     */
    fun setAsleep(flag: Boolean) {
        if (flag) {
            setState(Body.ASLEEP, true)
            velocity.zero()
            angularVelocity = 0.0
            forces.clear()
            torques.clear()
        } else {
            // check if the body is asleep
            if (getState(Body.ASLEEP)) {
                // if the body is asleep then wake it up
                sleepTime = 0.0
                setState(Body.ASLEEP, false)
            }
            // otherwise do nothing
        }
    }

    /**
     * Returns true if this [Body] is active.
     * @return boolean
     */
    fun isActive(): Boolean {
        return getState(Body.ACTIVE)
    }

    /**
     * Sets whether this [Body] is active or not.
     * @param flag true if this [Body] should be active
     */
    fun setActive(flag: Boolean) {
        setState(Body.ACTIVE, flag)
    }

    /**
     * Returns true if this [Body] has been added to an [Island].
     * @return boolean true if this [Body] has been added to an [Island]
     */
    fun isOnIsland(): Boolean {
        return getState(Body.ISLAND)
    }

    /**
     * Sets the flag indicating that the [Body] has been added to an [Island].
     * @param flag true if the [Body] has been added to an [Island]
     */
    fun setOnIsland(flag: Boolean) {
        setState(Body.ISLAND, flag)
    }

    /**
     * Returns true if this [Body] is a bullet.
     * @see .setBullet
     * @return boolean
     * @since 1.2.0
     */
    fun isBullet(): Boolean {
        return getState(Body.BULLET)
    }

    /**
     * Sets the bullet flag for this [Body].
     *
     *
     * A bullet is a very fast moving body that requires
     * continuous collision detection with **all** other
     * [Body]s to ensure that no collisions are missed.
     * @param flag true if this [Body] is a bullet
     * @since 1.2.0
     */
    fun setBullet(flag: Boolean) {
        setState(Body.BULLET, flag)
    }

    /**
     * Returns true if the given [Body] is connected
     * to this [Body] by a [Joint].
     *
     *
     * Returns false if the given body is null.
     * @param body the suspect connected body
     * @return boolean
     */
    fun isConnected(body: Body?): Boolean {
        // check for a null body
        if (body == null) return false
        val size = joints!!.size
        // check the size
        if (size == 0) return false
        // loop over all the joints
        for (i in 0 until size) {
            val je: JointEdge = joints!![i]
            // testing object references should be sufficient
            if (je.other === body) {
                // if it is then return true
                return true
            }
        }
        // not found, so return false
        return false
    }

    /**
     * Returns true if the given [Body] is connected to this
     * [Body], given the collision flag, via a [Joint].
     *
     *
     * If the given collision flag is true, this method will return true
     * only if collision is allowed between the two joined [Body]s.
     *
     *
     * If the given collision flag is false, this method will return true
     * only if collision is **NOT** allowed between the two joined [Body]s.
     *
     *
     * If the [Body]s are connected by more than one joint, if any allows
     * collision, then the bodies are considered connected AND allowing collision.
     *
     *
     * Returns false if the given body is null.
     * @param body the suspect connected body
     * @param collisionAllowed the collision allowed flag
     * @return boolean
     */
    fun isConnected(body: Body?, collisionAllowed: Boolean): Boolean {
        // check for a null body
        if (body == null) return false
        val size = joints!!.size
        // check the size
        if (size == 0) return false
        // loop over all the joints
        var allowed = false
        var connected = false
        for (i in 0 until size) {
            val je: JointEdge = joints!![i]
            // testing object references should be sufficient
            if (je.other === body) {
                // get the joint
                val joint: Joint = je.interaction!!
                // set that they are connected
                connected = true
                // check if collision is allowed
                // we do an or here to find if there is at least one
                // joint joining the two bodies that allows collision
                allowed = allowed or joint.isCollisionAllowed
            }
        }
        // if they are not connected at all we can ignore the collision
        // allowed flag passed in and return false
        if (!connected) return false
        // if at least one joint between the two bodies allow collision
        // then the allowed variable will be true, check this against
        // the desired flag passed in
        return if (allowed == collisionAllowed) {
            true
        } else false
        // not found, so return false
    }

    /**
     * Returns true if the given [Body] is in collision with this [Body].
     *
     *
     * Returns false if the given body is null.
     * @param body the [Body] to test
     * @return boolean true if the given [Body] is in collision with this [Body]
     * @since 1.2.0
     */
    fun isInContact(body: Body?): Boolean {
        // check for a null body
        if (body == null) return false
        // get the number of contacts
        val size = contacts.size
        // check for zero contacts
        if (size == 0) return false
        // loop over the contacts
        for (i in 0 until size) {
            val ce = contacts[i]
            // is the other body equal to the given body?
            if (ce.other == body) {
                // if so then return true
                return true
            }
        }
        // if we get here then we know no contact exists
        return false
    }

    /**
     * Returns the transform of the last iteration.
     *
     *
     * This transform represents the last frame's position and
     * orientation.
     * @return [Transform]
     */
    fun getInitialTransform(): Transform {
        return transform0
    }

    /**
     * Returns an AABB that contains the maximal space in which
     * the [Collidable] exists from the initial transform
     * to the final transform.
     *
     *
     * This method takes the bounding circle, using the world center
     * and rotation disc radius, at the initial and final transforms
     * and creates an AABB containing both.
     * @return [AABB]
     * @since 3.1.1
     */
    fun createSweptAABB(): AABB? {
        return this.createSweptAABB(transform0, transform)
    }

    /**
     * Creates a swept [AABB] from the given start and end [Transform]s
     * for this [Body].
     *
     *
     * This method may return a degenerate AABB, where the min == max, if the body
     * has not moved and does not have any fixtures.  If this body does have
     * fixtures, but didn't move, an AABB encompassing the initial and final center
     * points is returned.
     * @param initialTransform the initial [Transform]
     * @param finalTransform the final [Transform]
     * @return [AABB]
     * @since 3.1.1
     */
    fun createSweptAABB(initialTransform: Transform, finalTransform: Transform): AABB? {
        // get the initial transform's world center
        val iCenter: Vector2 = initialTransform.getTransformed(mass!!.center!!)
        // get the final transform's world center
        val fCenter: Vector2 = finalTransform.getTransformed(mass!!.center!!)
        // return an AABB containing both points (expanded into circles by the
        // rotation disc radius)
        val sweptAABB = AABB.createAABBFromPoints(iCenter, fCenter)
        sweptAABB.expand(this.radius * 2)
        return sweptAABB
    }

    /**
     * Returns the change in position computed from last frame's transform
     * and this frame's transform.
     * @return Vector2
     * @since 3.1.5
     */
    fun getChangeInPosition(): Vector2? {
        return transform!!.translation.subtract(transform0.translation)
    }

    /**
     * Returns the change in orientation computed from last frame's transform
     * and this frame's transform.
     *
     *
     * This method will return a change in the range [0, 2).  This isn't as useful
     * if the angular velocity is greater than 2 per time step.  Since we don't have
     * the timestep here, we can't compute the exact change in this case.
     * @return double
     * @since 3.1.5
     */
    fun getChangeInOrientation(): Double {
        var ri: Double = transform0.rotationAngle
        var rf = transform!!.rotationAngle
        val twopi: Double = 2.0 * PI

        // put the angles in the range [0, 2pi] rather than [-pi, pi]
        if (ri < 0) ri += twopi
        if (rf < 0) rf += twopi

        // compute the difference
        var r = rf - ri

        // determine which way the angular velocity was going so that
        // we know which angle is correct
        // check if the end is smaller than the start and for a positive velocity
        if (rf < ri && angularVelocity > 0) r += twopi
        // check if the end is larger than the start and for a negative velocity
        if (rf > ri && angularVelocity < 0) r -= twopi

        // return the rotation
        return r
    }

    override val localCenter: Vector2
        get() = mass!!.center!!

    /**
     * Returns the center of mass for the body in world coordinates.
     * @return [Vector2] the center of mass in world coordinates
     */

    override val worldCenter: Vector2
        get() = transform!!.getTransformed(mass!!.center!!)

    /**
     * Returns the linear velocity.
     * @return [Vector2]
     * @since 3.1.5
     */
    fun getLinearVelocity(): Vector2 {
        return velocity!!
    }

    /**
     * Returns the velocity of this body at the given world space point.
     * @param point the point in world space
     * @return [Vector2]
     * @since 3.1.5
     */
    fun getLinearVelocity(point: Vector2): Vector2? {
        // get the world space center point
        val c: Vector2 = worldCenter
        // compute the r vector from the center of mass to the point
        val r: Vector2 = c.to(point)
        // compute the velocity
        return r.cross(angularVelocity).add(velocity)
    }

    /**
     * Sets the linear velocity.
     *
     *
     * Call the [.setAsleep] method to wake up the [Body]
     * if the [Body] is asleep and the velocity is not zero.
     * @param velocity the desired velocity
     * @throws NullPointerException if velocity is null
     * @since 3.1.5
     */
    fun setLinearVelocity(velocity: Vector2?) {
        if (velocity == null) throw NullPointerException(message("dynamics.body.nullVelocity"))
        this.velocity.set(velocity)
    }

    /**
     * Sets the linear velocity.
     *
     *
     * Call the [.setAsleep] method to wake up the [Body]
     * if the [Body] is asleep and the velocity is not zero.
     * @param x the linear velocity along the x-axis
     * @param y the linear velocity along the y-axis
     * @since 3.1.5
     */
    fun setLinearVelocity(x: Double, y: Double) {
        velocity.x = x
        velocity.y = y
    }

    /**
     * Returns the force applied in the last iteration.
     *
     *
     * This is the accumulated force from the last iteration.
     * @return [Vector2]
     */
    fun copyForce(): Vector2? {
        return force!!.copy()
    }

    /**
     * Returns the total force currently stored in the force accumulator.
     * @return [Vector2]
     * @since 3.0.1
     */
    fun getAccumulatedForce(): Vector2? {
        val fSize = forces.size
        val force = Vector2()
        for (i in 0 until fSize) {
            val tf: Vector2 = forces[i].force
            force.add(tf)
        }
        return force
    }

    /**
     * Returns the total torque currently stored in the torque accumulator.
     * @return double
     * @since 3.0.1
     */
    fun getAccumulatedTorque(): Double {
        val tSize = torques.size
        var torque = 0.0
        for (i in 0 until tSize) {
            torque += torques[i].torque
        }
        return torque
    }

    /**
     * Returns a list of [Body]s connected
     * by [Joint]s.
     *
     *
     * If a body is connected to another body with more
     * than one joint, this method will return just one
     * entry for the connected body.
     * @return List&lt;[Body]&gt;
     * @since 1.0.1
     */
    fun getJoinedBodies(): List<Body>? {
        val size = joints!!.size
        // create a list of the correct capacity
        val bodies: MutableList<Body> =
            ArrayList(size)
        // add all the joint bodies
        for (i in 0 until size) {
            val je: JointEdge = joints!![i]
            // get the other body
            val other: Body = je.other
            // make sure that the body hasn't been added
            // to the list already
            if (!bodies.contains(other)) {
                bodies.add(other)
            }
        }
        // return the connected bodies
        return bodies
    }

    /**
     * Returns a list of [Joint]s that this
     * [Body] is connected with.
     * @return List&lt;[Joint]&gt;
     * @since 1.0.1
     */
    fun getJoints(): List<Joint>? {
        val size = joints!!.size
        // create a list of the correct capacity
        val joints: MutableList<Joint> = ArrayList(size)
        // add all the joints
        for (i in 0 until size) {
            val je: JointEdge = this.joints!![i]
            joints.add(je.interaction!!)
        }
        // return the connected joints
        return joints
    }

    /**
     * Returns a list of [Body]s that are in
     * contact with this [Body].
     *
     *
     * Passing a value of true results in a list containing only
     * the sensed contacts for this body.  Passing a value of false
     * results in a list containing only normal contacts.
     *
     *
     * Calling this method from any of the [CollisionListener] methods
     * may produce incorrect results.
     *
     *
     * If this body has multiple contact constraints with another body (which can
     * happen when either body has multiple fixtures), this method will only return
     * one entry for the in contact body.
     * @param sensed true for only sensed contacts; false for only normal contacts
     * @return List&lt;[Body]&gt;
     * @since 1.0.1
     */
    fun getInContactBodies(sensed: Boolean): List<Body>? {
        val size = contacts!!.size
        // create a list of the correct capacity
        val bodies: MutableList<Body> =
            ArrayList(size)
        // add all the contact bodies
        for (i in 0 until size) {
            val ce = contacts!![i]
            // check for sensor contact
            val constraint = ce.interaction
            if (sensed == constraint!!.isSensor) {
                // get the other body
                val other = ce.other
                // make sure the body hasn't been added to
                // the list already
                if (!bodies.contains(other)) {
                    // add it to the list
                    bodies.add(other)
                }
            }
        }
        // return the connected bodies
        return bodies
    }

    /**
     * Returns a list of [ContactPoint]s
     *
     *
     * Passing a value of true results in a list containing only
     * the sensed contacts for this body.  Passing a value of false
     * results in a list containing only normal contacts.
     *
     *
     * Calling this method from any of the [CollisionListener] methods
     * may produce incorrect results.
     *
     *
     * Modifying the [ContactPoint]s returned is not advised.  Use the
     * [ContactListener] methods instead.
     * @param sensed true for only sensed contacts; false for only normal contacts
     * @return List&lt;[ContactPoint]&gt;
     * @since 1.0.1
     */
    fun getContacts(sensed: Boolean): List<ContactPoint>? {
        val size = contacts!!.size
        // create a list to store the contacts (worst case initial capacity)
        val contactPoints: MutableList<ContactPoint> = ArrayList(size * 2)
        // add all the contact points
        for (i in 0 until size) {
            val ce = contacts!![i]
            // check for sensor contact
            val constraint = ce.interaction
            if (sensed == constraint!!.isSensor) {
                // loop over the contacts
                val contacts = constraint.contacts
                val csize = contacts!!.size
                for (j in 0 until csize) {
                    // get the contact
                    val contact = contacts[j]!!
                    // create the contact point
                    val contactPoint = ContactPoint(constraint, contact)
                    // add the point
                    contactPoints.add(contactPoint)
                }
            }
        }
        // return the connected bodies
        return contactPoints
    }

}