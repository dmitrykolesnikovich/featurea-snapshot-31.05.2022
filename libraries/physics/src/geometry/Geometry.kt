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

import org.dyn4j.Epsilon
import org.dyn4j.resources.Messages.getString
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.*

/**
 * Contains static methods to perform standard geometric operations.
 *
 *
 * This class can be used to create [Shape]s of varying types via the `create`* methods.
 * While [Shape]s can be created using their constructors as well, the methods here can place their
 * centers on the origin and also make copies of the given input to avoid reuse issues.
 *
 *
 * This class also contains various helper methods for cleaning vector arrays and lists and performing
 * various operations on [Shape]s.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 */
object Geometry {
    /** 2 * PI constant  */
    @JvmField
    val TWO_PI: Double = 2.0 * PI

    /** The value of 1/3  */
    const val INV_3 = 1.0 / 3.0

    /** The value of the inverse of the square root of 3; 1/sqrt(3)  */
    @JvmField
    val INV_SQRT_3: Double = 1.0 / sqrt(3.0)

    /**
     * Returns the winding, Clockwise or Counter-Clockwise, for the given
     * list of points of a polygon.
     *
     *
     * This method determines the winding by computing a signed "area".
     * @param points the points of a polygon
     * @return double negative for Clockwise winding; positive for Counter-Clockwise winding
     * @throws NullPointerException if points is null or an element of points is null
     * @throws IllegalArgumentException if points contains less than 2 elements
     * @since 2.2.0
     */
    @JvmStatic
    fun getWinding(points: List<Vector2?>?): Double {
        // check for a null list
        if (points == null) throw NullPointerException(getString("geometry.nullPointList"))
        // get the size
        val size = points.size
        // the size must be larger than 1
        if (size < 2) throw IllegalArgumentException(getString("geometry.invalidSizePointList2"))
        // determine the winding by computing a signed "area"
        var area = 0.0
        for (i in 0 until size) {
            // get the current point and the next point
            val p1 = points[i]
            val p2 = points[if (i + 1 == size) 0 else i + 1]
            // check for null
            if (p1 == null || p2 == null) throw NullPointerException(getString("geometry.nullPointListElements"))
            // add the signed area
            area += p1.cross(p2)
        }
        // return the area
        return area
    }

    /**
     * Returns the winding, Clockwise or Counter-Clockwise, for the given
     * array of points of a polygon.
     * @param points the points of a polygon
     * @return double negative for Clockwise winding; positive for Counter-Clockwise winding
     * @throws NullPointerException if points is null or an element of points is null
     * @throws IllegalArgumentException if points contains less than 2 elements
     * @since 2.2.0
     */
    @JvmStatic
    fun getWinding(vararg points: Vector2): Double {
        // get the size
        val size = points.size
        // the size must be larger than 1
        if (size < 2) throw IllegalArgumentException(getString("geometry.invalidSizePointArray2"))
        // determine the winding by computing a signed "area"
        var area = 0.0
        for (i in 0 until size) {
            // get the current point and the next point
            val p1 = points[i]
            val p2 = points[if (i + 1 == size) 0 else i + 1]
            // add the signed area
            area += p1.cross(p2)
        }
        // return the area
        return area
    }

    /**
     * Reverses the order of the polygon points within the given array.
     *
     *
     * This method performs a simple array reverse.
     * @param points the polygon points
     * @throws NullPointerException if points is null
     * @since 2.2.0
     */
    @JvmStatic
    fun reverseWinding(points: Array<out Vector2>) {
        val points = points as Array<Vector2>
        // get the length
        val size = points.size
        // check for a length of 1
        if (size == 1 || size == 0) return
        // otherwise perform the swapping loop
        var i = 0
        var j = size - 1
        var temp: Vector2
        while (j > i) {
            // swap
            temp = points[j]
            points[j] = points[i]
            points[i] = temp
            // increment
            j--
            i++
        }
    }

    /**
     * Reverses the order of the polygon points within the given list.
     *
     *
     * This method performs a simple list reverse.
     * @param points the polygon points
     * @throws NullPointerException if points is null
     * @since 2.2.0
     */
    @JvmStatic
    fun reverseWinding(points: MutableList<Vector2>) {
        // check for a null list
        if (points == null) throw NullPointerException(getString("geometry.nullPointList"))
        // check for a length of 0 or 1
        if (points.size <= 1) return
        // otherwise reverse the list
        points.reverse()
    }

    /**
     * Returns the centroid of the given points by performing an average.
     * @param points the list of points
     * @return [Vector2] the centroid
     * @throws NullPointerException if points is null or an element of points is null
     * @throws IllegalArgumentException if points is an empty list
     */
    @JvmStatic
    fun getAverageCenter(points: List<Vector2>?): Vector2 {
        // check for null list
        if (points == null) throw NullPointerException(getString("geometry.nullPointList"))
        // check for empty list
        if (points.isEmpty()) throw IllegalArgumentException(getString("geometry.invalidSizePointList1"))
        // get the size
        val size = points.size
        // check for a list of one point
        if (size == 1) {
            val p = points[0] ?: throw NullPointerException(getString("geometry.nullPointListElements"))
            // make sure its not null
            // return a copy
            return p.copy()
        }

        // otherwise perform the average
        val ac = Vector2()
        for (i in 0 until size) {
            val point = points[i] ?: throw NullPointerException(getString("geometry.nullPointListElements"))
            // check for null
            ac.add(point)
        }
        return ac.divide(size.toDouble())
    }

    /**
     * Returns the centroid of the given points by performing an average.
     * @see .getAverageCenter
     * @param points the array of points
     * @return [Vector2] the centroid
     * @throws NullPointerException if points is null or an element of points is null
     * @throws IllegalArgumentException if points is an empty array
     */
    @JvmStatic
    fun getAverageCenter(vararg points: Vector2?): Vector2 {
        // check for null array
        if (points == null) throw NullPointerException(getString("geometry.nullPointArray"))
        // get the length
        val size = points.size
        // check for empty
        if (size == 0) throw IllegalArgumentException(getString("geometry.invalidSizePointArray1"))
        // check for a list of one point
        if (size == 1) {
            val p = points[0] ?: throw NullPointerException(getString("geometry.nullPointArrayElements"))
            // check for null
            return p.copy()
        }

        // otherwise perform the average
        val ac = Vector2()
        for (i in 0 until size) {
            val point = points[i] ?: throw NullPointerException(getString("geometry.nullPointArrayElements"))
            // check for null
            ac.add(point)
        }
        return ac.divide(size.toDouble())
    }

    /**
     * Returns the area weighted centroid for the given points.
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
     * @param points the [Polygon] points
     * @return [Vector2] the area weighted centroid
     * @throws NullPointerException if points is null or an element of points is null
     * @throws IllegalArgumentException if points is empty
     */
    @JvmStatic
    fun getAreaWeightedCenter(points: List<Vector2>): Vector2 {
        // calculate the average center
        // note that this also performs the necessary checks and throws any exceptions needed
        val ac = getAverageCenter(points)
        val size = points.size

        // otherwise perform the computation
        val center = Vector2()
        var area = 0.0
        // loop through the vertices
        for (i in 0 until size) {
            // get two verticies
            var p1 = points[i]
            var p2 = if (i + 1 < size) points[i + 1] else points[0]
            p1 = p1.difference(ac)
            p2 = p2.difference(ac)
            // perform the cross product (yi * x(i+1) - y(i+1) * xi)
            val d = p1.cross(p2)
            // multiply by half
            val triangleArea = 0.5 * d
            // add it to the total area
            area += triangleArea

            // area weighted centroid
            // (p1 + p2) * (D / 3)
            // = (x1 + x2) * (yi * x(i+1) - y(i+1) * xi) / 3
            // we will divide by the total area later
            center.add(p1.add(p2).multiply(INV_3).multiply(triangleArea))
        }
        // check for zero area
        if (abs(area) <= Epsilon.E) {
            // zero area can only happen if all the points are the same point
            // in which case just return a copy of the first
            return points[0].copy()
        }
        // finish the centroid calculation by dividing by the total area
        center.divide(area).add(ac)
        // return the center
        return center
    }

