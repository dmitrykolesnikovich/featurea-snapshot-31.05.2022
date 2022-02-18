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
 * Represents a [Convex] [Shape].
 *
 *
 * A [Convex] [Shape] is a [Shape] that given a line, the line will only
 * intersect at most 2 non-coincident non-colinear edges.
 *
 *
 * Working with convex shapes specifically allows collision detection algorithms to be very
 * fast.  If non-convex shapes are required, they are typically handled by attaching multiple
 * convex shapes together.
 * @author William Bittle
 * @version 1.0.3
 * @since 1.0.0
 */
interface Convex : Shape, Transformable, DataContainer {
    /**
     * Returns an array of separating axes to test for this [Shape].
     *
     *
     * The `foci` parameter is an array of **circular** focal points of the other [Shape].
     *
     *
     * If foci points are given, this method will return the separating axes for this [Shape]'s voronoi regions
     * also.  The points in the foci array are assumed to be in world space.
     *
     *
     * The returned axes are normalized and in world space.
     * @param foci the world space points representing foci of curved [Shape]s; can be null
     * @param transform the local to world space [Transform] of this [Convex] [Shape]
     * @return [Vector2][]
     * @throws UnsupportedOperationException if this shape doesn't support this method
     */
    fun getAxes(foci: Array<Vector2>?, transform: Transform): Array<Vector2>?

    /**
     * Returns an array of world space foci points for **circular** curved edges.
     *
     *
     * This method returns null if the [Shape] has zero curved edges.
     *
     *
     * The returned points are in world space.
     * @param transform the local to world space [Transform] of this [Convex] [Shape]
     * @return [Vector2][]
     * @throws UnsupportedOperationException if this shape doesn't support this method
     */
    fun getFoci(transform: Transform): Array<Vector2>?

    /**
     * Returns the [Feature] farthest in the direction of the given vector.
     *
     *
     * The returned feature is in world space.
     * @param vector the direction
     * @param transform the local to world space [Transform] of this [Convex] [Shape]
     * @return [Feature]
     */
    fun getFarthestFeature(vector: Vector2, transform: Transform): Feature

    /**
     * Returns the point farthest in the direction of the given vector.  If two points are
     * equally distant along the given [Vector2] the first one is used.
     *
     *
     * The returned point is in world space.
     * @param vector the direction
     * @param transform the local to world space [Transform] of this [Convex] [Shape]
     * @return [Vector2]
     */
    fun getFarthestPoint(vector: Vector2, transform: Transform): Vector2
}