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
import org.dyn4j.geometry.Ellipse.Companion.FARTHEST_POINT_EPSILON
import org.dyn4j.geometry.Ellipse.Companion.FARTHEST_POINT_MAX_ITERATIONS
import org.dyn4j.geometry.Ellipse.Companion.INV_GOLDEN_RATIO
import org.dyn4j.resources.message
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Implementation of an Ellipse [Convex] [Shape].
 *
 *
 * An ellipse must have a width and height greater than zero.
 *
 *
 * **This shape is only supported by the GJK collision detection algorithm**
 *
 *
 * An `UnsupportedOperationException` is thrown when this shape is used with SAT.  If you
 * are using or are planning on using the SAT collision detection algorithm, you can use the
 * [Geometry.createPolygonalEllipse] method to create a half ellipse
 * [Polygon] approximation. Another option is to use the GJK or your own collision detection
 * algorithm for this shape only and use SAT on others.
 * @author William Bittle
 * @version 3.4.0
 * @since 3.1.7
 */
class Ellipse : AbstractShape, Convex, Shape, Transformable, DataContainer {

    companion object {
        /** The inverse of the golden ratio  */
        val INV_GOLDEN_RATIO: Double = 1.0 / ((sqrt(5.0) + 1.0) * 0.5)

        /** The maximum number of iterations to perform when finding the farthest point  */
        val FARTHEST_POINT_MAX_ITERATIONS = 50

        /** The desired accuracy for the farthest point  */
        val FARTHEST_POINT_EPSILON = 1.0e-8
    }


    /** The half-width  */
    var halfWidth = 0.0

    /** The half-height  */
    var halfHeight = 0.0

    /** The local rotation  */
    var rotation: Rotation? = null

