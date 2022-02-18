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

import org.dyn4j.Reference
import org.dyn4j.geometry.Vector2

/**
 * Represents an edge of a polygon storing the next and previous edges
 * and the vertices that make up this edge.
 *
 *
 * The edge also stores a helper vertex which is used during y-monotone
 * decomposition.
 * @author William Bittle
 * @version 3.2.0
 * @since 2.2.0
 */
class SweepLineEdge(val referenceY: Reference<Double>) : Comparable<SweepLineEdge> {

    /** The first vertex of the edge in Counter-Clockwise order  */
    var v0: SweepLineVertex? = null

    /** The second vertex of the edge in Counter-Clockwise order  */
    var v1: SweepLineVertex? = null

    /** The helper vertex of this edge  */
    lateinit var helper: SweepLineVertex

    /**
     * The inverted slope of the edge (run/rise); This will be
     * Double.POSITIVE_INFINITY if its a horizontal edge
     */
    var slope = 0.0

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append(v0)
            .append(" to ")
            .append(v1)
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
    override operator fun compareTo(o: SweepLineEdge): Int {
        // check for equality
        if (this == o) return 0

        // compare the intersection of the sweep line and the edges
        // to see which is to the left or right
        val y = referenceY.value!!
        val x1 = getSortValue(y)
        val x2 = o.getSortValue(y)
        return if (x1 < x2) {
            -1
        } else {
            1
        }
    }

    /**
     * Returns the intersection point of the given y value (horizontal
     * sweep line) with this edge.
     *
     *
     * Returns the x value of the corresponding intersection point.
     * @param y the horizontal line y value
     * @return double
     */
    fun getSortValue(y: Double): Double {
        // get the minimum x vertex
        // (if we use the min x vertex rather than an 
        // arbitrary one, we can save a step to check
        // if the edge is vertical)
        var min: Vector2 = v0!!.point!!
        if (v1!!.point.x < v0!!.point.x) {
            min = v1!!.point
        }
        // check for a horizontal line
        return if (slope == Double.POSITIVE_INFINITY) {
            // for horizontal lines, use the min x
            min.x
        } else {
            // otherwise compute the intersection point
            min.x + (y - min.y) * slope
        }
    }// if they do, is the vector of the
    // two points to the right or to the left
    // otherwise just compare the y values
// check if the points have the same y value

    /**
     * Returns true if the interior of the polygon is
     * to the right of this edge.
     *
     *
     * Given that the polygon's vertex winding is Counter-
     * Clockwise, if the vertices that make this edge
     * decrease along the y axis then the interior of the
     * polygon is to the right, otherwise its to the
     * left.
     * @return boolean
     */
    val isInteriorRight: Boolean
        get() {
            val diff: Double = v0!!.point.y - v1!!.point.y
            // check if the points have the same y value
            return if (diff == 0.0) {
                // if they do, is the vector of the
                // two points to the right or to the left
                if (v0!!.point.x < v1!!.point.x) {
                    true
                } else {
                    false
                }
                // otherwise just compare the y values
            } else if (diff > 0.0) {
                true
            } else {
                false
            }
        }

}