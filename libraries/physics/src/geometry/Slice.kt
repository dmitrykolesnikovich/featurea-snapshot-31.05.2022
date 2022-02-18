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
import org.dyn4j.geometry.Geometry.getRotationRadius
import org.dyn4j.geometry.RobustGeometry.getLocation
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Implementation of a Slice [Convex] [Shape].
 *
 *
 * A slice is a piece of a [Circle].
 *
 *
 * This shape can represent any slice of a circle up to 180 degrees (half circle).
 * @author William Bittle
 * @version 3.4.0
 * @since 3.1.5
 */
class Slice : AbstractShape, Convex, Shape, Transformable, DataContainer {

    /** Half the total circular section in radians  */
    @JvmField
    var alpha = 0.0

    /** Cosine of half the total circular section in radians  */
    @JvmField
    var cosAlpha = 0.0

    /** The radius passed in at creation  */
    @JvmField
    var sliceRadius = 0.0

    /** The vertices of the slice  */
    @JvmField
    var vertices: Array<Vector2>

    /** The normals of the polygonal sides  */
    @JvmField
    var normals: Array<Vector2>

    /** The local rotation in radians  */
    @JvmField
    var rotation: Rotation? = null

    /**
     * Validated constructor.
     *
     *
     * This method creates a slice of a circle with the **circle center** at the origin
     * and half of theta below the x-axis and half above.
     * @param valid always true or this constructor would not be called
     * @param radius the radius of the circular section
     * @param theta the angular extent in radians; must be greater than zero and less than or equal to
     * @param center the center
     */
    private constructor(radius: Double, theta: Double, center: Vector2) :
            super(center, max(center.x, center.distance(Vector2(radius, 0.0).rotate(0.5 * theta)))) {
        sliceRadius = radius
        alpha = theta * 0.5

        // compute the triangular section of the pie (and cache cos(alpha))
        val x: Double = radius * cos(alpha).also({ cosAlpha = it })
        val y: Double = radius * sin(alpha)
        vertices = arrayOf( // the origin
            Vector2(),  // the top point
            Vector2(x, y),  // the bottom point
            Vector2(x, -y)
        )
        val v1 = vertices[1].to(vertices[0])
        val v2 = vertices[0].to(vertices[2])
        v1.left().normalize()
        v2.left().normalize()
        normals = arrayOf(v1, v2)

        // Initially the slice is aligned to the world space x axis
        rotation = Rotation()
    }

    /**
     * Full constructor.
     *
     *
     * This method creates a slice of a circle with the **circle center** at the origin
     * and half of theta below the x-axis and half above.
     * @param radius the radius of the circular section
     * @param theta the angular extent in radians; must be greater than zero and less than or equal to
     * @throws IllegalArgumentException throw if 1) radius is less than or equal to zero or 2) theta is less than or equal to zero or 3) theta is greater than 180 degrees
     */
    constructor(radius: Double, theta: Double) : this(
        radius,
        theta,
        Vector2(2.0 * radius * sin(theta * 0.5) / (1.5 * theta), 0.0)
    )