    /**
     * Validated constructor.
     *
     *
     * This creates an axis-aligned ellipse fitting inside a rectangle of the given width and
     * height centered at the origin.
     * @param valid always true or this constructor would not be called
     * @param width the width
     * @param height the height
     */
    constructor(width: Double, height: Double) : super(max(width, height) * 0.5) {
        // compute the major and minor axis lengths
        // (the x,y radii)
        halfWidth = width * 0.5
        halfHeight = height * 0.5

        // Initially the ellipse is aligned to the world space x axis
        rotation = Rotation()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Wound#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Ellipse[").append(super.toString())
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
        throw UnsupportedOperationException(message("geometry.ellipse.satNotSupported"))
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
        throw UnsupportedOperationException(message("geometry.ellipse.satNotSupported"))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFarthestPoint(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestPoint(vector: Vector2, transform: Transform): Vector2 {
        // convert the world space vector(n) to local space
        var localAxis = transform.getInverseTransformedR(vector!!)

        // private implementation
        localAxis = this.getFarthestPoint(localAxis)

        // then finally convert back into world space coordinates
        transform.transform(localAxis)
        return localAxis
    }

    /**
     * Modifies the given local space axis into the farthest point along that axis and
     * additionally returns it.
     * @param localAxis the direction vector in local space
     * @return [Vector2]
     * @since 3.4.0
     */
    private fun getFarthestPoint(localAxis: Vector2): Vector2 {
        // localAxis is already in local coordinates
        if (rotation!!.isIdentity) {
            // This is the case most of the time, and saves a lot of computations
            this.getFarthestPointOnAlignedEllipse(localAxis)
        } else {
            // invert the local rotation
            localAxis.inverseRotate(rotation!!)
            this.getFarthestPointOnAlignedEllipse(localAxis)

            // include local rotation
            localAxis.rotate(rotation!!)
        }

        // add the radius along the vector to the center to get the farthest point
        localAxis.add(center!!)
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
        localAxis.y *= halfHeight
        // then normalize it
        localAxis.normalize()
        // then scale again to get a point in the ellipse
        localAxis.x *= halfWidth
        localAxis.y *= halfHeight
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFarthestFeature(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestFeature(
        vector: Vector2,
        transform: Transform
    ): Feature {
        // obtain the farthest point along the given vector
        val farthest = this.getFarthestPoint(vector, transform)
        // for an ellipse the farthest feature along a vector will always be a vertex
        return PointFeature(farthest)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#project(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun project(vector: Vector2, transform: Transform): Interval {
        // get the world space farthest point
        val p1 = this.getFarthestPoint(vector, transform)
        // get the center in world space
        val center = transform.getTransformed(center!!)
        // project the center onto the axis
        val c = center.dot(vector!!)
        // project the point onto the axis
        val d = p1!!.dot(vector)
        // get the interval along the axis
        return Interval(2 * c - d, d)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#createAABB(org.dyn4j.geometry.Transform)
	 */
    override fun createAABB(transform: Transform): AABB {
        // Fast computation of Ellipse AABB without resorting to getFarthestPoint related methods
        // Also see http://www.iquilezles.org/www/articles/ellipses/ellipses.htm

        // u is a unit vector with the world and local rotation
        val u = rotation!!.toVector()
        transform.transformR(u)
        val x2 = u.x * u.x
        val y2 = u.y * u.y

        // Half width half height squared
        val hw2 = halfWidth * halfWidth
        val hh2 = halfHeight * halfHeight

        // calculate the resulting AABB's half width and half height
        val aabbHalfWidth: Double = sqrt(x2 * hw2 + y2 * hh2)
        val aabbHalfHeight: Double = sqrt(y2 * hw2 + x2 * hh2)

        // compute world center
        val cx = transform.getTransformedX(center!!)
        val cy = transform.getTransformedY(center)

        // combine to form the ellipse AABB
        val minx = cx - aabbHalfWidth
        val miny = cy - aabbHalfHeight
        val maxx = cx + aabbHalfWidth
        val maxy = cy + aabbHalfHeight
        return AABB(minx, miny, maxx, maxy)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#createMass(double)
	 */
    override fun createMass(density: Double): Mass {
        val area: Double = PI * halfWidth * halfHeight
        val m = area * density
        // inertia about the z see http://math.stackexchange.com/questions/152277/moment-of-inertia-of-an-ellipse-in-2d
        val I = m * (halfWidth * halfWidth + halfHeight * halfHeight) / 4.0
        return Mass(center, m, I)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#getRadius(org.dyn4j.geometry.Vector2)
	 */
    override fun getRadius(center: Vector2): Double {
        // annoyingly, finding the radius of a rotated/translated ellipse
        // about another point is the same as finding the farthest point
        // from an arbitrary point. The solution to this is a quartic function
        // that has no analytic solution, so we are stuck with a maximization problem.
        // Thankfully, this method shouldn't be called that often, in fact
        // it should only be called when the user modifies the shapes on a body.

        // we need to translate/rotate the point so that this ellipse is
        // considered centered at the origin with it's semi-major axis aligned
        // with the x-axis and its semi-minor axis aligned with the y-axis
        val p = center.difference(this.center!!).inverseRotate(rotation!!)

        // get the farthest point.
        val fp: Vector2 = getFarthestPointOnEllipse(halfWidth, halfHeight, p)

        // get the distance between the two points. The distance will be the
        // same if we translate/rotate the points back to the real position
        // and rotation, so don't bother
        return p.distance(fp)
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
        localPoint.inverseRotate(rotation!!, center!!)
        val x = localPoint.x - center.x
        val y = localPoint.y - center.y
        val x2 = x * x
        val y2 = y * y
        val a2 = halfWidth * halfWidth
        val b2 = halfHeight * halfHeight
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

    /**
     * Returns the height.
     * @return double
     */
    val height: Double
        get() = halfHeight * 2

}

/**
 * Returns the point on this ellipse farthest from the given point.
 *
 *
 * This method assumes that this ellipse is centered on the origin and
 * has it's semi-major axis aligned with the x-axis and its semi-minor
 * axis aligned with the y-axis.
 *
 *
 * This method performs a Golden Section Search to find the point of
 * maximum distance from the given point.
 * @param a the half width of the ellipse
 * @param b the half height of the ellipse
 * @param point the query point
 * @return [Vector2]
 * @since 3.4.0
 */
fun getFarthestPointOnEllipse(
    a: Double,
    b: Double,
    point: Vector2
): Vector2 {
    var a = a
    var b = b
    var px = point.x
    var py = point.y

    // check the semi-major/minor axes
    var flipped = false
    if (a < b) {
        // swap the semi-major/minor axes
        var temp = a
        a = b
        b = temp

        // if we swap the axes, then we needt
        // also rotate our point
        temp = px
        px = -py
        py = temp
        flipped = true
    }

    // solve as if point is in 3rd quadrant
    // due to the symmetry of the ellipse we only have
    // to solve this problem in one quadrant and then
    // just flip signs to get the anwser in the original
    // quadrant
    var quadrant = 3
    if (px >= 0 && py >= 0) {
        quadrant = 1
        px = -px
        py = -py
    } else if (px >= 0 && py <= 0) {
        quadrant = 4
        px = -px
    } else if (px <= 0 && py >= 0) {
        quadrant = 2
        py = -py
    }
    var p: Vector2? = null
    if (py == 0.0) {
        // then its on the x-axis and the farthest point is easy to calculate
        p = Vector2(if (px < 0) a else -a, 0.0)
    } else {
        p = getFarthestPointOnBoundedEllipse(
            0.0,
            a,
            a,
            b,
            Vector2(px, py)
        )
    }

    // translate the point to the correct quadrant
    if (quadrant == 1) {
        p!!.x *= -1
        p!!.y *= -1
    } else if (quadrant == 2) {
        p!!.y *= -1
    } else if (quadrant == 4) {
        p!!.x *= -1
    }

    // flip the point's coorindates if the
    // semi-major/minor axes were flipped
    if (flipped) {
        val temp = p!!.x
        p!!.x = p!!.y
        p!!.y = -temp
    }
    return p
}


/**
 * Performs a golden section search of the ellipse bounded between the interval [xmin, xmax] for the farthest
 * point from the given point.
 *
 *
 * This method assumes that this ellipse is centered on the origin and
 * has it's semi-major axis aligned with the x-axis and its semi-minor
 * axis aligned with the y-axis.
 * @param xmin the minimum x value
 * @param xmax the maximum x value
 * @param a the half width of the ellipse
 * @param b the half height of the ellipse
 * @param point the query point
 * @return [Vector2]
 * @since 3.4.0
 */
fun getFarthestPointOnBoundedEllipse(
    xmin: Double,
    xmax: Double,
    a: Double,
    b: Double,
    point: Vector2
): Vector2 {
    val px = point.x
    val py = point.y

    // our bracketing bounds will be [x0, x1]
    var x0 = xmin
    var x1 = xmax
    val q = Vector2(px, py)
    val p = Vector2()
    val aa = a * a
    val ba = b / a

    // compute the golden ratio test points
    var x2 = x1 - (x1 - x0) * INV_GOLDEN_RATIO
    var x3 = x0 + (x1 - x0) * INV_GOLDEN_RATIO
    var fx2: Double = getSquaredDistance(aa, ba, x2, q, p)
    var fx3: Double = getSquaredDistance(aa, ba, x3, q, p)

    // our bracket is now: [x0, x2, x3, x1]
    // iteratively reduce the bracket
    for (i in 0 until FARTHEST_POINT_MAX_ITERATIONS) {
        if (fx2 < fx3) {
            if (abs(x1 - x2) <= FARTHEST_POINT_EPSILON) {
                break
            }
            x0 = x2
            x2 = x3
            fx2 = fx3
            x3 = x0 + (x1 - x0) * INV_GOLDEN_RATIO
            fx3 = getSquaredDistance(aa, ba, x3, q, p)
        } else {
            if (abs(x3 - x0) <= FARTHEST_POINT_EPSILON) {
                break
            }
            x1 = x3
            x3 = x2
            fx3 = fx2
            x2 = x1 - (x1 - x0) * INV_GOLDEN_RATIO
            fx2 = getSquaredDistance(aa, ba, x2, q, p)
        }
    }
    return p
}

/**
 * Returns the distance from the ellipse at the given x to the given point q.
 * @param a2 the ellipse semi-major axis squared (a * a)
 * @param ba the ellipse semi-minor axis divided by the semi-major axis (b / a)
 * @param x the x of the point on the ellipse
 * @param q the query point
 * @param p output; the point on the ellipse
 * @return double
 * @since 3.4.0
 */
private fun getSquaredDistance(
    a2: Double,
    ba: Double,
    x: Double,
    q: Vector2,
    p: Vector2
): Double {
    // compute the y value for the given x on the ellipse:
    // (x^2/a^2) + (y^2/b^2) = 1
    // y^2 = (1 - (x / a)^2) * b^2
    // y^2 = b^2/a^2(a^2 - x^2)
    // y = (b / a) * sqrt(a^2 - x^2)
    var a2x2 = a2 - x * x
    if (a2x2 < 0) {
        // this should never happen, but just in case of numeric instability
        // we'll just set it to zero
        a2x2 = 0.0
        // x^2/a^2 can never be greater than 1 since a must always be
        // greater than or equal to the largest x value on the ellipse
    }
    val sa2x2: Double = sqrt(a2x2)
    val y = ba * sa2x2

    // compute the distance from the ellipse point to the query point
    val xx = q.x - x
    val yy = q.y - y
    val d2 = xx * xx + yy * yy
    p.x = x
    p.y = y

    // return the distance
    return d2
}