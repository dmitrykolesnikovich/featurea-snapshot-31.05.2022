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
package org.dyn4j.geometry.decompose

/**
 * Represents a half edge of the [DoubleEdgeList].
 * @author William Bittle
 * @version 3.2.0
 * @since 2.2.0
 */
class DoubleEdgeListHalfEdge {
    /** The half edge origin  */
    var origin: DoubleEdgeListVertex? = null

    /** The adjacent twin of this half edge  */
    var twin: DoubleEdgeListHalfEdge? = null

    /** The adjacent edge next in the list having the same face  */
    var next: DoubleEdgeListHalfEdge? = null

    /**
     * Returns this half edge's face.
     * @return [DoubleEdgeListFace]
     */
    /** The adjacent face of this half edge  */
    var face: DoubleEdgeListFace? = null

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append(origin)
            .append(" to ")
            .append(next!!.origin)
        return sb.toString()
    }

    /**
     * Returns this half edge's destination.
     * @return [DoubleEdgeListVertex]
     */
    val destination: DoubleEdgeListVertex?
        get() = next!!.origin// walk around the face

    /**
     * Returns the previous half edge having the same
     * face as this half edge.
     * @return [DoubleEdgeListHalfEdge]
     */
    val previous: DoubleEdgeListHalfEdge?
        get() {
            var edge = twin!!.next!!.twin
            // walk around the face
            while (edge!!.next != this) {
                edge = edge.next!!.twin
            }
            return edge
        }

}