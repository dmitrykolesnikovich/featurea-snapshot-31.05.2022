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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of a prismatic joint.
 *
 *
 * A prismatic joint constrains the linear motion of two bodies along an axis
 * and prevents relative rotation.  The whole system can rotate and translate
 * freely.
 *
 *
 * The initial relative rotation of the bodies will remain unchanged unless
 * updated by calling [.setReferenceAngle] method.  The bodies
 * are not required to be aligned in any particular way.
 *
 *
 * The world space anchor point can be any point but is typically a point on
 * the axis of allowed motion, usually the world center of either of the joined
 * bodies.
 *
 *
 * The limits are linear limits along the axis.  The limits are checked against
 * the separation of the local anchor points, rather than the separation of the
 * bodies.  This can have the effect of offsetting the limit values.  The best
 * way to describe the effect is to examine the "0 to 0" limit case.  This case
 * specifies that the bodies should not move along the axis, forcing them to
 * stay at their *initial location* along the axis.  So if the bodies
 * were initially separated when they were joined, they will stay separated at
 * that initial distance.
 *
 *
 * This joint also supports a motor.  The motor is a linear motor along the
 * axis.  The motor speed can be positive or negative to indicate motion along
 * or opposite the axis direction.  The maximum motor force must be greater
 * than zero for the motor to apply any motion.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 * @see [Documentation](http://www.dyn4j.org/documentation/joints/.Prismatic_Joint)
 *
 * @see [Prismatic Constraint](http://www.dyn4j.org/2011/03/prismatic-constraint/)
 */
class PrismaticJoint : Joint,
    Shiftable, DataContainer {

    /** The local anchor point on the first [Body]  */
    var localAnchor1: Vector2

    /** The local anchor point on the second [Body]  */
    var localAnchor2: Vector2

    /** Whether the motor is enabled or not  */
    var isMotorEnabled: Boolean
        set(value) {
            // only wake the bodies if the enable flag changed
            if (field != value) {
                // wake up the joined bodies
                body1.setAsleep(false)
                body2.setAsleep(false)
                // set the new value
                field = value
            }
        }

    /** The target velocity in meters / second  */
    var motorSpeed = 0.0
        set(value) {
            if (field != value) {
                // only wake up the bodies if the motor is currently enabled
                if (isMotorEnabled) {
                    // wake up the joined bodies
                    body1.setAsleep(false)
                    body2.setAsleep(false)
                }
                // set the new value
                field = value
            }
        }

    /** The maximum force the motor can apply in newtons  */
    var maximumMotorForce = 0.0
        set(value) {
            // make sure its greater than or equal to zero
            if (value < 0.0) throw IllegalArgumentException(getString("dynamics.joint.invalidMaximumMotorForce"))
            // don't do anything if the max motor force isn't changing
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

    /** Whether the limit is enabled or not  */
    var isLimitEnabled: Boolean
        set(value) {
            if (field != value) {
                // wake up the joined bodies
                body1.setAsleep(false)
                body2.setAsleep(false)
                // set the new value
                field = value
            }
        }

    /** The upper limit in meters  */
    var upperLimit = 0.0
        set(value) {
            // check for valid value
            if (value < lowerLimit) throw IllegalArgumentException(getString("dynamics.joint.invalidUpperLimit"))
            if (field != value) {
                // make sure the limits are enabled and that the limit has changed
                if (isLimitEnabled) {
                    // wake up the joined bodies
                    body1.setAsleep(false)
                    body2!!.setAsleep(false)
                    // reset the limit impulse
                    impulse.z = 0.0
                }
                // set the new value
                field = value
            }
        }

    /** The lower limit in meters  */
    var lowerLimit = 0.0
        set(value) {
            // check for valid value
            if (value > upperLimit) throw IllegalArgumentException(getString("dynamics.joint.invalidLowerLimit"))
            if (field != value) {
                // make sure the limits are enabled and that the limit has changed
                if (isLimitEnabled) {
                    // wake up the joined bodies
                    body1.setAsleep(false)
                    body2.setAsleep(false)
                    // reset the limit impulse
                    impulse.z = 0.0
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
     *
     *
     * This can also be used to override the initial angle between the bodies.
     * @param angle the reference angle
     * @see .getReferenceAngle
     * @since 3.0.1
     */
    /** The initial angle between the two [Body]s  */
    var referenceAngle: Double
    // internal
    /** The axis representing the allowed line of motion  */
    private val xAxis: Vector2

    /** The perpendicular axis of the line of motion  */
    private val yAxis: Vector2
    // current state
    /**
     * Returns the current state of the limit.
     * @return [LimitState]
     * @since 3.2.0
     */
    /** The current state of the limit  */
    var limitState: LimitState
        private set

    /** The constraint mass; K = J * Minv * Jtrans  */
    private val K: Matrix33

    /** The mass of the motor  */
    private var motorMass = 0.0
    // pre-computed values for J, recalculated each time step
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
    private val impulse: Vector3

    /** The impulse applied by the motor  */
    private var motorImpulse = 0.0

    /**
     * Minimal constructor.
     * @param body1 the first [Body]
     * @param body2 the second [Body]
     * @param anchor the anchor point in world coordinates
     * @param axis the axis of allowed motion
     * @throws NullPointerException if body1, body2, anchor or axis is null
     * @throws IllegalArgumentException if body1 == body2
     */

    constructor(body1: Body, body2: Body, anchor: Vector2, axis: Vector2) : super(body1, body2) {
        // verify the bodies are not the same instanceø
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
        // get the initial rotation
        referenceAngle = body1.transform.rotationAngle - body2.transform.rotationAngle
        // initialize
        K = Matrix33()
        impulse = Vector3()
        isLimitEnabled = false
        isMotorEnabled = false
        limitState = LimitState.INACTIVE
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("PrismaticJoint[").append(super.toString())
            .append("|Anchor=").append(anchor1)
            .append("|Axis=").append(getAxis())
            .append("|IsMotorEnabled=").append(isMotorEnabled)
            .append("|MotorSpeed=").append(motorSpeed)
            .append("|MaximumMotorForce=").append(maximumMotorForce)
            .append("|ReferenceAngle=").append(referenceAngle)
            .append("|IsLimitEnabled=").append(isLimitEnabled)
            .append("|LowerLimit=").append(lowerLimit)
            .append("|UpperLimit=").append(upperLimit)
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
        val linearTolerance = settings!!.getLinearTolerance()
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
        val d =
            body1.worldCenter.sum(r1).subtract(body2.worldCenter.sum(r2))
        axis = body2.getWorldVector(xAxis)
        perp = body2.getWorldVector(yAxis)

        // compute the K matrix
        // s1 = r1.cross(perp)
        // s2 = (r2 + d).cross(perp)
        // a1 = r1.cross(axis)
        // a2 = (r2 + d).cross(axis)
        s1 = r1.cross(perp!!)
        s2 = r2.sum(d).cross(perp!!)
        a1 = r1.cross(axis!!)
        a2 = r2.sum(d).cross(axis!!)
        K.m00 = invM1 + invM2 + s1 * s1 * invI1 + s2 * s2 * invI2
        K.m01 = s1 * invI1 + s2 * invI2
        K.m02 = s1 * a1 * invI1 + s2 * a2 * invI2
        K.m10 = K.m01
        K.m11 = invI1 + invI2
        // handle prismatic constraint between two fixed rotation bodies
        if (K.m11 <= Epsilon.E) K.m11 = 1.0
        K.m12 = a1 * invI1 + a2 * invI2
        K.m20 = K.m02
        K.m21 = K.m12
        K.m22 = invM1 + invM2 + a1 * a1 * invI1 + a2 * a2 * invI2

        // compute the motor mass
        motorMass = K.m22
        if (abs(motorMass) > Epsilon.E) {
            motorMass = 1.0 / motorMass
        }

        // check if the motor is still enabled
        if (!isMotorEnabled) {
            // if not then make the current motor impulse zero
            motorImpulse = 0.0
        }

        // is the limit enabled
        if (isLimitEnabled) {
            // determine the current state of the limit
            val dist = axis!!.dot(d)
            if (abs(upperLimit - lowerLimit) < 2.0 * linearTolerance) {
                // if the limits are close enough then they are basically equal
                limitState = LimitState.EQUAL
            } else if (dist <= lowerLimit) {
                // if the current distance along the axis is less than the limit
                // then the joint is at the lower limit
                // check if its already at the lower limit
                if (limitState !== LimitState.AT_LOWER) {
                    // if its not already at the lower limit then
                    // set the state and clear the impulse for the
                    // joint limit
                    limitState = LimitState.AT_LOWER
                    impulse.z = 0.0
                }
            } else if (dist >= upperLimit) {
                // if the current distance along the axis is greater than the limit
                // then the joint is at the upper limit
                // check if its already at the upper limit
                if (limitState !== LimitState.AT_UPPER) {
                    // if its not already at the upper limit then
                    // set the state and clear the impulse for the
                    // joint limit
                    limitState = LimitState.AT_UPPER
                    impulse.z = 0.0
                }
            } else {
                // otherwise the joint is currently within the limits
                // so set the limit to inactive
                limitState = LimitState.INACTIVE
                impulse.z = 0.0
            }
        } else {
            limitState = LimitState.INACTIVE
            impulse.z = 0.0
        }

        // warm start
        // account for variable time step
        impulse.multiply(step!!.deltaTimeRatio)
        motorImpulse *= step.deltaTimeRatio

        // compute the applied impulses
        // Pc = Jtrans * lambda

        // where Jtrans = |  perp   axis | excluding rotational elements
        //                | -perp  -axis |
        // we only compute the impulse for body1 since body2's impulse is
        // just the negative of body1's impulse
        val P = Vector2()
        // perp.product(impulse.x) + axis.product(motorImpulse + impulse.z)
        P.x = perp!!.x * impulse.x + (motorImpulse + impulse.z) * axis!!.x
        P.y = perp!!.y * impulse.x + (motorImpulse + impulse.z) * axis!!.y

        // where Jtrans = |  s1   a1 | excluding linear elements
        //                |   1    1 |
        //                | -s2  -a2 |
        val l1 =
            impulse.x * s1 + impulse.y + (motorImpulse + impulse.z) * a1
        val l2 =
            impulse.x * s2 + impulse.y + (motorImpulse + impulse.z) * a2

        // apply the impulses
        body1.getLinearVelocity()!!.add(P.product(invM1))
        body1.angularVelocity = (body1.angularVelocity + invI1 * l1)
        body2.getLinearVelocity()!!.subtract(P.product(invM2))
        body2.angularVelocity = (body2.angularVelocity - invI2 * l2)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solveVelocityConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solveVelocityConstraints(
        step: Step,
        settings: Settings
    ) {
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

        // solve the motor constraint
        if (isMotorEnabled && limitState !== LimitState.EQUAL) {
            // compute Jv + b
            val Cdt = axis!!.dot(v1!!.difference(v2!!)) + a1 * w1 - a2 * w2
            // compute lambda = Kinv * (Jv + b)
            var impulse = motorMass * (motorSpeed - Cdt)
            // clamp the impulse between the max force
            val oldImpulse = motorImpulse
            val maxImpulse = maximumMotorForce * step!!.deltaTime
            motorImpulse = Interval.clamp(motorImpulse + impulse, -maxImpulse, maxImpulse)
            impulse = motorImpulse - oldImpulse

            // apply the impulse
            val P = axis!!.product(impulse)
            val l1 = impulse * a1
            val l2 = impulse * a2
            v1.add(P.product(invM1))
            w1 += l1 * invI1
            v2.subtract(P.product(invM2))
            w2 -= l2 * invI2
        }

        // solve the linear and angular constraint (excluding the limit)
        val Cdt = Vector2()
        Cdt.x = perp!!.dot(v1!!.difference(v2!!)) + s1 * w1 - s2 * w2
        Cdt.y = w1 - w2

        // is the limit enabled?
        if (isLimitEnabled && limitState !== LimitState.INACTIVE) {
            // solve the linear and angular constraints with the limit constraint
            val Cdtl = axis!!.dot(v1.difference(v2)) + a1 * w1 - a2 * w2
            val b = Vector3(Cdt.x, Cdt.y, Cdtl)
            // solve for the impulse
            var impulse = K.solve33(b.negate())
            // save the previous impulse
            val f1 = this.impulse.copy()
            // add the impulse to the accumulated impulse
            this.impulse.add(impulse)

            // check the limit state
            if (limitState === LimitState.AT_LOWER) {
                // if the joint is at the lower limit then clamp
                // the accumulated impulse applied by the limit constraint
                this.impulse.z = max(this.impulse.z, 0.0)
            } else if (limitState === LimitState.AT_UPPER) {
                // if the joint is at the upper limit then clamp
                // the accumulated impulse applied by the limit constraint
                this.impulse.z = min(this.impulse.z, 0.0)
            }

            // solve for the corrected impulse
            val f2_1 = Cdt.negate()
                .difference(Vector2(K.m02, K.m12).multiply(this.impulse.z - f1.z))
            val f2r = K.solve22(f2_1).add(f1.x, f1.y)
            this.impulse.x = f2r.x
            this.impulse.y = f2r.y

            // only apply the impulse found in this iteration (given clamping)
            impulse = this.impulse.difference(f1)

            // compute the applied impulses
            // Pc = Jtrans * lambda

            // where Jtrans = |  perp   axis | excluding rotational elements
            //                | -perp  -axis |
            // we only compute the impulse for body1 since body2's impulse is
            // just the negative of body1's impulse
            val P = Vector2()
            // perp.product(impulse.x) + axis.product(impulse.y)
            P.x = perp!!.x * impulse.x + impulse.z * axis!!.x
            P.y = perp!!.y * impulse.x + impulse.z * axis!!.y

            // where Jtrans = |  s1   a1 | excluding linear elements
            //                |   1    1 |
            //                | -s2  -a2 |
            val l1 = impulse.x * s1 + impulse.y + impulse.z * a1
            val l2 = impulse.x * s2 + impulse.y + impulse.z * a2
            v1.add(P.product(invM1))
            w1 += l1 * invI1
            v2.subtract(P.product(invM2))
            w2 -= l2 * invI2
        } else {
            // otherwise just solve the linear and angular constraints
            val f2r = K.solve22(Cdt.negate())
            impulse.x += f2r.x
            impulse.y += f2r.y

            // compute the applied impulses
            // Pc = Jtrans * lambda
            val P = perp!!.product(f2r.x)
            val l1 = f2r.x * s1 + f2r.y
            val l2 = f2r.x * s2 + f2r.y
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
    override fun solvePositionConstraints(
        step: Step,
        settings: Settings
    ): Boolean {
        val maxLinearCorrection = settings!!.getMaximumLinearCorrection()
        val linearTolerance = settings.getLinearTolerance()
        val angularTolerance = settings.getAngularTolerance()
        val t1 = body1.transform
        val t2 = body2!!.transform
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
        val C = Vector2()
        C.x = perp!!.dot(d)
        C.y = t1.rotationAngle - t2.rotationAngle - referenceAngle
        var Cz = 0.0
        var linearError = 0.0
        var angularError = 0.0
        var limitActive = false

        // check if the limit is enabled
        if (isLimitEnabled) {
            // compute a1 and a2
            a1 = r1.cross(axis!!)
            a2 = r2.sum(d).cross(axis!!)

            // what's the current distance
            val dist = axis!!.dot(d)
            // check for equal limits
            if (abs(upperLimit - lowerLimit) < 2.0 * linearTolerance) {
                // then apply the limit and clamp it
                Cz = Interval.clamp(dist, -maxLinearCorrection, maxLinearCorrection)
                linearError = abs(dist)
                limitActive = true
            } else if (dist <= lowerLimit) {
                // if its less than the lower limit then attempt to correct it
                Cz = Interval.clamp(
                    dist - lowerLimit + linearTolerance,
                    -maxLinearCorrection,
                    0.0
                )
                linearError = lowerLimit - dist
                limitActive = true
            } else if (dist >= upperLimit) {
                // if its less than the lower limit then attempt to correct it
                Cz = Interval.clamp(
                    dist - upperLimit - linearTolerance,
                    0.0,
                    maxLinearCorrection
                )
                linearError = dist - upperLimit
                limitActive = true
            }
        }

        // compute the linear constraint
        s1 = r1.cross(perp!!)
        s2 = r2.sum(d).cross(perp!!)

        // compute the overall linear error
        linearError = max(linearError, abs(C.x))
        angularError = abs(C.y)
        val impulse: Vector3
        // check if the limit is active
        if (limitActive) {
            // then solve the linear and angular constraints along with the limit constraint
            K.m00 = invM1 + invM2 + s1 * s1 * invI1 + s2 * s2 * invI2
            K.m01 = s1 * invI1 + s2 * invI2
            K.m02 = s1 * a1 * invI1 + s2 * a2 * invI2
            K.m10 = K.m01
            K.m11 = invI1 + invI2
            // handle prismatic constraint between two fixed rotation bodies
            if (K.m11 <= Epsilon.E) K.m11 = 1.0
            K.m12 = a1 * invI1 + a2 * invI2
            K.m20 = K.m02
            K.m21 = K.m12
            K.m22 = invM1 + invM2 + a1 * a1 * invI1 + a2 * a2 * invI2
            val Clim = Vector3(C.x, C.y, Cz)
            impulse = K.solve33(Clim.negate())
        } else {
            // then solve just the linear and angular constraints
            K.m00 = invM1 + invM2 + s1 * s1 * invI1 + s2 * s2 * invI2
            K.m01 = s1 * invI1 + s2 * invI2
            K.m02 = 0.0
            K.m10 = K.m01
            K.m11 = invI1 + invI2
            // handle prismatic constraint between two fixed rotation bodies
            if (K.m11 <= Epsilon.E) K.m11 = 1.0
            K.m12 = 0.0
            K.m20 = 0.0
            K.m21 = 0.0
            K.m22 = 0.0
            val impulsec = K.solve22(C.negate())
            impulse = Vector3(impulsec.x, impulsec.y, 0.0)
        }

        // compute the applied impulses
        // Pc = Jtrans * lambda

        // where Jtrans = |  perp   axis | excluding rotational elements
        //                | -perp  -axis |
        // we only compute the impulse for body1 since body2's impulse is
        // just the negative of body1's impulse
        val P = Vector2()
        // perp.product(impulse.x) + axis.product(impulse.y)
        P.x = perp!!.x * impulse.x + impulse.z * axis!!.x
        P.y = perp!!.y * impulse.x + impulse.z * axis!!.y

        // where Jtrans = |  s1   a1 | excluding linear elements
        //                |   1    1 |
        //                | -s2  -a2 |
        val l1 = impulse.x * s1 + impulse.y + impulse.z * a1
        val l2 = impulse.x * s2 + impulse.y + impulse.z * a2

        // apply the impulse
        body1.translate(P.product(invM1))
        body1.rotateAboutCenter(l1 * invI1)
        body2.translate(P.product(-invM2))
        body2.rotateAboutCenter(-l2 * invI2)

        // return if we corrected the error enough
        return linearError <= linearTolerance && angularError <= angularTolerance
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
        val force = Vector2()
        // compute the impulse
        force.x = impulse.x * perp!!.x + (motorImpulse + impulse.z) * axis!!.x
        force.y = impulse.x * perp!!.y + (motorImpulse + impulse.z) * axis!!.y
        // multiply by invdt to obtain the force
        force.multiply(invdt)
        return force
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionTorque(double)
	 */
    override fun getReactionTorque(invdt: Double): Double = invdt * impulse.y

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
    val jointSpeed: Double
        get() {
            val t1 = body1.transform
            val t2 = body2.transform
            val c1 = body1.worldCenter
            val c2 = body2.worldCenter
            val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
            val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))
            val d = c1.sum(r1).subtract(c2.sum(r2))
            val axis = body2.getWorldVector(xAxis)
            val v1 = body1.getLinearVelocity()
            val v2 = body2.getLinearVelocity()
            val w1 = body1.angularVelocity
            val w2 = body2.angularVelocity
            return d.dot(axis.cross(w2)) + axis.dot(v1.sum(r1.cross(w1)).subtract(v2.sum(r2.cross(w2))))
        }

    /**
     * Returns the current joint translation.
     * @return double
     */
    val jointTranslation: Double
        get() {
            val p1 = body1.getWorldPoint(localAnchor1)
            val p2 = body2.getWorldPoint(localAnchor2)
            val d = p2.difference(p1)
            val axis = body2.getWorldVector(xAxis)
            return d.dot(axis)
        }

    /**
     * Returns the applied motor force.
     * @param invdt the inverse delta time
     * @return double
     */
    fun getMotorForce(invdt: Double): Double {
        return motorImpulse * invdt
    }

    /**
     * Sets the upper and lower limits.
     *
     *
     * The lower limit must be less than or equal to the upper limit.
     * @param lowerLimit the lower limit in meters
     * @param upperLimit the upper limit in meters
     * @throws IllegalArgumentException if lowerLimit is greater than upperLimit
     */
    fun setLimits(lowerLimit: Double, upperLimit: Double) {
        if (lowerLimit > upperLimit) throw IllegalArgumentException(getString("dynamics.joint.invalidLimits"))
        // make sure the limits are enabled and that the limit has changed
        if (this.lowerLimit != lowerLimit || this.upperLimit != upperLimit) {
            if (isLimitEnabled) {
                // wake up the bodies
                body1.setAsleep(false)
                body2.setAsleep(false)
            }
            // reset the limit impulse
            impulse.z = 0.0
            // set the values
            this.upperLimit = upperLimit
            this.lowerLimit = lowerLimit
        }
    }

    /**
     * Sets the upper and lower limits and enables the limits.
     *
     *
     * The lower limit must be less than or equal to the upper limit.
     * @param lowerLimit the lower limit in meters
     * @param upperLimit the upper limit in meters
     * @throws IllegalArgumentException if lowerLimit is greater than upperLimit
     * @since 2.2.2
     */
    fun setLimitsEnabled(lowerLimit: Double, upperLimit: Double) {
        if (lowerLimit > upperLimit) throw IllegalArgumentException(getString("dynamics.joint.invalidLimits"))
        // enable the limits
        isLimitEnabled = true
        // set the limits
        setLimits(lowerLimit, upperLimit)
        // NOTE: one of these will wake the bodies
    }

    /**
     * Returns the axis in which the joint is allowed move along in world coordinates.
     * @return [Vector2]
     * @since 3.0.0
     */
    fun getAxis(): Vector2 = body2.getWorldVector(xAxis)

}