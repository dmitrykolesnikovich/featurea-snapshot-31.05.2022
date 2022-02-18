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
import kotlin.math.abs

/**
 * Implementation of a weld joint.
 *
 *
 * A weld joint joins two bodies together as if they were a single body with
 * two fixtures.  Both their relative linear and angular motion are constrained
 * to keep them attached to each other.  The system as a whole can rotate and
 * translate freely.
 *
 *
 * Using a frequency greater than zero allows the joint to function as a
 * torsion spring about the anchor point.  A good starting point is a frequency
 * of 8.0 and damping ratio of 0.3 then adjust as necessary.
 * @author William Bittle
 * @version 3.2.1
 * @since 1.0.0
 * @see [Documentation](http://www.dyn4j.org/documentation/joints/.Weld_Joint)
 *
 * @see [Weld Constraint](http://www.dyn4j.org/2010/12/weld-constraint/)
 */
class WeldJoint : Joint, Shiftable, DataContainer {
    /** The local anchor point on the first [Body]  */
    var localAnchor1: Vector2

    /** The local anchor point on the second [Body]  */
    var localAnchor2: Vector2

    /**
     * Returns the reference angle.
     *
     *
     * The reference angle is the angle calculated when the joint was created from the
     * two joined bodies.  The reference angle is the angular difference between the
     * bodies.
     * @return double
     * @since 3.0.1
     */
    /**
     * Sets the reference angle.
     *
     *
     * This method can be used to set the reference angle to override the computed
     * reference angle from the constructor.  This is useful in recreating the joint
     * from a current state.
     * @param angle the reference angle in radians
     * @see .getReferenceAngle
     * @since 3.0.1
     */
    /** The initial angle between the two [Body]s  */
    var referenceAngle: Double

    /** The oscillation frequency in hz  */
    var frequency: Double
        set(value) {
            if (value < 0) throw IllegalArgumentException(getString("dynamics.joint.invalidFrequency"))
            field = value
        }

    /** The damping ratio  */
    var dampingRatio: Double
        set(value) {
            if (value < 0 || value > 1) throw IllegalArgumentException(getString("dynamics.joint.invalidDampingRatio"))
            field = value
        }
    // current state
    /** The constraint mass; K = J * Minv * Jtrans  */
    private val K: Matrix33

    /** The bias for adding work to the constraint (simulating a spring)  */
    private var bias: Double

