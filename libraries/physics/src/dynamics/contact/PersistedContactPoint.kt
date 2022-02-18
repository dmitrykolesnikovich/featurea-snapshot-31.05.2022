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

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.geometry.Vector2


/**
 * Represents a persisted contact point.
 *
 *
 * A persisted contact point is a contact point that was retained
 * from the last iteration and therefore contains the previous point,
 * normal, and depth.
 * @author William Bittle
 * @see ContactPoint
 *
 * @version 3.3.0
 * @since 1.0.0
 */
class PersistedContactPoint : ContactPoint {
    /** The previous contact point  */
    var oldPoint: Vector2? = null

    /** The previous contact normal  */
    var oldNormal: Vector2? = null

    /** The previous penetration depth  */
    var oldDepth = 0.0

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
     * @param oldPoint the previous world space contact point
     * @param oldNormal the previous world space contact normal
     * @param oldDepth the previous penetration depth
     * @param sensor true if the contact is a sensor contact
     */
   constructor(
        id: ContactPointId,
        body1: Body,
        fixture1: BodyFixture,
        body2: Body,
        fixture2: BodyFixture,
        point: Vector2,
        normal: Vector2,
        depth: Double,
        oldPoint: Vector2,
        oldNormal: Vector2,
        oldDepth: Double,
        sensor: Boolean
    ) : super(id, body1, fixture1, body2, fixture2, point, normal, depth, sensor){
        this.oldPoint = oldPoint
        this.oldNormal = oldNormal
        this.oldDepth = oldDepth
    }

    /**
     * Helper constructor for a contact constraint and contact.
     * @param newConstraint the new constraint
     * @param newContact the new contact
     * @param oldConstraint the old constraint
     * @param oldContact the old contact
     */
    constructor(newConstraint: ContactConstraint, newContact: Contact, oldConstraint: ContactConstraint, oldContact: Contact)
            : super(newConstraint, newContact){
        oldDepth = oldContact.depth
        oldNormal = oldConstraint.normal
        oldPoint = oldContact.p
    }

    /**
     * Copy constructor (shallow).
     * @param pcp the [PersistedContactPoint] to copy
     */
    constructor(pcp: PersistedContactPoint) : super(pcp){
        oldPoint = pcp.oldPoint
        oldNormal = pcp.oldNormal
        oldDepth = pcp.oldDepth
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("PersistedContactPoint[Id=").append(id)
            .append("|Body1=").append(body1.hashCode())
            .append("|Fixture1=").append(fixture1.hashCode())
            .append("|Body2=").append(body2.hashCode())
            .append("|Fixture2=").append(fixture2.hashCode())
            .append("|Point=").append(point)
            .append("|Normal=").append(normal)
            .append("|Depth=").append(depth)
            .append("|PreviousPoint=").append(oldPoint)
            .append("|PreviousNormal=").append(oldNormal)
            .append("|PreviousDepth=").append(oldDepth)
            .append("]")
        return sb.toString()
    }

}