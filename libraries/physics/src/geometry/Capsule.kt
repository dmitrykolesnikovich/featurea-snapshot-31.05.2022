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
import org.dyn4j.geometry.Geometry.getRotationRadius
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max

/**
 * Implementation of a Capsule [Convex] [Shape].
 *
 *
 * A capsule can be described as a rectangle with two half circle caps on both ends. A capsule is created
 * by specifying the bounding rectangle of the entire [Shape].
 *
 *
 * If the height is larger than the width the caps will be on the top and bottom of the shape. Otherwise
 * the caps are on the left and right ends of the shape.
 *
 *
 * A capsule's width and height must be larger than zero and cannot be equal.  A [Circle] should be used
 * instead of an equal width/height capsule for both performance and stability.
 * @author William Bittle
 * @version 3.4.0
 * @since 3.1.5
 */
class Capsule : AbstractShape, Convex, Shape, Transformable, DataContainer {

    /**
     * Validated constructor.
     *
     *
     * Creates an axis-aligned capsule centered on the origin with the caps on
     * ends of the larger dimension.
     * @param valid always true or this constructor would not be called
     * @param width the bounding rectangle width
     * @param height the bounding rectangle height
     */
    constructor(valid: Boolean, width: Double, height: Double) : super(max(width, height) * 0.5) {
        // determine the major and minor axis
        var major = width
        var minor = height
        var vertical = false
        if (width < height) {
            major = height
            minor = width
            vertical = true
        }

        // set the width
        length = major
        // the cap radius is half the height
        capRadius = minor * 0.5

        // generate the cap focal points on the
        // major axis
        val f = (major - minor) * 0.5
        arrayOfNulls<Vector2>(2)
        foci = if (vertical) {
            // set the local x-axis (to the y-axis)
            localXAxis = Vector2(0.0, 1.0)
            arrayOf(Vector2(0.0, -f), Vector2(0.0, f))
        } else {
            // set the local x-axis
            localXAxis = Vector2(1.0, 0.0)
            arrayOf(Vector2(-f, 0.0), Vector2(f, 0.0))
        }
    }
    /**
     * Returns the length of the capsule.
     *
     *
     * The length is the largest dimension of the capsule's
     * bounding rectangle.
     * @return double
     */
    /** The bounding rectangle width  */
    @JvmField
    val length: Double

    /**
     * Returns the end cap radius.
     * @return double
     */
    /** The end cap radius  */
    @JvmField
    val capRadius: Double

    /** The focal points for the caps  */
    val foci: Array<Vector2>

    /** The local x-axis  */
    @JvmField
    var localXAxis: Vector2? = null

