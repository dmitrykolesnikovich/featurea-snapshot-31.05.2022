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
import org.dyn4j.geometry.*
import org.dyn4j.resources.message

/**
 * Implementation of a friction joint.
 *
 *
 * A friction joint is a constraint that drives both linear and angular
 * velocities to zero.
 *
 *
 * This joint is typically used with one dynamic and one static body.  In this
 * context, the joint will apply linear and angular friction to stop the body's motion.
 *
 *
 * Setting the maximum force and torque values will determine the rate at which the motion
 * is stopped.  These values are defaulted to 10 and 0.25 respectively.
 *
 *
 * NOTE: In versions 3.4.0 and below, the maximum force and torque values were 0 by default.
 * This was changed in 3.4.1 to allow users to better understand the use of this joint
 * when first using it.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 * @see [Documentation](http://www.dyn4j.org/documentation/joints/.Friction_Joint)
 */
class FrictionJoint(body1: Body, body2: Body, anchor: Vector2?) : Joint(body1, body2), Shiftable, DataContainer {

    /** The local anchor point on the first [Body]  */
    var localAnchor1: Vector2

    /** The local anchor point on the second [Body]  */
    var localAnchor2: Vector2

    /** The maximum force the constraint can apply  */
    var maximumForce: Double = 0.0
        set(value) {
            if (value < 0.0) throw IllegalArgumentException(message("dynamics.joint.friction.invalidMaximumForce"))
            field = value
        }

    /** The maximum torque the constraint can apply  */
    var maximumTorque: Double = 0.0
        set(value) {
            if (value < 0.0) throw IllegalArgumentException(message("dynamics.joint.friction.invalidMaximumTorque"))
            field = value
        }
    // current state
    /** The pivot mass; K = J * Minv * Jtrans  */
    private val K: Matrix22

    /** The mass for the angular constraint  */
    private var angularMass = 0.0
    // output
    /** The impulse applied to reduce linear motion  */
    private val linearImpulse: Vector2

    /** The impulse applied to reduce angular motion  */
    private var angularImpulse: Double

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("FrictionJoint[").append(super.toString())
            .append("|Anchor=").append(anchor1)
            .append("|MaximumForce=").append(maximumForce)
            .append("|MaximumTorque=").append(maximumTorque)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#initializeConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun initializeConstraints(step: Step, settings: Settings) {
        val t1: Transform = this.body1.transform
        val t2: Transform = this.body2.transform
        val m1: Mass = this.body1.mass!!
        val m2: Mass = this.body2.mass!!
        val invM1: Double = m1.inverseMass
        val invM2: Double = m2.inverseMass
        val invI1: Double = m1.inverseInertia
        val invI2: Double = m2.inverseInertia
        val r1: Vector2 = t1.getTransformedR(this.body1.localCenter.to(localAnchor1))
        val r2: Vector2 = t2.getTransformedR(this.body2.localCenter.to(localAnchor2))

        // compute the K inverse matrix
        K.m00 = invM1 + invM2 + r1.y * r1.y * invI1 + r2.y * r2.y * invI2
        K.m01 = -invI1 * r1.x * r1.y - invI2 * r2.x * r2.y
        K.m10 = K.m01
        K.m11 = invM1 + invM2 + r1.x * r1.x * invI1 + r2.x * r2.x * invI2

        // compute the angular mass
        angularMass = invI1 + invI2
        if (angularMass > Epsilon.E) {
            angularMass = 1.0 / angularMass
        }

        // account for variable time step
        linearImpulse.multiply(step.deltaTimeRatio)
        angularImpulse *= step.deltaTimeRatio