    /**
     * Returns the area weighted centroid for the given points.
     * @see .getAreaWeightedCenter
     * @param points the [Polygon] points
     * @return [Vector2] the area weighted centroid
     * @throws NullPointerException if points is null or an element of points is null
     * @throws IllegalArgumentException if points is empty
     */
    @JvmStatic
    fun getAreaWeightedCenter(vararg points: Vector2): Vector2 {
        // calculate the average center
        // note that this also performs the necessary checks and throws any exceptions needed
        val ac = getAverageCenter(*points)
        val size = points.size
        val center = Vector2()
        var area = 0.0
        // loop through the vertices
        for (i in 0 until size) {
            // get two verticies
            var p1 = points[i]
            var p2 = if (i + 1 < size) points[i + 1] else points[0]
            p1 = p1.difference(ac)
            p2 = p2.difference(ac)
            // perform the cross product (yi * x(i+1) - y(i+1) * xi)
            val d = p1.cross(p2)
            // multiply by half
            val triangleArea = 0.5 * d
            // add it to the total area
            area += triangleArea

            // area weighted centroid
            // (p1 + p2) * (D / 3)
            // = (x1 + x2) * (yi * x(i+1) - y(i+1) * xi) / 3
            // we will divide by the total area later
            center.add(p1.add(p2).multiply(INV_3).multiply(triangleArea))
        }
        // check for zero area
        if (abs(area) <= Epsilon.E) {
            // zero area can only happen if all the points are the same point
            // in which case just return a copy of the first
            return points[0].copy()
        }
        // finish the centroid calculation by dividing by the total area
        center.divide(area).add(ac)
        // return the center
        return center
    }

    /**
     * Returns the maximum radius of the given vertices rotated about the origin.
     *
     *
     * If the vertices array is null or empty, zero is returned.
     * @param vertices the polygon vertices
     * @return double
     * @see .getRotationRadius
     * @since 3.2.0
     */
    @JvmStatic
    fun getRotationRadius(vararg vertices: Vector2): Double {
        return getRotationRadius(center = Vector2(), vertices = *vertices)
    }

    /**
     * Returns the maximum radius of the given vertices rotated about the given center.
     *
     *
     * If the vertices array is null or empty, zero is returned.  If center is null
     * the origin will be used instead.
     * @param center the center point
     * @param vertices the polygon vertices
     * @return double
     * @since 3.2.0
     */
    @JvmStatic
    fun getRotationRadius(
        center: Vector2?,
        vararg vertices: Vector2?
    ): Double {
        // validate the vertices
        var center = center
        if (vertices == null) return 0.0
        // validate the center
        if (center == null) center = Vector2()
        // validate the length
        val size = vertices.size
        if (size == 0) return 0.0
        // find the maximum radius from the center
        var r2 = 0.0
        for (i in 0 until size) {
            val v = vertices[i]
            // validate each vertex
            if (v != null) {
                val r2t = center.distanceSquared(v)
                // keep the largest
                r2 = max(r2, r2t)
            }
        }
        // set the radius
        return sqrt(r2)
    }

    /**
     * Returns an array of normalized vectors representing the normals of all the
     * edges given the vertices.
     *
     *
     * This method assumes counter-clockwise ordering.
     *
     *
     * Returns null if the given vertices array is null or empty.
     * @param vertices the vertices
     * @return [Vector2][]
     * @throws NullPointerException if vertices contains a null element
     * @since 3.2.0
     */
    @JvmStatic
    fun getCounterClockwiseEdgeNormals(vararg vertices: Vector2): Array<Vector2>? {
        if (vertices == null) return null
        val size = vertices.size
        if (size == 0) return null
        val normals = arrayOfNulls<Vector2>(size) as Array<Vector2>
        for (i in 0 until size) {
            // get the edge points
            val p1 = vertices[i]
            val p2 = if (i + 1 == size) vertices[0] else vertices[i + 1]
            // create the edge and get its left perpedicular vector
            val n = p1.to(p2).left()
            // normalize it
            n.normalize()
            normals[i] = n
        }
        return normals
    }

    /**
     * Returns a new [Circle] with the given radius centered on the origin.
     * @param radius the radius in meters
     * @return [Circle]
     * @throws IllegalArgumentException if radius is less than or equal to zero
     */
    @JvmStatic
    fun createCircle(radius: Double): Circle {
        return Circle(radius)
    }

    /**
     * Returns a new [Polygon] with the given vertices.
     *
     *
     * This method makes a copy of both the array and the vertices within the array to
     * create the new [Polygon].
     *
     *
     * The center of the [Polygon] will be computed using the area weighted method.
     * @param vertices the array of vertices
     * @return [Polygon]
     * @throws NullPointerException if vertices is null or an element of vertices is null
     * @throws IllegalArgumentException if vertices contains less than 3 non-null vertices
     * @see .createPolygonAtOrigin
     */
    @JvmStatic
    fun createPolygon(vararg vertices: Vector2?): Polygon {
        // loop over the points an copy them
        val size = vertices.size
        // check the size
        val verts = arrayOfNulls<Vector2>(size)
        for (i in 0 until size) {
            val vertex = vertices[i]
            // check for null points
            if (vertex != null) {
                verts[i] = vertex.copy()
            } else {
                throw NullPointerException(getString("geometry.nullPolygonPoint"))
            }
        }
        return Polygon(*verts as Array<Vector2>)
    }

    /**
     * Returns a new [Polygon], using the given vertices, centered at the origin.
     *
     *
     * This method makes a copy of both the array and the vertices within the array to
     * create the new [Polygon].
     *
     *
     * This method translates the [Polygon] vertices so that the center is at the origin.
     * @param vertices the array of vertices
     * @return [Polygon]
     * @throws NullPointerException if vertices is null or an element of vertices is null
     * @throws IllegalArgumentException if vertices contains less than 3 non-null vertices
     */
    @JvmStatic
    fun createPolygonAtOrigin(vararg vertices: Vector2?): Polygon {
        val polygon = createPolygon(*vertices)
        val center = polygon.center
        polygon.translate(-center.x, -center.y)
        return polygon
    }

    /**
     * Returns a new [Polygon] with count number of points, where the
     * points are evenly distributed around the unit circle.  The resulting [Polygon]
     * will be centered on the origin.
     *
     *
     * The radius parameter is the distance from the center of the polygon to each vertex.
     * @param count the number of vertices
     * @param radius the radius from the center to each vertex in meters
     * @return [Polygon]
     * @throws IllegalArgumentException if count is less than 3 or radius is less than or equal to zero
     * @see .createUnitCirclePolygon
     * @see .createPolygonalCircle
     * @see .createPolygonalCircle
     */
    @JvmStatic
    fun createUnitCirclePolygon(count: Int, radius: Double): Polygon {
        return createUnitCirclePolygon(count, radius, 0.0)
    }

    /**
     * Returns a new [Polygon] with count number of points, where the
     * points are evenly distributed around the unit circle.  The resulting [Polygon]
     * will be centered on the origin.
     *
     *
     * The radius parameter is the distance from the center of the polygon to each vertex.
     *
     *
     * The theta parameter is a vertex angle offset used to rotate all the vertices
     * by the given amount.
     * @param count the number of vertices
     * @param radius the radius from the center to each vertex in meters
     * @param theta the vertex angle offset in radians
     * @return [Polygon]
     * @throws IllegalArgumentException if count is less than 3 or radius is less than or equal to zero
     * @see .createPolygonalCircle
     */
    @JvmStatic
    fun createUnitCirclePolygon(count: Int, radius: Double, theta: Double): Polygon {
        // check the count
        if (count < 3) throw IllegalArgumentException(getString("geometry.invalidVerticesSize"))
        // check the radius
        if (radius <= 0.0) throw IllegalArgumentException(getString("geometry.invalidRadius"))
        // call the more efficient method here
        return createPolygonalCircle(count, radius, theta)
    }

