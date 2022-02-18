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
import org.dyn4j.geometry.Shiftable
import org.dyn4j.geometry.Vector2
import org.dyn4j.resources.Messages.getString
import kotlin.math.abs

/**
 * Implementation of a pulley joint.
 *
 *
 * A pulley joint joins two bodies in a pulley system with a fixed length zero
 * mass rope.  The bodies are allowed to rotate freely.  The bodies are allowed
 * to translate freely up to the total length of the "rope."
 *
 *
 * The length of the "rope" connecting the two bodies is computed by distance
 * from the pulley anchors to the body anchors including the ratio (if any)
 * when the joint is created.  The length can be changed dynamically by calling
 * the [.setLength] method.
 *
 *
 * The pulley anchor points represent the "hanging" points for the respective
 * bodies and can be any world space point.
 *
 *
 * This joint can also model a block-and-tackle system by setting the ratio
 * using the [.setRatio] method.  A value of 1.0 indicates no
 * ratio.  Values between 0 and 1 exclusive indicate that the first body's
 * rope length will be 1/x times longer than the second body's rope length.
 * Values between 1 and infinity indicate that the second body's rope length
 * will be x times longer than the first body's rope length.
 *
 *
 * By default this joint acts very similar to two [DistanceJoint]s in
 * that the bodies are forced to be their respective rope-distance away from
 * the pulley anchors (i.e. not behaving like a rope).  To have the bodies
 * behave as if connected by flexible rope pass in `true` to the
 * [.setSlackEnabled] method.
 * @author William Bittle
 * @version 3.4.1
 * @since 2.1.0
 * @see [Documentation](http://www.dyn4j.org/documentation/joints/.Pulley_Joint)
 *
 * @see [Pulley Constraint](http://www.dyn4j.org/2010/12/pulley-constraint/)
 */
class PulleyJoint : Joint, Shiftable, DataContainer {
    /**
     * Returns the pulley anchor point for the first [Body]
     * in world coordinates.
     * @return [Vector2]
     */
    /** The world space pulley anchor point for the first [Body]  */
    var pulleyAnchor1: Vector2
        protected set

    /**
     * Returns the pulley anchor point for the second [Body]
     * in world coordinates.
     * @return [Vector2]
     */
    /** The world space pulley anchor point for the second [Body]  */
    var pulleyAnchor2: Vector2
        protected set

    /** The local anchor point on the first [Body]  */
    var localAnchor1: Vector2

    /** The local anchor point on the second [Body]  */
    var localAnchor2: Vector2

    /** The pulley ratio for modeling a block-and-tackle  */
    var ratio: Double = 0.0
        set(value) {
            if (value <= 0.0) throw IllegalArgumentException(getString("dynamics.joint.pulley.invalidRatio"))
            // make sure the ratio changed
            if (value != field) {
                // set the new ratio
                field = value
                // compute the new length
                length = length1 + this.ratio * length2
                // wake up both bodies
                body1.setAsleep(false)
                body2!!.setAsleep(false)
            }
        }

    /**
     * Returns true if slack in the rope is enabled.
     * @return boolean
     * @since 3.1.6
     */
    /**
     * Toggles the slack in the rope.
     *
     *
     * If slack is not enabled the rope length is fixed to the total length of the rope, acting like the [DistanceJoint].
     * @param flag true to enable slack
     * @since 3.1.6
     */
    /** True if slack in the rope is enabled  */
    var isSlackEnabled: Boolean
    // current state
    /**
     * Returns the current state of the limit.
     * @return [LimitState]
     * @since 3.2.0
     */
    /** The state of the limit (only used for slack)  */
    var limitState: LimitState
        private set

    /** The original length of the first side of the pulley  */
    private val length1: Double

    /** The original length of the second side of the pulley  */
    private val length2: Double

    /** The total length of the pulley system  */
    private var length: Double
        set(value) {
            if (value < 0.0) throw IllegalArgumentException(getString("dynamics.joint.pulley.invalidLength"))
            if (field != value) {
                field = value
                // wake up both bodies
                body1.setAsleep(false)
                body2!!.setAsleep(false)
            }
        }

    /** The normal from the first pulley anchor to the first [Body] anchor  */
    private var n1: Vector2? = null

    /** The normal from the second pulley anchor to the second [Body] anchor  */
    private var n2: Vector2? = null

