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

import org.dyn4j.DataContainer

/**
 * Base implementation of the [Shape] interface.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 */
abstract class AbstractShape : Shape, Transformable, DataContainer {

    /** Identity Transform instance  */
    private val IDENTITY = Transform()

    /** The center of this [Shape]  */
    override lateinit var center: Vector2

    /** The maximum radius  */
    override var radius = 0.0

    /** Custom user data object  */
    override var userData: Any? = null

    /**
     * Minimal constructor.
     * @param radius the rotation radius; must be greater than zero
     * @throws IllegalArgumentException if radius is zero or less
     */
    constructor(radius: Double) : this(Vector2(), radius)

    /**
     * Full constructor.
     * @param center the center
     * @param radius the rotation radius; must be greater than zero
     * @throws IllegalArgumentException if radius is zero or less
     * @throws NullPointerException if center is null
     */
    constructor(center: Vector2, radius: Double) {
        this.center = center
        this.radius = radius
    }

    /* (non-Javadoc)
	 * @see Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("HashCode=").append(this.hashCode())
            .append("|Center=").append(center)
            .append("|Radius=").append(radius)
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#rotate(double)
	 */
    override fun rotate(theta: Double) {
        this.rotate(theta, 0.0, 0.0)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#rotate(org.dyn4j.geometry.Rotation)
	 */
    override fun rotate(rotation: Rotation) {
        this.rotate(rotation, 0.0, 0.0)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#rotateAboutCenter(double)
	 */
    override fun rotateAboutCenter(theta: Double) {
        this.rotate(theta, center!!.x, center!!.y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(double, org.dyn4j.geometry.Vector)
	 */
    override fun rotate(theta: Double, point: Vector2) {
        this.rotate(theta, point.x, point.y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(org.dyn4j.geometry.Rotation, org.dyn4j.geometry.Vector)
	 */
    override fun rotate(rotation: Rotation, point: Vector2) {
        this.rotate(rotation, point.x, point.y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(double, double, double)
	 */
    override fun rotate(theta: Double, x: Double, y: Double) {
        this.rotate(Rotation(theta), x, y)
    }

    /*
	 * Subclasses of {@link AbstractShape} should override just this method
	 * if they need to perform additional operations on rotations.
	 */
    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(org.dyn4j.geometry.Rotation, double, double)
	 */
    override fun rotate(rotation: Rotation, x: Double, y: Double) {
        // only rotate the center if the point about which
        // we are rotating is not the center
        if (!center!!.equals(x, y)) {
            center!!.rotate(rotation!!, x, y)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#translate(double, double)
	 */
    override fun translate(x: Double, y: Double) {
        center!!.add(x, y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#translate(org.dyn4j.geometry.Vector)
	 */
    override fun translate(vector: Vector2) {
        this.translate(vector.x, vector.y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#project(org.dyn4j.geometry.Vector2)
	 */
    override fun project(n: Vector2): Interval {
        return this.project(n!!, IDENTITY)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#contains(org.dyn4j.geometry.Vector2)
	 */
    override operator fun contains(point: Vector2): Boolean {
        return this.contains(point!!, IDENTITY)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#createAABB()
	 */
    override fun createAABB(): AABB {
        return this.createAABB(IDENTITY)
    }
}