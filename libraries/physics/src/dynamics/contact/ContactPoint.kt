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
package org.dyn4j.dynamics.contact;

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.geometry.Vector2
import org.dyn4j.resources.message

/**
 * Represents a contact point and used to report events via the {@link ContactListener}.
 * @author William Bittle
 * @version 3.3.0
 * @since 1.0.0
 */
open class ContactPoint {

    /** The contact point id  */
    var id: ContactPointId? = null

    /** The first [Body] in contact  */
    var body1: Body? = null

    /** The second [Body] in contact  */
    var body2: Body? = null

    /** The first [Body]'s [BodyFixture]  */
    var fixture1: BodyFixture? = null

    /** The second [Body]'s [BodyFixture]  */
    var fixture2: BodyFixture? = null

    /** The world space contact point  */
    var point: Vector2? = null

    /** The world space contact normal  */
    var normal: Vector2? = null

    /** The penetration depth  */
    var depth = 0.0

    /** True if the contact is a sensor  */
    var isSensor = false

    /**
     * Full constructor.
     * @param id the contact point id
     * @param body1 the first [Body] in contact
     * @param fixture1 the first [Body]'s [BodyFixture]
     * @param body2 the second [Body] in contact
     * @param fixture2 the second [Body]'s [BodyFixture]
     * @param point the world space contact point
     * @param normal the world space contact normal
     * @param depth the penetration depth
     * @param isSensor true if the contact is a sensor contact
     */
    constructor(
        id: ContactPointId, body1: Body, fixture1: BodyFixture, body2: Body, fixture2: BodyFixture,
        point: Vector2, normal: Vector2, depth: Double, isSensor: Boolean
    ) {
        this.id = id
        this.body1 = body1
        this.fixture1 = fixture1
        this.body2 = body2
        this.fixture2 = fixture2
        this.point = point
        this.normal = normal
        this.depth = depth
        this.isSensor = isSensor
    }

    /**
     * Helper constructor for a contact constraint and contact.
     * @param constraint the constraint
     * @param contact the contact
     */
    constructor(constraint: ContactConstraint, contact: Contact) {
        id = ContactPointId(constraint.id, contact.id)
        body1 = constraint.body1
        fixture1 = constraint.fixture1
        body2 = constraint.body2
        fixture2 = constraint.fixture2
        point = contact.p
        normal = constraint.normal
        depth = contact.depth
        isSensor = constraint.isSensor
    }

    /**
     * Copy constructor (shallow).
     * @param contactPoint the [ContactPoint] to copy
     */
    constructor(contactPoint: ContactPoint) {
        if (contactPoint == null) throw NullPointerException(message("dynamics.contact.contactPoint.nullContactPoint"))
        // shallow copy all the fields
        id = contactPoint.id
        body1 = contactPoint.body1
        fixture1 = contactPoint.fixture1
        body2 = contactPoint.body2
        fixture2 = contactPoint.fixture2
        point = contactPoint.point
        normal = contactPoint.normal
        depth = contactPoint.depth
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("ContactPoint[Id=").append(id)
            .append("|Body1=").append(body1.hashCode())
            .append("|Fixture1=").append(fixture1.hashCode())
            .append("|Body2=").append(body2.hashCode())
            .append("|Fixture2=").append(fixture2.hashCode())
            .append("|Point=").append(point)
            .append("|Normal=").append(normal)
            .append("|Depth=").append(depth)
            .append("|Sensor=").append(isSensor)
            .append("]")
        return sb.toString()
    }

}
