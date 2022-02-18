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
import org.dyn4j.geometry.Interval
import org.dyn4j.geometry.Shiftable
import org.dyn4j.geometry.Vector2
import org.dyn4j.resources.Messages.getString
import kotlin.math.abs

/**
 * Implementation of a wheel joint.
 *
 *
 * A wheel joint is used to simulate a vehicle's wheel and suspension.  The
 * wheel is allowed to rotate freely about the given anchor point.  The
 * suspension is allowed to translate freely along the given axis.  The whole
 * system can translate and rotate freely.
 *
 *
 * By default the frequency and damping ratio are set to 8.0 and 0.0
 * respectively.  By definition this joint requires a frequency greater than
 * zero to perform properly.  If a wheel without suspension is required, use
 * a [RevoluteJoint] instead.
 *
 *
 * This joint also supports a motor.  The motor is an angular motor about the
 * anchor point.  The motor speed can be positive or negative to indicate a
 * clockwise or counter-clockwise rotation.  The maximum motor torque must be
 * greater than zero for the motor to apply any motion.
 * @author William Bittle
 * @version 3.4.1
 * @since 3.0.0
 * @see [Documentation](http://www.dyn4j.org/documentation/joints/.Wheel_Joint)
 */
class WheelJoint : Joint, Shiftable, DataContainer {
    /** The local anchor point on the first [Body]  */
    var localAnchor1: Vector2

    /** The local anchor point on the second [Body]  */
    var localAnchor2: Vector2

    /** Whether the motor is enabled or not  */
    var isMotorEnabled: Boolean
        set(value) {
            if (field != value) {
                // wake up the joined bodies
                body1.setAsleep(false)
                body2!!.setAsleep(false)
                // set the new value
                field = value
            }
        }

    /** The target velocity in radians / second  */
    var motorSpeed: Double
        set(value) {
            if (field != value) {
                if (isMotorEnabled) {
                    // wake up the joined bodies
                    body1.setAsleep(false)
                    body2.setAsleep(false)
                }
                // set the new value
                field = value
            }
        }

    /** The maximum torque the motor can apply in newton-meters  */
    var maximumMotorTorque: Double
        set(value) {
            // make sure its greater than or equal to zero
            if (value < 0.0) throw IllegalArgumentException(getString("dynamics.joint.invalidMaximumMotorTorque"))
            if (field != value) {
                if (isMotorEnabled) {
                    // wake up the joined bodies
                    body1.setAsleep(false)
                    body2.setAsleep(false)
                }
                // set the new value
                field = value
            }
        }

    /** The oscillation frequency in hz  */
    var frequency: Double
        set(value) {
            // check for valid value
            if (value <= 0) throw IllegalArgumentException(getString("dynamics.joint.invalidFrequencyZero"))
            // wake up both bodies
            body1.setAsleep(false)
            body2.setAsleep(false)
            // set the new value
            field = value
        }

    /** The damping ratio  */
    var dampingRatio: Double
        set(value) {
            // make sure its within range
            if (value < 0 || value > 1) throw IllegalArgumentException(getString("dynamics.joint.invalidDampingRatio"))
            // wake up both bodies
            body1.setAsleep(false)
            body2!!.setAsleep(false)
            // set the new value
            field = value
        }
    // internal
    /** The axis representing the allowed line of motion  */
    private val xAxis: Vector2

    /** The perpendicular axis of the line of motion  */
    private val yAxis: Vector2
    // current state
    /** The bias for adding work to the constraint (simulating a spring)  */
    private var bias: Double

    /** The damping portion of the constraint  */
    private var gamma: Double

    /** The point-on-line constraint mass; K = J * Minv * Jtrans  */
    private var invK: Double

    /** The spring/damper constraint mass  */
    private var springMass: Double

    /** The mass of the motor  */
    private var motorMass: Double

    /** The world space yAxis   */
    private var perp: Vector2? = null

    /** The world space xAxis  */
    private var axis: Vector2? = null

    /** s1 = (r1 + d).cross(perp)  */
    private var s1 = 0.0

    /** s2 = r2.cross(perp)  */
    private var s2 = 0.0

    /** a1 = (r1 + d).cross(axis)  */
    private var a1 = 0.0

    /** a2 = r2.cross(axis)  */
    private var a2 = 0.0
    // output
    /** The accumulated impulse for warm starting  */
    private var impulse: Double

    /** The impulse applied by the spring/damper  */
    private var springImpulse: Double