    /**
     * Validates the constructor input returning true if valid or throwing an exception if invalid.
     * @param radius the radius of the circular section
     * @param theta the angular extent in radians; must be greater than zero and less than or equal to
     * return true
     * @throws IllegalArgumentException throw if 1) radius is less than or equal to zero or 2) theta is less than or equal to zero or 3) theta is greater than 180 degrees
     */
    private fun validate(radius: Double, theta: Double): Boolean {
        // check the radius
        if (radius <= 0) throw IllegalArgumentException(message("geometry.slice.invalidRadius"))
        // check the theta
        if (theta <= 0 || theta > PI) throw IllegalArgumentException(message("geometry.slice.invalidTheta"))
        return true
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Wound#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Slice[").append(super.toString())
            .append("|Radius=").append(sliceRadius)
            .append("|Theta=").append(getTheta())
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getAxes(org.dyn4j.geometry.Vector2[], org.dyn4j.geometry.Transform)
	 */
    override fun getAxes(foci: Array<Vector2>?, transform: Transform): Array<Vector2>? {
        // get the size of the foci list
        val fociSize = foci?.size ?: 0
        // get the number of vertices this polygon has
        val size: Int = vertices.size
        // the axes of a polygon are created from the normal of the edges
        // plus the closest point to each focus
        val axes = arrayOfNulls<Vector2>(2 + fociSize) as Array<Vector2>
        var n = 0

        // add the normals of the sides
        axes[n++] = transform.getTransformedR(normals[0])
        axes[n++] = transform.getTransformedR(normals[1])

        // loop over the focal points and find the closest
        // points on the polygon to the focal points
        val focus = transform.getTransformed(vertices[0])
        for (i in 0 until fociSize) {
            // get the current focus
            val f = foci!![i]!!
            // create a place for the closest point
            var closest: Vector2 = focus
            var d = f.distanceSquared(closest)
            // find the minimum distance vertex
            for (j in 1 until size) {
                // get the vertex
                var p = vertices[j]
                // transform it into world space
                p = transform.getTransformed(p)
                // get the squared distance to the focus
                val dt = f.distanceSquared(p)
                // compare with the last distance
                if (dt < d) {
                    // if its closer then save it
                    closest = p
                    d = dt
                }
            }
            // once we have found the closest point create
            // a vector from the focal point to the point
            val axis: Vector2 = f.to(closest)
            // normalize it
            axis.normalize()
            // add it to the array
            axes[n++] = axis
        }
        // return all the axes
        return axes
    }

    /**
     * {@inheritDoc}
     *
     *
     * Returns a single point, the circle center.
     */
    override fun getFoci(transform: Transform): Array<Vector2>? {
        return arrayOf(
            transform.getTransformed(vertices[0])
        )
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFarthestPoint(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestPoint(vector: Vector2, transform: Transform): Vector2 {
        val localn = transform.getInverseTransformedR(vector!!)
        val localnRotated: Vector2

        // We need to normalize localn in order for localnRotated.x < cosAlpha to work
        // and we also use that to compute the farthest point in the circle part of the slice
        localn.normalize()

        // Include rotation if needed
        // Note that the vertices are already rotated and we need both the rotated and not rotated localn vector
        localnRotated = if (!rotation!!.isIdentity) {
            localn.copy().inverseRotate(rotation!!)
        } else {
            localn
        }

        // if (abs(angleBetween(localn, rotation)) < alpha)
        return if (localnRotated.x < cosAlpha) {
            val edge = vertices[0].dot(localn)
            var maxIndex = 0

            // Based on the sign of localnRotated.y we can rule out one vertex
            if (localnRotated.y < 0) {
                if (vertices[2].dot(localn) > edge) {
                    maxIndex = 2
                }
            } else {
                if (vertices[1].dot(localn) > edge) {
                    maxIndex = 1
                }
            }
            val point = Vector2(vertices[maxIndex])

            // transform the point into world space
            transform.transform(point)
            point
        } else {
            // NOTE: taken from Circle.getFarthestPoint with some modifications
            localn.multiply(sliceRadius).add(vertices[0])
            transform.transform(localn)
            localn
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFarthestFeature(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestFeature(vector: Vector2, transform: Transform): Feature {
        val localn = transform.getInverseTransformedR(vector)
        val localnRotated: Vector2

        // We need to normalize localn in order for localnRotated.x < cosAlpha to work
        // and we also use that to compute the farthest point in the circle part of the slice
        localn.normalize()

        // Include rotation if needed
        // Note that the vertices are already rotated and we need both the rotated and not rotated localn vector
        localnRotated = if (!rotation!!.isIdentity) {
            localn.copy().inverseRotate(rotation!!)
        } else {
            localn
        }

        // if (abs(angleBetween(localn, rotation)) < alpha)
        return if (localnRotated.x < cosAlpha) {
            // check if this section is nearly a half circle
            if (cosAlpha <= 1.0e-6) {
                // if so, we want to return the full back side
                return Segment.getFarthestFeature(
                    vertices[1],
                    vertices[2],
                    vector,
                    transform
                )
            }

            // otherwise check which side its on
            if (localnRotated.y > 0) {
                // then its the top segment
                Segment.getFarthestFeature(
                    vertices[0],
                    vertices[1],
                    vector,
                    transform
                )
            } else if (localnRotated.y < 0) {
                // then its the bottom segment
                Segment.getFarthestFeature(
                    vertices[0],
                    vertices[2],
                    vector,
                    transform
                )
            } else {
                // then its the tip point
                PointFeature(transform.getTransformed(vertices[0]))
            }
        } else {
            // taken from Slice::getFarthestPoint
            localn.multiply(sliceRadius).add(vertices[0])
            transform.transform(localn)
            PointFeature(localn)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#project(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun project(vector: Vector2, transform: Transform): Interval {
        // get the world space farthest point
        val p1 = this.getFarthestPoint(vector, transform)
        val p2 = this.getFarthestPoint(vector.negative, transform)
        // project the point onto the axis
        val d1 = p1.dot(vector)
        val d2 = p2.dot(vector)
        // get the interval along the axis
        return Interval(d2, d1)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#createAABB(org.dyn4j.geometry.Transform)
	 */
    override fun createAABB(transform: Transform): AABB {
        // Inlined projection of x axis
        // Interval x = this.project(Vector2.X_AXIS, transform);
        val minX = this.getFarthestPoint(Vector2.INV_X_AXIS, transform).x
        val maxX = this.getFarthestPoint(Vector2.X_AXIS, transform).x

        // Inlined projection of y axis
        // Interval y = this.project(Vector2.Y_AXIS, transform);
        val minY = this.getFarthestPoint(Vector2.INV_Y_AXIS, transform).y
        val maxY = this.getFarthestPoint(Vector2.Y_AXIS, transform).y
        return AABB(minX, minY, maxX, maxY)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#createMass(double)
	 */
    override fun createMass(density: Double): Mass {
        // area of a circular section is a = r^2 * alpha
        val r2 = sliceRadius * sliceRadius
        val m = density * r2 * alpha
        // inertia about z: http://www.efunda.com/math/areas/CircularSection.cfm
        val sina: Double = sin(alpha)
        val I = 1.0 / 18.0 * r2 * r2 * (9.0 * alpha * alpha - 8.0 * sina * sina) / alpha
        return Mass(center, m, I)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#getRadius(org.dyn4j.geometry.Vector2)
	 */
    override fun getRadius(center: Vector2): Double {
        // is the given center in region A?
        // \    /)
        //  \  /  )
        //   \/    )
        //A  /\    )
        //  /  \  )
        // /    \)
        return if (getLocation(center, vertices[1], vertices[0]) <= 0 &&
            getLocation(center, vertices[2], vertices[0]) >= 0
        ) {
            // if so, its the slice radius plus the distance from the
            // center to the tip of the slice
            sliceRadius + center.distance(vertices[0])
        } else {
            // otherwise its the rotation radius of the triangular section
            getRotationRadius(center = center, vertices = *vertices)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#contains(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun contains(point: Vector2, transform: Transform): Boolean {
        // see if the point is in the circle
        // transform the point into local space
        val lp = transform.getInverseTransformed(point!!)
        // get the transformed radius squared
        val radiusSquared = sliceRadius * sliceRadius
        // create a vector from the circle center to the given point
        val v = vertices[0].to(lp)
        if (v.magnitudeSquared <= radiusSquared) {
            // if its in the circle then we need to make sure its in the section
            if (getLocation(lp, vertices[0], vertices[1]) <= 0 &&
                getLocation(lp, vertices[0], vertices[2]) >= 0) {
                return true
            }
        }
        // if its not in the circle then no other checks need to be performed
        return false
    }


    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.AbstractShape#rotate(org.dyn4j.geometry.Rotation, double, double)
	 */
    override fun rotate(rotation: Rotation, x: Double, y: Double) {
        super.rotate(rotation, x, y)

        // rotate the pie vertices
        for (i in 0 until vertices.size) {
            vertices[i].rotate(rotation, x, y)
        }

        // rotate the pie normals
        for (i in 0 until normals.size) {
            normals[i].rotate(rotation)
        }

        // rotate the local x axis
        this.rotation!!.rotate(rotation)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.AbstractShape#translate(double, double)
	 */
    override fun translate(x: Double, y: Double) {
        // translate the centroid
        super.translate(x, y)
        // translate the pie vertices
        for (i in 0 until vertices.size) {
            vertices[i].add(x, y)
        }
    }

    /**
     * Returns the rotation about the local center in radians.
     * @return double the rotation in radians
     */
    fun getRotationAngle(): Double {
        return rotation!!.toRadians()
    }

    /**
     * @return the [Rotation] object that represents the local rotation
     */
    fun copyRotation(): Rotation? = rotation!!.copy()

    /**
     * Returns the angular extent of the slice in radians.
     * @return double
     */
    fun getTheta(): Double {
        return alpha * 2
    }

    /**
     * Returns the tip of the pie shape.
     *
     *
     * This is the center of the circle.
     * @return [Vector2]
     */
    fun getCircleCenter(): Vector2? {
        return vertices[0]
    }

}