    /**
     * Creates a square (equal height and width [Rectangle]) with the given size
     * centered at the origin.
     * @param size the size in meters
     * @return [Rectangle]
     * @throws IllegalArgumentException if size is less than or equal to zero
     */
    @JvmStatic
    fun createSquare(size: Double): Rectangle {
        // check the size
        if (size <= 0.0) throw IllegalArgumentException(getString("geometry.invalidSize"))
        return Rectangle(size, size)
    }

    /**
     * Creates a new [Rectangle] with the given width and height centered at the origin.
     * @param width the width in meters
     * @param height the height in meters
     * @return [Rectangle]
     * @throws IllegalArgumentException if width or height is less than or equal to zero
     */
    @JvmStatic
    fun createRectangle(width: Double, height: Double): Rectangle {
        return Rectangle(width, height)
    }

    /**
     * Creates a new [Triangle], using the given points.
     *
     *
     * This method makes a copy of the given points to create the [Triangle].
     *
     *
     * The center of the [Triangle] will be computed using the area weighted method.
     * @param p1 the first point
     * @param p2 the second point
     * @param p3 the third point
     * @return [Triangle]
     * @throws NullPointerException if p1, p2, or p3 is null
     * @see .createTriangleAtOrigin
     */
    @JvmStatic
    fun createTriangle(
        p1: Vector2?,
        p2: Vector2?,
        p3: Vector2?
    ): Triangle {
        if (p1 == null || p2 == null || p3 == null) throw NullPointerException(
            getString(
                "geometry.nullTrianglePoint"
            )
        )
        return Triangle(p1.copy(), p2.copy(), p3.copy())
    }

    /**
     * Creates a new [Triangle] with the given points centered at the origin.
     *
     *
     * This method makes a copy of the given points to create the [Triangle].
     * @param p1 the first point
     * @param p2 the second point
     * @param p3 the third point
     * @return [Triangle]
     * @throws NullPointerException if p1, p2, or p3 is null
     */
    @JvmStatic
    fun createTriangleAtOrigin(
        p1: Vector2?,
        p2: Vector2?,
        p3: Vector2?
    ): Triangle {
        val triangle = createTriangle(p1, p2, p3)
        val center = triangle.center
        triangle.translate(-center.x, -center.y)
        return triangle
    }

    /**
     * Creates a right angle [Triangle] with the center at the origin.
     * @param width the width of the base in meters
     * @param height the height in meters
     * @return [Triangle]
     * @throws IllegalArgumentException if width or height is less than or equal to zero
     */
    @JvmStatic
    fun createRightTriangle(width: Double, height: Double): Triangle {
        return createRightTriangle(width, height, false)
    }

    /**
     * Creates a right angle [Triangle] with the center at the origin.
     * @param width the width of the base in meters
     * @param height the height in meters
     * @param mirror true if the triangle should be mirrored along the y-axis
     * @return [Triangle]
     * @throws IllegalArgumentException if width or height is less than or equal to zero
     */
    @JvmStatic
    fun createRightTriangle(width: Double, height: Double, mirror: Boolean): Triangle {
        // check the width
        if (width <= 0.0) throw IllegalArgumentException(getString("geometry.invalidWidth"))
        // check the height
        if (height <= 0.0) throw IllegalArgumentException(getString("geometry.invalidHeight"))
        val top = Vector2(0.0, height)
        val left = Vector2(0.0, 0.0)
        val right = Vector2(if (mirror) -width else width, 0.0)
        val triangle: Triangle
        triangle = if (mirror) {
            // make sure it has anti-clockwise winding
            Triangle(top, right, left)
        } else {
            Triangle(top, left, right)
        }
        val center = triangle.center
        triangle.translate(-center.x, -center.y)
        return triangle
    }

    /**
     * Creates an equilateral [Triangle] with the center at the origin.
     * @param height the height of the triangle in meters
     * @return [Triangle]
     * @throws IllegalArgumentException if height is less than or equal to zero
     */
    @JvmStatic
    fun createEquilateralTriangle(height: Double): Triangle {
        // check the size
        if (height <= 0.0) throw IllegalArgumentException(getString("geometry.invalidSize"))
        // compute a where height = a * sqrt(3) / 2.0 (a is the width of the base
        val a = 2.0 * height * INV_SQRT_3
        // create the triangle
        return createIsoscelesTriangle(a, height)
    }

    /**
     * Creates an isosceles [Triangle] with the center at the origin.
     * @param width the width of the base in meters
     * @param height the height in meters
     * @return [Triangle]
     * @throws IllegalArgumentException if width or height is less than or equal to zero
     */
    @JvmStatic
    fun createIsoscelesTriangle(width: Double, height: Double): Triangle {
        // check the width
        if (width <= 0.0) throw IllegalArgumentException(getString("geometry.invalidWidth"))
        // check the height
        if (height <= 0.0) throw IllegalArgumentException(getString("geometry.invalidHeight"))
        val top = Vector2(0.0, height)
        val left = Vector2(-width * 0.5, 0.0)
        val right = Vector2(width * 0.5, 0.0)
        // create the triangle
        val triangle = Triangle(top, left, right)
        val center = triangle.center
        triangle.translate(-center.x, -center.y)
        return triangle
    }

    /**
     * Creates a new [Segment] with the given points.
     *
     *
     * This method makes a copy of the given points to create the [Segment].
     *
     *
     * The center of the [Segment] will be the average of the two points.
     * @param p1 the first point
     * @param p2 the second point
     * @return [Segment]
     * @throws NullPointerException if p1 or p2 is null
     * @see .createSegmentAtOrigin
     */
    @JvmStatic
    fun createSegment(p1: Vector2?, p2: Vector2?): Segment {
        if (p1 == null || p2 == null) throw NullPointerException(getString("geometry.nullSegmentPoint"))
        return Segment(p1.copy(), p2.copy())
    }

    /**
     * Creates a new [Segment] with the given points.
     *
     *
     * This method makes a copy of the given points to create the [Segment].
     *
     *
     * This method translates the [Segment] vertices so that the center is at the origin.
     * @param p1 the first point
     * @param p2 the second point
     * @return [Segment]
     * @throws NullPointerException if p1 or p2 is null
     */
    @JvmStatic
    fun createSegmentAtOrigin(
        p1: Vector2?,
        p2: Vector2?
    ): Segment {
        val segment = createSegment(p1, p2)
        val center = segment.center
        segment.translate(-center.x, -center.y)
        return segment
    }

    /**
     * Creates a new [Segment] from the origin to the given end point
     *
     *
     * This method makes a copy of the given point to create the [Segment].
     * @param end the end point
     * @return [Segment]
     * @throws NullPointerException if end is null
     */
    @JvmStatic
    fun createSegment(end: Vector2?): Segment {
        return createSegment(Vector2(), end)
    }

    /**
     * Creates a new [Segment] with the given length with the center
     * at the origin.
     *
     *
     * Renamed from createSegment(double).
     * @param length the length of the segment in meters
     * @return [Segment]
     * @throws IllegalArgumentException if length is less than or equal to zero
     * @since 2.2.3
     */
    @JvmStatic
    fun createHorizontalSegment(length: Double): Segment {
        // check the length
        if (length <= 0.0) throw IllegalArgumentException(getString("geometry.invalidLength"))
        val start = Vector2(-length * 0.5, 0.0)
        val end = Vector2(length * 0.5, 0.0)
        return Segment(start, end)
    }

