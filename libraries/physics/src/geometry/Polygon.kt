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
import org.dyn4j.geometry.Geometry.getAreaWeightedCenter
import org.dyn4j.geometry.Geometry.getCounterClockwiseEdgeNormals
import org.dyn4j.geometry.Geometry.getRotationRadius
import org.dyn4j.geometry.RobustGeometry.getLocation
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.math.abs
import kotlin.math.sign


/**
 * Implementation of an arbitrary polygon [Convex] [Shape].
 *
 *
 * A [Polygon] must have at least 3 vertices where one of which is not colinear with the other two.
 * A [Polygon] must also be [Convex] and have counter-clockwise winding of points.
 *
 *
 * A polygon cannot have coincident vertices.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 */
open class Polygon : AbstractShape, Convex, Wound, Shape, Transformable, DataContainer {

    override val woundVertices: Array<Vector2> get() = vertices
    override val woundNormals: Array<Vector2> get() = normals

    /** The polygon vertices  */
    @JvmField
    val vertices: Array<Vector2>

    /** The polygon normals  */
    @JvmField
    val normals: Array<Vector2>

    constructor(center: Vector2, radius: Double, vertices: Array<Vector2>, normals: Array<Vector2>) :
            super(center, radius) {
        this.vertices = vertices
        this.normals = normals
    }

    private constructor(vertices: Array<Vector2>, center: Vector2) :
            super(center, getRotationRadius(center = center, vertices = vertices)) {
        validate(*vertices)
        this.vertices = vertices
        this.normals = getCounterClockwiseEdgeNormals(*vertices)!!
    }

    constructor(vararg vertices: Vector2) :
            this(vertices = vertices as Array<Vector2>, center = getAreaWeightedCenter(*vertices))

