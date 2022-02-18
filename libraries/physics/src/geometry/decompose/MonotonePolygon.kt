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
 * Represents a monotone polygon.
 *
 *
 * A monotone polygon can be triangulated in O(n) time.  Algorithms within this package may decompose
 * a polygon into monotone pieces, which are then used to decompose into triangles.
 * @author William Bittle
 * @version 3.2.0
 * @since 2.2.0
 * @param <E> the vertex data type
</E> */
class MonotonePolygon<E>(type: MonotonePolygonType, vertices: MutableList<MonotoneVertex<E>>) {

    /** The type of monotone polygon  */
    val type: MonotonePolygonType

    /** The sorted array of vertices  */
    val vertices: MutableList<MonotoneVertex<E>>

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("MonotonePolygon[Type=").append(type)
        sb.append("|Vertices={")
        val size = vertices!!.size
        for (i in 0 until size) {
            if (i != 0) sb.append(",")
            sb.append(vertices[i])
        }
        sb.append("}]")
        return sb.toString()
    }

    /**
     * Returns the maximum vertex in the sorted array.
     * @return [MonotoneVertex]
     */
    val maximum: MonotoneVertex<E>
        get() = vertices!![0]

    /**
     * Returns the minimum vertex in the sorted array.
     * @return [MonotoneVertex]
     */
    val minimum: MonotoneVertex<E>
        get() = vertices!![vertices.size - 1]

    /**
     * Full constructor.
     * @param type the monotone polygon type
     * @param vertices the sorted array of vertices; descending order
     */
    init {
        this.type = type
        this.vertices = vertices
    }
}