    /** The damping portion of the constraint  */
    private var gamma: Double
    // output
    /** The accumulated impulse for warm starting  */
    private val impulse: Vector3

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("WeldJoint[").append(super.toString())
            .append("|Anchor=").append(anchor1)
            .append("|ReferenceAngle=").append(referenceAngle)
            .append("|Frequency=").append(frequency)
            .append("|DampingRatio=").append(dampingRatio)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#initializeConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun initializeConstraints(
        step: Step,
        settings: Settings
    ) {
        val t1 = body1.transform
        val t2 = body2.transform
        val m1 = body1.mass
        val m2 = body2.mass
        val invM1 = m1!!.inverseMass
        val invM2 = m2!!.inverseMass
        val invI1 = m1.inverseInertia
        val invI2 = m2.inverseInertia
        val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
        val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))

        // compute the K inverse matrix
        K.m00 = invM1 + invM2 + r1.y * r1.y * invI1 + r2.y * r2.y * invI2
        K.m01 = -r1.y * r1.x * invI1 - r2.y * r2.x * invI2
        K.m02 = -r1.y * invI1 - r2.y * invI2
        K.m10 = K.m01
        K.m11 = invM1 + invM2 + r1.x * r1.x * invI1 + r2.x * r2.x * invI2
        K.m12 = r1.x * invI1 + r2.x * invI2
        K.m20 = K.m02
        K.m21 = K.m12
        K.m22 = invI1 + invI2
        if (frequency > 0.0 && K.m22 > 0.0) {
            var invI = invI1 + invI2
            val i = if (invI <= Epsilon.E) 0.0 else 1.0 / invI

            // compute the current angle between relative to the reference angle
            val r = relativeRotation
            val dt = step!!.deltaTime
            // compute the natural frequency; f = w / (2 * pi) -> w = 2 * pi * f
            val w = Geometry.TWO_PI * frequency
            // compute the damping coefficient; dRatio = d / (2 * m * w) -> d = 2 * m * w * dRatio
            val d = 2.0 * i * dampingRatio * w
            // compute the spring constant; w = sqrt(k / m) -> k = m * w * w
            val k = i * w * w

            // compute gamma = CMF = 1 / (hk + d)
            gamma = dt * (d + dt * k)
            // check for zero before inverting
            gamma = if (gamma <= Epsilon.E) 0.0 else 1.0 / gamma
            // compute the bias = x * ERP where ERP = hk / (hk + d)
            bias = r * dt * k * gamma

            // compute the effective mass
            invI += gamma
            // check for zero before inverting
            K.m22 = if (invI <= Epsilon.E) 0.0 else 1.0 / invI
        } else {
            gamma = 0.0
            bias = 0.0
        }

        // account for variable time step
        impulse.multiply(step.deltaTimeRatio)

        // warm start
        val impulse = Vector2(impulse.x, impulse.y)
        body1.getLinearVelocity().add(impulse.product(invM1))
        body1.angularVelocity = (body1.angularVelocity + invI1 * (r1.cross(impulse) + this.impulse.z))
        body2.getLinearVelocity().subtract(impulse.product(invM2))
        body2.angularVelocity = (body2.angularVelocity - invI2 * (r2.cross(impulse) + this.impulse.z))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solveVelocityConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solveVelocityConstraints(step: Step, settings: Settings) {
        val t1 = body1.transform
        val t2 = body2!!.transform
        val m1 = body1.mass
        val m2 = body2.mass
        val invM1 = m1!!.inverseMass
        val invM2 = m2!!.inverseMass
        val invI1 = m1.inverseInertia
        val invI2 = m2.inverseInertia
        val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
        val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))
        if (frequency > 0.0) {
            // get the relative angular velocity
            val rav = body1.angularVelocity - body2.angularVelocity
            // solve for the spring/damper impulse
            val j2 = -K.m22 * (rav + bias + gamma * impulse.z)
            impulse.z += j2
            body1.angularVelocity = (body1.angularVelocity + invI1 * j2)
            body2.angularVelocity = (body2.angularVelocity - invI2 * j2)

            // solve the point-to-point and angle constraint
            val v1 =
                body1.getLinearVelocity()!!.sum(r1.cross(body1.angularVelocity))
            val v2 =
                body2.getLinearVelocity()!!.sum(r2.cross(body2.angularVelocity))
            val anchorV = v1.subtract(v2)
            val j1 = K.solve22(anchorV).negate()
            impulse.x += j1.x
            impulse.y += j1.y
            body1.getLinearVelocity()!!.add(j1.product(invM1))
            body1.angularVelocity = (body1.angularVelocity + invI1 * r1.cross(j1))
            body2.getLinearVelocity()!!.subtract(j1.product(invM2))
            body2.angularVelocity = (body2.angularVelocity - invI2 * r2.cross(j1))
        } else {
            val v1 =
                body1.getLinearVelocity()!!.sum(r1.cross(body1.angularVelocity))
            val v2 =
                body2.getLinearVelocity()!!.sum(r2.cross(body2.angularVelocity))
            val anchorV = v1.subtract(v2)
            val C = Vector3(
                anchorV.x,
                anchorV.y,
                body1.angularVelocity - body2.angularVelocity
            )
            var impulse: Vector3? = null
            impulse = if (K.m22 > 0.0) {
                K.solve33(C.negate())
            } else {
                val impulse2 = K.solve22(anchorV).negate()
                Vector3(impulse2.x, impulse2.y, 0.0)
            }
            this.impulse.add(impulse)

            // apply the impulse
            val imp = Vector2(impulse.x, impulse.y)
            body1.getLinearVelocity()!!.add(imp.product(invM1))
            body1.angularVelocity = (body1.angularVelocity + invI1 * (r1.cross(imp) + impulse.z))
            body2.getLinearVelocity()!!.subtract(imp.product(invM2))
            body2.angularVelocity = (body2.angularVelocity - invI2 * (r2.cross(imp) + impulse.z))
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solvePositionConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solvePositionConstraints(step: Step, settings: Settings): Boolean {
        val linearTolerance = settings!!.getLinearTolerance()
        val angularTolerance = settings.getAngularTolerance()
        val t1 = body1.transform
        val t2 = body2!!.transform
        val m1 = body1.mass
        val m2 = body2.mass
        val invM1 = m1!!.inverseMass
        val invM2 = m2!!.inverseMass
        val invI1 = m1.inverseInertia
        val invI2 = m2.inverseInertia
        val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
        val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))
        val p1 = body1.worldCenter.add(r1)
        val p2 = body2.worldCenter.add(r2)
        val C1 = p1.difference(p2)
        val C2 = relativeRotation
        val C = Vector3(C1.x, C1.y, C2)
        val linearError = C1.magnitude
        var angularError: Double = abs(C2)

        // compute the K inverse matrix
        K.m00 = invM1 + invM2 + r1.y * r1.y * invI1 + r2.y * r2.y * invI2
        K.m01 = -r1.y * r1.x * invI1 - r2.y * r2.x * invI2
        K.m02 = -r1.y * invI1 - r2.y * invI2
        K.m10 = K.m01
        K.m11 = invM1 + invM2 + r1.x * r1.x * invI1 + r2.x * r2.x * invI2
        K.m12 = r1.x * invI1 + r2.x * invI2
        K.m20 = K.m02
        K.m21 = K.m12
        K.m22 = invI1 + invI2
        if (frequency > 0.0) {
            // only solve the linear constraint
            angularError = 0.0
            val j = K.solve22(C1).negate()
            body1.translate(j.product(invM1))
            body1.rotateAboutCenter(invI1 * r1.cross(j))
            body2.translate(j.product(-invM2))
            body2.rotateAboutCenter(-invI2 * r2.cross(j))
        } else {
            var impulse: Vector3? = null
            impulse = if (K.m22 > 0.0) {
                K.solve33(C.negate())
            } else {
                val impulse2 = K.solve22(C1).negate()
                Vector3(impulse2.x, impulse2.y, 0.0)
            }

            // translate and rotate the objects
            val imp = Vector2(impulse.x, impulse.y)
            body1.translate(imp.product(invM1))
            body1.rotateAboutCenter(invI1 * (r1.cross(imp) + impulse.z))
            body2.translate(imp.product(-invM2))
            body2.rotateAboutCenter(-invI2 * (r2.cross(imp) + impulse.z))
        }
        return linearError <= linearTolerance && angularError <= angularTolerance
    }

    /**
     * Returns the relative angle between the two bodies given the reference angle.
     * @return double
     */
    private val relativeRotation: Double
        private get() {
            var rr =
                body1.transform.rotationAngle - body2!!.transform.rotationAngle - referenceAngle
            if (rr < -PI) rr += Geometry.TWO_PI
            if (rr > PI) rr -= Geometry.TWO_PI
            return rr
        }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getAnchor1()
	 */
    override val anchor1: Vector2
        get() = body1.getWorldPoint(localAnchor1)

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getAnchor2()
	 */
    override val anchor2: Vector2
        get() = body2!!.getWorldPoint(localAnchor2)

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionForce(double)
	 */
    override fun getReactionForce(invdt: Double): Vector2? {
        val impulse = Vector2(impulse.x, impulse.y)
        return impulse.multiply(invdt)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionTorque(double)
	 */
    override fun getReactionTorque(invdt: Double): Double {
        return impulse.z * invdt
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shiftable#shift(org.dyn4j.geometry.Vector2)
	 */
    override fun shift(shift: Vector2) {
        // nothing to translate here since the anchor points are in local coordinates
        // they will move with the bodies
    }

    /**
     * Returns true if this distance joint is a spring distance joint.
     * @return boolean
     * @since 3.0.1
     */
    val isSpring: Boolean
        get() = frequency > 0.0

    /**
     * Returns true if this distance joint is a spring distance joint
     * with damping.
     * @return boolean
     * @since 3.0.1
     */
    val isSpringDamper: Boolean
        get() = frequency > 0.0 && dampingRatio > 0.0

    /**
     * Minimal constructor.
     * @param body1 the first [Body]
     * @param body2 the second [Body]
     * @param anchor the anchor point in world coordinates
     * @throws NullPointerException if body1, body2, or anchor is null
     * @throws IllegalArgumentException if body1 == body2
     */
    constructor(body1: Body, body2: Body, anchor: Vector2) : super(body1, body2) {
        // verify the bodies are not the same instance
        if (body1 == body2) throw IllegalArgumentException(getString("dynamics.joint.sameBody"))
        // set the anchor point
        localAnchor1 = body1.getLocalPoint(anchor)
        localAnchor2 = body2.getLocalPoint(anchor)
        // set the reference angle
        referenceAngle = body1.transform.rotationAngle - body2.transform.rotationAngle
        // initialize
        K = Matrix33()
        impulse = Vector3()
        frequency = 0.0
        dampingRatio = 0.0
        gamma = 0.0
        bias = 0.0
    }
}