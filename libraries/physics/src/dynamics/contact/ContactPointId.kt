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

import org.dyn4j.collision.manifold.ManifoldPointId

/**
 * Represents a contact point id to identify contacts from frame to frame.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.1.2
 */
class ContactPointId
/**
 * Full constructor.
 * @param contactConstraintId the contact constraint id
 * @param manifoldPointId the manifold point id
 */(
    /** The contact constraint id  */
    val contactConstraintId: ContactConstraintId,
    /** The manifold point id  */
    val manifoldPointId: ManifoldPointId
) {
    /**
     * Returns the [ContactConstraintId] for this contact.
     * @return [ContactConstraintId]
     */

    /**
     * Returns the [ManifoldPointId] for this contact.
     * @return [ManifoldPointId]
     */

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other === this) return true
        if (other is ContactPointId) {
            val id = other
            if (id.contactConstraintId.equals(contactConstraintId) && id.manifoldPointId == manifoldPointId) {
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
        hash = hash * 31 + contactConstraintId.hashCode()
        hash = hash * 31 + manifoldPointId.hashCode()
        return hash
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("ContactPointId[ContactConstraintId=").append(contactConstraintId)
            .append("|ManifoldPointId=").append(manifoldPointId)
            .append("]")
        return sb.toString()
    }

}