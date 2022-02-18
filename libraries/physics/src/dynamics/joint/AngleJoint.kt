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
import kotlin.math.PI
import kotlin.math.abs

/**
 * Implementation of an angle joint.
 *
 *
 * A angle joint constrains the relative rotation of two bodies.  The bodies
 * will continue to translate freely.
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
 * The angle joint also allows a ratio value that allow the bodies to rotate at
 * a specified value relative to the other.  This can be used to simulate gears.
 *
 *
 * Since the AngleJoint class defaults the upper and lower limits to the same
 * value and by default the limits are enabled, you will need to modify the
 * limits, or disable the limit to see the effect of the ratio.
 *
 *
 * When the angle between the bodies reaches a limit, and limits are enabled,
 * the ratio will be turned off.
 *
 *
 * NOTE: The [.getAnchor1] and [.getAnchor2] methods return
 * the world space center points for the joined bodies.  This constraint
 * doesn't need anchor points.
 * @author William Bittle
 * @version 3.4.1
 * @since 2.2.2
 * @see [Documentation](http://www.dyn4j.org/documentation/joints/.Angle_Joint)
 *
 * @see [Angle Constraint](http://www.dyn4j.org/2010/12/angle-constraint/)
 */
class AngleJoint(body1: Body, body2: Body) : Joint(body1, body2), Shiftable, DataContainer {
    /**
     * Returns the angular velocity ratio between the two bodies.
     * @return double
     * @since 3.1.0
     */
    /**
     * Sets the angular velocity ratio between the two bodies.
     *
     *
     * To disable the ratio and fix their velocities set the ratio to 1.0.
     *
     *
     * The ratio can be negative to reverse the direction of the velocity
     * of the other body.
     * @param ratio the ratio
     * @since 3.1.0
     */
    /** The angular velocity ratio  */
    var ratio: Double

    /** The lower limit  */
    var lowerLimit: Double = 0.0
        set(value) {
            if (value > upperLimit) throw IllegalArgumentException(message("dynamics.joint.invalidLowerLimit"))
            if (field != value) {
                if (isLimitEnabled) {
                    // wake up both bodies
                    this.body1.setAsleep(false)
                    this.body2.setAsleep(false)
                }
                // set the new target angle
                field = value
            }
        }

    /** The upper limit  */
    var upperLimit: Double = 0.0
        set(value) {
            // make sure the minimum is less than or equal to the maximum
            if (value < lowerLimit) throw IllegalArgumentException(message("dynamics.joint.invalidUpperLimit"))
            if (field != value) {
                if (isLimitEnabled) {
                    // wake up both bodies
                    this.body1.setAsleep(false)
                    this.body2.setAsleep(false)
                }
                // set the new target angle
                field = value
            }
        }