    /**
     * Minimal constructor.
     *
     *
     * Creates an axis-aligned capsule centered on the origin with the caps on
     * ends of the larger dimension.
     * @param width the bounding rectangle width
     * @param height the bounding rectangle height
     * @throws IllegalArgumentException thrown if width or height are less than or equal to zero or if the width and height are near equal
     */
    constructor(width: Double, height: Double) : this(validate(width, height), width, height)

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.AbstractShape#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Capsule[").append(super.toString())
            .append("|Width=").append(length)
            .append("|CapRadius=").append(capRadius)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getAxes(org.dyn4j.geometry.Vector2[], org.dyn4j.geometry.Transform)
	 */
    override fun getAxes(foci: Array<Vector2>?, transform: Transform): Array<Vector2>? {
        // check for given foci
        if (foci != null) {
            // we need to include the shortest vector from foci to foci
            val axes = arrayOfNulls<Vector2>(2 + foci.size) as Array<Vector2>
            axes[0] = transform.getTransformedR(localXAxis!!)
            axes[1] = transform.getTransformedR(localXAxis!!.rightHandOrthogonalVector)
            val f1: Vector2 = transform.getTransformed(this.foci[0]!!)
            val f2: Vector2 = transform.getTransformed(this.foci[1]!!)
            for (i in foci.indices) {
                // get the one closest to the given focus
                val d1: Double = f1.distanceSquared(foci[i]!!)
                val d2: Double = f2.distanceSquared(foci[i]!!)
                var v: Vector2? = null
                v = if (d1 < d2) {
                    f1.to(foci[i]!!)
                } else {
                    f2.to(foci[i]!!)
                }
                v.normalize()
                axes[2 + i] = v
            }
            return axes
        }
        // if there were no foci given then just return the normal axes for the
        // rectangular region
        return arrayOf(
            transform.getTransformedR(localXAxis!!),
            transform.getTransformedR(localXAxis!!.rightHandOrthogonalVector)
        )
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFoci(org.dyn4j.geometry.Transform)
	 */
    override fun getFoci(transform: Transform): Array<Vector2>? =
        arrayOf(transform.getTransformed(foci[0]!!), transform.getTransformed(foci[1]!!))

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFarthestPoint(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestPoint(vector: Vector2, transform: Transform): Vector2 {
        // make sure the given direction is normalized
        vector.normalize()
        // a capsule is just a radially expanded line segment
        val p: Vector2 = Segment.getFarthestPoint(foci[0], foci[1], vector, transform)
        // apply the radial expansion
        return p.add(vector.product(capRadius))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Convex#getFarthestFeature(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun getFarthestFeature(vector: Vector2, transform: Transform): Feature {
        // test whether the given direction is within a certain angle of the
        // local x axis. if so, use the edge feature rather than the point
        val localAxis: Vector2 = transform.getInverseTransformedR(vector)
        val n1: Vector2 = localXAxis!!.leftHandOrthogonalVector

        // get the squared length of the localaxis and add the fudge factor
        // should always 1.0 * factor since localaxis is normalized
        val d: Double =
            localAxis.dot(localAxis) * EDGE_FEATURE_SELECTION_CRITERIA
        // project the normal onto the localaxis normal
        val d1: Double = localAxis.dot(n1)

        // we only need to test one normal since we only care about its projection length
        // we can later determine which direction by the sign of the projection
        return if (abs(d1) < d) {
            // then its the farthest point
            val point: Vector2 = getFarthestPoint(vector, transform)
            PointFeature(point)
        } else {
            // compute the vector to add/sub from the foci
            val v: Vector2 = n1.multiply(capRadius)
            // compute an expansion amount based on the width of the shape
            val e: Vector2 = localXAxis!!.product(length * 0.5 * EDGE_FEATURE_EXPANSION_FACTOR)
            if (d1 > 0) {
                val p1: Vector2 = foci[0]!!.sum(v).subtract(e)
                val p2: Vector2 = foci[1]!!.sum(v).add(e)
                // return the full bottom side
                Segment.getFarthestFeature(p1, p2, vector, transform)
            } else {
                val p1: Vector2 = foci[0]!!.difference(v).subtract(e)
                val p2: Vector2 = foci[1]!!.difference(v).add(e)
                Segment.getFarthestFeature(p1, p2, vector, transform)
            }
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#project(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun project(vector: Vector2, transform: Transform): Interval {
        // get the world space farthest point
        val p1: Vector2 = getFarthestPoint(vector, transform)
        // get the center in world space
        val center: Vector2 = transform.getTransformed(center)
        // project the center onto the axis
        val c: Double = center.dot(vector)
        // project the point onto the axis
        val d: Double = p1.dot(vector)
        // get the interval along the axis
        return Interval(2 * c - d, d)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#createAABB(org.dyn4j.geometry.Transform)
	 */
    override fun createAABB(transform: Transform): AABB {
        // Inlined projection of x axis
        // Interval x = this.project(Vector2.X_AXIS, transform);
        var p1: Vector2 = getFarthestPoint(Vector2.X_AXIS, transform)
        var c: Double = transform.getTransformedX(center)
        val minX: Double = 2 * c - p1.x
        val maxX: Double = p1.x

        // Inlined projection of y axis
        // Interval y = this.project(Vector2.Y_AXIS, transform);
        p1 = getFarthestPoint(Vector2.Y_AXIS, transform)
        c = transform.getTransformedY(center)
        val minY: Double = 2 * c - p1.y
        val maxY: Double = p1.y
        return AABB(minX, minY, maxX, maxY)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#createMass(double)
	 */
    override fun createMass(density: Double): Mass {
        // the mass of a capsule is the mass of the rectangular section plus the mass
        // of two half circles (really just one circle)
        val h = capRadius * 2.0
        val w = length - h
        val r2 = capRadius * capRadius

        // compute the rectangular area
        val ra = w * h
        // compuate the circle area
        val ca: Double = r2 * PI
        val rm = density * ra
        val cm = density * ca
        val m = rm + cm

        // the inertia is slightly different. Its the inertia of the rectangular
        // region plus the inertia of half a circle moved from the center
        val d = w * 0.5
        // parallel axis theorem I2 = Ic + m * d^2
        val cI = 0.5 * cm * r2 + cm * d * d
        val rI = rm * (h * h + w * w) / 12.0
        // add the rectangular inertia and cicle inertia
        val I = rI + cI
        return Mass(center, m, I)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#getRadius(org.dyn4j.geometry.Vector2)
	 */
    override fun getRadius(center: Vector2): Double {
        return getRotationRadius(center = center, vertices = *foci) + capRadius
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shape#contains(org.dyn4j.geometry.Vector2, org.dyn4j.geometry.Transform)
	 */
    override fun contains(point: Vector2, transform: Transform): Boolean {
        // a capsule is just a radially expanded line segment
        val p: Vector2 = Segment.getPointOnSegmentClosestToPoint(point, transform.getTransformed(foci[0]), transform.getTransformed(foci[1]))
        val r2 = capRadius * capRadius
        val d2: Double = p.distanceSquared(point)
        return d2 <= r2
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.AbstractShape#rotate(org.dyn4j.geometry.Rotation, double, double)
	 */
    override fun rotate(rotation: Rotation, x: Double, y: Double) {
        super.rotate(rotation, x, y)

        // rotate the foci
        foci[0]!!.rotate(rotation, x, y)
        foci[1]!!.rotate(rotation, x, y)
        // rotate the local x-axis
        localXAxis!!.rotate(rotation)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.AbstractShape#translate(double, double)
	 */
    override fun translate(x: Double, y: Double) {
        super.translate(x, y)
        // translate the foci
        foci[0]!!.add(x, y)
        foci[1]!!.add(x, y)
    }

    /**
     * Returns the rotation about the local center in radians in the range [-, ].
     * @return double the rotation in radians
     */
    val rotationAngle: Double
        // localXAxis is already a unit vector so we can just return it as a {@link Rotation}
        get() = atan2(localXAxis!!.y, localXAxis!!.x)

    /**
     * @return the [Rotation] object that represents the local rotation
     */
    val rotation: Rotation
        // localXAxis is already a unit vector so we can just return it as a {@link Rotation}
        get() = Rotation(localXAxis!!.x, localXAxis!!.y)

    companion object {
        /**
         * The Capsule shape has two edge features which could be returned from the [.getFarthestFeature]
         * method. Under normal floating point conditions the edges will never be selected as the farthest features. Due to this,
         * stacking of capsule shapes is very unstable (or any resting contact that involves the edge). We introduce this factor
         * (% of projected normal) to help select the edge in cases where the collision normal is nearly parallel to the edge normal.
         */
        protected const val EDGE_FEATURE_SELECTION_CRITERIA = 0.98

        /**
         * Because we are selecting an edge even when the farthest feature should be a vertex, when the edges are clipped
         * against each other (in the ClippingManifoldSolver) they will not overlap. Due to this, we introduce an expansion
         * value (% of the width) that expands the edge feature so that in these cases a collision manifold is still generated.
         */
        protected const val EDGE_FEATURE_EXPANSION_FACTOR = 0.1

        /**
         * Validates the constructor input returning true if valid or throwing an exception if invalid.
         * @param width the bounding rectangle width
         * @param height the bounding rectangle height
         * @return boolean true
         * @throws IllegalArgumentException thrown if width or height are less than or equal to zero or if the width and height are near equal
         */
        private fun validate(width: Double, height: Double): Boolean {
            // validate the width and height
            if (width <= 0) throw IllegalArgumentException(message("geometry.capsule.invalidWidth"))
            if (height <= 0) throw IllegalArgumentException(message("geometry.capsule.invalidHeight"))

            // check for basically a circle
            if (abs(width - height) < Epsilon.E) throw IllegalArgumentException(message("geometry.capsule.degenerate"))
            return true
        }
    }

}