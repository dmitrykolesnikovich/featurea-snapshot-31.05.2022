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
package org.dyn4j.dynamics.joint

import org.dyn4j.DataContainer
import org.dyn4j.Epsilon
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.Settings
import org.dyn4j.dynamics.Step
import org.dyn4j.geometry.Geometry
import org.dyn4j.geometry.Matrix22
import org.dyn4j.geometry.Shiftable
import org.dyn4j.geometry.Vector2
import org.dyn4j.resources.Messages.getString

/**
 * Implementation of a pin joint.
 *
 *
 * A pin joint is a joint that pins a body to a specified world space point
 * using a spring-damper system.  This joint will attempt to place the given
 * anchor point at the target position.
 *
 *
 * NOTE: The anchor point does not have to be within the bounds of the body.
 *
 *
 * By default the target position will be the given world space anchor. Use
 * the [.setTarget] method to set a different target.
 *
 *
 * The pin joint requires the spring-damper system to function properly and
 * as such the frequency value must be greater than zero.  Use a
 * [RevoluteJoint] instead if a spring-damper system is not desired.
 * A good starting point is a frequency of 8.0 and damping ratio of 0.3
 * then adjust as necessary.
 *
 *
 * The [.getAnchor1] method returns the target and the
 * [.getAnchor2] method returns the world space anchor point.
 *
 *
 * Both the [.getBody1] and [.getBody2] methods return the same
 * body.
 *
 *
 * Renamed from MouseJoint in 3.2.0.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 * @see [Documentation](http://www.dyn4j.org/documentation/joints/.Pin_Joint)
 */