    /** Whether the limits are enabled  */
    var isLimitEnabled: Boolean = false
        set(value) {
            // only wake the bodies if the flag changes
            if (field != value) {
                // wake up both bodies
                this.body1.setAsleep(false)
                this.body2.setAsleep(false)
                // set the flag
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
     * See the class documentation for more details.
     * @param angle the reference angle in radians
     * @see .getReferenceAngle
     * @since 3.0.1
     */
    /** The initial angle between the two bodies  */
    var referenceAngle: Double
    // current state
    /** The current state of the joint limits  */
    private var limitState: LimitState

    /** The inverse effective mass  */
    private var invK = 0.0
    // output
    /** The impulse applied to reduce angular motion  */
    private var impulse: Double

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("AngleJoint[").append(super.toString())
            .append("|Ratio=").append(ratio)
            .append("|LowerLimit=").append(lowerLimit)
            .append("|UpperLimit=").append(upperLimit)
            .append("|IsLimitEnabled=").append(isLimitEnabled)
            .append("|ReferenceAngle=").append(referenceAngle)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#initializeConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun initializeConstraints(step: Step, settings: Settings) {
        val angularTolerance: Double = settings.getAngularTolerance()
        val m1: Mass = this.body1.mass!!
        val m2: Mass = this.body2.mass!!
        val invI1: Double = m1.inverseInertia
        val invI2: Double = m2.inverseInertia

        // check if the limits are enabled
        if (isLimitEnabled) {
            // compute the current angle
            val angle = relativeRotation

            // if they are enabled check if they are equal
            if (abs(upperLimit - lowerLimit) < 2.0 * angularTolerance) {
                // if so then set the state to equal
                limitState = LimitState.EQUAL
            } else {
                // make sure we have valid settings
                if (upperLimit > lowerLimit) {
                    // check against the max and min distances
                    if (angle >= upperLimit) {
                        // is the limit already at the upper limit
                        if (limitState !== LimitState.AT_UPPER) {
                            impulse = 0.0
                        }
                        // set the state to at upper
                        limitState = LimitState.AT_UPPER
                    } else if (angle <= lowerLimit) {
                        // is the limit already at the lower limit
                        if (limitState !== LimitState.AT_LOWER) {
                            impulse = 0.0
                        }
                        // set the state to at lower
                        limitState = LimitState.AT_LOWER
                    } else {
                        // set the state to inactive
                        limitState = LimitState.INACTIVE
                        impulse = 0.0
                    }
                }
            }
        } else {
            // neither is enabled so no constraint needed at this time
            limitState = LimitState.INACTIVE
            impulse = 0.0
        }

        // compute the mass
        if (limitState === LimitState.INACTIVE) {
            // compute the angular mass including the ratio
            invK = invI1 + ratio * ratio * invI2
        } else {
            // compute the angular mass normally
            invK = invI1 + invI2
        }
        if (invK > Epsilon.E) {
            invK = 1.0 / invK
        }

        // account for variable time step
        impulse *= step.deltaTimeRatio

        // warm start
        this.body1.angularVelocity = (this.body1.angularVelocity + invI1 * impulse)
        // we only want to apply the ratio to the impulse if the limits are not active.  When the
        // limits are active we effectively disable the ratio
        this.body2.angularVelocity = (this.body2.angularVelocity - invI2 * impulse * if (limitState === LimitState.INACTIVE) ratio else 1.0)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solveVelocityConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solveVelocityConstraints(step: Step, settings: Settings) {
        val m1: Mass = this.body1.mass!!
        val m2: Mass = this.body2.mass!!
        val invI1: Double = m1.inverseInertia
        val invI2: Double = m2.inverseInertia

        // check if the limit needs to be applied (if we are at one of the limits
        // then we ignore the ratio)
        if (limitState !== LimitState.INACTIVE) {
            // solve the angular constraint
            // get the relative velocity
            val C: Double = this.body1.angularVelocity - this.body2.angularVelocity
            // get the impulse required to obtain the speed
            var impulse = invK * -C
            if (limitState === LimitState.EQUAL) {
                this.impulse += impulse
            } else if (limitState === LimitState.AT_LOWER) {
                val newImpulse = this.impulse + impulse
                if (newImpulse < 0.0) {
                    impulse = -this.impulse
                    this.impulse = 0.0
                }
            } else if (limitState === LimitState.AT_UPPER) {
                val newImpulse = this.impulse + impulse
                if (newImpulse > 0.0) {
                    impulse = -this.impulse
                    this.impulse = 0.0
                }
            }

            // apply the impulse
            this.body1.angularVelocity = (this.body1.angularVelocity + invI1 * impulse)
            this.body2.angularVelocity = (this.body2.angularVelocity - invI2 * impulse)
        } else if (ratio != 1.0) {
            // the limit is inactive and the ratio is not one
            // get the relative velocity
            val C: Double = this.body1.angularVelocity - ratio * this.body2.angularVelocity
            // get the impulse required to obtain the speed
            val impulse = invK * -C

            // apply the impulse
            this.body1.angularVelocity = (this.body1.angularVelocity + invI1 * impulse)
            this.body2.angularVelocity = (this.body2.angularVelocity - invI2 * impulse * ratio)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solvePositionConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solvePositionConstraints(step: Step, settings: Settings): Boolean {
        // check if the constraint needs to be applied
        return if (limitState !== LimitState.INACTIVE) {
            val angularTolerance: Double = settings.getAngularTolerance()
            val maxAngularCorrection: Double = settings.getMaximumAngularCorrection()
            val m1: Mass = this.body1.mass!!
            val m2: Mass = this.body2.mass!!
            val invI1: Double = m1.inverseInertia
            val invI2: Double = m2.inverseInertia

            // get the current angle between the bodies
            val angle = relativeRotation
            var impulse = 0.0
            var angularError = 0.0
            // check the limit state
            if (limitState === LimitState.EQUAL) {
                // if the limits are equal then clamp the impulse to maintain
                // the constraint between the maximum
                val j: Double =
                    Interval.clamp(angle - lowerLimit, -maxAngularCorrection, maxAngularCorrection)
                impulse = -j * invK
                angularError = abs(j)
            } else if (limitState === LimitState.AT_LOWER) {
                // if the joint is at the lower limit then clamp only the lower value
                var j = angle - lowerLimit
                angularError = -j
                j = Interval.clamp(j + angularTolerance, -maxAngularCorrection, 0.0)
                impulse = -j * invK
            } else if (limitState === LimitState.AT_UPPER) {
                // if the joint is at the upper limit then clamp only the upper value
                var j = angle - upperLimit
                angularError = j
                j = Interval.clamp(j - angularTolerance, 0.0, maxAngularCorrection)
                impulse = -j * invK
            }

            // apply the corrective impulses to the bodies
            this.body1.rotateAboutCenter(invI1 * impulse)
            this.body2.rotateAboutCenter(-invI2 * impulse)
            angularError <= angularTolerance
        } else {
            true
        }
    }

    /**
     * Returns the relative angle between the two bodies given the reference angle.
     * @return double
     */
    private val relativeRotation: Double
        private get() {
            var rr: Double = this.body1.transform.rotationAngle - this.body2.transform
                .rotationAngle - referenceAngle
            if (rr < -PI) rr += Geometry.TWO_PI
            if (rr > PI) rr -= Geometry.TWO_PI
            return rr
        }

    /**
     * {@inheritDoc}
     *
     *
     * Not applicable to this joint.
     * This method returns the first body's world center.
     */
    override val anchor1: Vector2
        get() = this.body1.worldCenter

    /**
     * {@inheritDoc}
     *
     *
     * Not applicable to this joint.
     * This method returns the second body's world center.
     */
    override val anchor2: Vector2
        get() = this.body2.worldCenter

    /**
     * {@inheritDoc}
     *
     *
     * Not applicable to this joint. Returns a new zero [Vector2].
     */
    override fun getReactionForce(invdt: Double): Vector2 {
        return Vector2()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionTorque(double)
	 */
    override fun getReactionTorque(invdt: Double): Double {
        return impulse * invdt
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shiftable#shift(org.dyn4j.geometry.Vector2)
	 */
    override fun shift(shift: Vector2) {
        // nothing to do here since there are no anchor points
    }

    /**
     * Returns the relative angle between the two [Body]s in radians in the range [-, ].
     * @return double
     * @since 3.1.0
     */
    val jointAngle: Double
        get() = relativeRotation

    /**
     * Sets both the lower and upper limits.
     *
     *
     * See the class documentation for more details on the limit ranges.
     * @param lowerLimit the lower limit in radians
     * @param upperLimit the upper limit in radians
     * @throws IllegalArgumentException if lowerLimit is greater than upperLimit
     */
    fun setLimits(lowerLimit: Double, upperLimit: Double) {
        // make sure the min < max
        if (lowerLimit > upperLimit) throw IllegalArgumentException(message("dynamics.joint.invalidLimits"))
        if (this.lowerLimit != lowerLimit || this.upperLimit != upperLimit) {
            if (isLimitEnabled) {
                // wake up the bodies
                this.body1.setAsleep(false)
                this.body2.setAsleep(false)
            }
            // set the limits
            this.upperLimit = upperLimit
            this.lowerLimit = lowerLimit
        }
    }

    /**
     * Sets both the lower and upper limits and enables them.
     *
     *
     * See the class documentation for more details on the limit ranges.
     * @param lowerLimit the lower limit in radians
     * @param upperLimit the upper limit in radians
     * @throws IllegalArgumentException if lowerLimit is greater than upperLimit
     */
    fun setLimitsEnabled(lowerLimit: Double, upperLimit: Double) {
        // enable the limits
        isLimitEnabled = (true)
        // set the values
        this.setLimits(lowerLimit, upperLimit)
    }

    /**
     * Sets both the lower and upper limits to the given limit.
     *
     *
     * See the class documentation for more details on the limit ranges.
     * @param limit the desired limit
     */
    fun setLimits(limit: Double) {
        if (lowerLimit != limit || upperLimit != limit) {
            if (isLimitEnabled) {
                // wake up the bodies
                this.body1.setAsleep(false)
                this.body2.setAsleep(false)
            }
            // set the limits
            upperLimit = limit
            lowerLimit = limit
        }
    }

    /**
     * Sets both the lower and upper limits to the given limit and enables them.
     *
     *
     * See the class documentation for more details on the limit ranges.
     * @param limit the desired limit
     */
    fun setLimitsEnabled(limit: Double) {
        this.setLimitsEnabled(limit, limit)
    }

    /**
     * Minimal constructor.
     * @param body1 the first [Body]
     * @param body2 the second [Body]
     * @throws NullPointerException if body1 or body2 is null
     * @throws IllegalArgumentException if body1 == body2
     */
    init {
        // default no collision allowed
        // verify the bodies are not the same instance
        if (body1 === body2) throw IllegalArgumentException(message("dynamics.joint.sameBody"))
        // initialize
        ratio = 1.0
        impulse = 0.0
        // compute the reference angle
        referenceAngle = body1.transform.rotationAngle - body2.transform.rotationAngle
        // set both limits
        upperLimit = referenceAngle
        lowerLimit = referenceAngle
        // set enabled
        isLimitEnabled = true
        // default the limit state
        limitState = LimitState.EQUAL
    }

}