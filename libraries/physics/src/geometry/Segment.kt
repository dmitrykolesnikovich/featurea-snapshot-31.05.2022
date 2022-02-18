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
import org.dyn4j.Epsilon
import org.dyn4j.geometry.Geometry.getAverageCenter
import org.dyn4j.geometry.Geometry.getRotationRadius
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.abs

/**
 * Implementation of a Line Segment [Convex] [Shape].
 *
 *
 * This class represents a line segment that is infinitely thin.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 */
open class Segment : AbstractShape, Convex, Wound, Shape, Transformable, DataContainer {

    override val woundVertices: Array<Vector2> get() = vertices
    override val woundNormals: Array<Vector2> get() = normals

    /** The segment vertices  */
    @JvmField
    /*override*/ val vertices: Array<Vector2>

    /** The segment normals  */
    @JvmField
    /*override*/ val normals: Array<Vector2>

    /** The segment length  */
    @JvmField
    var length = 0.0

    /**
     * Validated constructor.
     *
     *
     * Creates a new segment using the given points.  The center will be the
     * average of the points.
     * @param valid always true or this constructor would not be called
     * @param point1 the first point
     * @param point2 the second point
     */
    constructor(vertices: Array<Vector2>, segment: Vector2, length: Double) :
            super(getAverageCenter(*vertices), length * 0.5) {
        // assign the verices
        this.vertices = vertices
        // create the normals
        normals = arrayOfNulls<Vector2>(2) as Array<Vector2>
        normals[0] = segment.copy().apply { normalize() }
        normals[1] = segment.right().apply { normalize() }
        // compute the length
        this.length = length
    }

    /**
     * Full constructor.
     *
     *
     * Creates a new segment using the given points.  The center will be the
     * average of the points.
     *
     *
     * A segment's points cannot be null or the same point.
     * @param point1 the first point
     * @param point2 the second point
     * @throws NullPointerException if point1 or point2 is null
     * @throws IllegalArgumentException if point1 == point2
     */
    constructor(point1: Vector2, point2: Vector2) :
            this(arrayOf(point1, point2), point1.to(point2), point1.distance(point2))

