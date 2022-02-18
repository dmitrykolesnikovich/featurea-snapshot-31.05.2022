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
import org.dyn4j.resources.message

/**
 * Implementation of the Gift Wrapping convex hull algorithm.
 *
 *
 * This algorithm handles coincident and colinear points by ignoring them during processing. This ensures
 * the produced hull will not have coincident or colinear vertices.
 *
 *
 * This algorithm is O(nh) worst case where n is the number of points and h is the
 * number of sides in the resulting convex hull.
 * @author William Bittle
 * @version 3.4.0
 * @since 2.2.0
 */
class GiftWrap : HullGenerator {
    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.hull.HullGenerator#generate(org.dyn4j.geometry.Vector2[])
	 */
    override fun generate(vararg points: Vector2): Array<out Vector2?> {
        // check for null array
        if (points == null) throw NullPointerException(message("geometry.hull.nullArray"))

        // get the size
        val size = points.size
        // check the size
        if (size <= 2) return points

        // find the left most point
        var x = Double.MAX_VALUE
        var y = Double.MAX_VALUE
        var leftMost: Vector2? = null
        for (i in 0 until size) {
            val p = points[i] ?: throw NullPointerException(message("geometry.hull.nullPoints"))
            // check for null points
            // check the x cooridate
            if (p.x < x) {
                x = p.x
                leftMost = p
                y = p.y
            } else if (p.x == x && p.y < y) {
                x = p.x
                leftMost = p
                y = p.y
            }
        }
        var current = leftMost

        // use a linked hash set to maintain insertion order
        // but also to have the set property of no duplicates
        val hull: MutableSet<Vector2?> =
            LinkedHashSet()
        do {
            hull.add(current)
            // check all the points to see if anything is more left than the next point
            var next = points[0]
            if (current === next) next = points[1]
            // loop over the points to find a more left point than the current
            for (j in 1 until size) {
                val test = points[j]
                if (test === current) continue
                if (test === next) continue
                // check the point relative to the current line
                // Use the robust side of line test because otherwise this algorithm
                // can fall in an endless loop
                val location = getLocation(test!!, current!!, next!!)
                if (location < 0.0) {
                    next = test
                } else if (location == 0.0) {
                    // in the case of colinear or coincident verticies
                    // only select this vertex if it's farther away
                    // than the current vertex
                    val d1 = test.distanceSquared(current)
                    val d2 = next.distanceSquared(current)
                    // we also need to confirm that it's farther away in
                    // the direction of the current->next vector
                    val dot: Double = current.to(next).dot(current.to(test))
                    if (d1 > d2 && dot >= 0) {
                        next = test
                    } else {
                        // if it's not farther, compute the winding
                        val l1: Vector2 = current.to(next)
                        val l2: Vector2 = next.to(test)
                        val cross = l1.cross(l2)

                        // if the winding is anti-clockwise but the location test
                        // yielded they were colinear, then we encountered 
                        // sufficient numeric error - trust the winding
                        // in these cases
                        if (cross < 0.0) {
                            next = test
                        }
                    }
                }
            }
            current = next
            // loop until we repeat the first leftMost point
        } while (leftMost !== current)

        // copy the list into an array
        val hullPoints: Array<Vector2?> = hull.toTypedArray()

        // return the array
        return hullPoints
    }
}