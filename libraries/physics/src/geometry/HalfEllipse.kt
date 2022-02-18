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

import featurea.math.cbrt
import org.dyn4j.DataContainer
import org.dyn4j.geometry.RobustGeometry.getLocation
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Implementation of an Half-Ellipse [Convex] [Shape].
 *
 *
 * A half ellipse must have a width and height greater than zero and the height parameter is the height of the half.
 *
 *
 * **This shape is only supported by the GJK collision detection algorithm**.
 *
 *
 * An `UnsupportedOperationException` is thrown when this shape is used with SAT.  If you are using
 * or are planning on using the SAT collision detection algorithm, you can use the
 * [Geometry.createPolygonalHalfEllipse] method to create a half ellipse
 * [Polygon] approximation. Another option is to use the GJK or your own collision detection
 * algorithm for this shape only and use SAT on others.
 * @author William Bittle
 * @version 3.4.0
 * @since 3.1.7
 */
class HalfEllipse : AbstractShape, Convex, Shape, Transformable, DataContainer {
    /**
     * The half ellipse inertia constant.
     * @see [Elliptical Half](http://www.efunda.com/math/areas/ellipticalhalf.cfm)
     */
    private val INERTIA_CONSTANT: Double = PI / 8.0 - 8.0 / (9.0 * PI)

    /** The ellipse height  */
    @JvmField
    var height = 0.0

    /** The half-width  */
    var halfWidth = 0.0

    /** The local rotation  */
    var rotation: Rotation? = null

    /** The ellipse center  */
    var ellipseCenter: Vector2? = null

    /** The first vertex of the bottom  */
    var vertexLeft: Vector2? = null

    /** The second vertex of the bottom  */
    var vertexRight: Vector2? = null

    /**
     * Validated constructor.
     *
     *
     * This creates an axis-aligned half ellipse fitting inside a rectangle
     * of the given width and height.
     * @param valid always true or this constructor would not be called
     * @param width the width
     * @param height the height
     * @param center the center
     * @param vertexLeft the first vertex
     * @param vertexRight the second vertex
     */
    constructor(
        width: Double,
        height: Double,
        center: Vector2,
        vertexLeft: Vector2,
        vertexRight: Vector2
    ) : super(center, center.distance(vertexRight)) {

        // set height. width can be computed as halfWidth * 2 when needed
        this.height = height

        // compute the major and minor axis lengths
        // (the x,y radii)
        halfWidth = width * 0.5

        // set the ellipse center
        ellipseCenter = Vector2()

        // Initially the half ellipse is aligned to the world space x axis
        rotation = Rotation()

        // setup the vertices
        this.vertexLeft = vertexLeft
        this.vertexRight = vertexRight
    }

