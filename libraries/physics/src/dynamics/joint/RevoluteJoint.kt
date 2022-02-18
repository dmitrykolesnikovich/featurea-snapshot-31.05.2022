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
 * Implementation of a pivot joint.
 *
 *
 * A pivot joint allows two bodies to rotate freely about a common point, but
 * does not allow them to translate relative to one another.  The system as a
 * whole can translate and rotate freely.
 *
 *
 * By default the lower and upper limit angles are set to the current angle
 * between the bodies.  When the lower and upper limits are equal, the bodies
 * rotate together and are not allowed rotate relative to one another.  By
 * default the limits are disabled.
 *
 *
 * If the lower and upper limits are set explicitly, the values must follow
 * these restrictions:
 *
 *  * lower limit  upper limit
 *  * lower limit &gt; -180
 *  * upper limit &lt; 180
 *
 * To create a joint with limits outside of this range use the
 * [.setReferenceAngle] method.  This method sets the baseline
 * angle for the joint, which represents 0 radians in the context of the
 * limits.  For example:
 * <pre>
 * // we would like the joint limits to be [30, 260]
 * // this is the same as the limits [-60, 170] if the reference angle is 90
 * joint.setLimits(Math.toRadians(-60), Math.toRadians(170));
 * joint.setReferenceAngle(Math.toRadians(90));
