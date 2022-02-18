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
package org.dyn4j.dynamics.contact

import org.dyn4j.Epsilon
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.Settings
import org.dyn4j.dynamics.Step
import org.dyn4j.geometry.*
import kotlin.math.max
import kotlin.math.min

/**
 * Represents an impulse based rigid [Body] physics collision resolver.
 * @author William Bittle
 * @version 3.4.0
 * @since 3.2.0
 */
open class SequentialImpulses : ContactConstraintSolver {

    /**
     * Compute the mass coefficient for a [Contact].
     *
     * @param contactConstraint The [ContactConstraint] of the contact
     * @param contact The contact
     * @param n The normal
     * @return The mass coefficient
     * @since 3.4.0
     */
    private fun getMassCoefficient(contactConstraint: ContactConstraint, contact: Contact, n: Vector2): Double {
        return this.getMassCoefficient(contactConstraint, contact.r1!!, contact.r2!!, n)
    }

    /**
     * Compute the mass coefficient for a [Contact].
     *
     * @param contactConstraint The [ContactConstraint] of the contact
     * @param r1 The contact.r1 field
     * @param r2 The contact.r2 field
     * @param n The normal
     * @return The mass coefficient
     * @since 3.4.0
     */
    private fun getMassCoefficient(contactConstraint: ContactConstraint, r1: Vector2, r2: Vector2, n: Vector2): Double {
        val m1: Mass = contactConstraint.body1!!.mass!!
        val m2: Mass = contactConstraint.body2!!.mass!!
        val r1CrossN: Double = r1.cross(n)
        val r2CrossN: Double = r2.cross(n)
        return m1.inverseMass + m2.inverseMass + m1.inverseInertia * r1CrossN * r1CrossN + m2.inverseInertia * r2CrossN * r2CrossN
    }

    /**
     * Helper method to update the bodies of a [ContactConstraint]
     *
     * @param contactConstraint The [ContactConstraint] of the bodies
     * @param contact The corresponding [contact]
     * @param J
     * @since 3.4.0
     */
    private fun updateBodies(contactConstraint: ContactConstraint, contact: Contact, J: Vector2) {
        val b1 = contactConstraint.body1
        val b2 = contactConstraint.body2
        val m1 = b1!!.mass
        val m2 = b2!!.mass

        // b1.getVelocity().add(J.product(invM1));
        b1.getLinearVelocity().add(J.x * m1!!.inverseMass, J.y * m1.inverseMass)
        b1.angularVelocity = (b1.angularVelocity + m1.inverseInertia * contact.r1!!.cross(J))

        // b2.getVelocity().subtract(J.product(invM2));
        b2.getLinearVelocity().subtract(J.x * m2!!.inverseMass, J.y * m2.inverseMass)
        b2.angularVelocity = (b2.angularVelocity - m2.inverseInertia * contact.r2!!.cross(J))
    }


    /**
     * Compute the relative velocity to the [ContactConstraint]'s normal.
     *
     * @param contactConstraint The [ContactConstraint]
     * @param contact The [Contact]
     * @return double
     * @since 3.4.0
     */
    private fun getRelativeVelocityAlongNormal(contactConstraint: ContactConstraint, contact: Contact): Double {
        val rv: Vector2 = getRelativeVelocity(contactConstraint, contact)
        return contactConstraint.normal!!.dot(rv)
    }

    /**
     * Compute the relative velocity of this [ContactConstraint]'s bodies.
     *
     * @param contactConstraint The [ContactConstraint]
     * @param contact The [Contact]
     * @return The relative velocity vector
     * @since 3.4.0
     */
    private fun getRelativeVelocity(contactConstraint: ContactConstraint, contact: Contact): Vector2 {
        val b1 = contactConstraint.body1
        val b2 = contactConstraint.body2
        val lv1 = contact.r1!!.cross(b1!!.angularVelocity).add(b1.getLinearVelocity())
        val lv2 = contact.r2!!.cross(b2!!.angularVelocity).add(b2.getLinearVelocity())
        return lv1.subtract(lv2)
    }

