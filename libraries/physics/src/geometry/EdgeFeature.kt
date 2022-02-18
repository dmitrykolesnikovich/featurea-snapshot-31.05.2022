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
package org.dyn4j.geometry

import kotlin.jvm.JvmField

/**
 * Implementation of an edge [Feature] of a [Shape].
 *
 *
 * An [EdgeFeature] represents a **linear** edge of a [Shape] connecting
 * two vertices.  It's not the intent of this class to represent curved edges.
 *
 *
 * The index is the index of the edge in the [Shape].
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 */
class EdgeFeature(vertex1: PointFeature, vertex2: PointFeature, max: PointFeature, edge: Vector2, index: Int) : Feature(index) {

    /** The first vertex of the edge  */
    @JvmField
    val vertex1: PointFeature

    /** The second vertex of the edge  */
    @JvmField
    val vertex2: PointFeature

    /** The vertex of maximum projection along a [Vector2]  */
    @JvmField
    val max: PointFeature

    /** The edge vector  */
    @JvmField
    val edge: Vector2

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("EdgeFeature[Vertex1=").append(vertex1)
            .append("|Vertex2=").append(vertex2)
            .append("|Edge=").append(edge)
            .append("|Max=").append(max)
            .append("|Index=").append(this.index)
            .append("]")
        return sb.toString()
    }

    /**
     * Returns the maximum point.
     * @return [PointFeature]
     */
    val maximum: PointFeature
        get() = max

    /**
     * Creates an edge feature.
     * @param vertex1 the first vertex of the edge
     * @param vertex2 the second vertex of the edge
     * @param max the maximum point
     * @param edge the vector representing the edge
     * @param index the index of the edge
     */
    init {
        this.vertex1 = vertex1
        this.vertex2 = vertex2
        this.edge = edge
        this.max = max
    }
}