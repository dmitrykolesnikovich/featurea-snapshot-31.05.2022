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
import org.dyn4j.resources.Messages
import org.dyn4j.resources.message
import kotlin.math.abs

/**
 * Implementation of a fixed length distance joint.
 *
 *
 * Given the two world space anchor points a distance is computed and used
 * to constrain the attached [Body]s at that distance.  The bodies can rotate
 * freely about the anchor points and the whole system can move and rotate freely, but
 * the distance between the two anchor points is fixed.
 *
 *
 * This joint doubles as a spring/damper distance joint where the length can
 * change but is constantly approaching the target distance.  Enable the
 * spring/damper by setting the frequency and damping ratio to values greater than
 * zero.  A good starting point is a frequency of 8.0 and damping ratio of 0.3
 * then adjust as necessary.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 * @see [Documentation](http://www.dyn4j.org/documentation/joints/.Distance_Joint)
 *
 * @see [Distance Constraint](http://www.dyn4j.org/2010/09/distance-constraint/)
 */
class DistanceJoint(body1: Body, body2: Body, anchor1: Vector2?, anchor2: Vector2?) :
    Joint(body1, body2), Shiftable, DataContainer {

    /** The local anchor point on the first [Body]  */
    var localAnchor1: Vector2

    /** The local anchor point on the second [Body]  */
    var localAnchor2: Vector2

    /** The oscillation frequency in hz  */
    var frequency = 0.0
        set(value) {
            if (value < 0) throw IllegalArgumentException(message("dynamics.joint.invalidFrequency"))
            field = value
        }

    /** The damping ratio  */
    var dampingRatio = 0.0
        set(value) {
            if (value < 0.0 || value > 1.0) throw IllegalArgumentException(message("dynamics.joint.invalidDampingRatio"))
            field = value
        }

    /** The computed distance between the two world space anchor points  */
    var distance: Double = 0.0
        set(value) {
            // make sure the distance is greater than zero
            if (value < 0.0) throw IllegalArgumentException(message("dynamics.joint.distance.invalidDistance"))
            if (field != value) {
                // wake up both bodies
                this.body1.setAsleep(false)
                this.body2.setAsleep(false)
                // set the new target distance
                field = value
            }
        }
    // current state
    /** The effective mass of the two body system (Kinv = J * Minv * Jtrans)  */
    private var invK = 0.0

    /** The normal  */
    private var n: Vector2? = null

    /** The bias for adding work to the constraint (simulating a spring)  */
    private var bias = 0.0

    /** The damping portion of the constraint  */
    private var gamma = 0.0
    // output
    /** The accumulated impulse from the previous time step  */
    private var impulse = 0.0

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("DistanceJoint[").append(super.toString())
            .append("|Anchor1=").append(anchor1)
            .append("|Anchor2=").append(anchor2)
            .append("|Frequency=").append(frequency)
            .append("|DampingRatio=").append(dampingRatio)
            .append("|Distance=").append(distance)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#initializeConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun initializeConstraints(step: Step, settings: Settings) {

        val linearTolerance: Double = settings.getLinearTolerance()
        val t1: Transform = body1.transform
        val t2: Transform = body2.transform
        val m1: Mass = body1.mass!!
        val m2: Mass = body2.mass!!
        val invM1: Double = m1.inverseMass
        val invM2: Double = m2.inverseMass
        val invI1: Double = m1.inverseInertia
        val invI2: Double = m2.inverseInertia

        // compute the normal
        val r1: Vector2 = t1.getTransformedR(this.body1.localCenter.to(localAnchor1))
        val r2: Vector2 = t2.getTransformedR(this.body2.localCenter.to(localAnchor2))
        n = r1.sum(this.body1.worldCenter).subtract(r2.sum(this.body2.worldCenter))
        val n = n!!

        // get the current length
        val length: Double = n.magnitude
        // check for the tolerance
        if (length < linearTolerance) {
            n.zero()
        } else {
            // normalize it
            n.multiply(1.0 / length)
        }

        // compute K inverse
        val cr1n: Double = r1.cross(n)
        val cr2n: Double = r2.cross(n)
        var invMass = invM1 + invI1 * cr1n * cr1n
        invMass += invM2 + invI2 * cr2n * cr2n

        // check for zero before inverting
        invK = if (invMass <= Epsilon.E) 0.0 else 1.0 / invMass

        // see if we need to compute spring damping
        if (frequency > 0.0) {
            val dt: Double = step.deltaTime
            // get the current compression/extension of the spring
            val x = length - distance
            // compute the natural frequency; f = w / (2 * pi) -> w = 2 * pi * f
            val w: Double = Geometry.TWO_PI * frequency
            // compute the damping coefficient; dRatio = d / (2 * m * w) -> d = 2 * m * w * dRatio
            val d = 2.0 * invK * dampingRatio * w
            // compute the spring constant; w = sqrt(k / m) -> k = m * w * w
            val k = invK * w * w

            // compute gamma = CMF = 1 / (hk + d)
            gamma = dt * (d + dt * k)
            // check for zero before inverting
            gamma = if (gamma <= Epsilon.E) 0.0 else 1.0 / gamma
            // compute the bias = x * ERP where ERP = hk / (hk + d)
            bias = x * dt * k * gamma

            // compute the effective mass			
            invMass += gamma
            // check for zero before inverting
            invK = if (invMass <= Epsilon.E) 0.0 else 1.0 / invMass
        } else {
            gamma = 0.0
            bias = 0.0
        }

        // warm start
        impulse *= step.deltaTimeRatio
        val J: Vector2 = n.product(impulse)
        body1.getLinearVelocity().add(J.product(invM1))
        body1.angularVelocity = (body1.angularVelocity + invI1 * r1.cross(J))
        body2.getLinearVelocity().subtract(J.product(invM2))
        body2.angularVelocity = (body2.angularVelocity - invI2 * r2.cross(J))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solveVelocityConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solveVelocityConstraints(step: Step, settings: Settings) {
        val n = n!!

        val t1: Transform = body1.transform
        val t2: Transform = body2.transform
        val m1: Mass = body1.mass!!
        val m2: Mass = body2.mass!!
        val invM1: Double = m1.inverseMass
        val invM2: Double = m2.inverseMass
        val invI1: Double = m1.inverseInertia
        val invI2: Double = m2.inverseInertia

        // compute r1 and r2
        val r1: Vector2 = t1.getTransformedR(this.body1.localCenter.to(localAnchor1))
        val r2: Vector2 = t2.getTransformedR(this.body2.localCenter.to(localAnchor2))

        // compute the relative velocity
        val v1: Vector2 = body1.getLinearVelocity().sum(r1.cross(body1.angularVelocity))
        val v2: Vector2 = body2.getLinearVelocity().sum(r2.cross(body2.angularVelocity))

        // compute Jv
        val Jv: Double = n.dot(v1.difference(v2))

        // compute lambda (the magnitude of the impulse)
        val j = -invK * (Jv + bias + gamma * impulse)
        impulse += j

        // apply the impulse
        val J: Vector2 = n.product(j)
        body1.getLinearVelocity().add(J.product(invM1))
        body1.angularVelocity = (body1.angularVelocity + invI1 * r1.cross(J))
        body2.getLinearVelocity().subtract(J.product(invM2))
        body2.angularVelocity = (body2.angularVelocity - invI2 * r2.cross(J))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.joint.Joint#solvePositionConstraints(org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solvePositionConstraints(step: Step, settings: Settings): Boolean {
        // check if this is a spring damper
        if (frequency > 0.0) {
            // don't solve position constraints for spring damper
            return true
        }
        val linearTolerance: Double = settings.getLinearTolerance()
        val maxLinearCorrection: Double = settings.getMaximumLinearCorrection()
        val t1: Transform = body1.transform
        val t2: Transform = body2.transform
        val m1: Mass = body1.mass!!
        val m2: Mass = body2.mass!!
        val invM1: Double = m1.inverseMass
        val invM2: Double = m2.inverseMass
        val invI1: Double = m1.inverseInertia
        val invI2: Double = m2.inverseInertia
        val c1: Vector2 = body1.worldCenter
        val c2: Vector2 = body2.worldCenter

        // recompute n since it may have changed after integration
        val r1: Vector2 = t1.getTransformedR(this.body1.localCenter.to(localAnchor1))
        val r2: Vector2 = t2.getTransformedR(this.body2.localCenter.to(localAnchor2))
        n = r1.sum(body1.worldCenter).subtract(r2.sum(body2.worldCenter))
        val n = n!!

        // solve the position constraint
        val l: Double = n.normalize()
        var C = l - distance
        C = Interval.clamp(C, -maxLinearCorrection, maxLinearCorrection)
        val impulse = -invK * C
        val J: Vector2 = n.product(impulse)

        // translate and rotate the objects
        body1.translate(J.product(invM1))
        body1.rotate(invI1 * r1.cross(J), c1)
        body2.translate(J.product(-invM2))
        body2.rotate(-invI2 * r2.cross(J), c2)
        return abs(C) < linearTolerance
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
    override fun getReactionForce(invdt: Double): Vector2 {
        return n!!.product(impulse * invdt)
    }

    /**
     * {@inheritDoc}
     *
     *
     * Not applicable to this joint. Always returns zero.
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
     * Returns true if this distance joint is a spring distance joint.
     * @return boolean
     */
    val isSpring: Boolean
        get() = frequency > 0.0

    /**
     * Returns true if this distance joint is a spring distance joint
     * with damping.
     * @return boolean
     */
    val isSpringDamper: Boolean
        get() = frequency > 0.0 && dampingRatio > 0.0

    /**
     * Minimal constructor.
     *
     *
     * Creates a fixed distance [Joint] where the joined
     * [Body]s do not participate in collision detection and
     * resolution.
     * @param body1 the first [Body]
     * @param body2 the second [Body]
     * @param anchor1 in world coordinates
     * @param anchor2 in world coordinates
     * @throws NullPointerException if body1, body2, anchor1, or anchor2 is null
     * @throws IllegalArgumentException if body1 == body2
     */
    init {
        // verify the bodies are not the same instance
        if (body1 === body2) throw IllegalArgumentException(message("dynamics.joint.sameBody"))
        // verify the anchor points are not null
        if (anchor1 == null) throw NullPointerException(message("dynamics.joint.nullAnchor1"))
        if (anchor2 == null) throw NullPointerException(message("dynamics.joint.nullAnchor2"))
        // get the local anchor points
        localAnchor1 = body1.getLocalPoint(anchor1)
        localAnchor2 = body2.getLocalPoint(anchor2)
        // compute the initial distance
        distance = anchor1.distance(anchor2)
    }
}