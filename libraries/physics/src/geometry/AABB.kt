/*
 * Copyright (c) 2010-2017 William Bittle  http://www.dyn4j.org/
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

import org.dyn4j.resources.Messages
import org.dyn4j.resources.message
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of an Axis-Align Bounding Box.
 *
 *
 * An [AABB] has minimum and maximum coordinates that define the box.
 *
 *
 * An [AABB] can be unioned or intersected with other [AABB]s to combine
 * them into another [AABB].  If an intersection produces no result, a degenerate [AABB]
 * is returned.  A degenerate [AABB] can be tested by the [.isDegenerate] methods and
 * is defined as an [AABB] who's maximum and minimum are equal.
 *
 *
 * [AABB]s can also be tested for overlap and (full) containment using the [.overlaps]
 * and [.contains] method.
 *
 *
 * The [.expand] method can be used to expand the bounds of the [AABB] by some amount.
 * @author William Bittle
 * @version 3.4.0
 * @since 3.0.0
 */
class AABB : Translatable {
    /**
     * Returns the minimum x extent.
     * @return double
     */
    /** The minimum extent along the x-axis  */
    var minX = 0.0
        protected set

    /**
     * Returns the minimum y extent.
     * @return double
     */
    /** The minimum extent along the y-axis  */
    var minY = 0.0
        protected set

    /**
     * Returns the maximum x extent.
     * @return double
     */
    /** The maximum extent along the x-axis  */
    var maxX = 0.0
        protected set

    /**
     * Returns the maximum y extent.
     * @return double
     */
    /** The maximum extent along the y-axis  */
    var maxY = 0.0
        protected set

    /**
     * Full constructor.
     * @param minX the minimum x extent
     * @param minY the minimum y extent
     * @param maxX the maximum x extent
     * @param maxY the maximum y extent
     */
    constructor(minX: Double, minY: Double, maxX: Double, maxY: Double) {
        // check the min and max
        if (minX > maxX || minY > maxY) throw IllegalArgumentException(message("geometry.aabb.invalidMinMax"))
        this.minX = minX
        this.minY = minY
        this.maxX = maxX
        this.maxY = maxY
    }

    /**
     * Full constructor.
     * @param min the minimum extent
     * @param max the maximum extent
     * @throws IllegalArgumentException if either coordinate of the given min is greater than the given max
     */
    constructor(min: Vector2, max: Vector2) : this(min.x, min.y, max.x, max.y) {}

    /**
     * Full constructor.
     * @param radius the radius of a circle fitting inside an AABB
     * @since 3.1.5
     */
    constructor(radius: Double) : this(null, radius) {}

    /**
     * Full constructor.
     *
     *
     * Creates an AABB for a circle with the given center and radius.
     * @param center the center of the circle
     * @param radius the radius of the circle
     * @since 3.1.5
     * @throws IllegalArgumentException if the given radius is less than zero
     */
    constructor(center: Vector2?, radius: Double) {
        if (radius < 0) throw IllegalArgumentException(message("geometry.aabb.invalidRadius"))
        if (center == null) {
            minX = -radius
            minY = -radius
            maxX = radius
            maxY = radius
        } else {
            minX = center.x - radius
            minY = center.y - radius
            maxX = center.x + radius
            maxY = center.y + radius
        }
    }

    /**
     * Copy constructor.
     * @param aabb the [AABB] to copy
     * @since 3.1.1
     */
    constructor(aabb: AABB) {
        minX = aabb.minX
        minY = aabb.minY
        maxX = aabb.maxX
        maxY = aabb.maxY
    }

    /**
     * Returns a copy of this [AABB].
     * @return [AABB]
     * @since 3.4.0
     */
    fun copy(): AABB {
        return AABB(this)
    }