    /** The impulse applied by the motor  */
    private var motorImpulse: Double

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("WheelJoint[").append(super.toString())
            .append("|WorldAnchor=").append(anchor1)
            .append("|Axis=").append(getAxis())
            .append("|IsMotorEnabled=").append(isMotorEnabled)
            .append("|MotorSpeed=").append(motorSpeed)
            .append("|MaximumMotorTorque=").append(maximumMotorTorque)
            .append("|Frequency=").append(frequency)
            .append("|DampingRatio=").append(dampingRatio)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#initializeConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun initializeConstraints(step: Step, settings: Settings) {
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

        // compute the vector between the two world space anchor points
        val d =
            body1.worldCenter.sum(r1).subtract(body2.worldCenter.sum(r2))

        // get the world vectors of the axes
        axis = body2.getWorldVector(xAxis)
        perp = body2.getWorldVector(yAxis)

        // compute invK for the point-on-line constraint
        run {

            // s1 = r1.cross(perp)
            // s2 = (r2 + d).cross(perp)
            this.s1 = r1.cross(this.perp!!)
            this.s2 = r2.sum(d).cross(this.perp!!)
            this.invK = invM1 + invM2 + this.s1 * this.s1 * invI1 + this.s2 * this.s2 * invI2
            // make sure we don't divide by zero
            if (this.invK > Epsilon.E) {
                this.invK = 1.0 / this.invK
            }
        }

        // compute the spring mass for the spring constraint
        if (frequency > 0.0) {
            // then we include the spring constraint
            // a1 = r1.cross(axis)
            // a2 = (r2 + d).cross(axis)
            a1 = r1.cross(axis!!)
            a2 = r2.sum(d).cross(axis!!)
            val invMass = invM1 + invM2 + a1 * a1 * invI1 + a2 * a2 * invI2
            // make sure we don't divide by zero
            if (invMass > Epsilon.E) {
                // invert the spring mass
                springMass = 1.0 / invMass
                // compute the current spring extension (we are solving for zero here)
                val c = d.dot(axis!!)
                // get the delta time
                val dt = step!!.deltaTime
                // compute the natural frequency; f = w / (2 * pi) -> w = 2 * pi * f
                val w = Geometry.TWO_PI * frequency
                // compute the damping coefficient; dRatio = d / (2 * m * w) -> d = 2 * m * w * dRatio
                val dc = 2.0 * springMass * dampingRatio * w
                // compute the spring constant; w = sqrt(k / m) -> k = m * w * w
                val k = springMass * w * w

                // compute gamma = CMF = 1 / (hk + d)
                gamma = dt * (dc + dt * k)
                // check for zero before inverting
                gamma = if (abs(gamma) <= Epsilon.E) 0.0 else 1.0 / gamma
                // compute the bias = x * ERP where ERP = hk / (hk + d)
                bias = c * dt * k * gamma

                // compute the effective mass			
                springMass = invMass + gamma
                // check for zero before inverting
                springMass = if (abs(springMass) <= Epsilon.E) 0.0 else 1.0 / springMass
            }
        } else {
            // don't include the spring constraint
            springMass = 0.0
            springImpulse = 0.0
        }

        // check if the motor is enabled
        if (isMotorEnabled) {
            // compute the motor mass
            motorMass = invI1 + invI2
            if (abs(motorMass) > Epsilon.E) {
                motorMass = 1.0 / motorMass
            }
        } else {
            // clear the previous motor impulse
            motorMass = 0.0
            motorImpulse = 0.0
        }

        // warm start
        // account for variable time step
        impulse *= step!!.deltaTimeRatio
        springImpulse *= step.deltaTimeRatio
        motorImpulse *= step.deltaTimeRatio

        // we only compute the impulse for body1 since body2's impulse is
        // just the negative of body1's impulse
        val P = Vector2()
        // perp.product(impulse) + axis.product(springImpulse)
        P.x = perp!!.x * impulse + springImpulse * axis!!.x
        P.y = perp!!.y * impulse + springImpulse * axis!!.y
        val l1 = impulse * s1 + springImpulse * a1 + motorImpulse
        val l2 = impulse * s2 + springImpulse * a2 + motorImpulse

