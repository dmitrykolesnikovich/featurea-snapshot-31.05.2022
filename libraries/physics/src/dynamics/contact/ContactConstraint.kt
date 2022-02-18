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

import org.dyn4j.collision.manifold.Manifold
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.dynamics.Constraint
import org.dyn4j.geometry.Matrix22
import org.dyn4j.geometry.Shiftable
import org.dyn4j.geometry.Vector2

/**
 * Represents a [Contact] constraint for each [Body] pair.
 * @author William Bittle
 * @version 3.2.5
 * @since 1.0.0
 */
class ContactConstraint : Constraint, Shiftable {
    /** The unique contact id  */
    var id: ContactConstraintId

    /** The first [Body]'s [BodyFixture]  */
    val fixture1: BodyFixture

    /** The second [Body]'s [BodyFixture]  */
    val fixture2: BodyFixture

    /** The [Contact]s  */
    lateinit var contacts: MutableList<Contact>

    /** The penetration normal  */
    var normal: Vector2? = null

    /** The tangent of the normal  */
    var tangent: Vector2? = null

    /** The coefficient of friction  */
    var friction = 0.0

    /** The coefficient of restitution  */
    var restitution = 0.0

    /** Whether the contact is a sensor contact or not  */
    var isSensor = false

    /** The surface speed of the contact patch  */
    var tangentSpeed = 0.0

    /** True if the contact should be evaluated  */
    var isEnabled = false

    /** The K matrix for block solving a contact pair  */
    var K: Matrix22? = null

    /** The inverse of the [.K] matrix  */
    var invK: Matrix22? = null

    constructor(
        body1: Body,
        fixture1: BodyFixture,
        body2: Body,
        fixture2: BodyFixture,
        manifold: Manifold,
        friction: Double,
        restitution: Double
    ) :  super(body1, body2){
        // set the involved convex shapes
        this.fixture1 = fixture1
        this.fixture2 = fixture2
        // create the constraint id
        id = ContactConstraintId(body1, fixture1, body2, fixture2)
        // get the manifold points
        val points = manifold.points
        // get the manifold point size
        val mSize = points.size
        // create contact array
        contacts = ArrayList(mSize)
        // create contacts for each point
        for (l in 0 until mSize) {
            // get the manifold point
            val point = points[l]
            // create a contact from the manifold point
            val contact = Contact(
                point.id,
                point.point!!,
                point.depth,
                this.body1.getLocalPoint(point.point!!),
                this.body2.getLocalPoint(point.point!!)
            )
            // add the contact to the array
            contacts.add(contact)
        }
        // set the normal
        normal = manifold.normal
        // set the tangent
        tangent = normal!!.leftHandOrthogonalVector
        // set coefficients
        this.friction = friction
        this.restitution = restitution
        // set the sensor flag (if either fixture is a sensor then the
        // contact constraint between the fixtures is a sensor)
        isSensor = fixture1.isSensor || fixture2.isSensor
        // by default the tangent speed is zero
        tangentSpeed = 0.0
        isEnabled = true
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("ContactConstraint[").append(super.toString())
            .append("|Body1=").append(this.body1.hashCode())
            .append("|Fixture1=").append(fixture1.hashCode())
            .append("|Body2=").append(this.body2.hashCode())
            .append("|Fixture2=").append(fixture2.hashCode())
            .append("|Normal=").append(normal)
            .append("|Tangent=").append(tangent)
            .append("|Friction=").append(friction)
            .append("|Restitution=").append(restitution)
            .append("|IsSensor=").append(isSensor)
            .append("|TangentSpeed=").append(tangentSpeed)
            .append("|Enabled=").append(isEnabled)
            .append("|Contacts={")
        val size = contacts!!.size
        for (i in 0 until size) {
            if (i != 0) sb.append(",")
            sb.append(contacts[i])
        }
        sb.append("}]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shiftable#shift(org.dyn4j.geometry.Vector2)
	 */
    override fun shift(shift: Vector2) {
        val size = contacts!!.size
        // loop over the contacts
        for (i in 0 until size) {
            val c = contacts[i]
            // translate the world space contact point
            c.p.add(shift)
            // c.p1 and c.p2 are in local coordinates
            // and don't need to be shifted
        }
    }

}