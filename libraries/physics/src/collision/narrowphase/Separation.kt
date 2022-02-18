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
import org.dyn4j.geometry.Vector2

/**
 * Represents a [Separation] of one [Convex] [Shape] between another.
 *
 *
 * The separation normal should always be normalized.
 * @author William Bittle
 * @version 3.0.2
 * @since 1.0.0
 */
class Separation {
    /** The normalized axis of separation  */
    var normal: Vector2? = null

    /**
     * Returns the separation distance.
     * @return double
     */
    /**
     * Sets the separation distance.
     * @param distance the separation distance
     */
    /** The separating distance along the axis  */
    var distance = 0.0

    /** The closest point on the first [Convex] [Shape] to the second  */
    var point1: Vector2? = null

    /** The closest point on the second [Convex] [Shape] to the first  */
    var point2: Vector2? = null

    /**
     * Default constructor.
     */
    constructor() {}

    /**
     * Full constructor.
     * @param normal the penetration normal
     * @param distance the separation distance
     * @param point1 the closest point on the first [Convex] [Shape] to the second
     * @param point2 the closest point on the second [Convex] [Shape] to the first
     */
    constructor(normal: Vector2, distance: Double, point1: Vector2?, point2: Vector2?) {
        this.normal = normal
        this.distance = distance
        this.point1 = point1
        this.point2 = point2
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Separation[Point1=").append(point1)
            .append("|Point2=").append(point2)
            .append("|Normal=").append(normal)
            .append("|Distance=").append(distance)
            .append("]")
        return sb.toString()
    }

    /**
     * Clears the separation information.
     */
    fun clear() {
        normal = null
        distance = 0.0
        point1 = null
        point2 = null
    }

}