    /**
     * Creates a new [Segment] with the given length with the center
     * at the origin.
     * @param length the length of the segment in meters
     * @return [Segment]
     * @throws IllegalArgumentException if length is less than or equal to zero
     * @since 2.2.3
     */
    @JvmStatic
    fun createVerticalSegment(length: Double): Segment {
        // check the length
        if (length <= 0.0) throw IllegalArgumentException(getString("geometry.invalidLength"))
        val start = Vector2(0.0, -length * 0.5)
        val end = Vector2(0.0, length * 0.5)
        return Segment(start, end)
    }

    /**
     * Creates a new [Capsule] bounded by the given rectangle width and height.
     *
     *
     * The capsule will be axis-aligned and centered on the origin with the caps on the
     * ends of the largest dimension.
     *
     *
     * If width and height are equal use a [Circle] shape instead.
     * @param width the bounding rectangle width
     * @param height the bounding rectangle height
     * @return [Capsule]
     * @throws IllegalArgumentException if width or height are less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun createCapsule(width: Double, height: Double): Capsule {
        return Capsule(width, height)
    }

    /**
     * Creates a new [Slice] with the given circle radius and arc length theta.
     *
     *
     * A [Slice] is an arbitrary slice of a circle. The specified radius is the radius
     * of the circle. The slice will be positioned with the *circle center* on the origin.
     *
     *
     * Theta is the total arc length of the slice specified in radians. Theta is halved, putting
     * half the arc length below the x-axis and half above.
     *
     *
     * Theta cannot be greater than .
     * @param radius the circle radius
     * @param theta the total arc length in radians
     * @return [Slice]
     * @throws IllegalArgumentException if radius is less than or equal to zero; if theta is less than or equal to zero or is greater than
     * @since 3.1.5
     */
    @JvmStatic
    fun createSlice(radius: Double, theta: Double): Slice {
        return Slice(radius, theta)
    }

    /**
     * Creates a new [Slice] with the given circle radius and arc length theta.
     *
     *
     * A [Slice] is an arbitrary slice of a circle. The specified radius is the radius
     * of the circle. The slice will be positioned with the *centroid* at the origin.
     *
     *
     * Theta is the total arc length of the slice specified in radians. Theta is halved, putting
     * half the arc length below the x-axis and half above.
     *
     *
     * Theta cannot be greater than .
     * @param radius the circle radius
     * @param theta the total arc length in radians
     * @return [Slice]
     * @throws IllegalArgumentException if radius is less than or equal to zero; if theta is less than or equal to zero or is greater than
     * @since 3.1.5
     */
    @JvmStatic
    fun createSliceAtOrigin(radius: Double, theta: Double): Slice {
        val slice = Slice(radius, theta)
        slice.translate(-slice.center.x, -slice.center.y)
        return slice
    }