class PinJoint(
    body: Body,
    anchor: Vector2,
    frequency: Double,
    dampingRatio: Double,
    maximumForce: Double
) : Joint(body, body), Shiftable, DataContainer {
    /**
     * {@inheritDoc}
     *
     *
     * Returns the target point in world space.
     */
    /** The world space target point  */
    override lateinit var anchor1: Vector2
        protected set

    /** The local anchor point for the body  */
    var anchor: Vector2

    /** The oscillation frequency in hz  */
    var frequency: Double = 0.0
        set(value) {
            if (value <= 0) throw IllegalArgumentException(getString("dynamics.joint.invalidFrequencyZero"))
            field = value
        }

    /** The damping ratio  */
    var dampingRatio: Double = 0.0
        set(value) {
            // make sure its within range
            if (value < 0.0 || value > 1.0) throw IllegalArgumentException(getString("dynamics.joint.invalidDampingRatio"))
            // set the new value
            field = value
        }

    /** The maximum force this constraint can apply  */
    var maximumForce: Double = 0.0
        set(value) {
            if (value < 0.0) throw IllegalArgumentException(getString("dynamics.joint.pin.invalidMaximumForce"))
            field = value
        }
    // current state
    /** The constraint mass; K = J * Minv * Jtrans  */
    private val K: Matrix22

    /** The bias for adding work to the constraint (simulating a spring)  */
    private var bias: Vector2? = null

    /** The damping portion of the constraint  */
    private var gamma = 0.0
    // output
    /** The impulse applied to the body to satisfy the constraint  */
    private val impulse: Vector2

    override var isCollisionAllowed: Boolean
        get() = false
        set(value) {
            // always false
        }

    /* (non-Javadoc)
         * @see org.dyn4j.dynamics.joint.Joint#toString()
         */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("PinJoint[").append(super.toString())
            .append("|Target=").append(anchor1)
            .append("|Anchor=").append(anchor)
            .append("|Frequency=").append(frequency)
            .append("|DampingRatio=").append(dampingRatio)
            .append("|MaximumForce=").append(maximumForce)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#initializeConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun initializeConstraints(step: Step, settings: Settings) {
        val body = body2
        val transform = body!!.transform
        val mass = body2!!.mass
        var m = mass!!.mass
        val invM = mass.inverseMass
        val invI = mass.inverseInertia

        // check if the mass is zero
        if (m <= Epsilon.E) {
            // if the mass is zero, use the inertia
            // this will allow the pin joint to work with
            // all mass types other than INFINITE
            m = mass.inertia
        }

        // compute the natural frequency; f = w / (2 * pi) -> w = 2 * pi * f
        val w = Geometry.TWO_PI * frequency
        // compute the damping coefficient; dRatio = d / (2 * m * w) -> d = 2 * m * w * dRatio
        val d = 2.0 * m * dampingRatio * w
        // compute the spring constant; w = sqrt(k / m) -> k = m * w * w
        val k = m * w * w

        // get the delta time
        val dt = step!!.deltaTime
        // compute gamma = CMF = 1 / (hk + d)
        gamma = dt * (d + dt * k)
        // check for zero before inverting
        if (gamma > Epsilon.E) {
            gamma = 1.0 / gamma
        }

        // compute the r vector
        val r = transform.getTransformedR(body.localCenter.to(anchor))

        // compute the bias = ERP where ERP = hk / (hk + d)
        bias = body.worldCenter.add(r).difference(anchor1)
        bias!!.multiply(dt * k * gamma)

        // compute the K inverse matrix
        K.m00 = invM + r.y * r.y * invI
        K.m01 = -invI * r.x * r.y
        K.m10 = K.m01
        K.m11 = invM + r.x * r.x * invI

        // apply the spring
        K.m00 += gamma
        K.m11 += gamma

        // warm start
        impulse.multiply(step.deltaTimeRatio)
        body.getLinearVelocity()!!.add(impulse.product(invM))
        body.angularVelocity = (body.angularVelocity + invI * r.cross(impulse))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solveVelocityConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solveVelocityConstraints(step: Step, settings: Settings) {
        val body = body2
        val transform = body!!.transform
        val mass = body2!!.mass
        val invM = mass!!.inverseMass
        val invI = mass.inverseInertia

        // compute r
        val r = transform.getTransformedR(body.localCenter.to(anchor))

        // Cdot = v + cross(w, r)
        val C = r.cross(body.angularVelocity).add(body.getLinearVelocity()!!)
        // compute Jv + b
        C.add(bias!!)
        C.add(impulse.product(gamma))
        C.negate()
        var J = K.solve(C)

        // clamp using the maximum force
        val oldImpulse = impulse.copy()
        impulse.add(J)
        val maxImpulse = step!!.deltaTime * maximumForce
        if (impulse.magnitudeSquared > maxImpulse * maxImpulse) {
            impulse.normalize()
            impulse.multiply(maxImpulse)
        }
        J = impulse.difference(oldImpulse)
        body.getLinearVelocity()!!.add(J.product(invM))
        body.angularVelocity = (body.angularVelocity + invI * r.cross(J))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solvePositionConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solvePositionConstraints(step: Step, settings: Settings): Boolean {
        // nothing to do here for this joint
        return true
    }

    /**
     * {@inheritDoc}
     *
     *
     * Returns the anchor point on the body in world space.
     */
    override val anchor2: Vector2 get() = body2.getWorldPoint(anchor)

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionForce(double)
	 */
    override fun getReactionForce(invdt: Double): Vector2? {
        return impulse.product(invdt)
    }

    /**
     * {@inheritDoc}
     *
     *
     * Not applicable to this joint.
     * Always returns zero.
     */
    override fun getReactionTorque(invdt: Double): Double = 0.0


    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shiftable#shift(org.dyn4j.geometry.Vector2)
	 */
    override fun shift(shift: Vector2) {
        // the target point must be moved
        anchor1.add(shift)
    }

    /**
     * Returns the target point in world coordinates.
     * @param target the target point
     * @throws NullPointerException if target is null
     */
    fun setTarget(target: Vector2?) {
        // make sure the target is non null
        if (target == null) throw NullPointerException(getString("dynamics.joint.pin.nullTarget"))
        // only wake the body if the target has changed
        if (!target.equals(anchor1)) {
            // wake up the body
            body2.setAsleep(false)
            // set the new target
            anchor1 = target
        }
    }

    /**
     * Returns the target point in world coordinates
     * @return [Vector2]
     */
    fun getTarget(): Vector2 = anchor1

    /**
     * Full constructor.
     * @param body the body to attach the joint to
     * @param anchor the anchor point on the body
     * @param frequency the oscillation frequency in hz
     * @param dampingRatio the damping ratio
     * @param maximumForce the maximum force this constraint can apply in newtons
     * @throws NullPointerException if body or anchor is null
     * @throws IllegalArgumentException if frequency is less than or equal to zero, or if dampingRatio is less than zero or greater than one, or if maxForce is less than zero
     */
    init {
        // verify the frequency
        if (frequency <= 0) throw IllegalArgumentException(getString("dynamics.joint.invalidFrequencyZero"))
        // verify the damping ratio
        if (dampingRatio < 0 || dampingRatio > 1) throw IllegalArgumentException(getString("dynamics.joint.invalidDampingRatio"))
        // verity the max force
        if (maximumForce < 0.0) throw IllegalArgumentException(getString("dynamics.joint.pin.invalidMaximumForce"))
        anchor1 = anchor
        this.anchor = body.getLocalPoint(anchor)
        this.frequency = frequency
        this.dampingRatio = dampingRatio
        this.maximumForce = maximumForce

        // initialize
        K = Matrix22()
        impulse = Vector2()
    }

}