    /**
     * Validates the constructor input returning true if valid or throwing an exception if invalid.
     * @param point1 the first point
     * @param point2 the second point
     * @return boolean true
     * @throws NullPointerException if point1 or point2 is null
     * @throws IllegalArgumentException if point1 == point2
     */
    private fun validate(point1: Vector2?, point2: Vector2?): Boolean {
        // make sure either point is not null
        if (point1 == null) throw NullPointerException(message("geometry.segment.nullPoint1"))
        if (point2 == null) throw NullPointerException(message("geometry.segment.nullPoint2"))
        // make sure the two points are not coincident
        if (point1.equals(point2)) {
            throw IllegalArgumentException(message("geometry.segment.samePoint"))
        }
        return true
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Wound#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Segment[").append(super.toString())
            .append("|Length=").append(length)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Wound#getVertexIterator()
	 */
    override val vertexIterator: Iterator<Vector2>
        get() = WoundIterator(vertices)

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Wound#getNormalIterator()
	 */
    override val normalIterator: Iterator<Vector2>
        get() = WoundIterator(normals)

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#getRadius(org.dyn4j.geometry.Vector2)
	 */
    override fun getRadius(center: Vector2): Double {
        return getRotationRadius(center = center, vertices = *vertices)
    }

    /**
     * Returns point1 in local coordinates.
     * @return [Vector2]
     */
    val point1: Vector2
        get() = vertices[0]

    /**
     * Returns point2 in local coordinates.
     * @return [Vector2]
     */
    val point2: Vector2
        get() = vertices[1]


    /**
     * Returns the point on the **line** that this [Segment]
     * defines closest to the given point.
     *
     *
     * This method works in this [Segment]'s local space.
     * @param point the local space point
     * @return [Vector2]
     * @throws NullPointerException if the given point is null
     * @since 3.1.5
     * @see .getPointOnLineClosestToPoint
     */
    fun getPointOnLineClosestToPoint(point: Vector2): Vector2 {
        return getPointOnLineClosestToPoint(
            point,
            vertices[0],
            vertices[1]
        )
    }


    /**
     * Returns the point on this [Segment] closest to the given point.
     *
     *
     * This method works in this [Segment]'s local space.
     * @param point the local space point
     * @return [Vector2]
     * @throws NullPointerException if the given point is null
     * @since 3.1.5
     * @see .getPointOnSegmentClosestToPoint
     */
    fun getPointOnSegmentClosestToPoint(point: Vector2): Vector2? {
        return Segment.getPointOnSegmentClosestToPoint(
            point,
            vertices[0],
            vertices[1]
        )
    }




    /**
     * Returns the intersection of the given [Segment] and this [Segment].
     *
     *
     * This method assumes that both this and the given segment are in the same space (either
     * local or world space).
     *
     *
     * If the segments do not intersect, are parallel, or are coincident, null is returned.
     * @param segment the other segment
     * @return [Vector2]
     * @throws NullPointerException if the given segment is null
     * @since 3.1.5
     * @see .getSegmentIntersection
     */
    fun getSegmentIntersection(segment: Segment): Vector2? {
        return getSegmentIntersection(vertices[0], vertices[1], segment.vertices[0], segment.vertices[1])
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getAxes(java.util.List, org.dyn4j.geometry.Transform)
	 */
    override fun getAxes(foci: Array<Vector2>?, transform: Transform): Array<Vector2>? {
        // get the number of foci
        val size = foci?.size ?: 0
        // create an array to hold the axes
        val axes = arrayOfNulls<Vector2>(2 + size) as Array<Vector2>
        var n = 0
        // get the vertices
        val p1 = transform.getTransformed(vertices[0])
        val p2 = transform.getTransformed(vertices[1])
        // use both the edge and its normal
        axes[n++] = transform.getTransformedR(normals[1]!!)
        axes[n++] = transform.getTransformedR(normals[0]!!)
        var axis: Vector2
        // add the voronoi region axes if point is supplied
        for (i in 0 until size) {
            // get the focal point
            val f = foci!![i]
            // find the closest point
            axis = if (p1.distanceSquared(f!!) < p2.distanceSquared(f)) {
                p1.to(f)
            } else {
                p2.to(f)
            }
            // normalize the axis
            axis.normalize()
            // add the axis to the array
            axes[n++] = axis
        }
        // return all the axes
        return axes
    }

    /**
     * {@inheritDoc}
     *
     *
     * Not applicable to this shape.  Always returns null.
     * @return null
     */
    override fun getFoci(transform: Transform): Array<Vector2>? {
        return null
    }

    /**
     * {@inheritDoc}
     *
     *
     * Should almost always return false since this shape represents an infinitely
     * thin line segment. Use the [.contains]
     * method instead for better, though technically inaccurate, results.
     */
    override fun contains(point: Vector2, transform: Transform): Boolean {
        // put the point in local coordinates
        val p = transform.getInverseTransformed(point!!)
        // create a reference to the end points
        val p1 = vertices[0]
        val p2 = vertices[1]
        // get the location of the given point relative to this segment
        val value: Double = getLocation(p, p1, p2)
        // see if the point is on the line created by this line segment
        if (abs(value) <= Epsilon.E) {
            val distSqrd = p1.distanceSquared(p2)
            return if (p.distanceSquared(p1) <= distSqrd
                && p.distanceSquared(p2) <= distSqrd
            ) {
                // if the distance to the point from both points is less than or equal
                // to the length of the segment squared then we know its on the line segment
                true
            } else false
            // if the point is further away from either point than the length of the
            // segment then its not on the segment
        }
        return false
    }

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
     * If the radius is greater than zero then the point is tested to be
     * within the shape expanded radially by the radius.
     * @param point world space point
     * @param transform [Transform] for this [Shape]
     * @param radius the expansion radius; in the range [0, ]
     * @return boolean
     */
    fun contains(point: Vector2, transform: Transform, radius: Double): Boolean {
        // if the radius is zero or less then perform the normal procedure
        if (radius <= 0) {
            return contains(point, transform)
        } else {
            // put the point in local coordinates
            val p = transform.getInverseTransformed(point!!)
            // otherwise act like the segment is two circles and a rectangle
            if (vertices[0].distanceSquared(p) <= radius * radius) {
                return true
            } else if (vertices[1].distanceSquared(p) <= radius * radius) {
                return true
            } else {
                // see if the point is in the rectangle portion
                val l = vertices[0].to(vertices[1])
                val p1 = vertices[0].to(p)
                val p2 = vertices[1].to(p)
                if (l.dot(p1) > 0 && -l.dot(p2) > 0) {
                    val dist = p1.project(l.rightHandOrthogonalVector).magnitudeSquared
                    if (dist <= radius * radius) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#project(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun project(vector: Vector2, transform: Transform): Interval {
        var v = 0.0
        // get the vertices
        val p1 = transform.getTransformed(vertices[0])
        val p2 = transform.getTransformed(vertices[1])
        // project the first
        var min = vector.dot(p1)
        var max = min
        // project the second
        v = vector.dot(p2)
        if (v < min) {
            min = v
        } else if (v > max) {
            max = v
        }
        return Interval(min, max)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFurthestPoint(org.dyn4j.geometry.Vector, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestPoint(
        vector: Vector2,
        transform: Transform
    ): Vector2 {
        return getFarthestPoint(
            vertices[0],
            vertices[1],
            vector,
            transform
        )
    }

    /**
     * Returns the feature farthest in the direction of n.
     *
     *
     * For a [Segment] it's always the [Segment] itself.
     * @param vector the direction
     * @param transform the local to world space [Transform] of this [Convex] [Shape]
     * @return [EdgeFeature]
     */
    override fun getFarthestFeature(
        vector: Vector2,
        transform: Transform
    ): EdgeFeature {
        return getFarthestFeature(
            vertices[0],
            vertices[1],
            vector,
            transform
        )
    }


    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.AbstractShape#rotate(org.dyn4j.geometry.Rotation, double, double)
	 */
    override fun rotate(rotation: Rotation, x: Double, y: Double) {
        super.rotate(rotation, x, y)
        vertices[0].rotate(rotation!!, x, y)
        vertices[1].rotate(rotation, x, y)
        normals[0]!!.rotate(rotation)
        normals[1]!!.rotate(rotation)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.AbstractShape#translate(double, double)
	 */
    override fun translate(x: Double, y: Double) {
        super.translate(x, y)
        vertices[0].add(x, y)
        vertices[1].add(x, y)
    }

    /**
     * Creates a [Mass] object using the geometric properties of
     * this [Segment] and the given density.
     *
     *  m = d * length
     * I = l<sup>2</sup> * m / 12
     * @param density the density in kg/m<sup>2</sup>
     * @return [Mass] the [Mass] of this [Segment]
     */
    override fun createMass(density: Double): Mass {
        val length = length
        // compute the mass
        val mass = density * length
        // compute the inertia tensor
        val inertia = length * length * mass / 12.0
        // since we know that a line segment has only two points we can
        // feel safe using the averaging method for the centroid
        return Mass(center, mass, inertia)
    }

    /**
     * {@inheritDoc}
     *
     *
     * Be aware that this method could produce an infinitely thin
     * AABB if this segment is aligned to either the x or y-axis.
     */
    override fun createAABB(transform: Transform): AABB {
        // get the transformed points
        val p0 = transform.getTransformed(vertices[0])
        val p1 = transform.getTransformed(vertices[1])

        // create the aabb
        return AABB.createAABBFromPoints(p0, p1)
    }

    /**
     * Returns the line intersection of the given [Segment] and this [Segment].
     *
     *
     * This method treats this segment and the given segment as defining **lines** rather than segments.
     *
     *
     * This method assumes that both this and the given segment are in the same space (either
     * local or world space).
     *
     *
     * If the lines are parallel or coincident, null is returned.
     * @param segment the other segment
     * @return [Vector2]
     * @throws NullPointerException if the given segment is null
     * @since 3.1.5
     * @see .getLineIntersection
     */
    fun getLineIntersection(segment: Segment): Vector2? {
        return getLineIntersection(
            vertices[0],
            vertices[1],
            segment.vertices[0],
            segment.vertices[1]
        )
    }

    companion object {
        /**
         * Returns the intersection point of the two lines or null if they are parallel or coincident.
         *
         *
         * If we let:
         *
         *  A = A<sub>p2</sub> - A<sub>p1</sub>
         * B = B<sub>p2</sub> - B<sub>p1</sub>
         * we can create two parametric equations:
         *
         *  Q = A<sub>p1</sub> + t<sub>a</sub>A
         * Q = B<sub>p1</sub> + t<sub>b</sub>B
         * Where Q is the intersection point:
         *
         *  A<sub>p1</sub> + t<sub>a</sub>A = B<sub>p1</sub> + t<sub>b</sub>B
         * We can solve for t<sub>b</sub> by applying the cross product with A on both sides:
         *
         *  (A<sub>p1</sub> + t<sub>a</sub>A) x A = (B<sub>p1</sub> + t<sub>b</sub>B) x A
         * A<sub>p1</sub> x A = B<sub>p1</sub> x A + t<sub>b</sub>B x A
         * (A<sub>p1</sub> - B<sub>p1</sub>) x A = t<sub>b</sub>B x A
         * t<sub>b</sub> = ((A<sub>p1</sub> - B<sub>p1</sub>) x A) / (B x A)
         * If B x A == 0 then the lines are parallel.  If both the top and bottom are zero
         * then the lines are coincident.
         *
         *
         * If the lines are parallel or coincident, null is returned.
         * @param ap1 the first point of the first line
         * @param ap2 the second point of the first line
         * @param bp1 the first point of the second line
         * @param bp2 the second point of the second line
         * @return Vector2 the intersection point; null if the lines are parallel or coincident
         * @see .getSegmentIntersection
         * @throws NullPointerException if ap1, ap2, bp1 or bp2 is null
         * @since 3.1.1
         */
        @JvmStatic
        fun getLineIntersection(
            ap1: Vector2,
            ap2: Vector2,
            bp1: Vector2,
            bp2: Vector2
        ): Vector2? {
            val A: Vector2 = ap1.to(ap2)
            val B: Vector2 = bp1.to(bp2)

            // compute the bottom
            val BxA = B.cross(A)
            if (abs(BxA) <= Epsilon.E) {
                // the lines are parallel and don't intersect
                return null
            }

            // compute the top
            val ambxA = ap1.difference(bp1).cross(A)
            if (abs(ambxA) <= Epsilon.E) {
                // the lines are coincident
                return null
            }

            // compute tb
            val tb = ambxA / BxA
            // compute the intersection point
            return B.product(tb).add(bp1)
        }


        /**
         * Determines where the point is relative to the given line.
         *
         *  Set L = linePoint2 - linePoint1
         * Set P = point - linePoint1
         * location = L.cross(P)
         * Returns 0 if the point lies on the line created from the line segment.<br></br>
         * Assuming a right handed coordinate system:<br></br>
         * Returns &lt; 0 if the point lies on the right side of the line<br></br>
         * Returns &gt; 0 if the point lies on the left side of the line
         *
         *
         * Assumes all points are in world space.
         * @param point the point
         * @param linePoint1 the first point of the line
         * @param linePoint2 the second point of the line
         * @throws NullPointerException if point, linePoint1, or linePoint2 is null
         * @return double
         */
        @JvmStatic
        fun getLocation(
            point: Vector2,
            linePoint1: Vector2,
            linePoint2: Vector2
        ): Double {
            return (linePoint2.x - linePoint1.x) * (point.y - linePoint1.y) -
                    (point.x - linePoint1.x) * (linePoint2.y - linePoint1.y)
        }

        /**
         * Returns the point on the given line closest to the given point.
         *
         *
         * Project the point onto the line:
         *
         *  V<sub>line</sub> = P<sub>1</sub> - P<sub>0</sub>
         * V<sub>point</sub> = P<sub>0</sub> - P
         * P<sub>closest</sub> = V<sub>point</sub>.project(V<sub>line</sub>)
         * Assumes all points are in world space.
         * @see Vector2.project
         * @param point the point
         * @param linePoint1 the first point of the line
         * @param linePoint2 the second point of the line
         * @throws NullPointerException if point, linePoint1, or linePoint2 is null
         * @return [Vector2]
         */
        @JvmStatic
        fun getPointOnLineClosestToPoint(
            point: Vector2,
            linePoint1: Vector2,
            linePoint2: Vector2
        ): Vector2 {
            // create a vector from the point to the first line point
            val p1ToP = point.difference(linePoint1)
            // create a vector representing the line
            val line = linePoint2.difference(linePoint1)
            // get the length squared of the line
            val ab2 = line.dot(line)
            // check ab2 for zero (linePoint1 == linePoint2)
            if (ab2 <= Epsilon.E) return linePoint1.copy()
            // get the projection of AP on AB
            val ap_ab = p1ToP.dot(line)
            // get the position from the first line point to the projection
            val t = ap_ab / ab2
            // create the point on the line
            return line.multiply(t).add(linePoint1)
        }

        /**
         * Returns the intersection point of the two line segments or null if they are parallel, coincident
         * or don't intersect.
         *
         *
         * If we let:
         *
         *  A = A<sub>p2</sub> - A<sub>p1</sub>
         * B = B<sub>p2</sub> - B<sub>p1</sub>
         * we can create two parametric equations:
         *
         *  Q = A<sub>p1</sub> + t<sub>a</sub>A
         * Q = B<sub>p1</sub> + t<sub>b</sub>B
         * Where Q is the intersection point:
         *
         *  A<sub>p1</sub> + t<sub>a</sub>A = B<sub>p1</sub> + t<sub>b</sub>B
         * We can solve for t<sub>b</sub> by applying the cross product with A on both sides:
         *
         *  (A<sub>p1</sub> + t<sub>a</sub>A) x A = (B<sub>p1</sub> + t<sub>b</sub>B) x A
         * A<sub>p1</sub> x A = B<sub>p1</sub> x A + t<sub>b</sub>B x A
         * (A<sub>p1</sub> - B<sub>p1</sub>) x A = t<sub>b</sub>B x A
         * t<sub>b</sub> = ((A<sub>p1</sub> - B<sub>p1</sub>) x A) / (B x A)
         * If B x A == 0 then the segments are parallel.  If the top == 0 then they don't intersect.  If both the
         * top and bottom are zero then the segments are coincident.
         *
         *
         * If t<sub>b</sub> or t<sub>a</sub> less than zero or greater than 1 then the segments do not intersect.
         *
         *
         * If the segments do not intersect, are parallel, or are coincident, null is returned.
         * @param ap1 the first point of the first line segment
         * @param ap2 the second point of the first line segment
         * @param bp1 the first point of the second line segment
         * @param bp2 the second point of the second line segment
         * @return Vector2 the intersection point; null if the line segments don't intersect, are parallel, or are coincident
         * @see .getLineIntersection
         * @throws NullPointerException if ap1, ap2, bp1, or bp2 is null
         * @since 3.1.1
         */
        @JvmStatic
        fun getSegmentIntersection(ap1: Vector2, ap2: Vector2, bp1: Vector2, bp2: Vector2): Vector2? {
            val A: Vector2 = ap1.to(ap2)
            val B: Vector2 = bp1.to(bp2)

            // compute the bottom
            val BxA = B.cross(A)
            if (abs(BxA) <= Epsilon.E) {
                // the line segments are parallel and don't intersect
                return null
            }

            // compute the top
            val ambxA = ap1.difference(bp1).cross(A)
            if (abs(ambxA) <= Epsilon.E) {
                // the line segments are coincident
                return null
            }

            // compute tb
            val tb = ambxA / BxA
            if (tb < 0.0 || tb > 1.0) {
                // no intersection
                return null
            }

            // compute the intersection point
            val ip = B.product(tb).add(bp1)

            // since both are segments we need to verify that
            // ta is also valid.
            // compute ta
            val ta = ip.difference(ap1).dot(A) / A.dot(A)
            return if (ta < 0.0 || ta > 1.0) {
                // no intersection
                null
            } else ip
        }


        /**
         * Returns the farthest feature on the given segment.
         *
         *
         * This will always return the segment itself, but must return it with the correct winding
         * and the correct maximum.
         * @param v1 the first segment vertex
         * @param v2 the second segment vertex
         * @param vector the direction
         * @param transform the local to world space [Transform] of this [Convex] [Shape]
         * @return [EdgeFeature]
         * @throws NullPointerException if v1, v2, vector, or transform is null
         * @since 3.1.5
         */
        @JvmStatic
        fun getFarthestFeature(
            v1: Vector2?,
            v2: Vector2?,
            vector: Vector2,
            transform: Transform
        ): EdgeFeature {
            // the farthest feature for a line is always the line itself
            var max: Vector2? = null
            // get the vertices
            val p1 = transform.getTransformed(v1!!)
            val p2 = transform.getTransformed(v2!!)
            // project them onto the vector
            val dot1 = vector.dot(p1)
            val dot2 = vector.dot(p2)
            // find the greatest projection
            var index = 0
            if (dot1 >= dot2) {
                max = p1
                index = 0
            } else {
                max = p2
                index = 1
            }
            // return the points of the segment in the
            // opposite direction as the other shape
            val vp1 = PointFeature(p1, 0)
            val vp2 = PointFeature(p2, 1)
            val vm = PointFeature(max, index)
            // make sure the edge is the right winding
            return if (p1.to(p2).right().dot(vector) > 0) {
                EdgeFeature(vp2, vp1, vm, p2.to(p1), 0)
            } else {
                EdgeFeature(vp1, vp2, vm, p1.to(p2), 0)
            }
        }


        /**
         * Returns the farthest point on the given segment.
         * @param v1 the first point of the segment
         * @param v2 the second point of the segment
         * @param vector the direction
         * @param transform the local to world space [Transform] of this [Convex] [Shape]
         * @return [Vector2]
         * @throws NullPointerException if v1, v2, vector, or transform is null
         * @since 3.1.5
         */
        @JvmStatic
        fun getFarthestPoint(
            v1: Vector2?,
            v2: Vector2?,
            vector: Vector2,
            transform: Transform
        ): Vector2 {
            // get the vertices and the center
            val p1 = transform.getTransformed(v1!!)
            val p2 = transform.getTransformed(v2!!)
            // project them onto the vector
            val dot1 = vector.dot(p1)
            val dot2 = vector.dot(p2)
            // find the greatest projection
            return if (dot1 >= dot2) {
                p1
            } else {
                p2
            }
        }

        /**
         * Returns the point on the given line segment closest to the given point.
         *
         *
         * If the point closest to the given point is on the line created by the
         * given line segment, but is not on the line segment then either of the segments
         * end points will be returned.
         *
         *
         * Assumes all points are in world space.
         * @see Segment.getPointOnLineClosestToPoint
         * @param point the point
         * @param linePoint1 the first point of the line
         * @param linePoint2 the second point of the line
         * @return [Vector2]
         * @throws NullPointerException if point, linePoint1, or linePoint2 is null
         */
        @JvmStatic
        fun getPointOnSegmentClosestToPoint(
            point: Vector2,
            linePoint1: Vector2,
            linePoint2: Vector2
        ): Vector2 {
            // create a vector from the point to the first line point
            val p1ToP = point.difference(linePoint1)
            // create a vector representing the line
            val line = linePoint2.difference(linePoint1)
            // get the length squared of the line
            val ab2 = line.dot(line)
            // get the projection of AP on AB
            val ap_ab = p1ToP.dot(line)
            // check ab2 for zero (linePoint1 == linePoint2)
            if (ab2 <= Epsilon.E) return linePoint1.copy()
            // get the position from the first line point to the projection
            var t = ap_ab / ab2
            // make sure t is in between 0.0 and 1.0
            t = Interval.clamp(t, 0.0, 1.0)
            // create the point on the line
            return line.multiply(t).add(linePoint1)
        }
    }

}
