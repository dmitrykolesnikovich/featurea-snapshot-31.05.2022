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
import org.dyn4j.geometry.Interval
import org.dyn4j.geometry.Shiftable
import org.dyn4j.geometry.Vector2
import org.dyn4j.resources.Messages.getString
import kotlin.math.abs

/**
 * Implementation a maximum and/or minimum length distance joint.
 *
 *
 * A rope joint contains the distance between two bodies.  The bodies can
 * rotate freely about the anchor points.  The system as a whole can rotate and
 * translate freely as well.
 *
 *
 * This joint is like the [DistanceJoint], but includes an upper and
 * lower limit and does not include a spring-damper system.
 *
 *
 * By default the lower and upper limits are set to the current distance
 * between the given anchor points and will function identically like a
 * [DistanceJoint].  The upper and lower limits can be enabled
 * separately.
 * @author William Bittle
 * @version 3.4.1
 * @since 2.2.1
 * @see [Documentation](http://www.dyn4j.org/documentation/joints/.Rope_Joint)
 *
 * @see [Distance Constraint](http://www.dyn4j.org/2010/09/distance-constraint/)
 *
 * @see [Max Distance Constraint](http://www.dyn4j.org/2010/12/max-distance-constraint/)
 */
class RopeJoint: Joint, Shiftable, DataContainer {
    /** The local anchor point on the first [Body]  */
    var localAnchor1: Vector2

    /** The local anchor point on the second [Body]  */
    var localAnchor2: Vector2

    /** The maximum distance between the two world space anchor points  */
    var upperLimit: Double = 0.0
        set(value) {
            // make sure the distance is greater than zero
            if (value < 0.0) throw IllegalArgumentException(getString("dynamics.joint.rope.lessThanZeroUpperLimit"))
            // make sure the minimum is less than or equal to the maximum
            if (value < lowerLimit) throw IllegalArgumentException(getString("dynamics.joint.invalidUpperLimit"))
            if (field != value) {
                // make sure its changed and enabled before waking the bodies
                if (isUpperLimitEnabled) {
                    // wake up both bodies
                    body1.setAsleep(false)
                    body2!!.setAsleep(false)
                }
                // set the new target distance
                field = value
            }
        }

    /** The minimum distance between the two world space anchor points  */
    var lowerLimit: Double
        set(value) {
            // make sure the distance is greater than zero
            if (value < 0.0) throw IllegalArgumentException(getString("dynamics.joint.rope.lessThanZeroLowerLimit"))
            // make sure the minimum is less than or equal to the maximum
            if (value > upperLimit) throw IllegalArgumentException(getString("dynamics.joint.invalidLowerLimit"))
            if (field!= value) {
                // make sure its changed and enabled before waking the bodies
                if (isLowerLimitEnabled) {
                    // wake up both bodies
                    body1.setAsleep(false)
                    body2!!.setAsleep(false)
                }
                // set the new target distance
                field = value
            }
        }

    /** Whether the maximum distance is enabled  */
    var isUpperLimitEnabled: Boolean
        set(value) {
            if (field != value) {
                // wake up both bodies
                body1.setAsleep(false)
                body2.setAsleep(false)
                // set the flag
                field = value
            }
        }

    /** Whether the minimum distance is enabled  */
    var isLowerLimitEnabled: Boolean
        set(value) {
            if (field != value) {
                // wake up both bodies
                body1.setAsleep(false)
                body2.setAsleep(false)
                // set the flag
                field = value
            }
        }

    // current state
    /** The effective mass of the two body system (Kinv = J * Minv * Jtrans)  */
    private var invK = 0.0

    /** The normal  */
    private var n: Vector2? = null

    /**
     * Returns the current state of the limit.
     * @return [LimitState]
     * @since 3.2.0
     */
    /** The current state of the joint limits  */
    var limitState: LimitState? = null
        private set
    // output
    /** The accumulated impulse from the previous time step  */
    private var impulse = 0.0

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("RopeJoint[").append(super.toString())
            .append("|Anchor1=").append(anchor1)
            .append("|Anchor2=").append(anchor2)
            .append("|IsLowerLimitEnabled=").append(isLowerLimitEnabled)
            .append("|LowerLimit").append(lowerLimit)
            .append("|IsUpperLimitEnabled=").append(isUpperLimitEnabled)
            .append("|UpperLimit=").append(upperLimit)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#initializeConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun initializeConstraints(step: Step, settings: Settings) {
        val linearTolerance = settings!!.getLinearTolerance()
        val t1 = body1.transform
        val t2 = body2!!.transform
        val m1 = body1.mass
        val m2 = body2.mass
        val invM1 = m1!!.inverseMass
        val invM2 = m2!!.inverseMass
        val invI1 = m1.inverseInertia
        val invI2 = m2.inverseInertia

