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
 * Represents a geometric [Shape].
 *
 *
 * The [Shape] class implements the [Transformable] interface and modifies the
 * internal state of the [Shape] directly (translating the vertices for example).
 *
 *
 * The various implementing classes may allow mutation of the shape indirectly by returning
 * mutable objects.  It's recommended that a [Shape], after creation and use, remain
 * unchanged and instead be replaced with a new [Shape] if modification is necessary.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 */
interface Shape : Transformable, DataContainer {
    /**
     * Returns the center/centroid of the [Shape] in local coordinates.
     * @return [Vector2]
     */
    var center: Vector2

    /**
     * Returns the maximum radius of the shape from the center.
     * @return double
     * @since 2.0.0
     */
    var radius: Double

    /**
     * Returns the radius of the shape if the given point was the
     * center for this shape.
     * @param center the center point
     * @return double
     * @throws NullPointerException if the given point is null
     * @since 3.0.2
     */
    fun getRadius(center: Vector2): Double

    /**
     * Rotates the [Shape] about it's center.
     *
     *
     * This method replaced the overriding functionality of the
     * rotate method from the [Transformable] interface.
     * @param theta the rotation angle in radians
     * @since 3.1.1
     */
    fun rotateAboutCenter(theta: Double)

    /**
     * Returns the [Interval] of this [Shape] projected onto the given [Vector2]
     * given the [Transform].
     *
     *
     * This is the same as calling [.project] and passing a new [Transform].
     * @param vector [Vector2] to project onto
     * @return [Interval]
     * @throws NullPointerException if the given vector is null
     * @since 3.1.5
     */
    fun project(vector: Vector2): Interval

    /**
     * Returns the [Interval] of this [Shape] projected onto the given [Vector2]
     * given the [Transform].
     * @param vector [Vector2] to project onto
     * @param transform [Transform] for this [Shape]
     * @return [Interval]
     * @throws NullPointerException if the given vector or transform is null
     */
    fun project(vector: Vector2, transform: Transform): Interval

    /**
     * Returns true if the given point is inside this [Shape].
     *
     *
     * If the given point lies on an edge the point is considered
     * to be inside the [Shape].
     *
     *
     * The given point is assumed to be in world space.
     *
     *
     * This is the same as calling [.contains] and passing a new [Transform].
     * @param point world space point
     * @return boolean
     * @throws NullPointerException if the given point is null
     * @since 3.1.5
     */
    operator fun contains(point: Vector2): Boolean

    /**
     * Returns true if the given point is inside this [Shape].
     *
     *
     * If the given point lies on an edge the point is considered
     * to be inside the [Shape].
     *
     *
     * The given point is assumed to be in world space.
     * @param point world space point
     * @param transform [Transform] for this [Shape]
     * @throws NullPointerException if the given point or transform is null
     * @return boolean
     */
    fun contains(point: Vector2, transform: Transform): Boolean

    /**
     * Creates a [Mass] object using the geometric properties of
     * this [Shape] and the given density.
     * @param density the density in kg/m<sup>2</sup>
     * @return [Mass] the [Mass] of this [Shape]
     */
    fun createMass(density: Double): Mass

    /**
     * Creates an [AABB] from this [Shape].
     *
     *
     * This is the same as calling [.createAABB] and passing a new [Transform].
     * @return [AABB] the [AABB] enclosing this [Shape]
     * @since 3.1.4
     */
    fun createAABB(): AABB

    /**
     * Creates an [AABB] from this [Shape] after applying the given
     * transformation to the shape.
     * @param transform the [Transform] for this [Shape]
     * @return [AABB] the [AABB] enclosing this [Shape]
     * @throws NullPointerException if the given transform is null
     * @since 3.0.0
     */
    fun createAABB(transform: Transform): AABB
}