    /**
     * Creates a new [Ellipse] bounded by the given rectangle width and height.
     *
     *
     * The ellipse will be axis-aligned and centered on the origin.
     *
     *
     * If width and height are equal use a [Circle] shape instead.
     * @param width the bounding rectangle width
     * @param height the bounding rectangle height
     * @return [Ellipse]
     * @throws IllegalArgumentException if width or height are less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun createEllipse(width: Double, height: Double): Ellipse {
        return Ellipse(width, height)
    }

    /**
     * Creates a new [HalfEllipse] bounded by the given rectangle width and height.
     *
     *
     * The ellipse will be axis-aligned with the base of the half ellipse on the x-axis. The given height
     * is the height of the half, not the height of the full ellipse.
     *
     *
     * If width and height are equal use a [Slice] shape with `theta = Math.PI` instead.
     * @param width the bounding rectangle width
     * @param height the bounding rectangle height
     * @return [HalfEllipse]
     * @throws IllegalArgumentException if width or height are less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun createHalfEllipse(width: Double, height: Double): HalfEllipse {
        return HalfEllipse(width, height)
    }

    /**
     * Creates a new [HalfEllipse] bounded by the given rectangle width and height.
     *
     *
     * The ellipse will be axis-aligned with the base of the half ellipse on the x-axis. The given height
     * is the height of the half, not the height of the full ellipse.
     *
     *
     * If width and height are equal use a [Slice] shape with `theta = Math.PI` instead.
     * @param width the bounding rectangle width
     * @param height the bounding rectangle height
     * @return [HalfEllipse]
     * @throws IllegalArgumentException if width or height are less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun createHalfEllipseAtOrigin(width: Double, height: Double): HalfEllipse {
        val half = HalfEllipse(width, height)
        val c = half.center
        half.translate(-c.x, -c.y)
        return half
    }

    /**
     * Creates a new [Polygon] in the shape of a circle with count number of vertices centered
     * on the origin.
     * @param count the number of vertices to use; must be greater than 2
     * @param radius the radius of the circle; must be greater than zero
     * @return [Polygon]
     * @throws IllegalArgumentException thrown if count is less than 3 or the radius is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun createPolygonalCircle(count: Int, radius: Double): Polygon {
        return createPolygonalCircle(count, radius, 0.0)
    }

    /**
     * Creates a new [Polygon] in the shape of a circle with count number of vertices centered
     * on the origin.
     * @param count the number of vertices to use; must be greater than or equal to 3
     * @param radius the radius of the circle; must be greater than zero
     * @param theta the radial offset for the points in radians
     * @return [Polygon]
     * @throws IllegalArgumentException thrown if count is less than 3 or the radius is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun createPolygonalCircle(count: Int, radius: Double, theta: Double): Polygon {
        // validate the input
        if (count < 3) throw IllegalArgumentException(getString("geometry.circleInvalidCount"))
        if (radius <= 0.0) throw IllegalArgumentException(getString("geometry.circleInvalidRadius"))

        // compute the angular increment
        val pin = TWO_PI / count
        // make sure the resulting output is an even number of vertices
        val vertices = arrayOfNulls<Vector2>(count) as Array<Vector2>
        val c: Double = cos(pin)
        val s: Double = sin(pin)
        var t = 0.0
        var x = radius
        var y = 0.0
        // initialize at theta if necessary
        if (theta != 0.0) {
            x = radius * cos(theta)
            y = radius * sin(theta)
        }
        for (i in 0 until count) {
            vertices[i] = Vector2(x, y)

            //apply the rotation matrix
            t = x
            x = c * x - s * y
            y = s * t + c * y
        }
        return Polygon(*vertices)
    }

    /**
     * Creates a new [Polygon] in the shape of a [Slice] with count number of vertices with the
     * circle center centered on the origin.
     *
     *
     * This method returns a polygon with count + 3 vertices.
     * @param count the number of vertices to use; must be greater than or equal to 1
     * @param radius the radius of the circle; must be greater than zero
     * @param theta the arc length of the slice in radians; must be greater than zero
     * @return [Polygon]
     * @throws IllegalArgumentException thrown if count is less than 1 or the radius is less than or equal to zero or theta is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun createPolygonalSlice(count: Int, radius: Double, theta: Double): Polygon {
        // validate the input
        if (count < 1) throw IllegalArgumentException(getString("geometry.sliceInvalidCount"))
        if (radius <= 0.0) throw IllegalArgumentException(getString("geometry.sliceInvalidRadius"))
        if (theta <= 0.0) throw IllegalArgumentException(getString("geometry.sliceInvalidTheta"))

        // compute the angular increment
        val pin = theta / (count + 1)
        // make sure the resulting output is an even number of vertices
        val vertices = arrayOfNulls<Vector2>(count + 3) as Array<Vector2>
        val c: Double = cos(pin)
        val s: Double = sin(pin)
        var t = 0.0

        // initialize at minus theta
        var x: Double = radius * cos(-theta * 0.5)
        var y: Double = radius * sin(-theta * 0.5)

        // set the first and last points of the arc
        vertices[0] = Vector2(x, y)
        vertices[count + 1] = Vector2(x, -y)
        for (i in 1 until count + 1) {
            //apply the rotation matrix
            t = x
            x = c * x - s * y
            y = s * t + c * y
            // add a point
            vertices[i] = Vector2(x, y)
        }

        // finish off by adding the origin
        vertices[count + 2] = Vector2()
        return Polygon(*vertices)
    }

    /**
     * Creates a new [Polygon] in the shape of a [Slice] with count number of vertices centered on the origin.
     *
     *
     * This method returns a polygon with count + 3 vertices.
     * @param count the number of vertices to use; must be greater than or equal to 1
     * @param radius the radius of the circle; must be greater than zero
     * @param theta the arc length of the slice in radians; must be greater than zero
     * @return [Polygon]
     * @throws IllegalArgumentException thrown if count is less than 1 or the radius is less than or equal to zero or theta is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun createPolygonalSliceAtOrigin(
        count: Int,
        radius: Double,
        theta: Double
    ): Polygon {
        val polygon = createPolygonalSlice(count, radius, theta)
        val center = polygon.center
        polygon.translate(-center.x, -center.y)
        return polygon
    }

    /**
     * Creates a new [Polygon] in the shape of an ellipse with count number of vertices centered
     * on the origin.
     *
     *
     * The count should be greater than or equal to 4 and a multiple of 2.  If not, the returned polygon will have count - 1
     * vertices.
     * @param count the number of vertices to use; must be greater than or equal to 4; should be even, if not, count - 1 vertices will be generated
     * @param width the width of the ellipse
     * @param height the height of the ellipse
     * @return [Polygon]
     * @throws IllegalArgumentException thrown if count is less than 4 or the width or height are less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun createPolygonalEllipse(count: Int, width: Double, height: Double): Polygon {
        // validate the input
        if (count < 4) throw IllegalArgumentException(getString("geometry.ellipseInvalidCount"))
        if (width <= 0.0) throw IllegalArgumentException(getString("geometry.ellipseInvalidWidth"))
        if (height <= 0.0) throw IllegalArgumentException(getString("geometry.ellipseInvalidHeight"))
        val a = width * 0.5
        val b = height * 0.5
        val n2 = count / 2
        // compute the angular increment
        val pin2: Double = PI / n2
        // make sure the resulting output is an even number of vertices
        val vertices = arrayOfNulls<Vector2>(n2 * 2)  as Array<Vector2>

        // use the parametric equations:
        // x = a * cos(t)
        // y = b * sin(t)
        var j = 0
        for (i in 0 until n2 + 1) {
            val t = pin2 * i
            // since the under side of the ellipse is the same
            // as the top side, only with a negated y, lets save
            // some time by creating the under side at the same time
            val x: Double = a * cos(t)
            val y: Double = b * sin(t)
            if (i > 0) {
                vertices[vertices.size - j] = Vector2(x, -y)
            }
            vertices[j++] = Vector2(x, y)
        }
        return Polygon(*vertices)
    }

    /**
     * Creates a new [Polygon] in the shape of a half ellipse with count number of vertices with the
     * base at the origin.
     *
     *
     * Returns a polygon with count + 2 vertices.
     *
     *
     * The height is the total height of the half not the half height.
     * @param count the number of vertices to use; must be greater than or equal to 1
     * @param width the width of the half ellipse
     * @param height the height of the half ellipse; should be the total height
     * @return [Polygon]
     * @throws IllegalArgumentException thrown if count is less than 1 or the width or height are less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun createPolygonalHalfEllipse(
        count: Int,
        width: Double,
        height: Double
    ): Polygon {
        // validate the input
        if (count < 1) throw IllegalArgumentException(getString("geometry.halfEllipseInvalidCount"))
        if (width <= 0.0) throw IllegalArgumentException(getString("geometry.halfEllipseInvalidWidth"))
        if (height <= 0.0) throw IllegalArgumentException(getString("geometry.halfEllipseInvalidHeight"))
        val a = width * 0.5
        val b = height * 0.5

        // compute the angular increment
        val inc: Double = PI / (count + 1)
        // make sure the resulting output is an even number of vertices
        val vertices = arrayOfNulls<Vector2>(count + 2) as Array<Vector2>

        // set the start and end vertices
        vertices[0] = Vector2(a, 0.0)
        vertices[count + 1] = Vector2(-a, 0.0)

        // use the parametric equations:
        // x = a * cos(t)
        // y = b * sin(t)
        for (i in 1 until count + 1) {
            val t = inc * i
            // since the under side of the ellipse is the same
            // as the top side, only with a negated y, lets save
            // some time by creating the under side at the same time
            val x: Double = a * cos(t)
            val y: Double = b * sin(t)
            vertices[i] = Vector2(x, y)
        }
        return Polygon(*vertices)
    }

    /**
     * Creates a new [Polygon] in the shape of a half ellipse with count number of vertices centered
     * on the origin.
     *
     *
     * Returns a polygon with count + 2 vertices.
     *
     *
     * The height is the total height of the half not the half height.
     * @param count the number of vertices to use; should be even, if not, count - 1 vertices will be generated
     * @param width the width of the half ellipse
     * @param height the height of the half ellipse; should be the total height
     * @return [Polygon]
     * @throws IllegalArgumentException thrown if count is less than 1 or the width or height are less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun createPolygonalHalfEllipseAtOrigin(
        count: Int,
        width: Double,
        height: Double
    ): Polygon {
        val polygon =
            createPolygonalHalfEllipse(count, width, height)
        val center = polygon.center
        polygon.translate(-center.x, -center.y)
        return polygon
    }

    /**
     * Creates a new [Polygon] in the shape of a capsule using count number of vertices on each
     * cap, centered on the origin.  The caps will be on the ends of the largest dimension.
     *
     *
     * The returned polygon will have 4 + 2 * count number of vertices.
     * @param count the number of vertices to use for one cap; must be greater than or equal to 1
     * @param width the bounding rectangle width
     * @param height the bounding rectangle height
     * @return [Polygon]
     * @since 3.1.5
     */
    @JvmStatic
    fun createPolygonalCapsule(count: Int, width: Double, height: Double): Polygon {
        // validate the input
        if (count < 1) throw IllegalArgumentException(getString("geometry.capsuleInvalidCount"))
        if (width <= 0.0) throw IllegalArgumentException(getString("geometry.capsuleInvalidWidth"))
        if (height <= 0.0) throw IllegalArgumentException(getString("geometry.capsuleInvalidHeight"))

        // if the width and height are close enough to being equal, just return a circle
        if (abs(width - height) < Epsilon.E) {
            return createPolygonalCircle(count, width)
        }

        // compute the angular increment
        val pin: Double = PI / (count + 1)
        // 4 rect verts plus 2 * circle half verts
        val vertices = arrayOfNulls<Vector2>(4 + 2 * count)  as Array<Vector2>
        val c: Double = cos(pin)
        val s: Double = sin(pin)
        var t = 0.0

        // get the major and minor axes
        var major = width
        var minor = height
        var vertical = false
        if (width < height) {
            major = height
            minor = width
            vertical = true
        }

        // get the radius from the minor axis
        val radius = minor * 0.5

        // compute the x/y offsets
        val offset = major * 0.5 - radius
        var ox = 0.0
        var oy = 0.0
        if (vertical) {
            // aligned to the y
            oy = offset
        } else {
            // aligned to the x
            ox = offset
        }
        var n = 0

        // right cap
        var ao = if (vertical) 0.0 else PI * 0.5
        var x: Double = radius * cos(pin - ao)
        var y: Double = radius * sin(pin - ao)
        for (i in 0 until count) {
            vertices[n++] = Vector2(x + ox, y + oy)

            //apply the rotation matrix
            t = x
            x = c * x - s * y
            y = s * t + c * y
        }

        // add in top/left vertices
        if (vertical) {
            vertices[n++] = Vector2(-radius, oy)
            vertices[n++] = Vector2(-radius, -oy)
        } else {
            vertices[n++] = Vector2(ox, radius)
            vertices[n++] = Vector2(-ox, radius)
        }

        // left cap
        ao = if (vertical) PI else PI * 0.5
        x = radius * cos(pin + ao)
        y = radius * sin(pin + ao)
        for (i in 0 until count) {
            vertices[n++] = Vector2(x - ox, y - oy)

            //apply the rotation matrix
            t = x
            x = c * x - s * y
            y = s * t + c * y
        }

        // add in bottom/right vertices
        if (vertical) {
            vertices[n++] = Vector2(radius, -oy)
            vertices[n++] = Vector2(radius, oy)
        } else {
            vertices[n++] = Vector2(-ox, -radius)
            vertices[n++] = Vector2(ox, -radius)
        }
        return Polygon(*vertices)
    }

