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

import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Shape
import org.dyn4j.geometry.Transform
import org.dyn4j.geometry.Vector2

/**
 * Represents the Minkowski sum of the given [Convex] [Shape]s.
 *
 *
 * This class is used by the [Gjk] and [Epa] classes to compute support points.
 *
 *
 * This class doesn't actually compute the Minkowski sum.
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 */
class MinkowskiSum(convex1: Convex, transform1: Transform, convex2: Convex, transform2: Transform) {
    /** The first [Convex]  */
    val convex1: Convex

    /** The second [Convex]  */
    val convex2: Convex

    /** The first [Convex]'s [Transform]  */
    val transform1: Transform

    /** The second [Convex]'s [Transform]  */
    val transform2: Transform

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("MinkowskiSum[Convex1=").append(convex1.hashCode())
            .append("|Transform1=").append(transform1)
            .append("|Convex2=").append(convex2.hashCode())
            .append("|Transform2=").append(transform2)
            .append("]")
        return sb.toString()
    }

    /**
     * Returns the farthest point in the Minkowski sum given the direction.
     * @param direction the search direction
     * @return [Vector2] the point farthest in the Minkowski sum in the given direction
     */
    fun getSupportPoint(direction: Vector2): Vector2 {
        // get the farthest point in the given direction in convex1
        val point1: Vector2 = convex1.getFarthestPoint(direction, transform1)
        direction.negate()
        // get the farthest point in the opposite direction in convex2
        val point2: Vector2 = convex2.getFarthestPoint(direction, transform2)
        direction.negate()
        // return the Minkowski sum point
        return point1.subtract(point2)
    }

    /**
     * Returns the farthest point, and the support points in the shapes, in the Minkowski sum given the direction.
     * @param direction the search direction
     * @return [MinkowskiSumPoint] the point farthest in the Minkowski sum in the given direction
     */
    fun getSupportPoints(direction: Vector2): MinkowskiSumPoint {
        // get the farthest point in the given direction in convex1
        val point1: Vector2 = convex1.getFarthestPoint(direction, transform1)
        direction.negate()
        // get the farthest point in the opposite direction in convex2
        val point2: Vector2 = convex2.getFarthestPoint(direction, transform2)
        direction.negate()
        // set the Minkowski sum point given the support points
        return MinkowskiSumPoint(point1, point2)
    }

    /**
     * Full constructor.
     * @param convex1 the first [Convex]
     * @param transform1 the first [Convex]'s [Transform]
     * @param convex2 the second [Convex]
     * @param transform2 the second [Convex]'s [Transform]
     */
    init {
        this.convex1 = convex1
        this.convex2 = convex2
        this.transform1 = transform1
        this.transform2 = transform2
    }
}