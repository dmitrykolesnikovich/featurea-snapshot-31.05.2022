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
import org.dyn4j.resources.Messages.getString
import kotlin.math.PI

/**
 * Implementation a motor joint.
 *
 *
 * A motor joint uses a motor to apply forces and torques to move the joined
 * bodies together.
 *
 *
 * The motor is limited by a maximum force and torque.  By default these are
 * zero and will need to be set before the joint will function properly.
 * Larger values will allow the motor to apply more force and torque to the
 * bodies.  This can have two effects.  The first is that the bodies will
 * move to their correct positions faster.  The second is that the bodies
 * will be moving faster and may overshoot more causing more oscillation.
 * Use the [.setCorrectionFactor] method to help reduce the
 * oscillation.
 *
 *
 * The linear and angular targets are the target distance and angle that the
 * bodies should achieve relative to each other's position and rotation.  By
 * default, the linear target will be the distance between the two body centers
 * and the angular target will be the relative rotation of the bodies.  Use the
 * [.setLinearTarget] and [.setAngularTarget]
 * methods to set the desired relative translation and rotate between the
 * bodies.
 *
 *
 * This joint is ideal for character movement as it allows direct control of
 * the motion using targets, but yet still allows interaction with the
 * environment.  The best way to achieve this effect is to have the second body
 * be an infinite mass body that doesn't collide with anything.  Then, simply
 * set the current position and rotation of the infinite mass body.  The
 * character body will move and rotate smoothly, participating in any collision
 * or with other joints to match the infinite mass body.
 * @author William Bittle
 * @version 3.2.1
 * @since 3.1.0
 * @see [Documentation](http://www.dyn4j.org/documentation/joints/.Motor_Joint)
 */
class MotorJoint(body1: Body, body2: Body) : Joint(body1, body2), Shiftable, DataContainer {

    /** The linear target distance from body1's world space center  */
    var linearTarget: Vector2? = null
        set(value) {
            if (!value!!.equals(field)) {
                body1.setAsleep(false)
                body2.setAsleep(false)
                field = value
            }
        }

    /** The target angle between the two body's angles  */
    var angularTarget: Double = 0.0
        set(value) {
            if (value != field) {
                body1.setAsleep(false)
                body2!!.setAsleep(false)
                field = value
            }
        }

    /** The correction factor in the range [0, 1]  */
    var correctionFactor: Double = 0.0
        set(value) {
            if (value < 0.0 || value > 1.0) throw IllegalArgumentException(getString("dynamics.joint.motor.invalidCorrectionFactor"))
            field = value
        }

    /** The maximum force the constraint can apply  */
    var maximumForce = 0.0
        set(value) {
            if (value < 0.0) throw IllegalArgumentException(getString("dynamics.joint.friction.invalidMaximumForce"))
            field = value
        }

    /** The maximum torque the constraint can apply  */
    var maximumTorque = 0.0
        set(value) {
            if (value < 0.0) throw IllegalArgumentException(getString("dynamics.joint.friction.invalidMaximumTorque"))
            field = value
        }
    // current state
    /** The pivot mass; K = J * Minv * Jtrans  */
    private val K: Matrix22

    /** The mass for the angular constraint  */
    private var angularMass = 0.0

    /** The calculated linear error in the target distance  */
    private var linearError: Vector2? = null

    /** The calculated angular error in the target angle  */
    private var angularError = 0.0
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
        sb.append("MotorJoint[").append(super.toString())
            .append("|LinearTarget=").append(linearTarget)
            .append("|AngularTarget=").append(angularTarget)
            .append("|CorrectionFactor=").append(correctionFactor)
            .append("|MaximumForce=").append(maximumForce)
            .append("|MaximumTorque=").append(maximumTorque)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#initializeConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun initializeConstraints(step: Step, settings: Settings) {
        val t1 = body1.transform
        val t2 = body2!!.transform
        val m1 = body1.mass
        val m2 = body2.mass
        val invM1 = m1!!.inverseMass
        val invM2 = m2!!.inverseMass
        val invI1 = m1.inverseInertia
        val invI2 = m2.inverseInertia
        val r1 =
            t1.getTransformedR(linearTarget!!.difference(body1.localCenter))
        val r2 = t2.getTransformedR(body2.localCenter.negative)

        // compute the K inverse matrix
        K.m00 = invM1 + invM2 + r1.y * r1.y * invI1 + r2.y * r2.y * invI2
        K.m01 = -invI1 * r1.x * r1.y - invI2 * r2.x * r2.y
        K.m10 = K.m01
        K.m11 = invM1 + invM2 + r1.x * r1.x * invI1 + r2.x * r2.x * invI2
        K.invert()

        // compute the angular mass
        angularMass = invI1 + invI2
        if (angularMass > Epsilon.E) {
            angularMass = 1.0 / angularMass
        }

        // compute the error in the linear and angular targets
        val d1 = r1.sum(body1.worldCenter)
        val d2 = r2.sum(body2.worldCenter)
        linearError = d2.subtract(d1)
        angularError = getAngularError()