    /** The effective mass of the two body system (Kinv = J * Minv * Jtrans)  */
    private var invK = 0.0
    // output
    /** The accumulated impulse from the previous time step  */
    private var impulse: Double

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("PulleyJoint[").append(super.toString())
            .append("|PulleyAnchor1=").append(pulleyAnchor1)
            .append("|PulleyAnchor2=").append(pulleyAnchor2)
            .append("|Anchor1=").append(anchor1)
            .append("|Anchor2=").append(anchor2)
            .append("|Ratio=").append(ratio)
            .append("|Length=").append(length)
            .append("|SlackEnabled=").append(isSlackEnabled)
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

        // put the body anchors in world space
        val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
        val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))
        val p1 = r1.sum(body1.worldCenter)
        val p2 = r2.sum(body2.worldCenter)
        val s1 = pulleyAnchor1
        val s2 = pulleyAnchor2

        // compute the axes
        n1 = s1.to(p1)
        n2 = s2.to(p2)

        // get the lengths
        val l1 = n1!!.normalize()
        val l2 = n2!!.normalize()

        // get the current total length
        val l = l1 + ratio * l2

        // check if we need to solve the constraint
        if (l > length || !isSlackEnabled) {
            limitState = LimitState.AT_UPPER

            // check for near zero length
            if (l1 <= 10.0 * linearTolerance) {
                // zero out the axis
                n1!!.zero()
            }

            // check for near zero length		
            if (l2 <= 10.0 * linearTolerance) {
                // zero out the axis
                n2!!.zero()
            }

            // compute the inverse effective masses (K matrix, in this case its a scalar) for the constraints
            val r1CrossN1 = r1.cross(n1!!)
            val r2CrossN2 = r2.cross(n2!!)
            val pm1 = invM1 + invI1 * r1CrossN1 * r1CrossN1
            val pm2 = invM2 + invI2 * r2CrossN2 * r2CrossN2
            invK = pm1 + ratio * ratio * pm2
            // make sure we can invert it
            if (invK > Epsilon.E) {
                invK = 1.0 / invK
            } else {
                invK = 0.0
            }

            // warm start the constraints taking
            // variable time steps into account
            val dtRatio = step!!.deltaTimeRatio
            impulse *= dtRatio

            // compute the impulse along the axes
            val J1 = n1!!.product(-impulse)
            val J2 = n2!!.product(-ratio * impulse)

            // apply the impulse
            body1.getLinearVelocity()!!.add(J1.product(invM1))
            body1.angularVelocity = (body1.angularVelocity + invI1 * r1.cross(J1))
            body2.getLinearVelocity()!!.add(J2.product(invM2))
            body2.angularVelocity = (body2.angularVelocity + invI2 * r2.cross(J2))
        } else {
            // clear the impulse and don't solve anything
            impulse = 0.0
            limitState = LimitState.INACTIVE
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solveVelocityConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solveVelocityConstraints(step: Step, settings: Settings) {
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

            // compute Jv + b
            val C = -n1!!.dot(v1) - ratio * n2!!.dot(v2)
            // compute the impulse
            val impulse = invK * -C
            this.impulse += impulse

            // compute the impulse along each axis
            val J1 = n1!!.product(-impulse)
            val J2 = n2!!.product(-impulse * ratio)

            // apply the impulse
            body1.getLinearVelocity()!!.add(J1.product(invM1))
            body1.angularVelocity = (body1.angularVelocity + invI1 * r1.cross(J1))
            body2.getLinearVelocity()!!.add(J2.product(invM2))
            body2.angularVelocity = (body2.angularVelocity + invI2 * r2.cross(J2))
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solvePositionConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solvePositionConstraints(step: Step, settings: Settings): Boolean {
        return if (limitState !== LimitState.INACTIVE) {
            val linearTolerance = settings!!.getLinearTolerance()
            val t1 = body1.transform
            val t2 = body2!!.transform
            val m1 = body1.mass
            val m2 = body2.mass
            val invM1 = m1!!.inverseMass
            val invM2 = m2!!.inverseMass
            val invI1 = m1.inverseInertia
            val invI2 = m2.inverseInertia

            // put the body anchors in world space
            val r1 = t1.getTransformedR(body1.localCenter.to(localAnchor1))
            val r2 = t2.getTransformedR(body2.localCenter.to(localAnchor2))
            val p1 = r1.sum(body1.worldCenter)
            val p2 = r2.sum(body2.worldCenter)
            val s1 = pulleyAnchor1
            val s2 = pulleyAnchor2

            // compute the axes
            n1 = s1.to(p1)
            n2 = s2.to(p2)

            // normalize and save the length
            val l1 = n1!!.normalize()
            val l2 = n2!!.normalize()

            // make sure the length is not near zero
            if (l1 <= 10.0 * linearTolerance) {
                n1!!.zero()
            }
            // make sure the length is not near zero
            if (l2 <= 10.0 * linearTolerance) {
                n2!!.zero()
            }
            var linearError = 0.0

            // recompute K
            val r1CrossN1 = r1.cross(n1!!)
            val r2CrossN2 = r2.cross(n2!!)
            val pm1 = invM1 + invI1 * r1CrossN1 * r1CrossN1
            val pm2 = invM2 + invI2 * r2CrossN2 * r2CrossN2
            invK = pm1 + ratio * ratio * pm2
            // make sure we can invert it
            if (invK > Epsilon.E) {
                invK = 1.0 / invK
            } else {
                invK = 0.0
            }

            // compute the constraint error
            val C = length - l1 - ratio * l2
            linearError = abs(C)

            // clamping the impulse does not work with the limit state
            // clamp the error
//			C = Interval.clamp(C + linearTolerance, -maxLinearCorrection, maxLinearCorrection);
            val impulse = -invK * C

            // compute the impulse along the axes
            val J1 = n1!!.product(-impulse)
            val J2 = n2!!.product(-ratio * impulse)

            // apply the impulse
            body1.translate(J1.x * invM1, J1.y * invM1)
            body1.rotateAboutCenter(r1.cross(J1) * invI1)
            body2.translate(J2.x * invM2, J2.y * invM2)
            body2.rotateAboutCenter(r2.cross(J2) * invI2)
            linearError < linearTolerance
        } else {
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
        get() = body2.getWorldPoint(localAnchor2)

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#getReactionForce(double)
	 */
    override fun getReactionForce(invdt: Double): Vector2? = n2!!.product(impulse * invdt)

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
        // we must move the world space pulley anchors
        pulleyAnchor1.add(shift)
        pulleyAnchor2.add(shift)
    }

    /**
     * Returns the current length from the first pulley anchor point to the
     * anchor point on the first [Body].
     *
     *
     * This is used, in conjunction with length2, to compute the total length
     * when the ratio is changed.
     * @return double
     */
    fun getLength1(): Double {
        // get the body anchor point in world space
        val ba = body1.getWorldPoint(localAnchor1)
        return pulleyAnchor1.distance(ba)
    }

    /**
     * Returns the current length from the second pulley anchor point to the
     * anchor point on the second [Body].
     *
     *
     * This is used, in conjunction with length1, to compute the total length
     * when the ratio is changed.
     * @return double
     */
    fun getLength2(): Double {
        // get the body anchor point in world space
        val ba = body2!!.getWorldPoint(localAnchor2)
        return pulleyAnchor2.distance(ba)
    }

    /**
     * Minimal constructor.
     *
     *
     * Creates a pulley joint between the two given [Body]s using the given anchor points.
     * @param body1 the first [Body]
     * @param body2 the second [Body]
     * @param pulleyAnchor1 the first pulley anchor point
     * @param pulleyAnchor2 the second pulley anchor point
     * @param bodyAnchor1 the first [Body]'s anchor point
     * @param bodyAnchor2 the second [Body]'s anchor point
     * @throws NullPointerException if body1, body2, pulleyAnchor1, pulleyAnchor2, bodyAnchor1, or bodyAnchor2 is null
     * @throws IllegalArgumentException if body1 == body2
     */
    constructor(body1: Body, body2: Body, pulleyAnchor1: Vector2, pulleyAnchor2: Vector2,
                bodyAnchor1: Vector2, bodyAnchor2: Vector2) : super(body1, body2) {
        // verify the bodies are not the same instance
        if (body1 == body2) throw IllegalArgumentException(getString("dynamics.joint.sameBody"))
        // set the pulley anchor points
        this.pulleyAnchor1 = pulleyAnchor1
        this.pulleyAnchor2 = pulleyAnchor2
        // get the local anchor points
        localAnchor1 = body1.getLocalPoint(bodyAnchor1)
        localAnchor2 = body2.getLocalPoint(bodyAnchor2)
        // default the ratio and minimum length
        ratio = 1.0
        // compute the lengths
        length1 = bodyAnchor1.distance(pulleyAnchor1)
        length2 = bodyAnchor2.distance(pulleyAnchor2)
        // compute the lengths
        // length = l1 + ratio * l2
        length = length1 + length2
        // initialize the other fields
        impulse = 0.0
        // initialize the slack parameters
        isSlackEnabled = false
        limitState = LimitState.AT_UPPER
    }
}