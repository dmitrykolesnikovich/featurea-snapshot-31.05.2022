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
import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Shape

/**
 * Represents and id for a contact constraint between two [Convex]
 * [Shape]s on two [Body]s.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 */
class ContactConstraintId(body1: Body, fixture1: BodyFixture, body2: Body, fixture2: BodyFixture) {
    /** The first [Body]  */
    private val body1: Body

    /** The second [Body]  */
    private val body2: Body

    /** The first [Body]'s [Convex] [Shape]  */
    private val fixture1: BodyFixture

    /** The second [Body]'s [Convex] [Shape]  */
    private val fixture2: BodyFixture

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other === this) return true
        if (other is ContactConstraintId) {
            val o =
                other
            if (body1 === o.body1 && body2 === o.body2 && fixture1 === o.fixture1 && fixture2 === o.fixture2 // the order of the objects doesn't matter
                || body1 === o.body2 && body2 === o.body1 && fixture1 === o.fixture2 && fixture2 === o.fixture1
            ) {
                return true
            }
        }
        return false
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    override fun hashCode(): Int {
        var hash = 1
        hash = hash * 31 + body1.hashCode() + body2.hashCode()
        hash = hash * 31 + fixture1.hashCode() + fixture2.hashCode()
        return hash
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("ContactConstraintId[Body1=").append(body1.hashCode())
            .append("|Body2=").append(body2.hashCode())
            .append("|Fixture1=").append(fixture1.hashCode())
            .append("|Fixture2=").append(fixture2.hashCode())
            .append("]")
        return sb.toString()
    }

    /**
     * Returns the first body.
     * @return Body
     * @since 3.4.0
     */
    fun getBody1(): Body {
        return body1
    }

    /**
     * Returns the second body.
     * @return Body
     * @since 3.4.0
     */
    fun getBody2(): Body {
        return body2
    }

    /**
     * Returns the fixture on the first body.
     * @return BodyFixture
     * @since 3.4.0
     */
    fun getFixture1(): BodyFixture {
        return fixture1
    }

    /**
     * Returns the fixture on the second body.
     * @return BodyFixture
     * @since 3.4.0
     */
    fun getFixture2(): BodyFixture {
        return fixture2
    }

    /**
     * Full constructor.
     * @param body1 the first [Body]
     * @param fixture1 the first [Body]'s [BodyFixture]
     * @param body2 the second [Body]
     * @param fixture2 the second [Body]'s [BodyFixture]
     */
    init {
        this.body1 = body1
        this.body2 = body2
        this.fixture1 = fixture1
        this.fixture2 = fixture2
    }
}