    /**
     * Sets this aabb to the given aabb's value and returns
     * this AABB.
     * @param aabb the aabb to copy
     * @return [AABB]
     * @since 3.2.5
     */
    fun set(aabb: AABB): AABB {
        minX = aabb.minX
        minY = aabb.minY
        maxX = aabb.maxX
        maxY = aabb.maxY
        return this
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("AABB[Min=")
            .append("(")
            .append(minX)
            .append(", ")
            .append(minY)
            .append(")")
            .append("|Max=")
            .append("(")
            .append(maxX)
            .append(", ")
            .append(maxY)
            .append(")")
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Translatable#translate(double, double)
	 */
    override fun translate(x: Double, y: Double) {
        minX += x
        minY += y
        maxX += x
        maxY += y
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Translatable#translate(org.dyn4j.geometry.Vector2)
	 */
    override fun translate(translation: Vector2) {
        translate(translation.x, translation.y)
    }

    /**
     * Returns a new AABB of this AABB translated by the
     * given translation amount.
     * @param translation the translation
     * @return AABB
     * @since 3.1.1
     */
    fun getTranslated(translation: Vector2): AABB {
        return AABB(
            minX + translation.x,
            minY + translation.y,
            maxX + translation.x,
            maxY + translation.y
        )
    }

    /**
     * Returns the width of this [AABB].
     * @return double
     * @since 3.0.1
     */
    val width: Double
        get() = maxX - minX

    /**
     * Returns the height of this [AABB].
     * @return double
     * @since 3.0.1
     */
    val height: Double
        get() = maxY - minY

    /**
     * Returns the perimeter of this [AABB].
     * @return double
     */
    val perimeter: Double
        get() = 2 * (maxX - minX + maxY - minY)

    /**
     * Returns the area of this [AABB];.
     * @return double
     */
    val area: Double
        get() = (maxX - minX) * (maxY - minY)

    /**
     * Performs a union of this [AABB] and the given [AABB] placing
     * the result of the union into this [AABB] and then returns
     * this [AABB]
     * @param aabb the [AABB] to union
     * @return [AABB]
     */
    fun union(aabb: AABB): AABB {
        minX = min(minX, aabb.minX)
        minY = min(minY, aabb.minY)
        maxX = max(maxX, aabb.maxX)
        maxY = max(maxY, aabb.maxY)
        return this
    }

    /**
     * Performs a union of this [AABB] and the given [AABB] returning
     * a new [AABB] containing the result.
     * @param aabb the [AABB] to union
     * @return [AABB] the resulting union
     */
    fun getUnion(aabb: AABB): AABB {
        return copy().union(aabb)
    }

    /**
     * Performs the intersection of this [AABB] and the given [AABB] placing
     * the result into this [AABB] and then returns this [AABB].
     *
     *
     * If the given [AABB] does not overlap this [AABB], this [AABB] is
     * set to a zero [AABB].
     * @param aabb the [AABB] to intersect
     * @return [AABB]
     * @since 3.1.1
     */
    fun intersection(aabb: AABB): AABB {
        minX = max(minX, aabb.minX)
        minY = max(minY, aabb.minY)
        maxX = min(maxX, aabb.maxX)
        maxY = min(maxY, aabb.maxY)

        // check for a bad AABB
        if (minX > maxX || minY > maxY) {
            // the two AABBs were not overlapping
            // set this AABB to a degenerate one
            minX = 0.0
            minY = 0.0
            maxX = 0.0
            maxY = 0.0
        }
        return this
    }

    /**
     * Performs the intersection of this [AABB] and the given [AABB] returning
     * the result in a new [AABB].
     *
     *
     * If the given [AABB] does not overlap this [AABB], a zero [AABB] is
     * returned.
     * @param aabb the [AABB] to intersect
     * @return [AABB]
     * @since 3.1.1
     */
    fun getIntersection(aabb: AABB): AABB {
        return copy().intersection(aabb)
    }

    /**
     * Expands this [AABB] by half the given expansion in each direction and
     * then returns this [AABB].
     *
     *
     * The expansion can be negative to shrink the [AABB].  However, if the expansion is
     * greater than the current width/height, the [AABB] can become invalid.  In this
     * case, the AABB will become a degenerate AABB at the mid point of the min and max for
     * the respective coordinates.
     * @param expansion the expansion amount
     * @return [AABB]
     */
    fun expand(expansion: Double): AABB {
        val e = expansion * 0.5
        minX -= e
        minY -= e
        maxX += e
        maxY += e
        // we only need to verify the new aabb if the expansion
        // was inwardly
        if (expansion < 0.0) {
            // if the aabb is invalid then set the min/max(es) to
            // the middle value of their current values
            if (minX > maxX) {
                val mid = (minX + maxX) * 0.5
                minX = mid
                maxX = mid
            }
            if (minY > maxY) {
                val mid = (minY + maxY) * 0.5
                minY = mid
                maxY = mid
            }
        }
        return this
    }

    /**
     * Returns a new [AABB] of this AABB expanded by half the given expansion
     * in both the x and y directions.
     *
     *
     * The expansion can be negative to shrink the [AABB].  However, if the expansion is
     * greater than the current width/height, the [AABB] can become invalid.  In this
     * case, the AABB will become a degenerate AABB at the mid point of the min and max for
     * the respective coordinates.
     * @param expansion the expansion amount
     * @return [AABB]
     * @since 3.1.1
     */
    fun getExpanded(expansion: Double): AABB {
        return copy().expand(expansion)
    }

    /**
     * Returns true if the given [AABB] and this [AABB] overlap.
     * @param aabb the [AABB] to test
     * @return boolean true if the [AABB]s overlap
     */
    fun overlaps(aabb: AABB): Boolean {
        return minX <= aabb.maxX && maxX >= aabb.minX && minY <= aabb.maxY && maxY >= aabb.minY
    }

    /**
     * Returns true if the given [AABB] is contained within this [AABB].
     * @param aabb the [AABB] to test
     * @return boolean
     */
    operator fun contains(aabb: AABB): Boolean {
        return minX <= aabb.minX && maxX >= aabb.maxX && minY <= aabb.minY && maxY >= aabb.maxY
    }

    /**
     * Returns true if the given point is contained within this [AABB].
     * @param point the point to test
     * @return boolean
     * @since 3.1.1
     */
    operator fun contains(point: Vector2): Boolean {
        return this.contains(point.x, point.y)
    }

    /**
     * Returns true if the given point's coordinates are contained within this [AABB].
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return boolean
     * @since 3.1.1
     */
    fun contains(x: Double, y: Double): Boolean {
        return minX <= x && maxX >= x && minY <= y && maxY >= y
    }

    /**
     * Returns true if this [AABB] is degenerate.
     *
     *
     * A degenerate [AABB] is one where its min and max x or y
     * coordinates are equal.
     * @return boolean
     * @since 3.1.1
     */
    val isDegenerate: Boolean
        get() = minX == maxX || minY == maxY

    /**
     * Returns true if this [AABB] is degenerate given
     * the specified error.
     *
     *
     * An [AABB] is degenerate given some error if
     * max - min &lt;= error for either the x or y coordinate.
     * @param error the allowed error
     * @return boolean
     * @since 3.1.1
     * @see .isDegenerate
     */
    fun isDegenerate(error: Double): Boolean {
        return abs(maxX - minX) <= error || abs(maxY - minY) <= error
    }

    companion object {
        /**
         * Method to create the valid AABB defined by the two points point1 and point2.
         *
         * @param point1 the first point
         * @param point2 the second point
         * @return The one and only one valid AABB formed by point1 and point2
         */
        fun createAABBFromPoints(point1: Vector2, point2: Vector2): AABB {
            return createAABBFromPoints(point1.x, point1.y, point2.x, point2.y)
        }

        /**
         * Method to create the valid AABB defined by the two points A(point1x, point1y) and B(point2x, point2y).
         *
         * @param point1x The x coordinate of point A
         * @param point1y The y coordinate of point A
         * @param point2x The x coordinate of point B
         * @param point2y The y coordinate of point B
         * @return The one and only one valid AABB formed by A and B
         */
        fun createAABBFromPoints(
            point1x: Double,
            point1y: Double,
            point2x: Double,
            point2y: Double
        ): AABB {
            var point1x = point1x
            var point1y = point1y
            var point2x = point2x
            var point2y = point2y
            if (point2x < point1x) {
                val temp = point1x
                point1x = point2x
                point2x = temp
            }
            if (point2y < point1y) {
                val temp = point1y
                point1y = point2y
                point2y = temp
            }
            return AABB(point1x, point1y, point2x, point2y)
        }
    }
}