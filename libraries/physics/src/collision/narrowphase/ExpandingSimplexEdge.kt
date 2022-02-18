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
package org.dyn4j.collision.narrowphase

import org.dyn4j.geometry.Vector2
import kotlin.math.abs

/**
 * Represents an edge of an [ExpandingSimplex].
 *
 *
 * An [ExpandingSimplexEdge] tracks its vertices, the edge normal, and the
 * distance to the origin.
 *
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.2.0
 */
internal class ExpandingSimplexEdge(point1: Vector2, point2: Vector2, winding: Int) :
    Comparable<ExpandingSimplexEdge> {
    /** The first point of the edge  */
    val point1: Vector2

    /** The second point of the edge  */
    val point2: Vector2

    /** The normal of the edge  */
    val normal: Vector2

    /** The perpendicular distance from the edge to the origin  */
    val distance: Double

    /* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
    override operator fun compareTo(o: ExpandingSimplexEdge): Int {
        if (distance < o.distance) return -1
        return if (distance > o.distance) 1 else 0
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("ExpandingSimplexEdge[Point1=").append(point1)
            .append("|Point2=").append(point2)
            .append("|Normal=").append(normal)
            .append("|Distance=").append(distance)
            .append("]")
        return sb.toString()
    }

    /**
     * Minimal constructor.
     * @param point1 the first point
     * @param point2 the second point
     * @param winding the winding
     */
    init {
        // create the edge
        // inline b - a
        normal = Vector2(point2.x - point1.x, point2.y - point1.y)
        // depending on the winding get the edge normal
        // it would be better to use Vector.tripleProduct(ab, ao, ab);
        // where ab is the edge and ao is a.to(ORIGIN) but this will
        // return an incorrect normal if the origin lies on the ab segment
        // therefore we use the winding of the simplex to determine the 
        // normal direction
        if (winding < 0) {
            normal.right()
        } else {
            normal.left()
        }
        // normalize the vector
        normal.normalize()
        // project the first point onto the normal (it doesnt matter which
        // you project since the normal is perpendicular to the edge)
        //double d = Math.abs(a.dot(normal));
        distance = abs(point1.x * normal.x + point1.y * normal.y)
        this.point1 = point1
        this.point2 = point2
    }
}