    /**
     * Minimal constructor.
     *
     *
     * This creates an axis-aligned half ellipse fitting inside a rectangle
     * of the given width and height.
     * @param width the width
     * @param height the height of the half
     * @throws IllegalArgumentException if either the width or height is less than or equal to zero
     */
    constructor(width: Double, height: Double) :
            this(
                width,
                height,
                Vector2(0.0, 4.0 * height / (3.0 * PI)),  // the left point
                Vector2(-width * 0.5, 0.0),  // the right point
                Vector2(width * 0.5, 0.0)
            )

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Wound#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("HalfEllipse[").append(super.toString())
            .append("|Width=").append(this.width)
            .append("|Height=").append(this.height)
            .append("]")
        return sb.toString()
    }

    /**
     * {@inheritDoc}
     *
     *
     * This method is not supported by this shape.
     * @throws UnsupportedOperationException when called
     */
    override fun getAxes(foci: Array<Vector2>?, transform: Transform): Array<Vector2>? {
        // this shape is not supported by SAT
        throw UnsupportedOperationException(message("geometry.halfEllipse.satNotSupported"))
    }

    /**
     * {@inheritDoc}
     *
     *
     * This method is not supported by this shape.
     * @throws UnsupportedOperationException when called
     */
    override fun getFoci(transform: Transform): Array<Vector2>? {
        // this shape is not supported by SAT
        throw UnsupportedOperationException(message("geometry.halfEllipse.satNotSupported"))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFarthestPoint(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestPoint(vector: Vector2, transform: Transform): Vector2 {
        // convert the world space vector(n) to local space
        val localAxis: Vector2 = getFarthestPoint(transform.getInverseTransformedR(vector!!))!!

        // then convert back into world space coordinates
        transform.transform(localAxis)
        return localAxis
    }

    /**
     * Modifies the given local space axis into the farthest point along that axis and
     * additionally returns it.
     * @param localAxis the direction vector in local space
     * @return [Vector2]
     */
    private fun getFarthestPoint(localAxis: Vector2): Vector2 {
        // localAxis is already in local coordinates

        // invert the local rotation
        localAxis.inverseRotate(rotation!!)
        if (localAxis.y <= 0) {
            if (localAxis.x >= 0) {
                localAxis.set(vertexRight!!)
            } else {
                localAxis.set(vertexLeft!!)
            }
            return localAxis
        }
        getFarthestPointOnAlignedEllipse(localAxis)

        // include local rotation (inverse again to restore the original rotation)
        localAxis.rotate(rotation!!)

        // add the radius along the vector to the center to get the farthest point
        localAxis.add(ellipseCenter!!)
        return localAxis
    }

    /**
     * Returns the farthest point along the given local space axis assuming the
     * ellipse and the given axis are aligned.
     *
     *
     * Typically this means that the ellipse is axis-aligned, but it could also
     * mean that the ellipse is not axis-aligned, but the given local space axis
     * has been rotated to match the alignment of the ellipse.
     * @since 3.4.0
     */
    private fun getFarthestPointOnAlignedEllipse(localAxis: Vector2) {
        // an ellipse is a circle with a non-uniform scaling transformation applied
        // so we can achieve that by scaling the input axis by the major and minor
        // axis lengths
        localAxis.x *= halfWidth
        localAxis.y *= this.height

        // then normalize it
        localAxis.normalize()

        // then scale again to get a point in the ellipse
        localAxis.x *= halfWidth
        localAxis.y *= this.height
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFarthestFeature(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestFeature(vector: Vector2, transform: Transform): Feature {
        val localAxis = transform.getInverseTransformedR(vector!!)

        // invert the local rotation
        localAxis.inverseRotate(rotation!!)
        return if (localAxis.y > 0) {
            // then its the farthest point
            getFarthestPointOnAlignedEllipse(localAxis)

            // include local rotation (inverse again to restore the original rotation)
            localAxis.rotate(rotation!!)
            // add the radius along the vector to the center to get the farthest point
            localAxis.add(ellipseCenter!!)
            transform.transform(localAxis)
            PointFeature(localAxis)
        } else {
            // Below code is equivalent to
            // return Segment.getFarthestFeature(this.vertexLeft, this.vertexRight, vector, transform);

            // Transform the vertices to world space
            val p1 = transform.getTransformed(vertexLeft!!)
            val p2 = transform.getTransformed(vertexRight!!)

            // The vector p1->p2 is always CCW winding
            val vp1 = PointFeature(p1, 0)
            val vp2 = PointFeature(p2, 1)

            // Choose the vertex that maximizes v.dot(vector)
            // localAxis is vector in local space and we can choose the correct vertex by
            // checking if localAxis points to the left or right
            val vmax = if (localAxis.x <= 0) vp1 else vp2
            EdgeFeature(vp1, vp2, vmax, p1.to(p2), 0)
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
        // Fast computation of HalfEllipse AABB without resorting to getFarthestPoint related methods
        // Based on the Ellipse AABB calculation + adjusting the result for the missing side

        // First calculate the Ellipse AABB
        // Taken from Ellipse#createAABB with slight modifications
        val u = rotation!!.toVector()
        transform.transformR(u)
        val x2 = u.x * u.x
        val y2 = u.y * u.y
        val hw2 = halfWidth * halfWidth
        val hh2 = this.height * this.height // half height squared
        val aabbHalfWidth: Double = sqrt(x2 * hw2 + y2 * hh2)
        val aabbHalfHeight: Double = sqrt(y2 * hw2 + x2 * hh2)
        val cx = transform.getTransformedX(ellipseCenter!!)
        val cy = transform.getTransformedY(ellipseCenter!!)
        var minx = cx - aabbHalfWidth
        var miny = cy - aabbHalfHeight
        var maxx = cx + aabbHalfWidth
        var maxy = cy + aabbHalfHeight

        // Now adjust for the missing side
        // Every time one point will come from the Ellipse AABB and the other from the left and right vertices
        // Depending on the total rotation u, there are four possible cases
        if (u.y > 0) {
            if (u.x > 0) {
                maxx = transform.getTransformedX(vertexRight!!)
                miny = transform.getTransformedY(vertexLeft!!)
            } else {
                maxx = transform.getTransformedX(vertexLeft!!)
                maxy = transform.getTransformedY(vertexRight!!)
            }
        } else {
            if (u.x > 0) {
                minx = transform.getTransformedX(vertexLeft!!)
                miny = transform.getTransformedY(vertexRight!!)
            } else {
                minx = transform.getTransformedX(vertexRight!!)
                maxy = transform.getTransformedY(vertexLeft!!)
            }
        }
        return AABB(minx, miny, maxx, maxy)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#createMass(double)
	 */
    override fun createMass(density: Double): Mass {
        val area: Double = PI * halfWidth * this.height
        val m = area * density * 0.5
        // moment of inertia given by: http://www.efunda.com/math/areas/ellipticalhalf.cfm
        val I = m * (halfWidth * halfWidth + this.height * this.height) * INERTIA_CONSTANT
        return Mass(center, m, I)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#getRadius(org.dyn4j.geometry.Vector2)
	 */
    override fun getRadius(center: Vector2): Double {
        // it turns out that a half ellipse is even more annoying than an ellipse

        // if the half ellipse is wider than it is tall
        return if (halfWidth >= this.height) {
            // then we have two solutions based on the point location
            // if the point is below the half ellipse, then we need to perform
            // a golden section search like the ellipse code
            if (getLocation(center, vertexLeft!!, vertexRight!!) <= 0) {
                getMaxDistanceEllipse(center)
            } else {
                // otherwise we can just take the greater distance of the vertices
                getMaxDistanceToVertices(center)
            }
        } else {
            // otherwise we have even more conditions
            this.getMaxDistanceHalfEllipse(center)
        }
    }

    /**
     * Returns the maximum distance between the two vertices of the ellipse and the given point.
     * @param point the point
     * @return double
     * @since 3.4.0
     */
    private fun getMaxDistanceToVertices(point: Vector2): Double {
        // find the maximum radius from the center
        val leftR = point.distanceSquared(vertexLeft!!)
        val rightR = point.distanceSquared(vertexRight!!)
        // keep the largest
        val r2: Double = max(leftR, rightR)
        return sqrt(r2)
    }

    /**
     * Returns the maximum distance from the given point to the ellipse.
     * @param point the point
     * @return double
     * @since 3.4.0
     */
    private fun getMaxDistanceEllipse(point: Vector2): Double {
        // we need to translate/rotate the point so that this ellipse is
        // considered centered at the origin with it's semi-major axis aligned
        // with the x-axis and its semi-minor axis aligned with the y-axis
        val p = point.difference(ellipseCenter!!).inverseRotate(rotation!!)

        // get the farthest point
        val fp: Vector2 = getFarthestPointOnEllipse(halfWidth, this.height, p)

        // get the distance between the two points. The distance will be the
        // same if we translate/rotate the points back to the real position
        // and rotation, so don't bother
        return p.distance(fp)
    }

    /**
     * Returns the maximum distance between the given point and the half ellipse.
     * @param point the point
     * @return double
     * @since 3.4.0
     */
    private fun getMaxDistanceHalfEllipse(point: Vector2): Double {
        val a = halfWidth
        val b = this.height

        // we need to translate/rotate the point so that this ellipse is
        // considered centered at the origin with it's semi-major axis aligned
        // with the x-axis and its semi-minor axis aligned with the y-axis
        val p = point.difference(ellipseCenter!!).inverseRotate(rotation!!)

        // if the point is below the x axis, then we only need to perform the ellipse code
        if (p.y < 0) {
            return getMaxDistanceEllipse(point)
        }

        // move the point to the 1st quadrant to conform my formulation
        if (p.x < 0) {
            p.x = -p.x
        }

        // if the point is above the evolute, then we only need to evaluate
        // the max distance of the two vertices
        // evolute: (ax)^2/3 + (by)^2/3 = (a^2 - b^2)^2/3

        // compute the y coordinate of the point on the evolute at p.x
        // ey = ((b^2 - a^2)^2/3 - (ax)^2/3)^3/2 / b
        val ab = b * b - a * a
        val ab2r3: Double = cbrt(ab * ab)
        val ax = a * p.x
        val ax2r3: Double = cbrt(ax * ax)
        var top = ab2r3 - ax2r3
        if (top < 0) {
            // the evolute isn't defined at p.x
            return getMaxDistanceToVertices(point)
        }
        top = sqrt(top)
        val ey = top * top * top / b
        if (p.y > ey) {
            // the point is above the evolute
            return getMaxDistanceToVertices(point)
        }

        // check if p.x is close to zero (if it is, then m will be inifinity)
        if (abs(p.x) < 1e-16) {
            // compare the distance to the points and the height
            val d1 = this.height - p.y
            val d2 = getMaxDistanceToVertices(point)
            return if (d1 > d2) d1 else d2
        }

        // else compute the bounds for the unimodal region for golden section to work

        // compute the slope of the evolute at x
        // m = -a^2/3 * sqrt((b^2 - a^2)^2/3 - (ax)^2/3) / (bx^1/3)
        val xr3: Double = cbrt(p.x)
        val a2r3: Double = cbrt(a * a)
        val m = -a2r3 * top / (b * xr3)

        // then compute the ellipse intersect of m, ex, and ey
        // y - ey = m(x - ex)
        // (x / a)^2 + (y / b)^2 = 1
        // solve for y then substitute
        // then examine terms to get quadratic equation parameters
        // qa = a^2m^2 + b^2
        // qb = 2a^2mey - 2a^2m^2ex
        // qc = a^2m^2ex^2 - 2a^2mexey + a^2ey^2 - b^2a^2
        val a2 = a * a
        val b2 = b * b
        val m2 = m * m
        val x2 = p.x * p.x
        val y2 = ey * ey

        // compute quadratic equation parameters
        val qa = a2 * m2 + b2
        val qb = 2 * a2 * m * ey - 2 * a2 * m2 * p.x
        val qc = a2 * m2 * x2 - 2 * a2 * m * p.x * ey + a2 * y2 - b2 * a2

        // use the quadratic equation to limit the search space
        val b24ac = qb * qb - 4 * qa * qc
        if (b24ac < 0) {
            // this would mean that the line from the evolute at p.x doesn't
            // intersect with the ellipse, which shouldn't be possible
            return getMaxDistanceToVertices(point)
        }
        val xmin: Double = (-qb - sqrt(b24ac)) / (2 * qa)
        val xmax = 0.0

        // get the farthest point on the ellipse
        val s: Vector2 = getFarthestPointOnBoundedEllipse(xmin, xmax, a, b, p)

        // then compare that with the farthest point of the two vertices
        val d1 = s.distance(p)
        val d2 = getMaxDistanceToVertices(point)
        return if (d1 > d2) d1 else d2
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#contains(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun contains(point: Vector2, transform: Transform): Boolean {
        // equation of an ellipse:
        // (x - h)^2/a^2 + (y - k)^2/b^2 = 1
        // for a point to be inside the ellipse, we can plug in
        // the point into this equation and verify that the value
        // is less than or equal to one

        // get the world space point into local coordinates
        val localPoint = transform.getInverseTransformed(point!!)
        // account for local rotation
        localPoint.inverseRotate(rotation!!, ellipseCenter!!)

        // translate into local coordinates
        val x = localPoint.x - ellipseCenter!!.x
        val y = localPoint.y - ellipseCenter!!.y

        // for half ellipse we have an early out
        if (y < 0) return false
        val x2 = x * x
        val y2 = y * y
        val a2 = halfWidth * halfWidth
        val b2 = this.height * this.height
        val value = x2 / a2 + y2 / b2
        return value <= 1.0
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.AbstractShape#rotate(org.dyn4j.geometry.Rotation, double, double)
	 */
    override fun rotate(rotation: Rotation, x: Double, y: Double) {
        super.rotate(rotation, x, y)

        // rotate the local axis as well
        this.rotation!!.rotate(rotation!!)
        vertexLeft!!.rotate(rotation, x, y)
        vertexRight!!.rotate(rotation, x, y)
        ellipseCenter!!.rotate(rotation, x, y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.AbstractShape#translate(double, double)
	 */
    override fun translate(x: Double, y: Double) {
        // translate the centroid
        super.translate(x, y)
        // translate the pie vertices
        vertexLeft!!.add(x, y)
        vertexRight!!.add(x, y)
        // translate the ellipse center
        ellipseCenter!!.add(x, y)
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
     * Returns the width.
     * @return double
     */
    val width: Double
        get() = halfWidth * 2

}