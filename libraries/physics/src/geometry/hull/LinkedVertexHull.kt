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
package org.dyn4j.geometry.hull

import org.dyn4j.geometry.RobustGeometry.getLocation
import org.dyn4j.geometry.Vector2

/**
 * Represents a convex hull of [LinkedVertex]es.
 *
 *
 * The root vertex can be any point on the hull.
 * @author William Bittle
 * @version 3.4.0
 * @since 3.2.0
 */
class LinkedVertexHull {

    /** The vertex that has the smallest x coordinate  */
    var leftMost: LinkedVertex? = null

    /** The vertex that has the largest x coordinate  */
    var rightMost: LinkedVertex? = null

    /** The total number of vertices on the hull  */
    var size = 0

    /** Default constructor  */
    constructor() {}

    /**
     * Create a convex [LinkedVertexHull] of one point.
     * @param point the point
     */
    constructor(point: Vector2?) {
        val root = LinkedVertex(point!!)
        leftMost = root
        rightMost = root
        size = 1
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("LinkedVertexHull[Size=").append(size)
            .append("|LeftMostPoint=").append(leftMost!!.point)
            .append("|RightMostPoint=").append(rightMost!!.point)
        return sb.toString()
    }

    /**
     * Returns a new array representing this convex hull.
     * @return [Vector2][]
     */
    fun toArray(): Array<Vector2> {
        val points = arrayOf<Vector2>()
        var vertex = leftMost
        for (i in 0 until size) {
            points[i] = vertex!!.point
            vertex = vertex.next
        }
        return points
    }

    companion object {
        /**
         * Merges the two given convex [LinkedVertexHull]s into one convex [LinkedVertexHull].
         *
         *
         * The left [LinkedVertexHull] should contain only points whose x coordinates are
         * less than all the points in the right [LinkedVertexHull].
         * @param left the left convex [LinkedVertexHull]
         * @param right the right convex [LinkedVertexHull]
         * @return [LinkedVertexHull] the merged convex hull
         */
        fun merge(
            left: LinkedVertexHull,
            right: LinkedVertexHull
        ): LinkedVertexHull {
            // This merge algorithm handles all cases, including point-point and point-segment without special cases.
            // It finds the upper and lower edges that connect the two hulls such that the resulting hull remains convex
            val hull = LinkedVertexHull()
            hull.leftMost = left.leftMost
            hull.rightMost = right.rightMost
            var lu = left.rightMost
            var ru = right.leftMost

            // We don't use strict inequalities when checking the result of getLocation
            // so we can remove coincident points in the hull.
            // As a result we must limit the number of loops that go to the left or right
            // because else ru = ru.prev can loop over and never terminate
            // We can walk at most side.size - 1 before looping over
            var limitRightU = right.size - 1
            var limitLeftU = left.size - 1
            while (true) {
                val prevLu = lu
                val prevRu = ru
                while (limitRightU > 0 && getLocation(ru!!.next!!.point, lu!!.point, ru.point) <= 0) {
                    ru = ru.next
                    limitRightU--
                }
                while (limitLeftU > 0 && getLocation(lu!!.prev!!.point, lu.point, ru!!.point) <= 0) {
                    lu = lu.prev
                    limitLeftU--
                }

                // If no progress is made there's nothing else to do
                if (lu == prevLu && ru == prevRu) {
                    break
                }
            }

            // Same as before, for the other side
            var ll = left.rightMost
            var rl = right.leftMost
            var limitRightL = right.size - 1
            var limitLeftL = left.size - 1
            while (true) {
                val prevLl = ll
                val prevRl = rl
                while (limitRightL > 0 && getLocation(rl!!.prev!!.point, ll!!.point, rl.point) >= 0) {
                    rl = rl.prev
                    limitRightL--
                }
                while (limitLeftL > 0 && getLocation(ll!!.next!!.point, ll.point, rl!!.point) >= 0) {
                    ll = ll.next
                    limitLeftL--
                }

                // If no progress is made there's nothing else to do
                if (ll == prevLl && rl == prevRl) {
                    break
                }
            }

            // link the hull
            lu!!.next = ru
            ru!!.prev = lu
            ll!!.prev = rl
            rl!!.next = ll

            // We could compute size with a closed-form type based on the four values
            // of limitLeft/Right/L/U but it is not straightforward and there is no observable
            // speed gain. So use a simple loop instead
            var size = 0
            var v = lu
            do {
                size++
                v = v!!.next
            } while (v != lu)

            // set the size
            hull.size = size

            // return the merged hull
            return hull
        }
    }
}