    /**
     * Returns a new list containing the 'cleansed' version of the given listing of polygon points.
     *
     *
     * This method ensures the polygon has CCW winding, removes colinear vertices, and removes coincident vertices.
     *
     *
     * If the given list is empty, the list is returned.
     * @param points the list polygon points
     * @return List&lt;[Vector2]&gt;
     * @throws NullPointerException if points is null or if points contains null elements
     */
    @JvmStatic
    fun cleanse(points: List<Vector2>?): List<Vector2> {
        // check for null list
        if (points == null) throw NullPointerException(getString("geometry.nullPointList"))
        // get the size of the points list
        val size = points.size
        // check the size
        if (size == 0) return points
        // create a result list
        val result: MutableList<Vector2> =
            ArrayList(size)
        var winding = 0.0

        // loop over the points
        for (i in 0 until size) {
            // get the current point
            val point = points[i]

            // get the adjacent points
            val prev = points[if (i - 1 < 0) size - 1 else i - 1]
            val next = points[if (i + 1 == size) 0 else i + 1]

            // check for null
            if (point == null || prev == null || next == null) throw NullPointerException(
                getString(
                    "geometry.nullPointListElements"
                )
            )

            // is this point equal to the next?
            val diff = point.difference(next)
            if (diff.isZero) {
                // then skip this point
                continue
            }

            // create the edge vectors
            val prevToPoint = prev.to(point)
            val pointToNext = point.to(next)

            // check if the previous point is equal to this point

            // since the next point is not equal to this point
            // if this is true we still need to add the point because
            // it is the last of a string of coincident vertices
            if (!prevToPoint.isZero) {
                // compute the cross product
                val cross = prevToPoint.cross(pointToNext)

                // if the cross product is near zero then point is a colinear point
                if (abs(cross) <= Epsilon.E) {
                    continue
                }
            }

            // sum the current signed area
            winding += point.cross(next)

            // otherwise the point is valid
            result.add(point)
        }

        // check the winding
        if (winding < 0.0) {
            reverseWinding(result)
        }
        return result
    }

    /**
     * Returns a new array containing the 'cleansed' version of the given array of polygon points.
     *
     *
     * This method ensures the polygon has CCW winding, removes colinear vertices, and removes coincident vertices.
     * @param points the list polygon points
     * @return [Vector2][]
     * @throws NullPointerException if points is null or points contains null elements
     */
    @JvmStatic
    fun cleanse(vararg points: Vector2): Array<Vector2> {
        // check for null
        if (points == null) throw NullPointerException(getString("geometry.nullPointArray"))
        // create a list from the array
        val pointList: List<Vector2> = points.toList()
        // cleanse the list
        val resultList =cleanse(pointList)
        // convert it back to an array
        val result = resultList.toTypedArray()
        // return the result
        return result
    }

    /**
     * Flips the given polygon about its center along the x-axis and
     * returns the result as a new polygon.
     *
     *
     * This method assumes that the line is through the origin.
     * @param polygon the polygon to flip
     * @return [Polygon]
     * @throws NullPointerException if the given polygon is null
     * @see .flip
     * @see .flip
     * @since 3.1.4
     */
    @JvmStatic
    fun flipAlongTheXAxis(polygon: Polygon?): Polygon {
        return flip(polygon, Vector2.X_AXIS, null)
    }

    /**
     * Flips the given polygon about its center along the y-axis and
     * returns the result as a new polygon.
     *
     *
     * This method assumes that the line is through the origin.
     * @param polygon the polygon to flip
     * @return [Polygon]
     * @throws NullPointerException if the given polygon is null
     * @see .flip
     * @see .flip
     * @since 3.1.4
     */
    @JvmStatic
    fun flipAlongTheYAxis(polygon: Polygon?): Polygon {
        return flip(polygon, Vector2.Y_AXIS, null)
    }

    /**
     * Flips the given polygon about the given point along the x-axis and
     * returns the result as a new polygon.
     * @param polygon the polygon to flip
     * @param point the point to flip about
     * @return [Polygon]
     * @throws NullPointerException if the given polygon is null
     * @see .flip
     * @see .flip
     * @since 3.1.4
     */
    @JvmStatic
    fun flipAlongTheXAxis(
        polygon: Polygon?,
        point: Vector2?
    ): Polygon {
        return flip(polygon, Vector2.X_AXIS, point)
    }

    /**
     * Flips the given polygon about the given point along the y-axis and
     * returns the result as a new polygon.
     * @param polygon the polygon to flip
     * @param point the point to flip about
     * @return [Polygon]
     * @throws NullPointerException if the given polygon is null
     * @see .flip
     * @see .flip
     * @since 3.1.4
     */
    @JvmStatic
    fun flipAlongTheYAxis(
        polygon: Polygon?,
        point: Vector2?
    ): Polygon {
        return flip(polygon, Vector2.Y_AXIS, point)
    }

    /**
     * Flips the given polygon about the given line and returns the result
     * as a new polygon.
     *
     *
     * This method assumes that the line is through the origin.
     * @param polygon the polygon to flip
     * @param axis the axis to flip about
     * @return [Polygon]
     * @throws NullPointerException if the given polygon or axis is null
     * @throws IllegalArgumentException if the given axis is the zero vector
     * @see .flip
     * @since 3.1.4
     */
    @JvmStatic
    fun flip(polygon: Polygon?, axis: Vector2?): Polygon {
        return flip(polygon, axis, null)
    }

    /**
     * Flips the given polygon about the given line and returns the result
     * as a new polygon.
     * @param polygon the polygon to flip
     * @param axis the axis to flip about
     * @param point the point to flip about; if null, the polygon center is used
     * @return [Polygon]
     * @throws NullPointerException if the given polygon or axis is null
     * @throws IllegalArgumentException if the given axis is the zero vector
     * @since 3.1.4
     */
    @JvmStatic
    fun flip(
        polygon: Polygon?,
        axis: Vector2?,
        point: Vector2?
    ): Polygon {
        // check for valid input
        var point = point
        if (polygon == null) throw NullPointerException(getString("geometry.nullFlipPolygon"))
        if (axis == null) throw NullPointerException(getString("geometry.nullFlipAxis"))
        if (axis.isZero) throw IllegalArgumentException(getString("geometry.zeroFlipAxis"))
        // just use the center of the polygon if the given point is null
        if (point == null) point = polygon.center
        // flip about the axis and point
        // make sure the axis is normalized
        axis.normalize()
        val pv = polygon.vertices
        val nv = arrayOfNulls<Vector2>(pv.size) as Array<Vector2>
        for (i in pv.indices) {
            val v0 = pv[i]
            // center on the origin
            val v1 = v0.difference(point)
            // get the projection of the point onto the axis
            val proj = v1.dot(axis)
            // get the point on the axis
            val vp = axis.product(proj)
            // get the point past the projection
            val rv = vp.add(vp.x - v1.x, vp.y - v1.y)
            nv[i] = rv.add(point)
        }
        // check the winding
        if (getWinding(*nv) < 0.0) {
            reverseWinding(nv)
        }
        return Polygon(*nv)
    }

