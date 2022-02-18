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
package org.dyn4j.collision.manifold

import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Shape

/**
 * Represents a [ManifoldPointId] that uses edge indexing.
 *
 *
 * The the edge and vertex indicies are the indicies of the edges
 * and verticies in the reference and incident [Convex] [Shape]s in
 * the collision.
 *
 *
 * The flipped flag is set when the default reference edge is swapped
 * to be the incident edge.
 *
 *
 * For a given [Convex] [Shape] the indicies should not change, although
 * there is no mechanism preventing this. In the case they change, this should only
 * affect any caching of this information.
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 * @see ManifoldPointId.DISTANCE
 */
class IndexedManifoldPointId constructor(val referenceEdge: Int, val incidentEdge: Int, val incidentVertex: Int,
                                         val isFlipped: Boolean = false) : ManifoldPointId {
    /**
     * Returns the reference edge index of this manifold
     * on the [Shape].
     *
     *
     * The reference edge is the edge that is most perpendicular to the collision normal.
     * @return int
     */

    /**
     * Returns the incident edge index of this manifold
     * on the other [Shape].
     * @return int
     */

    /**
     * Returns the index of the deepest collision point of the incident edge of this manifold on
     * the other [Shape].
     * @return int
     */

    /**
     * Returns true if the reference edge and incident edges were swapped.
     * @return boolean
     */

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other === this) return true
        if (other is IndexedManifoldPointId) {
            val o =
                other
            if (referenceEdge == o.referenceEdge && incidentEdge == o.incidentEdge && incidentVertex == o.incidentVertex && isFlipped == o.isFlipped
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
        var hash = referenceEdge
        hash = 37 * hash + incidentEdge
        hash = 37 * hash + incidentVertex
        hash = 37 * hash + if (isFlipped) 1231 else 1237
        return hash
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("IndexedManifoldPointId[ReferenceEdge=").append(referenceEdge)
            .append("|IncidentEdge=").append(incidentEdge)
            .append("|IncidentVertex=").append(incidentVertex)
            .append("|IsFlipped=").append(isFlipped)
            .append("]")
        return sb.toString()
    }

    /**
     * Full constructor.
     * @param referenceEdge the reference edge index
     * @param incidentEdge the incident edge index
     * @param incidentVertex the incident vertex index
     * @param isFlipped whether the reference and incident features flipped
     */
}