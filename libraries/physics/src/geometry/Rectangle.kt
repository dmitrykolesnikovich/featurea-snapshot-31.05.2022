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
import kotlin.math.abs
import kotlin.math.atan2

/**
 * Implementation of a Rectangle [Convex] [Shape].
 *
 *
 * This class represents both axis-aligned and oriented rectangles and squares.
 *
 *
 * A [Rectangle] must have a width and height greater than zero.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 */
class Rectangle private constructor(valid: Boolean, val width: Double, val height: Double, vertices: Array<Vector2>) :
    Polygon(
        Vector2(),
        vertices.get(0).magnitude,
        vertices,
        arrayOf(Vector2(0.0, -1.0), Vector2(1.0, 0.0), Vector2(0.0, 1.0), Vector2(-1.0, 0.0))
    ), Convex, Wound, Shape, Transformable, DataContainer {

    /**
     * Full constructor.
     *
     *
     * The center of the rectangle will be the origin.
     *
     *
     * A rectangle must have a width and height greater than zero.
     * @param width the width
     * @param height the height
     * @throws IllegalArgumentException if width or height is less than or equal to zero
     */
    constructor(width: Double, height: Double) : this(
        validate(width, height), width, height, arrayOf<Vector2>(
            Vector2(-width * 0.5, -height * 0.5),
            Vector2(width * 0.5, -height * 0.5),
            Vector2(width * 0.5, height * 0.5),
            Vector2(-width * 0.5, height * 0.5)
        )
    )

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Wound#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Rectangle[").append(super.toString())
            .append("|Width=").append(this.width)
            .append("|Height=").append(this.height)
            .append("]")
        return sb.toString()
    }// when the shape is created normals[1] will always be the positive x-axis
    // we can get the rotation by comparing it to the positive x-axis
    // since the normal vectors are rotated with the vertices when
    // a shape is rotated

    /**
     * Returns the rotation about the local center in radians in the range [-, ].
     * @return double the rotation in radians
     * @since 3.0.1
     */
    val rotationAngle: Double
        get() {
            // when the shape is created normals[1] will always be the positive x-axis
            // we can get the rotation by comparing it to the positive x-axis
            // since the normal vectors are rotated with the vertices when
            // a shape is rotated
            return atan2(this.normals.get(1).y, this.normals.get(1).x)
        }// normals[1] is already a unit vector representing the local axis so we can just return it as a {@link Rotation}

    /**
     * @return the [Rotation] object that represents the local rotation
     */
    val rotation: Rotation
        get() {
            // normals[1] is already a unit vector representing the local axis so we can just return it as a {@link Rotation}
            return Rotation(this.normals.get(1).x, this.normals.get(1).y)
        }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Polygon#getAxes(java.util.List, org.dyn4j.geometry.Transform)
	 */
    override fun getAxes(foci: Array<Vector2>?, transform: Transform): Array<Vector2>? {
        // get the number of foci
        val fociSize = foci?.size ?: 0
        // create an array to hold the axes
        val axes = arrayOfNulls<Vector2>(2 + fociSize) as Array<Vector2>
        var n = 0
        // return the normals to the surfaces, since this is a 
        // rectangle we only have two axes to test against
        axes[n++] = transform.getTransformedR(this.normals.get(1))
        axes[n++] = transform.getTransformedR(this.normals.get(2))
        // get the closest point to each focus
        for (i in 0 until fociSize) {
            // get the current focus
            val focus: Vector2 = foci!![i]!!
            // create a place for the closest point
            var closest: Vector2 = transform.getTransformed(this.vertices.get(0))
            var d: Double = focus.distanceSquared(closest)
            // find the minimum distance vertex
            for (j in 1..3) {
                // get the vertex
                var vertex: Vector2 = this.vertices.get(j)
                // transform it into world space
                vertex = transform.getTransformed(vertex)
                // get the squared distance to the focus
                val dt: Double = focus.distanceSquared(vertex)
                // compare with the last distance
                if (dt < d) {
                    // if its closer then save it
                    closest = vertex
                    d = dt
                }
            }
            // once we have found the closest point create 
            // a vector from the focal point to the point
            val axis: Vector2 = focus.to(closest)
            // normalize the axis
            axis.normalize()
            // add it to the array
            axes[n++] = axis
        }
        // return all the axes
        return axes
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Polygon#contains(org.dyn4j.geometry.Vector, org.dyn4j.geometry.Transform)
	 */
    override fun contains(point: Vector2, transform: Transform): Boolean {
        // put the point in local coordinates
        val p: Vector2 = transform.getInverseTransformed(point)
        // get the center and vertices
        val c: Vector2 = this.center
        val p1: Vector2 = this.vertices.get(0)
        val p2: Vector2 = this.vertices.get(1)
        val p4: Vector2 = this.vertices.get(3)
        // get the width and height squared
        val widthSquared: Double = p1.distanceSquared(p2)
        val heightSquared: Double = p1.distanceSquared(p4)
        // i could call the polygon one instead of this method, but im not sure which is faster
        val projectAxis0: Vector2 = p1.to(p2)
        val projectAxis1: Vector2 = p1.to(p4)
        // create a vector from the centroid to the point
        val toPoint: Vector2 = c.to(p)
        // find the projection of this vector onto the vector from the
        // centroid to the edge
        if (toPoint.project(projectAxis0).magnitudeSquared <= widthSquared * 0.25) {
            // if the projection of the v vector onto the x separating axis is
            // smaller than the half width then we know that the point is within the
            // x bounds of the rectangle
            if (toPoint.project(projectAxis1).magnitudeSquared <= heightSquared * 0.25) {
                // if the projection of the v vector onto the y separating axis is 
                // smaller than the half height then we know that the point is within
                // the y bounds of the rectangle
                return true
            }
        }
        // return null if they do not intersect
        return false
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Polygon#project(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun project(vector: Vector2, transform: Transform): Interval {
        // get the center and vertices
        val center: Vector2 = transform.getTransformed(this.center)
        // create the project axes
        val projectAxis0: Vector2 = transform.getTransformedR(this.normals.get(1))
        val projectAxis1: Vector2 = transform.getTransformedR(this.normals.get(2))
        // project the shape on the axis
        val c: Double = center.dot(vector)
        val e: Double =
            this.width * 0.5 * abs(projectAxis0.dot(vector)) + this.height * 0.5 * abs(
                projectAxis1.dot(vector)
            )
        return Interval(c - e, c + e)
    }

    /**
     * Creates a [Mass] object using the geometric properties of
     * this [Rectangle] and the given density.
     *
     *  m = d * h * w
     * I = m * (h<sup>2</sup> + w<sup>2</sup>) / 12
     * @param density the density in kg/m<sup>2</sup>
     * @return [Mass] the [Mass] of this [Rectangle]
     */
    override fun createMass(density: Double): Mass {
        val height: Double = this.height
        val width: Double = this.width
        // compute the mass
        val mass = density * height * width
        // compute the inertia tensor
        val inertia = mass * (height * height + width * width) / 12.0
        // since we know that a rectangle has only four points that are
        // evenly distributed we can feel safe using the averaging method 
        // for the centroid
        return Mass(this.center, mass, inertia)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#createAABB(org.dyn4j.geometry.Transform)
	 */
    override fun createAABB(transform: Transform): AABB {
        // since we know that this is a rectangle we can get away with much fewer
        // comparisons to find the correct AABB. Each vertex maps to one point of the
        // AABB, we have to find in which of the four possible rotation states this
        // rectangle currently is. This is done below by comparing the first two vertices

        // It's more convenient to use transform.getTransformed instead but we can
        // split to transform.getTransformedX/Y to save 4 Vector2 allocations 'for free'
        val v0x: Double = transform.getTransformedX(this.vertices.get(0))
        val v0y: Double = transform.getTransformedY(this.vertices.get(0))
        val v1x: Double = transform.getTransformedX(this.vertices.get(1))
        val v1y: Double = transform.getTransformedY(this.vertices.get(1))
        val v2x: Double = transform.getTransformedX(this.vertices.get(2))
        val v2y: Double = transform.getTransformedY(this.vertices.get(2))
        val v3x: Double = transform.getTransformedX(this.vertices.get(3))
        val v3y: Double = transform.getTransformedY(this.vertices.get(3))
        return if (v0y > v1y) {
            if (v0x < v1x) {
                AABB(v0x, v1y, v2x, v3y)
            } else {
                AABB(v1x, v2y, v3x, v0y)
            }
        } else {
            if (v0x < v1x) {
                AABB(v3x, v0y, v1x, v2y)
            } else {
                AABB(v2x, v3y, v0x, v1y)
            }
        }
    }

    companion object {
        /**
         * Validates the constructor input returning true if valid or throwing an exception if invalid.
         * @param width the width
         * @param height the height
         * @return boolean true
         * @throws IllegalArgumentException if width or height is less than or equal to zero
         */
        private fun validate(width: Double, height: Double): Boolean {
            if (width <= 0.0) throw IllegalArgumentException(message("geometry.rectangle.invalidWidth"))
            if (height <= 0.0) throw IllegalArgumentException(message("geometry.rectangle.invalidHeight"))
            return true
        }
    }

}