    /* (non-Javadoc)
 * @see org.dyn4j.dynamics.contact.ContactConstraintSolver#initialize(java.util.List, org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
 */
    override fun initialize(contactConstraints: List<ContactConstraint>, step: Step, settings: Settings) {
        // get the restitution velocity from the settings object
        val restitutionVelocity = settings.getRestitutionVelocity()

        // loop through the contact constraints
        val size = contactConstraints.size
        for (i in 0 until size) {
            val contactConstraint = contactConstraints[i]

            // get the contacts
            val contacts: List<Contact> = contactConstraint.contacts
            // get the size
            val cSize = contacts.size
            if (cSize == 0) return

            // get the bodies
            val b1 = contactConstraint.body1
            val b2 = contactConstraint.body2
            // get the body transforms
            val t1 = b1!!.transform
            val t2 = b2!!.transform
            // get the body masses
            val m1 = b1.mass
            val m2 = b2.mass
            val invM1 = m1!!.inverseMass
            val invM2 = m2!!.inverseMass
            val invI1 = m1.inverseInertia
            val invI2 = m2.inverseInertia

            // get the transformed centers of mass
            val c1 = t1.getTransformed(m1.center!!)
            val c2 = t2.getTransformed(m2.center!!)

            // get the penetration axis
            val N = contactConstraint.normal
            // get the tangent vector
            val T = contactConstraint.tangent

            // loop through the contact points
            for (j in 0 until cSize) {
                val contact = contacts[j]

                // calculate ra and rb
                contact.r1 = c1.to(contact.p)
                contact.r2 = c2.to(contact.p)

                // pre calculate the mass normal
                contact.massN = 1.0 / getMassCoefficient(contactConstraint, contact, N!!)
                // pre calculate the mass tangent
                contact.massT = 1.0 / getMassCoefficient(contactConstraint, contact, T!!)
                // set the velocity bias
                contact.vb = 0.0

                // find the relative velocity and project it onto the penetration normal
                val rvn = getRelativeVelocityAlongNormal(contactConstraint, contact)

                // if its negative then the bodies are moving away from one another
                if (rvn < -restitutionVelocity) {
                    // use the coefficient of elasticity
                    contact.vb += -contactConstraint.restitution * rvn
                }
            }

            // does this contact have 2 points?
            if (cSize == 2) {
                // setup the block solver
                val contact1 = contacts[0]
                val contact2 = contacts[1]
                val rn1A = contact1.r1!!.cross(N!!)
                val rn1B = contact1.r2!!.cross(N)
                val rn2A = contact2.r1!!.cross(N)
                val rn2B = contact2.r2!!.cross(N)

                // compute the K matrix for the constraints
                val K = Matrix22()
                K.m00 = invM1 + invM2 + invI1 * rn1A * rn1A + invI2 * rn1B * rn1B
                K.m01 = invM1 + invM2 + invI1 * rn1A * rn2A + invI2 * rn1B * rn2B
                K.m10 = K.m01
                K.m11 = invM1 + invM2 + invI1 * rn2A * rn2A + invI2 * rn2B * rn2B

                // check the condition number of the matrix
                val maxCondition = 1000.0
                if (K.m00 * K.m00 < maxCondition * K.determinant()) {
                    // if the condition number is below the max then we can
                    // assume that we can invert K
                    contactConstraint.K = K
                    contactConstraint.invK = K.inverse
                } else {
                    // otherwise the matrix is ill conditioned

                    // it looks like this will only be the case if the points are
                    // close to being the same point.  If they were the same point
                    // then the constraints would be redundant
                    // just choose one of the points as the point to solve

                    // let's choose the deepest point
                    if (contact1.depth > contact2.depth) {
                        // then remove the second contact
                        contactConstraint.contacts.removeAt(1)
                    } else {
                        // then remove the first contact
                        contactConstraint.contacts.removeAt(0)
                    }
                }
            }
        }

        // perform warm starting
        this.warmStart(contactConstraints, step, settings)
    }