        // apply the impulses
        body1.getLinearVelocity()!!.add(P.product(invM1))
        body1.angularVelocity = (body1.angularVelocity + invI1 * l1)
        body2.getLinearVelocity()!!.subtract(P.product(invM2))
        body2.angularVelocity = (body2.angularVelocity - invI2 * l2)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solveVelocityConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solveVelocityConstraints(step: Step, settings: Settings) {
        val m1 = body1.mass
        val m2 = body2!!.mass
        val invM1 = m1!!.inverseMass
        val invM2 = m2!!.inverseMass
        val invI1 = m1.inverseInertia
        val invI2 = m2.inverseInertia
        val v1 = body1.getLinearVelocity()
        val v2 = body2.getLinearVelocity()
        var w1 = body1.angularVelocity
        var w2 = body2.angularVelocity

        // solve the spring constraint
        run {
            val Cdt = this.axis!!.dot(v1!!.difference(v2!!)) + this.a1 * w1 - this.a2 * w2
            // compute the impulse
            val impulse = -this.springMass * (Cdt + this.bias + this.gamma * this.springImpulse)
            // accumulate the spring impulse
            this.springImpulse += impulse

            // compute the applied impulses
            // Pc = Jtrans * lambda
            val P = this.axis!!.product(impulse)
            val l1 = impulse * this.a1
            val l2 = impulse * this.a2
            v1.add(P.product(invM1))
            w1 += l1 * invI1
            v2.subtract(P.product(invM2))
            w2 -= l2 * invI2
        }

        // solve the motor constraint
        if (isMotorEnabled) {
            // compute Jv + b
            val Cdt = w1 - w2 - motorSpeed
            // compute lambda = Kinv * (Jv + b)
            var impulse = motorMass * -Cdt
            // clamp the impulse between the max torque
            val oldImpulse = motorImpulse
            val maxImpulse = maximumMotorTorque * step!!.deltaTime
            motorImpulse = Interval.clamp(motorImpulse + impulse, -maxImpulse, maxImpulse)
            impulse = motorImpulse - oldImpulse

            // apply the impulse
            w1 += impulse * invI1
            w2 -= impulse * invI2
        }

        // finally, solve the point-on-line constraint
        run {
            val Cdt = this.perp!!.dot(v1!!.difference(v2!!)) + this.s1 * w1 - this.s2 * w2
            val impulse = this.invK * -Cdt
            // accumulate the impulse
            this.impulse += impulse

            // compute the applied impulses
            // Pc = Jtrans * lambda
            val P = this.perp!!.product(impulse)
            val l1 = impulse * this.s1
            val l2 = impulse * this.s2
            v1.add(P.product(invM1))
            w1 += l1 * invI1
            v2.subtract(P.product(invM2))
            w2 -= l2 * invI2
        }

        // finally set the velocities
        // NOTE we dont have to update v1 or v2 because they are references
        body1.angularVelocity = (w1)
        body2.angularVelocity = (w2)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solvePositionConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solvePositionConstraints(step: Step, settings: Settings): Boolean {
        val linearTolerance = settings.getLinearTolerance()
        val t1 = body1.transform
        val t2 = body2.transform
        val m1 = body1.mass
        val m2 = body2.mass
        val invM1 = m1!!.inverseMass
        val invM2 = m2!!.inverseMass
        val invI1 = m1.inverseInertia
        val invI2 = m2.inverseInertia
        val c1 = body1.worldCenter
        val c2 = body2.worldCenter
        val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
        val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))
        val d = c1.sum(r1).subtract(c2.sum(r2))
        axis = body2.getWorldVector(xAxis)
        perp = body2.getWorldVector(yAxis)
        val Cx = perp!!.dot(d)
        val k = invM1 + invM2 + s1 * s1 * invI1 + s2 * s2 * invI2
        var impulse = 0.0

        // make sure k is not zero
        impulse = if (k > Epsilon.E) {
            -Cx / k
        } else {
            0.0
        }

        // apply the impulse
        val P = Vector2()
        P.x = perp!!.x * impulse
        P.y = perp!!.y * impulse
        val l1 = s1 * impulse
        val l2 = s2 * impulse
        body1.translate(P.product(invM1))
        body1.rotateAboutCenter(l1 * invI1)
        body2.translate(P.product(-invM2))
        body2.rotateAboutCenter(-l2 * invI2)

        // return if we corrected the error enough
        return abs(Cx) <= linearTolerance
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
        val force = Vector2()
        // compute the impulse
        force.x = impulse * perp!!.x + springImpulse * axis!!.x
        force.y = impulse * perp!!.y + springImpulse * axis!!.y
        // multiply by invdt to obtain the force
        force.multiply(invdt)
        return force
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionTorque(double)
	 */
    override fun getReactionTorque(invdt: Double): Double {
        return motorImpulse * invdt
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shiftable#shift(org.dyn4j.geometry.Vector2)
	 */
    override fun shift(shift: Vector2) {
        // nothing to translate here since the anchor points are in local coordinates
        // they will move with the bodies
    }

    /**
     * Returns the current joint speed.
     * @return double
     */
    @get:Deprecated("Replaced by {@link #getAngularSpeed()} in 3.2.1")
    val jointSpeed: Double
        get() = angularSpeed// get the world vectors of the axis

    // compute the velocities along the vectors pointing to the world space anchor points

    // project them onto the joint axis

    /**
     * Returns the linear speed along the axis between the two joined bodies
     * @return double
     * @since 3.2.1
     */
    val linearSpeed: Double
        get() {
            val t1 = body1.transform
            val t2 = body2!!.transform
            val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
            val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))

