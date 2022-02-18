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

import org.dyn4j.geometry.RobustGeometry.getLocation
import org.dyn4j.geometry.Segment
import org.dyn4j.geometry.Vector2
import kotlin.math.sign

/**
 * Represents a vertex on a polygon that stores information
 * about the left and right edges and left and right vertices.
 * @author William Bittle
 * @version 3.2.0
 * @since 2.2.0
 */
class SweepLineVertex : Comparable<SweepLineVertex>  {

    /** The vertex point  */
    var point: Vector2

    /** The index in the original simple polygon  */
    var index = 0

    /** The vertex type  */
    var type: SweepLineVertexType? = null

    /** The next vertex in Counter-Clockwise order  */
    var next: SweepLineVertex? = null

    /** The previous vertex in Counter-Clockwise order  */
    var prev: SweepLineVertex? = null

    /** The next edge in Counter-Clockwise order  */
    var left: SweepLineEdge? = null

    /** The previous edge in Counter-Clockwise order  */
    var right: SweepLineEdge? = null

    /**
     * Minimal constructor.
     * @param point the vertex point
     * @param index the index in the original simple polygon
     */
    constructor(point: Vector2, index: Int) {
        this.point = point
        this.index = index
    }

    /* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
    override operator fun compareTo(other: SweepLineVertex): Int {
        // sort by the y first then by x if the y's are equal
        val p = point
        val q = other.point
        val diff: Double = q.y - p.y
        return if (diff == 0.0) {
            // if the difference is near equal then compare the x values
            (p.x - q.x).sign.toInt()
        } else {
            diff.sign.toInt()
        }
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        return point.toString()
    }

    /**
     * Returns true if this [SweepLineVertex] is left of the given [SweepLineEdge].
     * @param edge the [SweepLineEdge]
     * @return boolean true if this [SweepLineVertex] is to the left of the given [SweepLineEdge]
     */
    fun isLeft(edge: SweepLineEdge): Boolean {
        // its in between the min and max x so we need to
        // do a side of line test
        val location: Double = getLocation(point, edge.v0!!.point, edge.v1!!.point)
        return if (location < 0.0) {
            true
        } else {
            false
        }
    }

    /**
     * Returns true if the interior is to the right of this vertex.
     *
     *
     * The left edge of this vertex is used to determine where the
     * interior of the polygon is.
     * @return boolean
     */
    fun isInteriorRight(): Boolean {
        return left!!.isInteriorRight
    }

}