    /**
     * Performs warm-starting of the contact constraints.
     * @param contactConstraints the contact constraints to solve
     * @param step the time step information
     * @param settings the current settings
     */
    protected fun warmStart(contactConstraints: List<ContactConstraint>, step: Step, settings: Settings) {
        // pre divide for performance
        val ratio = 1.0 / step.deltaTimeRatio

        // get the size
        val size = contactConstraints.size

        // we have to perform a separate loop to warm start
        for (i in 0 until size) {
            val contactConstraint = contactConstraints[i]

            // get the penetration axis
            val N = contactConstraint.normal
            // get the tangent vector
            val T = contactConstraint.tangent

            // get the contacts and contact size
            val contacts = contactConstraint.contacts
            val cSize = contacts!!.size
            for (j in 0 until cSize) {
                val contact = contacts[j]

                // scale the accumulated impulses by the delta time ratio
                contact!!.jn *= ratio
                contact!!.jt *= ratio

                // apply accumulated impulses to warm start the solver
                val J =
                    Vector2(N!!.x * contact!!.jn + T!!.x * contact.jt, N.y * contact.jn + T.y * contact.jt)
                this.updateBodies(contactConstraint, contact, J)
            }
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.contact.ContactConstraintSolver#solveVelocityContraints(java.util.List, org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solveVelocityContraints(contactConstraints: List<ContactConstraint>, step: Step, settings: Settings) {
        // loop through the contact constraints
        val size = contactConstraints.size
        for (i in 0 until size) {
            val contactConstraint = contactConstraints[i]

            // get the contact list
            val contacts: List<Contact> = contactConstraint.contacts
            val cSize = contacts.size
            if (cSize == 0) continue

            // get the penetration axis and tangent
            val N = contactConstraint.normal
            val T = contactConstraint.tangent
            val tangentSpeed = contactConstraint.tangentSpeed

            // evaluate friction impulse
            for (k in 0 until cSize) {
                val contact = contacts[k]

                // get the relative velocity
                val rv = this.getRelativeVelocity(contactConstraint, contact)

                // project the relative velocity onto the tangent normal
                val rvt = T!!.dot(rv) - tangentSpeed
                // calculate the tangential impulse
                var jt = contact.massT * -rvt

                // apply the coefficient of friction
                val maxJt = contactConstraint.friction * contact.jn

                // clamp the accumulated tangential impulse
                val Jt0 = contact.jt
                contact.jt = max(-maxJt, min(Jt0 + jt, maxJt))
                jt = contact.jt - Jt0

                // apply to the bodies immediately
                val J = Vector2(T!!.x * jt, T!!.y * jt)
                this.updateBodies(contactConstraint, contact, J)
            }

            // evalutate the normal impulse

            // check the number of contacts to solve
            if (cSize == 1) {
                // if its one then solve the one contact
                val contact = contacts[0]

                // get the relative velocity and project it onto the penetration normal
                val rvn = this.getRelativeVelocityAlongNormal(contactConstraint, contact)

                // calculate the impulse using the velocity bias
                var j = -contact.massN * (rvn - contact.vb)

                // clamp the accumulated impulse
                val j0 = contact.jn
                contact.jn = max(j0 + j, 0.0)
                j = contact.jn - j0

                // only update the bodies after processing all the contacts
                val J = Vector2(N!!.x * j, N!!.y * j)
                this.updateBodies(contactConstraint, contact, J)
            } else {
                // if its 2 then solve the contacts simultaneously using a mini-LCP

                // Block solver developed by Erin Cato and Dirk Gregorius (see Box2d).
                // Build the mini LCP for this contact patch
                //
                // vn = A * x + b, vn >= 0, x >= 0 and vn_i * x_i = 0 with i = 1..2
                //
                // A = J * W * JT and J = ( -n, -r1 x n, n, r2 x n )
                // b = vn_0 - velocityBias
                //
                // The system is solved using the "Total enumeration method" (s. Murty). The complementary constraint vn_i * x_i
                // implies that we must have in any solution either vn_i = 0 or x_i = 0. So for the 2D contact problem the cases
                // vn1 = 0 and vn2 = 0, x1 = 0 and x2 = 0, x1 = 0 and vn2 = 0, x2 = 0 and vn1 = 0 need to be tested. The first valid
                // solution that satisfies the problem is chosen.
                // 
                // In order to account for the accumulated impulse 'a' (because of the iterative nature of the solver which only requires
                // that the accumulated impulse is clamped and not the incremental impulse) we change the impulse variable (x_i).
                //
                // Substitute:
                // 
                // x = a + d
                // 
                // a := old total impulse
                // x := new total impulse
                // d := incremental impulse
                //
                // For the current iteration we extend the formula for the incremental impulse
                // to compute the new total impulse:
                //
                // vn = A * d + b
                //    = A * (x - a) + b
                //    = A * x + b - A * a
                //    = A * x + b'
                // b' = b - A * a;
                val contact1 = contacts[0]
                val contact2 = contacts[1]

                // create a vector containing the current accumulated impulses
                val a = Vector2(contact1.jn, contact2.jn)

                // get the relative velocity at both contacts and
                // compute the relative velocities along the collision normal
                var rvn1 = this.getRelativeVelocityAlongNormal(contactConstraint, contact1)
                var rvn2 = this.getRelativeVelocityAlongNormal(contactConstraint, contact2)

                // create the b vector
                val b = Vector2()
                b.x = rvn1 - contact1.vb
                b.y = rvn2 - contact2.vb
                b.subtract(contactConstraint.K!!.product(a))
                while (true) {

                    //
                    // Case 1: vn = 0
                    //
                    // 0 = A * x + b'
                    //
                    // Solve for x:
                    //
                    // x = - inv(A) * b'
                    //
                    val x = contactConstraint.invK!!.product(b).negate()
                    if (x.x >= 0.0 && x.y >= 0.0) {
                        this.updateBodies(contactConstraint, contact1, contact2, x, a)
                        break
                    }

                    //
                    // Case 2: vn1 = 0 and x2 = 0
                    //
                    //   0 = a11 * x1 + a12 * 0 + b1' 
                    // vn2 = a21 * x1 + a22 * 0 + b2'
                    //
                    x.x = -contact1.massN * b.x
                    x.y = 0.0
                    rvn1 = 0.0
                    rvn2 = contactConstraint.K!!.m10 * x.x + b.y
                    if (x.x >= 0.0 && rvn2 >= 0.0) {
                        this.updateBodies(contactConstraint, contact1, contact2, x, a)
                        break
                    }


                    //
                    // Case 3: vn2 = 0 and x1 = 0
                    //
                    // vn1 = a11 * 0 + a12 * x2 + b1' 
                    //   0 = a21 * 0 + a22 * x2 + b2'
                    //
                    x.x = 0.0
                    x.y = -contact2.massN * b.y
                    rvn1 = contactConstraint.K!!.m01 * x.y + b.x
                    rvn2 = 0.0
                    if (x.y >= 0.0 && rvn1 >= 0.0) {
                        this.updateBodies(contactConstraint, contact1, contact2, x, a)
                        break
                    }

                    //
                    // Case 4: x1 = 0 and x2 = 0
                    // 
                    // vn1 = b1
                    // vn2 = b2;
                    x.x = 0.0
                    x.y = 0.0
                    rvn1 = b.x
                    rvn2 = b.y
                    if (rvn1 >= 0.0 && rvn2 >= 0.0) {
                        this.updateBodies(contactConstraint, contact1, contact2, x, a)
                        break
                    }

                    // No solution, give up. This is hit sometimes, but it doesn't seem to matter.
                    break
                }
            }
        }
    }

    /**
     * Helper method to update bodies while performing the solveVelocityContraints step.
     *
     * @param contactConstraint The [ContactConstraint] of the contacts
     * @param contact1 The first contact
     * @param contact2 The second contact
     * @param x
     * @param a
     * @since 3.4.0
     */
    private fun updateBodies(contactConstraint: ContactConstraint, contact1: Contact, contact2: Contact,
                             x: Vector2, a: Vector2) {
        val b1 = contactConstraint.body1
        val b2 = contactConstraint.body2
        val m1 = b1!!.mass
        val m2 = b2!!.mass
        val N = contactConstraint.normal

        // find the incremental impulse
        // Vector2 d = x.difference(a);
        // apply the incremental impulse
        val J1 = N!!.product(x.x - a.x)
        val J2 = N.product(x.y - a.y)
        val Jx = J1.x + J2.x
        val Jy = J1.y + J2.y

        // v1.add(J1.sum(J2).multiply(invM1));
        b1.getLinearVelocity().add(Jx * m1!!.inverseMass, Jy * m1.inverseMass)
        b1.angularVelocity = (b1.angularVelocity + m1.inverseInertia * (contact1.r1!!.cross(J1) + contact2.r1!!.cross(J2)))

        // v2.subtract(J1.sum(J2).multiply(invM2));
        b2.getLinearVelocity().subtract(Jx * m2!!.inverseMass, Jy * m2.inverseMass)
        b2.angularVelocity = (b2.angularVelocity - m2.inverseInertia * (contact1.r2!!.cross(J1) + contact2.r2!!.cross(J2)))

        // set the new incremental impulse
        contact1.jn = x.x
        contact2.jn = x.y
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.contact.ContactConstraintSolver#solvePositionContraints(java.util.List, org.dyn4j.dynamics.Step, org.dyn4j.dynamics.Settings)
	 */
    override fun solvePositionContraints(contactConstraints: List<ContactConstraint>, step: Step, settings: Settings): Boolean {
        // immediately return true if there are no contact constraints to solve
        if (contactConstraints.isEmpty()) return true

        // track the minimum separation
        var minSeparation: Double = 0.0

        // get the max linear correction, baumgarte, and allowed penetration from
        // the settings object.
        val maxLinearCorrection: Double = settings.getMaximumLinearCorrection()
        val allowedPenetration: Double = settings.getLinearTolerance()
        val baumgarte: Double = settings.getBaumgarte()

        // loop through the contact constraints
        val size: Int = contactConstraints.size
        for (i in 0 until size) {
            val contactConstraint: ContactConstraint = contactConstraints.get(i)

            // get the contact list
            val contacts: List<Contact> = contactConstraint.contacts
            val cSize: Int = contacts.size
            if (cSize == 0) continue

            // get the bodies
            val b1: Body? = contactConstraint.body1
            var b2: Body? = contactConstraint.body2
            // get their transforms
            var t1: Transform = b1!!.transform
            var t2: Transform = b2!!.transform
            // get the masses
            var m1: Mass? = b1!!.mass
            var m2: Mass? = b2!!.mass

            // get the penetration axis
            var N: Vector2? = contactConstraint.normal

            // solve normal constraints
            for (k in 0 until cSize) {
                var contact: Contact = contacts.get(k)

                // get the world centers of mass
                // NOTE: the world center needs to be recomputed each iteration because
                //       we are modifying the transform in each iteration
                var c1: Vector2 = t1.getTransformed((m1!!.center)!!)
                var c2: Vector2 = t2.getTransformed((m2!!.center)!!)

                // get r1 and r2
                var r1: Vector2 = contact.p1!!.difference((m1!!.center)!!)
                t1.transformR(r1)
                var r2: Vector2 = contact.p2!!.difference((m2!!.center)!!)
                t2.transformR(r2)

                // get the world contact points
                var p1: Vector2 = c1.sum(r1)
                var p2: Vector2? = c2.sum(r2)
                var dp: Vector2 = p1.subtract((p2)!!)

                // estimate the current penetration
                var penetration: Double = dp.dot((N)!!) - contact.depth

                // track the maximum error
                minSeparation = min(minSeparation, penetration)

                // allow for penetration to avoid jitter
                var cp: Double = baumgarte * Interval.clamp(
                    penetration + allowedPenetration,
                    -maxLinearCorrection,
                    0.0
                )

                // compute the position impulse
                var K: Double = this.getMassCoefficient(contactConstraint, r1, r2, (N)!!)
                var jp: Double = if ((K > Epsilon.E)) (-cp / K) else 0.0

                // clamp the accumulated position impulse
                var jp0: Double = contact.jp
                contact.jp = max(jp0 + jp, 0.0)
                jp = contact.jp - jp0
                var J: Vector2 = N!!.product(jp)

                // translate and rotate the objects
                b1!!.translate(J.product(m1!!.inverseMass))
                b1!!.rotate(m1!!.inverseInertia * r1.cross(J), c1.x, c1.y)
                b2!!.translate(J.product(-m2!!.inverseMass))
                b2!!.rotate(-m2!!.inverseInertia * r2.cross(J), c2.x, c2.y)
            }
        }
        // check if the minimum separation between all objects is still
        // greater than or equal to allowed penetration plus half of allowed penetration
        // since we cannot expect it to be above allowed penetration alone
        return minSeparation >= -3.0 * allowedPenetration
    }

}