            // get the world vectors of the axis
            val axis = body2.getWorldVector(xAxis)

            // compute the velocities along the vectors pointing to the world space anchor points
            val v1 =
                r1.cross(body1.angularVelocity).add(body1.getLinearVelocity()!!)
            val v2 =
                r2.cross(body2.angularVelocity).add(body2.getLinearVelocity()!!)

            // project them onto the joint axis
            return v2.dot(axis) - v1.dot(axis)
        }

    /**
     * Returns the current angular speed between the two joined bodies.
     * @return double
     * @since 3.2.1
     */
    val angularSpeed: Double
        get() {
            val a1 = body1.angularVelocity
            val a2 = body2!!.angularVelocity
            return a2 - a1
        }

    /**
     * Returns the current joint translation.
     * @return double
     */
    @get:Deprecated("Replaced by {@link #getLinearTranslation()} in 3.2.1")
    val jointTranslation: Double
        get() = linearTranslation

    /**
     * Returns the current linear translation along the joint axis.
     * @return double
     * @since 3.2.1
     */
    val linearTranslation: Double
        get() {
            val p1 = body1.getWorldPoint(localAnchor1)
            val p2 = body2!!.getWorldPoint(localAnchor2)
            val d = p2.difference(p1)
            val axis = body2.getWorldVector(xAxis)
            return d.dot(axis)
        }

    /**
     * Returns the current angular translation between the joined bodies.
     * @return double
     * @since 3.2.1
     */
    val angularTranslation: Double
        get() {
            val a1 = body1.transform.rotationAngle
            val a2 = body2!!.transform.rotationAngle
            return a2 - a1
        }

    /**
     * Returns true if this wheel joint is a spring wheel joint.
     *
     *
     * Since the frequency cannot be less than or equal to zero, this should
     * always returne true.
     * @return boolean
     */
    val isSpring: Boolean
        get() = frequency > 0.0

    /**
     * Returns true if this wheel joint is a spring wheel joint
     * with damping.
     * @return boolean
     */
    val isSpringDamper: Boolean
        get() = frequency > 0.0 && dampingRatio > 0.0


    /**
     * Returns the applied motor torque.
     * @param invdt the inverse delta time from the time step
     * @return double
     */
    fun getMotorTorque(invdt: Double): Double = motorImpulse * invdt

    /**
     * Returns the axis in which the joint is allowed move along in world coordinates.
     * @return [Vector2]
     */
    fun getAxis(): Vector2 = body2.getWorldVector(xAxis)

    /**
     * Minimal constructor.
     * @param body1 the first [Body]
     * @param body2 the second [Body]
     * @param anchor the anchor point in world coordinates
     * @param axis the axis of allowed motion
     * @throws NullPointerException if body1, body2, anchor, or axis is null
     * @throws IllegalArgumentException if body1 == body2
     */
    constructor(body1: Body, body2: Body, anchor: Vector2, axis: Vector2) : super(body1, body2) {
        // verify the bodies are not the same instanceTriangle.kt
        if (body1 == body2) throw IllegalArgumentException(getString("dynamics.joint.sameBody"))
        // set the anchor point
        localAnchor1 = body1.getLocalPoint(anchor)
        localAnchor2 = body2.getLocalPoint(anchor)
        // make sure the axis is normalized
        val n = axis.normalized
        // get the axis in local coordinates
        xAxis = body2.getLocalVector(n)
        // get the perpendicular axis
        yAxis = xAxis.rightHandOrthogonalVector

        // initialize
        invK = 0.0
        impulse = 0.0
        motorMass = 0.0
        motorImpulse = 0.0
        springMass = 0.0
        springImpulse = 0.0

        // requires a spring damper by definition of the constraint.
        // if a spring/damper isn't needed, then use the RevoluteJoint instead.
        frequency = 8.0
        dampingRatio = 0.0
        gamma = 0.0
        bias = 0.0

        // no motor
        isMotorEnabled = false
        maximumMotorTorque = 0.0
        motorSpeed = 0.0
    }
}