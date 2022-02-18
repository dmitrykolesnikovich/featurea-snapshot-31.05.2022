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

import org.dyn4j.resources.message

/**
 * Implementation of a ray.
 *
 *
 * A ray is a vector with a start point.
 * @author William Bittle
 * @version 3.4.0
 * @since 2.0.0
 */
open class Ray(start: Vector2, direction: Vector2) {

    /** The start point  */
    var start: Vector2? = null
        set(value) {
            if (value == null) throw NullPointerException(message("geometry.ray.nullStart"))
            field = value
        }

    /** The direction  */
    var direction: Vector2? = null
        set(value) {
            if (value == null) throw NullPointerException(message("geometry.ray.nullDirection"))
            if (value.isZero) throw IllegalArgumentException(message("geometry.ray.zeroDirection"))
            field = Vector2(value)
        }

    /**
     * Creates a ray from the origin in the given direction.
     * @param direction the direction in radians
     * @since 3.0.2
     */
    constructor(direction: Double) : this(Vector2(direction)) {}

    /**
     * Creates a ray from the origin in the given direction.
     * @param direction the direction
     */
    constructor(direction: Vector2) : this(Vector2(), direction) {}

    /**
     * Creates a ray from the given start point in the given direction.
     * @param start the start point
     * @param direction the direction in radians
     * @since 3.0.2
     */
    constructor(start: Vector2, direction: Double) : this(start, Vector2(direction)) {}

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Ray[Start=").append(start)
            .append("|Direction=").append(getDirectionAngle())
            .append("]")
        return sb.toString()
    }

    /**
     * Returns the direction of this ray in radians.
     * @return double the direction in radians between [-, ]
     * @since 3.0.2
     */
    fun getDirectionAngle(): Double = direction!!.direction

    /**
     * Returns the direction.
     * @return [Vector2]
     * @since 3.0.2
     */
    val directionVector: Vector2
        get() = direction!!

    /**
     * Creates a ray from the given start point in the given direction.
     * @param start the start point
     * @param direction the direction
     * @throws NullPointerException if start or direction is null
     * @throws IllegalArgumentException if direction is the zero vector
     */
    init {
        if (direction.isZero) throw IllegalArgumentException(message("geometry.ray.zeroDirection"))
        this.start = start
        this.direction = direction.normalized
    }

}