    /**
     * Returns the Minkowski Sum of the given convex shapes.
     *
     *
     * This method computes the Minkowski Sum in O(n + m) time where n and m are the number
     * of vertices of the first and second convex respectively.
     *
     *
     * This method accepts any [Convex] [Wound] shape which basically means
     * [Polygon]s or [Segment]s.
     *
     *
     * This method throws an IllegalArgumentException if two [Segment]s are supplied
     * that are colinear (in this case the resulting Minkowski Sum would be another segment
     * rather than a polygon).
     * @param convex1 the first convex
     * @param convex2 the second convex
     * @param <E> either a [Wound] or [Convex] type
     * @return [Polygon]
     * @throws NullPointerException if convex1 or convex2 are null
     * @throws IllegalArgumentException if both convex1 and convex2 are [Segment]s and are colinear
     * @since 3.1.5
    </E> */
    @JvmStatic
    fun <E> minkowskiSum(convex1: E?, convex2: E?): Polygon where E : Wound?, E : Convex? {
        if (convex1 == null) throw NullPointerException(getString("geometry.nullMinkowskiSumConvex"))
        if (convex2 == null) throw NullPointerException(getString("geometry.nullMinkowskiSumConvex"))
        val p1v = convex1.woundVertices
        val p2v = convex2.woundVertices

        // check for two segments
        if (convex1 is Segment && convex2 is Segment) {
            // check if they are colinear
            val s1 = p1v[0].to(p1v[1])
            val s2 = p2v[0].to(p2v[1])
            if (s1.cross(s2) <= Epsilon.E) {
                throw IllegalArgumentException(getString("geometry.invalidMinkowskiSumSegments"))
            }
        }
        val c1 = p1v.size
        val c2 = p2v.size

        // find the minimum y-coordinate vertex in the first polygon
        // (in the case of a tie, use the minimum x-coordinate vertex)
        var i = 0
        var j = 0
        val min =
            Vector2(Double.MAX_VALUE, Double.MAX_VALUE)
        for (k in 0 until c1) {
            val v = p1v[k]
            if (v.y < min.y) {
                min.set(v)
                i = k
            } else if (v.y == min.y) {
                if (v.x < min.x) {
                    min.set(v)
                    i = k
                }
            }
        }
        // find the minimum y-coordinate vertex in the second polygon
        // (in the case of a tie, use the minimum x-coordinate vertex)
        min[Double.MAX_VALUE] = Double.MAX_VALUE
        for (k in 0 until c2) {
            val v = p2v[k]
            if (v.y < min.y) {
                min.set(v)
                j = k
            } else if (v.y == min.y) {
                if (v.x < min.x) {
                    min.set(v)
                    j = k
                }
            }
        }

        // iterate through the vertices
        val n1 = c1 + i
        val n2 = c2 + j
        // the maximum number of vertices for the output shape is m + n
        val sum: MutableList<Vector2> =
            ArrayList(c1 + c2)
        while (i <= n1 && j <= n2) {

            // get the current edges
            val v1s = p1v[i % c1]
            val v1e = p1v[(i + 1) % c1]
            val v2s = p2v[j % c2]
            val v2e = p2v[(j + 1) % c2]

            // add the vertex to the final output

            // on the first iteration we can assume this is a correct
            // one since we started at the minimum y-coordinate vertices

            // on subsequent interations we can assume this is a correct
            // one since the angle condition was used to increment the
            // vertex index
            sum.add(v1s.sum(v2s))

            // compute the edge vectors
            val e1 = v1s.to(v1e)
            val e2 = v2s.to(v2e)

            // get the angles between the x-axis; in the range [-pi, pi]
            var a1 = Vector2.X_AXIS.getAngleBetween(e1)
            var a2 = Vector2.X_AXIS.getAngleBetween(e2)

            // put the angles in the range [0, 2pi]
            if (a1 < 0) a1 += TWO_PI
            if (a2 < 0) a2 += TWO_PI

            // determine which vertex to use next
            if (a1 < a2) {
                i++
            } else if (a1 > a2) {
                j++
            } else {
                i++
                j++
            }
        }
        return Polygon(*sum.toTypedArray())
    }

    /**
     * Performs the Minkowski Sum of the given [Polygon] and [Circle].
     *
     *
     * Use the count parameter to specify the number of vertices to use per round corner.
     *
     *
     * If the given polygon has *n* number of vertices, the returned polygon will have
     * *n * 2 + n * count* number of vertices.
     *
     *
     * This method is O(n) where n is the number of vertices in the given polygon.
     * @param polygon the polygon
     * @param circle the circle to add to the polygon
     * @param count the number of vertices to add for each rounded corner; must be greater than zero
     * @return [Polygon]
     * @throws NullPointerException if the given polygon or circle is null
     * @throws IllegalArgumentException if the given radius or count is less than or equal to zero
     * @since 3.1.5
     * @see .minkowskiSum
     */
    @JvmStatic
    fun minkowskiSum(
        circle: Circle?,
        polygon: Polygon?,
        count: Int
    ): Polygon {
        return minkowskiSum(polygon, circle, count)
    }

    /**
     * Performs the Minkowski Sum of the given [Polygon] and [Circle].
     *
     *
     * Use the count parameter to specify the number of vertices to use per round corner.
     *
     *
     * If the given polygon has *n* number of vertices, the returned polygon will have
     * *n * 2 + n * count* number of vertices.
     *
     *
     * This method is O(n) where n is the number of vertices in the given polygon.
     * @param polygon the polygon
     * @param circle the circle to add to the polygon
     * @param count the number of vertices to add for each rounded corner; must be greater than zero
     * @return [Polygon]
     * @throws NullPointerException if the given polygon or circle is null
     * @throws IllegalArgumentException if the given radius or count is less than or equal to zero
     * @since 3.1.5
     * @see .minkowskiSum
     */
    @JvmStatic
    fun minkowskiSum(
        polygon: Polygon?,
        circle: Circle?,
        count: Int
    ): Polygon {
        if (circle == null) throw NullPointerException(getString("geometry.nullMinkowskiSumCircle"))
        return minkowskiSum(polygon, circle.radius, count)
    }

