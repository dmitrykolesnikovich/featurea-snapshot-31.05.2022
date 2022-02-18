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
import org.dyn4j.resources.message
import kotlin.math.PI

/**
 * Implementation of a Circle [Convex] [Shape].
 *
 *
 * A [Circle]'s radius must be greater than zero.
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 */
class Circle private constructor(valid: Boolean, radius: Double) : AbstractShape(radius), Convex, Shape, Transformable,
    DataContainer {
    /**
     * Full constructor.
     *
     *
     * Creates a new [Circle] centered on the origin with the given radius.
     * @param radius the radius
     * @throws IllegalArgumentException if the given radius is less than or equal to zero
     */
    constructor(radius: Double) : this(validate(radius), radius)

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#getRadius(org.dyn4j.geometry.Vector2)
	 */
    override fun getRadius(center: Vector2): Double {
        return radius + center.distance(this.center)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Wound#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Circle[").append(super.toString())
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#contains(org.dyn4j.geometry.Vector, org.dyn4j.geometry.Transform)
	 */
    override fun contains(point: Vector2, transform: Transform): Boolean {
        // transform the center
        val v: Vector2 = transform.getTransformed(center)
        // get the transformed radius squared
        val radiusSquared = radius * radius
        // create a vector from the center to the given point
        v.subtract(point)
        return if (v.magnitudeSquared <= radiusSquared) {
            true
        } else false
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#project(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun project(vector: Vector2, transform: Transform): Interval {
        // if the transform is not null then transform the center
        val center: Vector2 = transform.getTransformed(center)
        // project the center onto the given axis
        val c: Double = center.dot(vector)
        // the interval is defined by the radius
        return Interval(c - radius, c + radius)
    }

    /**
     * {@inheritDoc}
     *
     *
     * For a [Circle] this will always return a [PointFeature].
     */
    override fun getFarthestFeature(vector: Vector2, transform: Transform): PointFeature {
        // obtain the farthest point along the given vector
        val farthest: Vector2 = getFarthestPoint(vector, transform)
        // for a circle the farthest feature along a vector will always be a vertex
        return PointFeature(farthest)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFarthestPoint(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestPoint(vector: Vector2, transform: Transform): Vector2 {
        // make sure the axis is normalized
        val nAxis: Vector2 = vector.normalized
        // get the transformed center
        val center: Vector2 = transform.getTransformed(center)
        // add the radius along the vector to the center to get the farthest point
        center.x += radius * nAxis.x
        center.y += radius * nAxis.y
        // return the new point
        return center
    }

    /**
     * {@inheritDoc}
     *
     *
     * Circular shapes are handled specifically in the SAT algorithm since
     * they have an infinite number of axes. As a result this method returns
     * null.
     * @return null
     */
    override fun getAxes(foci: Array<Vector2>?, transform: Transform): Array<Vector2>? {
        // a circle has infinite separating axes and zero voronoi regions
        // therefore we return null
        return null
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFoci(org.dyn4j.geometry.Transform)
	 */
    override fun getFoci(transform: Transform): Array<Vector2>? {
        val foci = arrayOfNulls<Vector2>(1) as Array<Vector2>
        // a circle only has one focus
        foci[0] = transform.getTransformed(center)
        return foci
    }

    /**
     * {@inheritDoc}
     *
     *  m = d *  * r<sup>2</sup>
     * I = m * r<sup>2</sup> / 2
     */
    override fun createMass(density: Double): Mass {
        val r2 = radius * radius
        // compute the mass
        val mass: Double = density * PI * r2
        // compute the inertia tensor
        val inertia = mass * r2 * 0.5
        // use the center supplied to the circle
        return Mass(center, mass, inertia)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#createAABB(org.dyn4j.geometry.Transform)
	 */
    override fun createAABB(transform: Transform): AABB {
        // if the transform is not null then transform the center
        val center: Vector2 = transform.getTransformed(center)
        // return a new aabb
        return AABB(center, radius)
    }

    companion object {
        /**
         * Validates the constructor input returning true if valid or throwing an exception if invalid.
         * @param radius the radius
         * @return boolean true
         * @throws IllegalArgumentException if the given radius is less than or equal to zero
         */
        private fun validate(radius: Double): Boolean {
            if (radius <= 0.0) throw IllegalArgumentException(message("geometry.circle.invalidRadius"))
            return true
        }
    }
}