</pre> *
 * This joint also supports a motor.  The motor is an angular motor about the
 * anchor point.  The motor speed can be positive or negative to indicate a
 * clockwise or counter-clockwise rotation.  The maximum motor torque must be
 * greater than zero for the motor to apply any motion.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 * @see [Documentation](http://www.dyn4j.org/documentation/joints/.Revolute_Joint)
 *
 * @see [Point-to-Point Constraint](http://www.dyn4j.org/2010/07/point-to-point-constraint/)
 */
class RevoluteJoint : Joint, Shiftable, DataContainer {
    /** The local anchor point on the first [Body]  */
    var localAnchor1: Vector2

    /** The local anchor point on the second [Body]  */
    var localAnchor2: Vector2

    /** Whether the motor for this [Joint] is enabled or not  */
    var isMotorEnabled: Boolean
        set(value) {
            if (field != value) {
                // wake up the associated bodies
                body1.setAsleep(false)
                body2!!.setAsleep(false)
                // set the flag
                field = value
            }
        }

    /** The target motor speed; in radians / second  */
    var motorSpeed = 0.0
        set(value) {
            if (field != value) {
                // only wake the bodies if the motor is enabled
                if (isMotorEnabled) {
                    // if so, then wake up the bodies
                    body1.setAsleep(false)
                    body2.setAsleep(false)
                }
                // set the motor speed
                field = value
            }
        }

    /** The maximum torque the motor can apply  */
    var maximumMotorTorque = 0.0
        set(value) {
            if (value < 0.0) throw IllegalArgumentException(getString("dynamics.joint.invalidMaximumMotorTorque"))
            if (field != value) {
                if (isMotorEnabled) {
                    body1.setAsleep(false)
                    body2.setAsleep(false)
                }
                field = value
            }
        }

    /** Whether the [Joint] limits are enabled or not  */
    var isLimitEnabled: Boolean = false
        set(value) {
            if (field != value) {
                // wake up both bodies
                body1.setAsleep(false)
                body2!!.setAsleep(false)
                // set the new value
                field = value
                // clear the accumulated limit impulse
                impulse.z = 0.0
            }
        }

    /** The upper limit of the [Joint]  */
    var upperLimit: Double = 0.0
        set(value) {
            if (value < lowerLimit) throw IllegalArgumentException(getString("dynamics.joint.invalidUpperLimit"))
            if (field != value) {
                // only wake the bodies if the motor is enabled and the limit has changed
                if (isLimitEnabled) {
                    // wake up the bodies
                    body1.setAsleep(false)
                    body2!!.setAsleep(false)
                }
                // set the new value
                field = value
            }
        }

    /** The lower limit of the [Joint]  */
    var lowerLimit: Double = 0.0
        set(value) {
            if (value > upperLimit) throw IllegalArgumentException(getString("dynamics.joint.invalidLowerLimit"))
            if (field != value) {
                // only wake the bodies if the motor is enabled and the limit has changed
                if (isLimitEnabled) {
                    // wake up the bodies
                    body1.setAsleep(false)
                    body2.setAsleep(false)
                }
                // set the new value
                field = value
            }
        }

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
    // current state
    /**
     * Returns the current state of the limit.
     * @return [LimitState]
     * @since 3.2.0
     */
    /** The current state of the [Joint] limit  */
    var limitState: LimitState
        private set

    /** The pivot mass; K = J * Minv * Jtrans  */
    private val K: Matrix33

    /** The motor mass that resists motion  */
    private var motorMass = 0.0
    // output
    /** The accumulated impulse for warm starting  */
    private val impulse: Vector3

    /**
     * Returns the motor torque in newton-meters.
     * @return double
     */
    /** The impulse applied by the motor  */
    var motorTorque = 0.0
        private set

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("RevoluteJoint[").append(super.toString())
            .append("|Anchor=").append(anchor1)
            .append("|IsMotorEnabled=").append(isMotorEnabled)
            .append("|MotorSpeed=").append(motorSpeed)
            .append("|MaximumMotorTorque=").append(maximumMotorTorque)
            .append("|IsLimitEnabled=").append(isLimitEnabled)
            .append("|LowerLimit=").append(lowerLimit)
            .append("|UpperLimit=").append(upperLimit)
            .append("|ReferenceAngle=").append(referenceAngle)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#initializeConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun initializeConstraints(step: Step, settings: Settings) {
        val angularTolerance = settings.getAngularTolerance()
        val t1 = body1.transform
        val t2 = body2.transform
        val m1 = body1.mass
        val m2 = body2.mass
        val invM1 = m1!!.inverseMass
        val invM2 = m2!!.inverseMass
        val invI1 = m1.inverseInertia
        val invI2 = m2.inverseInertia

        // is the motor enabled?
        if (isMotorEnabled) {
            // compute the motor mass
            check(!(invI1 <= 0.0 && invI2 <= 0.0)) {
                // cannot have a motor with two bodies
                // who have fixed angular velocities
                getString("dynamics.joint.revolute.twoAngularFixedBodies")
            }
        }
        val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
        val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))

        // compute the K matrix
        K.m00 = invM1 + invM2 + r1.y * r1.y * invI1 + r2.y * r2.y * invI2
        K.m01 = -r1.y * r1.x * invI1 - r2.y * r2.x * invI2
        K.m02 = -r1.y * invI1 - r2.y * invI2
        K.m10 = K.m01
        K.m11 = invM1 + invM2 + r1.x * r1.x * invI1 + r2.x * r2.x * invI2
        K.m12 = r1.x * invI1 + r2.x * invI2
        K.m20 = K.m02
        K.m21 = K.m12
        K.m22 = invI1 + invI2

        // compute the motor mass
        motorMass = invI1 + invI2
        if (motorMass > Epsilon.E) {
            motorMass = 1.0 / motorMass
        }

        // check if the motor is still enabled
        if (!isMotorEnabled) {
            // if not then make the current motor impulse zero
            motorTorque = 0.0
        }

        // check if the joint limit is enabled
        if (isLimitEnabled) {
            // set the current state of the joint limit
            val angle = relativeRotation

            // see if the limits are close enough to be equal
            if (abs(upperLimit - lowerLimit) < 2.0 * angularTolerance) {
                // if they are close enough then they are equal
                limitState = LimitState.EQUAL
            } else if (angle <= lowerLimit) {
                // is it currently at the lower limit?
                if (limitState !== LimitState.AT_LOWER) {
                    // if not then make the limit impulse zero
                    impulse.z = 0.0
                }
                limitState = LimitState.AT_LOWER
            } else if (angle >= upperLimit) {
                // is it currently at the upper limit?
                if (limitState !== LimitState.AT_UPPER) {
                    // if not then make the limit impulse zero
                    impulse.z = 0.0
                }
                limitState = LimitState.AT_UPPER
            } else {
                // otherwise the limit constraint is inactive
                impulse.z = 0.0
                limitState = LimitState.INACTIVE
            }
        } else {
            limitState = LimitState.INACTIVE
        }

        // account for variable time step
        impulse.multiply(step!!.deltaTimeRatio)
        motorTorque *= step.deltaTimeRatio

        // warm start
        val impulse = Vector2(impulse.x, impulse.y)
        body1.getLinearVelocity()!!.add(impulse.product(invM1))
        body1.angularVelocity = (body1.angularVelocity + invI1 * (r1.cross(impulse) + motorTorque + this.impulse.z))
        body2.getLinearVelocity()!!.subtract(impulse.product(invM2))
        body2.angularVelocity = (body2.angularVelocity - invI2 * (r2.cross(impulse) + motorTorque + this.impulse.z))
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

        // solve the motor constraint
        if (isMotorEnabled && limitState !== LimitState.EQUAL) {
            // get the relative velocity - the target motor speed
            val C = body1.angularVelocity - body2.angularVelocity - motorSpeed
            // get the impulse required to obtain the speed
            var impulse = motorMass * -C
            // clamp the impulse between the maximum torque
            val oldImpulse = motorTorque
            val maxImpulse = maximumMotorTorque * step!!.deltaTime
            motorTorque = Interval.clamp(motorTorque + impulse, -maxImpulse, maxImpulse)
            // get the impulse we need to apply to the bodies
            impulse = motorTorque - oldImpulse

            // apply the impulse
            body1.angularVelocity = (body1.angularVelocity + invI1 * impulse)
            body2.angularVelocity = (body2.angularVelocity - invI2 * impulse)
        }
        val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
        val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))
        val v1 =
            body1.getLinearVelocity()!!.sum(r1.cross(body1.angularVelocity))
        val v2 =
            body2.getLinearVelocity()!!.sum(r2.cross(body2.angularVelocity))
        // the 2x2 version of Jv + b
        val Jvb2 = v1.subtract(v2)

        // check if the limit constraint is enabled
        if (isLimitEnabled && limitState !== LimitState.INACTIVE) {
            // solve the point to point constraint including the limit constraint
            val pivotW = body1.angularVelocity - body2.angularVelocity
            // the 3x3 version of Jv + b
            val Jvb3 = Vector3(Jvb2.x, Jvb2.y, pivotW)
            val impulse3 = K.solve33(Jvb3.negate())
            // check the state to determine how to apply the impulse
            if (limitState === LimitState.EQUAL) {
                // if its equal limits then this is basically a weld joint
                // so add all the impulse to satisfy the point-to-point and
                // angle constraints
                impulse.add(impulse3)
            } else if (limitState === LimitState.AT_LOWER) {
                // if its at the lower limit then clamp the rotational impulse
                // and solve the point-to-point constraint alone
                val newImpulse = impulse.z + impulse3.z
                if (newImpulse < 0.0) {
                    val reduced = K.solve22(Jvb2.negate())
                    impulse3.x = reduced.x
                    impulse3.y = reduced.y
                    impulse3.z = -impulse.z
                    impulse.x += reduced.x
                    impulse.y += reduced.y
                    impulse.z = 0.0
                }
            } else if (limitState === LimitState.AT_UPPER) {
                // if its at the upper limit then clamp the rotational impulse
                // and solve the point-to-point constraint alone
                val newImpulse = impulse.z + impulse3.z
                if (newImpulse > 0.0) {
                    val reduced = K.solve22(Jvb2.negate())
                    impulse3.x = reduced.x
                    impulse3.y = reduced.y
                    impulse3.z = -impulse.z
                    impulse.x += reduced.x
                    impulse.y += reduced.y
                    impulse.z = 0.0
                }
            }

            // apply the impulses
            val impulse = Vector2(impulse3.x, impulse3.y)
            body1.getLinearVelocity()!!.add(impulse.product(invM1))
            body1.angularVelocity = (body1.angularVelocity + invI1 * (r1.cross(impulse) + impulse3.z))
            body2.getLinearVelocity()!!.subtract(impulse.product(invM2))
            body2.angularVelocity = (body2.angularVelocity - invI2 * (r2.cross(impulse) + impulse3.z))
        } else {
            // solve the point-to-point constraint
            val impulse = K.solve22(Jvb2.negate())
            this.impulse.x += impulse.x
            this.impulse.y += impulse.y
            body1.getLinearVelocity()!!.add(impulse.product(invM1))
            body1.angularVelocity = (body1.angularVelocity + invI1 * r1.cross(impulse))
            body2.getLinearVelocity()!!.subtract(impulse.product(invM2))
            body2.angularVelocity = (body2.angularVelocity - invI2 * r2.cross(impulse))
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solvePositionConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solvePositionConstraints(step: Step, settings: Settings): Boolean {
        val linearTolerance = settings!!.getLinearTolerance()
        val angularTolerance = settings.getAngularTolerance()
        val maxAngularCorrection = settings.getMaximumAngularCorrection()
        val t1 = body1.transform
        val t2 = body2!!.transform
        val m1 = body1.mass
        val m2 = body2.mass
        val invM1 = m1!!.inverseMass
        val invM2 = m2!!.inverseMass
        val invI1 = m1.inverseInertia
        val invI2 = m2.inverseInertia
        var linearError = 0.0
        var angularError = 0.0

        // solve the angular constraint if the limits are active
        if (isLimitEnabled && limitState !== LimitState.INACTIVE) {
            // get the current angle between the bodies
            val angle = relativeRotation
            var impulse = 0.0
            // check the limit state
            if (limitState === LimitState.EQUAL) {
                // if the limits are equal then clamp the impulse to maintain
                // the constraint between the maximum
                val j = Interval.clamp(
                    angle - lowerLimit,
                    -maxAngularCorrection,
                    maxAngularCorrection
                )
                impulse = -j * motorMass
                angularError = abs(j)
            } else if (limitState === LimitState.AT_LOWER) {
                // if the joint is at the lower limit then clamp only the lower value
                var j = angle - lowerLimit
                angularError = -j
                j = Interval.clamp(j + angularTolerance, -maxAngularCorrection, 0.0)
                impulse = -j * motorMass
            } else if (limitState === LimitState.AT_UPPER) {
                // if the joint is at the upper limit then clamp only the upper value
                var j = angle - upperLimit
                angularError = j
                j = Interval.clamp(j - angularTolerance, 0.0, maxAngularCorrection)
                impulse = -j * motorMass
            }

            // apply the impulse
            body1.rotateAboutCenter(invI1 * impulse)
            body2.rotateAboutCenter(-invI2 * impulse)
        }

        // always solve the point-to-point constraint
        val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
        val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))
        var p1 = body1.worldCenter.add(r1)
        var p2 = body2.worldCenter.add(r2)
        var p = p1.difference(p2)
        linearError = p.magnitude

        // handle large separation
        val large = 10.0 * linearTolerance
        // is the joint separation enough?
        if (p.magnitudeSquared > large * large) {
            // solve the separation of the joint ignoring rotation
            var m = invM1 + invM2
            // invert if non-zero
            if (m > Epsilon.E) {
                m = 1.0 / m
            }

            // solve for the impulse
            val impulse = p.multiply(-m)
            // scale by a half (don't bring them all the way together)
            val scale = 0.5
            // apply the impulse
            body1.translate(impulse.product(invM1 * scale))
            body2.translate(impulse.product(-invM2 * scale))

            // recompute the separation vector
            p1 = body1.worldCenter.add(r1)
            p2 = body2.worldCenter.add(r2)
            p = p1.difference(p2)
        }

        // compute the K matrix
        val K = Matrix22()
        K.m00 = invM1 + invM2 + r1.y * r1.y * invI1 + r2.y * r2.y * invI2
        K.m01 = -invI1 * r1.x * r1.y - invI2 * r2.x * r2.y
        K.m10 = this.K.m01
        K.m11 = invM1 + invM2 + r1.x * r1.x * invI1 + r2.x * r2.x * invI2

        // solve for the impulse
        val J = K.solve(p.negate())

        // translate and rotate the objects
        body1.translate(J.product(invM1))
        body1.rotateAboutCenter(invI1 * r1.cross(J))
        body2.translate(J.product(-invM2))
        body2.rotateAboutCenter(-invI2 * r2.cross(J))
        return linearError <= linearTolerance && angularError <= angularTolerance
    }

    /**
     * Returns the relative angle between the two bodies given the reference angle.
     * @return double
     */
    private val relativeRotation: Double
        private get() {
            var rr =
                body1.transform.rotationAngle - body2.transform.rotationAngle - referenceAngle
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
        get() = body2.getWorldPoint(localAnchor2)

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionForce(double)
	 */
    override fun getReactionForce(invdt: Double): Vector2? {
        return Vector2(impulse.x * invdt, impulse.y * invdt)
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
     * Returns the relative speed at which the [Body]s
     * are rotating in radians/second.
     * @return double
     */
    val jointSpeed: Double
        get() = body2!!.angularVelocity - body1.angularVelocity

    /**
     * Returns the relative angle between the two [Body]s in radians in the range [-, ].
     * @return double
     */
    val jointAngle: Double
        get() = relativeRotation

    /**
     * Sets the upper and lower rotational limits.
     *
     *
     * The lower limit must be less than or equal to the upper limit.
     *
     *
     * See the class documentation for more details on the limit ranges.
     * @param lowerLimit the lower limit in radians
     * @param upperLimit the upper limit in radians
     * @throws IllegalArgumentException if the lowerLimit is greater than upperLimit
     */
    fun setLimits(lowerLimit: Double, upperLimit: Double) {
        if (lowerLimit > upperLimit) throw IllegalArgumentException(getString("dynamics.joint.invalidLimits"))
        if (this.lowerLimit != lowerLimit || this.upperLimit != upperLimit) {
            // only wake the bodies if the motor is enabled and one of the limits has changed
            if (isLimitEnabled) {
                // wake up the bodies
                body1.setAsleep(false)
                body2.setAsleep(false)
            }
            // set the values
            this.upperLimit = upperLimit
            this.lowerLimit = lowerLimit
        }
    }

    /**
     * Minimal constructor.
     * @param body1 the first [Body]
     * @param body2 the second [Body]
     * @param anchor the anchor point in world coordinates
     * @throws NullPointerException if body1, body2 or anchor is null
     * @throws IllegalArgumentException if body1 == body2
     */
    constructor(body1: Body, body2: Body, anchor: Vector2) : super(body1, body2){
        // default to no collision allowed between the bodies
        // verify the bodies are not the same instance
        if (body1 == body2) throw IllegalArgumentException(getString("dynamics.joint.sameBody"))
        // get the local space points
        localAnchor1 = body1.getLocalPoint(anchor)
        localAnchor2 = body2.getLocalPoint(anchor)
        // get the initial reference angle for the joint limits
        referenceAngle = body1.transform.rotationAngle - body2.transform.rotationAngle

        // default limits
        lowerLimit = referenceAngle
        upperLimit = referenceAngle
        isLimitEnabled = false

        // initialize
        limitState = LimitState.INACTIVE
        impulse = Vector3()
        K = Matrix33()
        isMotorEnabled = false
    }
}