        // account for variable time step
        linearImpulse.multiply(step!!.deltaTimeRatio)
        angularImpulse *= step.deltaTimeRatio

        // warm start
        body1.getLinearVelocity()!!.subtract(linearImpulse.product(invM1))
        body1.angularVelocity = (body1.angularVelocity - invI1 * (r1.cross(linearImpulse) + angularImpulse))
        body2.getLinearVelocity()!!.add(linearImpulse.product(invM2))
        body2.angularVelocity = (body2.angularVelocity + invI2 * (r2.cross(linearImpulse) + angularImpulse))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solveVelocityConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solveVelocityConstraints(step: Step, settings: Settings) {
        val dt = step!!.deltaTime
        val invdt = step.inverseDeltaTime
        val t1 = body1.transform
        val t2 = body2!!.transform
        val m1 = body1.mass
        val m2 = body2.mass
        val invM1 = m1!!.inverseMass
        val invM2 = m2!!.inverseMass
        val invI1 = m1.inverseInertia
        val invI2 = m2.inverseInertia

        // solve the angular constraint
        run {

            // get the relative velocity - the target motor speed
            val C =
                this.body2!!.angularVelocity - this.body1.angularVelocity + invdt * this.correctionFactor * this.angularError
            // get the impulse required to obtain the speed
            var impulse = this.angularMass * -C
            // clamp the impulse between the maximum torque
            val oldImpulse = this.angularImpulse
            val maxImpulse = this.maximumTorque * dt
            this.angularImpulse =
                Interval.clamp(this.angularImpulse + impulse, -maxImpulse, maxImpulse)
            // get the impulse we need to apply to the bodies
            impulse = this.angularImpulse - oldImpulse

            // apply the impulse
            this.body1.angularVelocity = (this.body1.angularVelocity - invI1 * impulse)
            this.body2.angularVelocity = (this.body2.angularVelocity + invI2 * impulse)
        }

        // solve the point-to-point constraint
        val r1 = t1.getTransformedR(body1.localCenter!!.negative)
        val r2 = t2.getTransformedR(body2.localCenter!!.negative)
        val v1 =
            body1.getLinearVelocity()!!.sum(r1.cross(body1.angularVelocity))
        val v2 =
            body2.getLinearVelocity()!!.sum(r2.cross(body2.angularVelocity))
        val pivotV = v2.subtract(v1)
        pivotV.add(linearError!!.product(correctionFactor * invdt))
        var impulse = K.multiply(pivotV)
        impulse.negate()

        // clamp by the maxforce
        val oldImpulse = linearImpulse.copy()
        linearImpulse.add(impulse)
        val maxImpulse = maximumForce * dt
        if (linearImpulse.magnitudeSquared > maxImpulse * maxImpulse) {
            linearImpulse.normalize()
            linearImpulse.multiply(maxImpulse)
        }
        impulse = linearImpulse.difference(oldImpulse)
        body1.getLinearVelocity().subtract(impulse.product(invM1))
        body1.angularVelocity = (body1.angularVelocity - invI1 * r1.cross(impulse))
        body2.getLinearVelocity().add(impulse.product(invM2))
        body2.angularVelocity = (body2.angularVelocity + invI2 * r2.cross(impulse))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solvePositionConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solvePositionConstraints(step: Step, settings: Settings): Boolean {
        // nothing to do here for this joint since there is no "hard" constraint
        return true
    }

    /**
     * Returns error in the angle between the joined bodies given the target
     * angle.
     * @return double
     */
    private fun getAngularError(): Double {
        var rr = body2.transform.rotationAngle - body1.transform.rotationAngle - angularTarget
        if (rr < -PI) rr += Geometry.TWO_PI
        if (rr > PI) rr -= Geometry.TWO_PI
        return rr
    }

    /**
     * {@inheritDoc}
     *
     *
     * Not applicable to this joint.
     * Returns the first body's world center.
     */
    override val anchor1: Vector2
        get() = body1.worldCenter

    /**
     * {@inheritDoc}
     *
     *
     * Not applicable to this joint.
     * Returns the second body's world center.
     */
    override val anchor2: Vector2
        get() = body2!!.worldCenter

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionForce(double)
	 */
    override fun getReactionForce(invdt: Double): Vector2? {
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
     * @throws NullPointerException if body1 or body2
     * @throws IllegalArgumentException if body1 == body2
     */
    init {
        // default no collision allowed
        // verify the bodies are not the same instance
        if (body1 == body2) throw IllegalArgumentException(getString("dynamics.joint.sameBody"))
        // default the linear target to body2's position in body1's frame
        linearTarget = body1.getLocalPoint(body2.worldCenter)
        // get the angular target for the joint
        angularTarget = body2.transform.rotationAngle - body1.transform.rotationAngle
        // initialize
        correctionFactor = 0.3
        K = Matrix22()
        linearImpulse = Vector2()
        angularImpulse = 0.0
    }

}