        // compute the normal
        val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
        val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))
        n = r1.sum(body1.worldCenter).subtract(r2.sum(body2.worldCenter))

        // get the current length
        val length = n!!.magnitude
        // check for the tolerance
        if (length < linearTolerance) {
            n!!.zero()
        } else {
            // normalize it
            n!!.multiply(1.0 / length)
        }

        // check if both limits are enabled
        // and get the current state of the limits
        if (isUpperLimitEnabled && isLowerLimitEnabled) {
            // if both are enabled check if they are equal
            if (abs(upperLimit - lowerLimit) < 2.0 * linearTolerance) {
                // if so then set the state to equal
                limitState = LimitState.EQUAL
            } else {
                // make sure we have valid settings
                if (upperLimit > lowerLimit) {
                    // check against the max and min distances
                    if (length > upperLimit) {
                        // set the state to at upper
                        limitState = LimitState.AT_UPPER
                    } else if (length < lowerLimit) {
                        // set the state to at lower
                        limitState = LimitState.AT_LOWER
                    } else {
                        // set the state to inactive
                        limitState = LimitState.INACTIVE
                    }
                }
            }
        } else if (isUpperLimitEnabled) {
            // check the maximum against the current length
            if (length > upperLimit) {
                // set the state to at upper
                limitState = LimitState.AT_UPPER
            } else {
                // no constraint needed at this time
                limitState = LimitState.INACTIVE
            }
        } else if (isLowerLimitEnabled) {
            // check the minimum against the current length
            if (length < lowerLimit) {
                // set the state to at lower
                limitState = LimitState.AT_LOWER
            } else {
                // no constraint needed at this time
                limitState = LimitState.INACTIVE
            }
        } else {
            // neither is enabled so no constraint needed at this time
            limitState = LimitState.INACTIVE
        }

        // check the length to see if we need to apply the constraint
        if (limitState !== LimitState.INACTIVE) {
            // compute K inverse
            val cr1n = r1.cross(n!!)
            val cr2n = r2.cross(n!!)
            var invMass = invM1 + invI1 * cr1n * cr1n
            invMass += invM2 + invI2 * cr2n * cr2n

            // check for zero before inverting
            invK = if (invMass <= Epsilon.E) 0.0 else 1.0 / invMass

            // warm start
            impulse *= step!!.deltaTimeRatio
            val J = n!!.product(impulse)
            body1.getLinearVelocity()!!.add(J.product(invM1))
            body1.angularVelocity = (body1.angularVelocity + invI1 * r1.cross(J))
            body2.getLinearVelocity()!!.subtract(J.product(invM2))
            body2.angularVelocity = (body2.angularVelocity - invI2 * r2.cross(J))
        } else {
            // clear the impulse
            impulse = 0.0
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solveVelocityConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solveVelocityConstraints(step: Step, settings: Settings) {
        // check if the constraint need to be applied
        if (limitState !== LimitState.INACTIVE) {
            val t1 = body1.transform
            val t2 = body2!!.transform
            val m1 = body1.mass
            val m2 = body2.mass
            val invM1 = m1!!.inverseMass
            val invM2 = m2!!.inverseMass
            val invI1 = m1.inverseInertia
            val invI2 = m2.inverseInertia

            // compute r1 and r2
            val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
            val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))

            // compute the relative velocity
            val v1 =
                body1.getLinearVelocity()!!.sum(r1.cross(body1.angularVelocity))
            val v2 =
                body2.getLinearVelocity()!!.sum(r2.cross(body2.angularVelocity))

            // compute Jv
            val Jv = n!!.dot(v1.difference(v2))

            // compute lambda (the magnitude of the impulse)
            val j = -invK * Jv
            impulse += j

            // apply the impulse
            val J = n!!.product(j)
            body1.getLinearVelocity().add(J.product(invM1))
            body1.angularVelocity = (body1.angularVelocity + invI1 * r1.cross(J))
            body2.getLinearVelocity().subtract(J.product(invM2))
            body2.angularVelocity = (body2.angularVelocity - invI2 * r2.cross(J))
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solvePositionConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solvePositionConstraints(step: Step, settings: Settings): Boolean {
        // check if the constraint need to be applied
        return if (limitState !== LimitState.INACTIVE) {
            // if the limits are equal it doesn't matter if we
            // use the maximum or minimum setting
            var targetDistance = upperLimit
            // determine the target distance
            if (limitState === LimitState.AT_LOWER) {
                // use the minimum distance as the target
                targetDistance = lowerLimit
            }
            val linearTolerance = settings!!.getLinearTolerance()
            val maxLinearCorrection = settings.getMaximumLinearCorrection()
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

            // recompute n since it may have changed after integration
            val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
            val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))
            n = r1.sum(body1.worldCenter).subtract(r2.sum(body2.worldCenter))

            // solve the position constraint
            val l = n!!.normalize()
            var C = l - targetDistance
            C = Interval.clamp(C, -maxLinearCorrection, maxLinearCorrection)
            val impulse = -invK * C
            val J = n!!.product(impulse)

            // translate and rotate the objects
            body1.translate(J.product(invM1))
            body1.rotate(invI1 * r1.cross(J), c1)
            body2.translate(J.product(-invM2))
            body2.rotate(-invI2 * r2.cross(J), c2)
            abs(C) < linearTolerance
        } else {
            // if not then just return true that the position constraint is satisfied
            true
        }
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
        return n!!.product(impulse * invdt)
    }

    /**
     * {@inheritDoc}
     *
     *
     * Not applicable to this joint.
     * Always returns zero.
     */
    override fun getReactionTorque(invdt: Double): Double {
        return 0.0
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shiftable#shift(org.dyn4j.geometry.Vector2)
	 */
    override fun shift(shift: Vector2) {
        // nothing to translate here since the anchor points are in local coordinates
        // they will move with the bodies
    }

    /**
     * Sets both the lower and upper limits.
     * @param lowerLimit the lower limit in meters; must be greater than or equal to zero
     * @param upperLimit the upper limit in meters; must be greater than or equal to zero
     * @throws IllegalArgumentException if lowerLimit is less than zero, upperLimit is less than zero, or lowerLimit is greater than upperLimit
     */
    fun setLimits(lowerLimit: Double, upperLimit: Double) {
        // make sure the minimum distance is greater than zero
        if (lowerLimit < 0.0) throw IllegalArgumentException(getString("dynamics.joint.rope.lessThanZeroLowerLimit"))
        // make sure the maximum distance is greater than zero
        if (upperLimit < 0.0) throw IllegalArgumentException(getString("dynamics.joint.rope.lessThanZeroUpperLimit"))
        // make sure the min < max
        if (lowerLimit > upperLimit) throw IllegalArgumentException(getString("dynamics.joint.invalidLimits"))
        if (this.lowerLimit != lowerLimit || this.upperLimit != upperLimit) {
            // make sure one of the limits is enabled and has changed before waking the bodies
            if (isLowerLimitEnabled || isUpperLimitEnabled) {
                // wake up the bodies
                body1.setAsleep(false)
                body2!!.setAsleep(false)
            }
            // set the limits
            this.upperLimit = upperLimit
            this.lowerLimit = lowerLimit
        }
    }

    /**
     * Sets both the lower and upper limits and enables both.
     * @param lowerLimit the lower limit in meters; must be greater than or equal to zero
     * @param upperLimit the upper limit in meters; must be greater than or equal to zero
     * @throws IllegalArgumentException if lowerLimit is less than zero, upperLimit is less than zero, or lowerLimit is greater than upperLimit
     */
    fun setLimitsEnabled(lowerLimit: Double, upperLimit: Double) {
        // enable the limits
        this.setLimitsEnabled(true)
        // set the values
        this.setLimits(lowerLimit, upperLimit)
    }

    /**
     * Enables or disables both the lower and upper limits.
     * @param flag true if both limits should be enabled
     * @since 2.2.2
     */
    fun setLimitsEnabled(flag: Boolean) {
        if (isUpperLimitEnabled != flag || isLowerLimitEnabled != flag) {
            isUpperLimitEnabled = flag
            isLowerLimitEnabled = flag
            // wake up the bodies
            body1.setAsleep(false)
            body2!!.setAsleep(false)
        }
    }

    /**
     * Sets both the lower and upper limits to the given limit.
     *
     *
     * This makes the joint a fixed length joint.
     * @param limit the desired limit
     * @throws IllegalArgumentException if limit is less than zero
     * @since 2.2.2
     */
    fun setLimits(limit: Double) {
        // make sure the distance is greater than zero
        if (limit < 0.0) throw IllegalArgumentException(getString("dynamics.joint.rope.invalidLimit"))
        if (lowerLimit != limit || upperLimit != limit) {
            // make sure one of the limits is enabled and has changed before waking the bodies
            if (isLowerLimitEnabled || isUpperLimitEnabled) {
                // wake up the bodies
                body1.setAsleep(false)
                body2!!.setAsleep(false)
            }
            // set the limits
            upperLimit = limit
            lowerLimit = limit
        }
    }

    /**
     * Sets both the lower and upper limits to the given limit and
     * enables both.
     *
     *
     * This makes the joint a fixed length joint.
     * @param limit the desired limit
     * @throws IllegalArgumentException if limit is less than zero
     * @since 2.2.2
     */
    fun setLimitsEnabled(limit: Double) {
        // enable the limits
        this.setLimitsEnabled(true)
        // set the values
        this.setLimits(limit)
    }

    /**
     * Minimal constructor.
     *
     *
     * Creates a rope joint between the two bodies that acts like a distance joint.
     * @param body1 the first [Body]
     * @param body2 the second [Body]
     * @param anchor1 in world coordinates
     * @param anchor2 in world coordinates
     * @throws NullPointerException if body1, body2, anchor1, or anchor2 is null
     * @throws IllegalArgumentException if body1 == body2
     */
    constructor(body1: Body, body2: Body, anchor1: Vector2, anchor2: Vector2) : super(body1, body2) {
        // verify the bodies are not the same instance
        if (body1 == body2) throw IllegalArgumentException(getString("dynamics.joint.sameBody"))
        // get the local anchor points
        localAnchor1 = body1.getLocalPoint(anchor1)
        localAnchor2 = body2.getLocalPoint(anchor2)
        // default to act like a fixed length distance joint
        isUpperLimitEnabled = true
        isLowerLimitEnabled = true
        // default the limits
        val distance = anchor1.distance(anchor2)
        upperLimit = distance
        lowerLimit = distance
    }
}