    /**
     * Validates the constructor input returning true if valid or throwing an exception if invalid.
     * @param vertices the array of vertices
     * @return boolean true
     * @throws NullPointerException if vertices is null or contains a null element
     * @throws IllegalArgumentException if vertices contains less than 3 points, contains coincident points, is not convex, or has clockwise winding
     */
    private fun validate(vararg vertices: Vector2?): Boolean {
        // check the vertex array
        if (vertices == null) throw NullPointerException(message("geometry.polygon.nullArray"))
        // get the size
        val size = vertices.size
        // check the size
        if (size < 3) throw IllegalArgumentException(message("geometry.polygon.lessThan3Vertices"))
        // check for null vertices
        for (i in 0 until size) {
            if (vertices[i] == null) throw NullPointerException(message("geometry.polygon.nullVertices"))
        }
        // check for convex
        var area = 0.0
        var sign = 0.0
        for (i in 0 until size) {
            val p0 = if (i - 1 < 0) vertices[size - 1]!! else vertices[i - 1]!!
            val p1 = vertices[i]
            val p2 = if (i + 1 == size) vertices[0]!! else vertices[i + 1]!!
            // check for coincident vertices
            if (p1!!.equals(p2)) {
                throw IllegalArgumentException(message("geometry.polygon.coincidentVertices"))
            }
            // check the cross product for CCW winding
            val cross: Double = p0.to(p1).cross(p1.to(p2))
            val tsign: Double = cross.sign
            area += cross
            // check for colinear edges (for now its allowed)
            if (abs(cross) > Epsilon.E) {
                // check for convexity
                if (sign != 0.0 && tsign != sign) {
                    throw IllegalArgumentException(message("geometry.polygon.nonConvex"))
                }
            }
            sign = tsign
        }
        // don't allow degenerate polygons
        if (abs(area) <= Epsilon.E) {
            throw IllegalArgumentException(message("geometry.polygon.zeroArea"))
        }
        // check for CCW
        if (area < 0.0) {
            throw IllegalArgumentException(message("geometry.polygon.invalidWinding"))
        }
        // if we've made it this far then continue;
        return true
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Wound#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Polygon[").append(super.toString())
            .append("|Vertices={")
        for (i in 0 until vertices.size) {
            if (i != 0) sb.append(",")
            sb.append(vertices[i])
        }
        sb.append("}").append("]")
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

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getAxes(java.util.List, org.dyn4j.geometry.Transform)
	 */
    override fun getAxes(foci: Array<Vector2>?, transform: Transform): Array<Vector2>? {
        // get the size of the foci list
        val fociSize = foci?.size ?: 0
        // get the number of vertices this polygon has
        val size: Int = vertices.size
        // the axes of a polygon are created from the normal of the edges
        // plus the closest point to each focus
        val axes = arrayOfNulls<Vector2>(size + fociSize) as Array<Vector2>
        var n = 0
        // loop over the edge normals and put them into world space
        for (i in 0 until size) {
            // create references to the current points
            val v = normals!![i]!!
            // transform it into world space and add it to the list
            axes[n++] = transform.getTransformedR(v)
        }
        // loop over the focal points and find the closest
        // points on the polygon to the focal points
        for (i in 0 until fociSize) {
            // get the current focus
            val f = foci!![i]!!
            // create a place for the closest point
            var closest: Vector2 = transform.getTransformed(vertices[0])
            var d = f.distanceSquared(closest!!)
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
     * Not applicable to this shape. Always returns null.
     * @return null
     */
    override fun getFoci(transform: Transform): Array<Vector2>? {
        return null
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#contains(org.dyn4j.geometry.Vector, org.dyn4j.geometry.Transform)
	 */
    override fun contains(point: Vector2, transform: Transform): Boolean {
        // if the polygon is convex then do a simple inside test
        // if the the sign of the location of the point on the side of an edge (or line)
        // is always the same and the polygon is convex then we know that the
        // point lies inside the polygon
        // This method doesn't care about vertex winding
        // inverse transform the point to put it in local coordinates
        val p = transform.getInverseTransformed(point)

        // start from the pair (p1 = last, p2 = first) so there's no need to check in the loop for wrap-around of the i + 1 vertice
        val size: Int = vertices.size
        var p1: Vector2 = vertices[size - 1]
        var p2 = vertices[0]

        // get the location of the point relative to the first two vertices
        var last: Double = getLocation(p, p1, p2)

        // loop through the rest of the vertices
        for (i in 0 until size - 1) {
            // p1 is now p2
            p1 = p2
            // p2 is the next point
            p2 = vertices[i + 1]
            // check if they are equal (one of the vertices)
            if (p.equals(p1) || p.equals(p2)) {
                return true
            }

            // do side of line test
            val location: Double = getLocation(p, p1, p2)

            // multiply the last location with this location
            // if they are the same sign then the opertation will yield a positive result
            // -x * -y = +xy, x * y = +xy, -x * y = -xy, x * -y = -xy
            if (last * location < 0) {
                // reminder: (-0.0 < 0.0) evaluates to false and not true
                return false
            }

            // update the last location, but only if it's not zero
            // a location of zero indicates that the point lies ON the line
            // through p1 and p2. We can ignore these values because the
            // convexity requirement of the shape will ensure that if it's
            // outside, a sign will change.
            if (abs(location) > Epsilon.E) {
                last = location
            }
        }
        return true
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.AbstractShape#rotate(org.dyn4j.geometry.Rotation, double, double)
	 */
    override fun rotate(rotation: Rotation, x: Double, y: Double) {
        super.rotate(rotation, x, y)
        val size: Int = vertices.size
        for (i in 0 until size) {
            vertices[i].rotate(rotation, x, y)
            normals[i]!!.rotate(rotation)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.AbstractShape#translate(double, double)
	 */
    override fun translate(x: Double, y: Double) {
        super.translate(x, y)
        val size: Int = vertices.size
        for (i in 0 until size) {
            vertices[i].add(x, y)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#project(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun project(vector: Vector2, transform: Transform): Interval {
        //System.out.println(1);
        var v = 0.0
        // get the first point
        var p = transform.getTransformed(vertices[0])
        // project the point onto the vector
        var min = vector.dot(p)
        var max = min
        // loop over the rest of the vertices
        val size: Int = vertices.size
        for (i in 1 until size) {
            // get the next point
            p = transform.getTransformed(vertices[i])
            // project it onto the vector
            v = vector.dot(p)
            if (v < min) {
                min = v
            } else if (v > max) {
                max = v
            }
        }
        return Interval(min, max)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFarthestFeature(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestFeature(vector: Vector2, transform: Transform): EdgeFeature {
        // transform the normal into local space
        val localn = transform.getInverseTransformedR(vector!!)
        val index: Int = getFarthestVertexIndex(localn)
        val count: Int = vertices.size
        val maximum = Vector2(vertices[index])

        // once we have the point of maximum
        // see which edge is most perpendicular
        val leftN = normals[if (index == 0) count - 1 else index - 1]!!
        val rightN = normals[index]!!
        // create the maximum point for the feature (transform the maximum into world space)
        transform.transform(maximum)
        val vm = PointFeature(maximum, index)
        // is the left or right edge more perpendicular?
        return if (leftN.dot(localn) < rightN.dot(localn)) {
            val l = if (index == count - 1) 0 else index + 1
            val left = transform.getTransformed(vertices[l])
            val vl = PointFeature(left, l)
            // make sure the edge is the right winding
            EdgeFeature(vm, vl, vm, maximum.to(left), index + 1)
        } else {
            val r = if (index == 0) count - 1 else index - 1
            val right = transform.getTransformed(vertices[r])
            val vr = PointFeature(right, r)
            // make sure the edge is the right winding
            EdgeFeature(vr, vm, vm, right.to(maximum), index)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFarthestPoint(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestPoint(vector: Vector2, transform: Transform): Vector2 {
        // transform the normal into local space
        val localn = transform.getInverseTransformedR(vector!!)

        // find the index of the farthest point
        val index: Int = getFarthestVertexIndex(localn)

        // transform the point into world space and return
        return transform.getTransformed(vertices[index])
    }

    /**
     * Internal helper method that returns the index of the point that is
     * farthest in direction of a vector.
     *
     * @param vector the direction
     * @return the index of the farthest vertex in that direction
     * @since 3.4.0
     */
    fun getFarthestVertexIndex(vector: Vector2): Int {
        /*
		 * The sequence a(n) = vector.dot(vertices[n]) has a maximum, a minimum and is monotonic (though not strictly monotonic) between those extrema.
		 * All indices are considered in modular arithmetic. I choose the initial index to be 0.
		 *
		 * Based on that I follow this approach:
		 * We start from an initial index n0. We want to an adjacent to n0 index n1 for which a(n1) > a(n0).
		 * If no such index exists then n0 is the maximum. Else we start in direction of n1 (i.e. left or right of n0)
		 * and while a(n) increases we continue to that direction. When the next number of the sequence does not increases anymore
		 * we can stop and we have found max{a(n)}.
		 *
		 * Although the idea is simple we need to be careful with some edge cases and the correctness of the algorithm in all cases.
		 * Although the sequence is not strictly monotonic the absence of equalities is intentional and wields the correct answer (see below).
		 *
		 * The correctness of this method relies on some properties:
		 * 1) If n0 and n1 are two adjacent indices and a(n0) = a(n1) then a(n0) and a(n1) are either max{a(n)} or min{a(n)}.
		 *    This holds for all convex polygons. This property can guarantee that if our initial index is n0 or n1 then it does not
		 *    matter to which side (left or right) we start searching.
		 * 2) The polygon has no coincident vertices.
		 *    This guarantees us that there are no adjacent n0, n1, n2 for which a(n0) = a(n1) = a(n2)
		 *    and that only two adjacent n0, n1 can exist with a(n0) = a(n1). This is important because if
		 *    those adjacent n0, n1, n2 existed the code below would always return the initial index, without knowing if
		 *    it's a minimum or maximum. But since only two adjacent indices can exist with a(n0) = a(n1) the code below
		 *    will always start searching in one direction and because of 1) this will give us the correct answer.
		 */

        // The initial starting index and the corresponding dot product
        var maxIndex = 0
        val n: Int = vertices.size
        var max = vector.dot(vertices[0])
        var candidateMax: Double
        if (max < vector.dot(vertices[1]).also { candidateMax = it }) {
            // Search to the right
            do {
                max = candidateMax
                maxIndex++
            } while (maxIndex + 1 < n && max < vector.dot(vertices[maxIndex + 1])
                    .also { candidateMax = it }
            )
        } else if (max < vector.dot(vertices[n - 1]).also { candidateMax = it }) {
            maxIndex = n // n = 0 (mod n)

            // Search to the left
            do {
                max = candidateMax
                maxIndex--
            } while (maxIndex > 0 && max <= vector.dot(vertices[maxIndex - 1]).also { candidateMax = it })
            //				  ,----------^^
            // The equality here makes this algorithm produce the same results with the old when there exist adjacent vertices
            // with the same a(n).
        }
        // else maxIndex = 0, because if neither of the above conditions is met, then the initial index is the maximum
        return maxIndex
    }

    /**
     * Creates a [Mass] object using the geometric properties of
     * this [Polygon] and the given density.
     *
     *
     * A [Polygon]'s centroid must be computed by the area weighted method since the
     * average method can be bias to one side if there are more points on that one
     * side than another.
     *
     *
     * Finding the area of a [Polygon] can be done by using the following
     * summation:
     *
     *  0.5 * (x<sub>i</sub> * y<sub>i + 1</sub> - x<sub>i + 1</sub> * y<sub>i</sub>)
     * Finding the area weighted centroid can be done by using the following
     * summation:
     *
     *  1 / (6 * A) * (p<sub>i</sub> + p<sub>i + 1</sub>) * (x<sub>i</sub> * y<sub>i + 1</sub> - x<sub>i + 1</sub> * y<sub>i</sub>)
     * Finding the inertia tensor can by done by using the following equation:
     *
     *
     * (p<sub>i + 1</sub> x p<sub>i</sub>) * (p<sub>i</sub><sup>2</sup> + p<sub>i</sub>  p<sub>i + 1</sub> + p<sub>i + 1</sub><sup>2</sup>)
     * m / 6 * -------------------------------------------
     * (p<sub>i + 1</sub> x p<sub>i</sub>)
     *
     * Where the mass is computed by:
     *
     *  d * area
     * @param density the density in kg/m<sup>2</sup>
     * @return [Mass] the [Mass] of this [Polygon]
     */
    override fun createMass(density: Double): Mass {
        // can't use normal centroid calculation since it will be weighted towards sides
        // that have larger distribution of points.
        val center = Vector2()
        var area = 0.0
        var I = 0.0
        val n: Int = vertices.size
        // get the average center
        val ac = Vector2()
        for (i in 0 until n) {
            ac.add(vertices[i])
        }
        ac.divide(n.toDouble())
        // loop through the vertices using two variables to avoid branches in the loop
        var i1 = n - 1
        var i2 = 0
        while (i2 < n) {

            // get two vertices
            var p1 = vertices[i1]
            var p2 = vertices[i2]
            // get the vector from the center to the point
            p1 = p1.difference(ac)
            p2 = p2.difference(ac)
            // perform the cross product (yi * x(i+1) - y(i+1) * xi)
            val D = p1.cross(p2)
            // multiply by half
            val triangleArea = 0.5 * D
            // add it to the total area
            area += triangleArea

            // area weighted centroid
            // (p1 + p2) * (D / 6)
            // = (x1 + x2) * (yi * x(i+1) - y(i+1) * xi) / 6
            // we will divide by the total area later
            center.x += (p1.x + p2.x) * Geometry.INV_3 * triangleArea
            center.y += (p1.y + p2.y) * Geometry.INV_3 * triangleArea

            // (yi * x(i+1) - y(i+1) * xi) * (p2^2 + p2 . p1 + p1^2)
            I += triangleArea * (p2.dot(p2) + p2.dot(p1) + p1.dot(p1))
            i1 = i2++
        }
        // compute the mass
        val m = density * area
        // finish the centroid calculation by dividing by the total area
        // and adding in the average center
        center.divide(area)
        val c = center.sum(ac)
        // finish the inertia tensor by dividing by the total area and multiplying by d / 6
        I *= density / 6.0
        // shift the axis of rotation to the area weighted center
        // (center is the vector from the average center to the area weighted center since
        // the average center is used as the origin)
        I -= m * center.magnitudeSquared
        return Mass(c, m, I)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#createAABB(org.dyn4j.geometry.Transform)
	 */
    override fun createAABB(transform: Transform): AABB {
        // get the first point
        val p = transform.getTransformed(vertices[0])

        // initialize min and max values
        var minX = p.x
        var maxX = p.x
        var minY = p.y
        var maxY = p.y

        // loop over the rest of the vertices
        val size: Int = vertices.size
        for (i in 1 until size) {
            // get the next point p = transform.getTransformed(this.vertices[i]);
            val px = transform.getTransformedX(vertices[i])
            val py = transform.getTransformedY(vertices[i])

            // compare the x values
            if (px < minX) {
                minX = px
            } else if (px > maxX) {
                maxX = px
            }

            // compare the y values
            if (py < minY) {
                minY = py
            } else if (py > maxY) {
                maxY = py
            }
        }

        // create the aabb
        return AABB(minX, minY, maxX, maxY)
    }

}