    /**
     * Returns a new polygon that has been radially expanded.  This is equivalent to the Minkowski sum of
     * a circle, of the given radius, and the given polygon.
     *
     *
     * Use the count parameter to specify the number of vertices to use per round corner.
     *
     *
     * If the given polygon has *n* number of vertices, the returned polygon will have
     * *n * 2 + n * count* number of vertices.
     *
     *
     * This method is O(n) where n is the number of vertices in the given polygon.
     * @param polygon the polygon to expand radially
     * @param radius the radial expansion; must be greater than zero
     * @param count the number of vertices to add for each rounded corner; must be greater than zero
     * @return [Polygon]
     * @throws NullPointerException if the given polygon is null
     * @throws IllegalArgumentException if the given radius or count is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun minkowskiSum(
        polygon: Polygon?,
        radius: Double,
        count: Int
    ): Polygon {
        // check for valid input
        if (polygon == null) throw NullPointerException(getString("geometry.nullMinkowskiSumPolygon"))
        if (radius <= 0) throw IllegalArgumentException(getString("geometry.invalidMinkowskiSumRadius"))
        if (count <= 0) throw IllegalArgumentException(getString("geometry.invalidMinkowskiSumCount"))
        val vertices = polygon.vertices
        val normals = polygon.normals
        val size = vertices.size
        val nVerts = arrayOfNulls<Vector2>(size * 2 + size * count) as Array<Vector2>
        // perform the expansion
        var j = 0
        for (i in 0 until size) {
            val v1 = vertices[i]
            val v2 = vertices[if (i + 1 == size) 0 else i + 1]
            val normal = normals[i]
            val nv1 = normal.product(radius).add(v1)
            val nv2 = normal.product(radius).add(v2)

            // generate the previous polygonal arc with count vertices
            // compute (circular) angle between the edges
            var cv1: Vector2? = null
            if (i == 0) {
                // if its the first iteration, then we need to compute the
                // last vertex's new position
                val tn = normals[size - 1]
                cv1 = v1.to(tn.product(radius).add(v1))
            } else {
                cv1 = v1.to(nVerts[j - 1])
            }
            val cv2 = v1.to(nv1)
            val theta = cv1.getAngleBetween(cv2)
            // compute the angular increment
            val pin = theta / (count + 1)
            val c: Double = cos(pin)
            val s: Double = sin(pin)
            var t = 0.0

            // compute the start theta
            var sTheta =
                Vector2.X_AXIS.getAngleBetween(normals[if (i - 1 < 0) size - 1 else i - 1])
            if (sTheta < 0) {
                sTheta += TWO_PI
            }

            // initialize at minus theta
            var x: Double = radius * cos(sTheta)
            var y: Double = radius * sin(sTheta)
            for (k in 0 until count) {
                //apply the rotation matrix
                t = x
                x = c * x - s * y
                y = s * t + c * y
                // add a point
                nVerts[j++] = Vector2(x, y).add(v1)
            }
            nVerts[j++] = nv1
            nVerts[j++] = nv2
        }
        return Polygon(*nVerts)
    }

    /**
     * Returns a scaled version of the given circle.
     * @param circle the circle
     * @param scale the scale; must be greater than zero
     * @return [Circle]
     * @throws NullPointerException if the given circle is null
     * @throws IllegalArgumentException if the given scale is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun scale(circle: Circle?, scale: Double): Circle {
        if (circle == null) throw NullPointerException(getString("geometry.nullShape"))
        if (scale <= 0) throw IllegalArgumentException(getString("geometry.invalidScale"))
        return Circle(circle.radius * scale)
    }

    /**
     * Returns a scaled version of the given capsule.
     * @param capsule the capsule
     * @param scale the scale; must be greater than zero
     * @return [Capsule]
     * @throws NullPointerException if the given capsule is null
     * @throws IllegalArgumentException if the given scale is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun scale(capsule: Capsule?, scale: Double): Capsule {
        if (capsule == null) throw NullPointerException(getString("geometry.nullShape"))
        if (scale <= 0) throw IllegalArgumentException(getString("geometry.invalidScale"))
        return Capsule(capsule.length * scale, capsule.capRadius * 2.0 * scale)
    }

    /**
     * Returns a scaled version of the given ellipse.
     * @param ellipse the ellipse
     * @param scale the scale; must be greater than zero
     * @return [Ellipse]
     * @throws NullPointerException if the given ellipse is null
     * @throws IllegalArgumentException if the given scale is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun scale(ellipse: Ellipse?, scale: Double): Ellipse {
        if (ellipse == null) throw NullPointerException(getString("geometry.nullShape"))
        if (scale <= 0) throw IllegalArgumentException(getString("geometry.invalidScale"))
        return Ellipse(ellipse.width * scale, ellipse.height * scale)
    }

    /**
     * Returns a scaled version of the given half-ellipse.
     * @param halfEllipse the half-ellipse
     * @param scale the scale; must be greater than zero
     * @return [HalfEllipse]
     * @throws NullPointerException if the given half-ellipse is null
     * @throws IllegalArgumentException if the given scale is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun scale(halfEllipse: HalfEllipse?, scale: Double): HalfEllipse {
        if (halfEllipse == null) throw NullPointerException(getString("geometry.nullShape"))
        if (scale <= 0) throw IllegalArgumentException(getString("geometry.invalidScale"))
        return HalfEllipse(halfEllipse.width * scale, halfEllipse.height * scale)
    }

    /**
     * Returns a scaled version of the given slice.
     * @param slice the slice
     * @param scale the scale; must be greater than zero
     * @return [Slice]
     * @throws NullPointerException if the given slice is null
     * @throws IllegalArgumentException if the given scale is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun scale(slice: Slice?, scale: Double): Slice {
        if (slice == null) throw NullPointerException(getString("geometry.nullShape"))
        if (scale <= 0) throw IllegalArgumentException(getString("geometry.invalidScale"))
        return Slice(slice.sliceRadius * scale, slice.getTheta())
    }

    /**
     * Returns a scaled version of the given polygon.
     * @param polygon the polygon
     * @param scale the scale; must be greater than zero
     * @return [Polygon]
     * @throws NullPointerException if the given polygon is null
     * @throws IllegalArgumentException if the given scale is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun scale(polygon: Polygon?, scale: Double): Polygon {
        if (polygon == null) throw NullPointerException(getString("geometry.nullShape"))
        if (scale <= 0) throw IllegalArgumentException(getString("geometry.invalidScale"))
        val oVertices = polygon.vertices
        val size = oVertices.size
        val vertices = arrayOfNulls<Vector2>(size) as Array<Vector2>
        val center = polygon.center
        for (i in 0 until size) {
            vertices[i] = center.to(oVertices[i]).multiply(scale).add(center)
        }
        return Polygon(*vertices)
    }

    /**
     * Returns a scaled version of the given segment.
     * @param segment the segment
     * @param scale the scale; must be greater than zero
     * @return [Segment]
     * @throws NullPointerException if the given segment is null
     * @throws IllegalArgumentException if the given scale is less than or equal to zero
     * @since 3.1.5
     */
    @JvmStatic
    fun scale(segment: Segment?, scale: Double): Segment {
        if (segment == null) throw NullPointerException(getString("geometry.nullShape"))
        if (scale <= 0) throw IllegalArgumentException(getString("geometry.invalidScale"))
        val length = segment.length * scale * 0.5
        val n = segment.vertices[0].to(segment.vertices[1])
        n.normalize()
        n.multiply(length)
        return Segment(segment.center.sum(n.x, n.y), segment.center.difference(n.x, n.y))
    }

    /**
     * Creates a list of [Link]s for the given vertices.
     *
     *
     * If the closed parameter is true, an extra link is created joining the last and first
     * vertices in the list.
     * @param vertices the poly-line vertices
     * @param closed true if the shape should be enclosed
     * @return List&lt;[Link]&gt;
     * @throws NullPointerException if the list of vertices is null or an element of the vertex list is null
     * @throws IllegalArgumentException if the list of vertices doesn't contain 2 or more elements
     * @since 3.2.2
     */
    @JvmStatic
    fun createLinks(
        vertices: List<Vector2>,
        closed: Boolean
    ): List<Link> {
        return createLinks(vertices.toTypedArray(), closed)
    }

    /**
     * Creates a [Link] chain for the given vertices.
     *
     *
     * If the closed parameter is true, an extra link is created joining the last and first
     * vertices in the array.
     * @param vertices the poly-line vertices
     * @param closed true if the shape should be enclosed
     * @return List&lt;[Link]&gt;
     * @throws NullPointerException if the array of vertices is null or an element of the vertex array is null
     * @throws IllegalArgumentException if the array of vertices doesn't contain 2 or more elements
     * @since 3.2.2
     */
    fun createLinks(
        vertices: Array<Vector2>?,
        closed: Boolean
    ): List<Link> {
        // check the vertex array
        if (vertices == null) throw NullPointerException(getString("geometry.nullPointArray"))
        // get the vertex length
        val size = vertices.size
        // the size must be larger than 1 (2 or more)
        if (size < 2) throw IllegalArgumentException(getString("geometry.invalidSizePointList2"))
        // generate the links
        val links: MutableList<Link> =
            ArrayList()
        for (i in 0 until size - 1) {
            val p1 = vertices[i].copy()
            val p2 = vertices[i + 1].copy()
            // check for null segment vertices
            if (p1 == null || p2 == null) {
                throw NullPointerException(getString("geometry.nullPointListElements"))
            }
            val link = Link(p1, p2)
            // link up the previous and this link
            if (i > 0) {
                val prev = links[i - 1]
                link.previous = prev
            }
            // add link to the list of links
            links.add(link)
        }
        if (closed) {
            // create a link to span the first and last vertex
            val p1 = vertices[0].copy()
            val p2 = vertices[size - 1].copy()
            val link = Link(p1, p2)
            // wire it up
            val prev = links[links.size - 1]
            val next = links[0]
            link.previous = prev
            link.next = next
            links.add(link)
        }
        return links
    }
}