        // warm start
        this.body1.getLinearVelocity().add(linearImpulse.product(invM1))
        this.body1.angularVelocity = (this.body1.angularVelocity + invI1 * (r1.cross(linearImpulse) + angularImpulse))
        this.body2.getLinearVelocity().subtract(linearImpulse.product(invM2))
        this.body2.angularVelocity = (this.body2.angularVelocity - invI2 * (r2.cross(linearImpulse) + angularImpulse))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solveVelocityConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solveVelocityConstraints(step: Step, settings: Settings) {
        val t1: Transform = this.body1.transform
        val t2: Transform = this.body2.transform
        val m1: Mass = this.body1.mass!!
        val m2: Mass = this.body2.mass!!
        val invM1: Double = m1.inverseMass
        val invM2: Double = m2.inverseMass
        val invI1: Double = m1.inverseInertia
        val invI2: Double = m2.inverseInertia

        // solve the angular constraint
        run {

            // get the relative velocity - the target motor speed
            val C: Double = this.body1.angularVelocity - this.body2.angularVelocity
            // get the impulse required to obtain the speed
            var impulse = this.angularMass * -C
            // clamp the impulse between the maximum torque
            val oldImpulse = this.angularImpulse
            val maxImpulse: Double = this.maximumTorque * step.deltaTime
            this.angularImpulse = Interval.clamp(this.angularImpulse + impulse, -maxImpulse, maxImpulse)
            // get the impulse we need to apply to the bodies
            impulse = this.angularImpulse - oldImpulse

            // apply the impulse
            this.body1.angularVelocity = (this.body1.angularVelocity + invI1 * impulse)
            this.body2.angularVelocity = (this.body2.angularVelocity - invI2 * impulse)
        }

        // solve the point-to-point constraint
        val r1: Vector2 = t1.getTransformedR(this.body1.localCenter.to(localAnchor1))
        val r2: Vector2 = t2.getTransformedR(this.body2.localCenter.to(localAnchor2))
        val v1: Vector2 = this.body1.getLinearVelocity().sum(r1.cross(this.body1.angularVelocity))
        val v2: Vector2 = this.body2.getLinearVelocity().sum(r2.cross(this.body2.angularVelocity))
        val pivotV: Vector2 = v1.subtract(v2)
        var impulse: Vector2 = K.solve(pivotV.negate())

        // clamp by the maxforce
        val oldImpulse: Vector2 = linearImpulse.copy()
        linearImpulse.add(impulse)
        val maxImpulse: Double = maximumForce * step.deltaTime
        if (linearImpulse.magnitudeSquared > maxImpulse * maxImpulse) {
            linearImpulse.normalize()
            linearImpulse.multiply(maxImpulse)
        }
        impulse = linearImpulse.difference(oldImpulse)
        this.body1.getLinearVelocity().add(impulse.product(invM1))
        this.body1.angularVelocity = (this.body1.angularVelocity + invI1 * r1.cross(impulse))
        this.body2.getLinearVelocity().subtract(impulse.product(invM2))
        this.body2.angularVelocity = (this.body2.angularVelocity - invI2 * r2.cross(impulse))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solvePositionConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solvePositionConstraints(step: Step, settings: Settings): Boolean {
        // nothing to do here for this joint
        return true
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getAnchor1()
	 */
    override val anchor1: Vector2
        get() = this.body1.getWorldPoint(localAnchor1)

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getAnchor2()
	 */
    override val anchor2: Vector2
        get() = this.body2.getWorldPoint(localAnchor2)

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionForce(double)
	 */
    override fun getReactionForce(invdt: Double): Vector2 {
        return linearImpulse.product(invdt)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionTorque(double)
	 */
    override fun getReactionTorque(invdt: Double): Double {
        return angularImpulse * invdt
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shiftable#shift(org.dyn4j.geometry.Vector2)
	 */
    override fun shift(shift: Vector2) {
        // nothing to translate here since the anchor points are in local coordinates
        // they will move with the bodies
    }

    /**
     * Minimal constructor.
     * @param body1 the first [Body]
     * @param body2 the second [Body]
     * @param anchor the anchor point in world coordinates
     * @throws NullPointerException if body1, body2, or anchor is null
     * @throws IllegalArgumentException if body1 == body2
     */
    init {
        // default no collision allowed
        // verify the bodies are not the same instance
        if (body1 === body2) throw IllegalArgumentException(message("dynamics.joint.sameBody"))
        // verify the anchor point is non null
        if (anchor == null) throw NullPointerException(message("dynamics.joint.nullAnchor"))
        // put the anchor in local space
        localAnchor1 = body1.getLocalPoint(anchor)
        localAnchor2 = body2.getLocalPoint(anchor)
        // initialize
        K = Matrix22()
        linearImpulse = Vector2()
        angularImpulse = 0.0

        // the maximum force in Newtons
        maximumForce = 10.0
        // the maximum torque in Newton-Meters
        maximumTorque = 0.25
    }
}