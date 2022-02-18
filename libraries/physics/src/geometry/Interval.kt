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

import featurea.math.max
import featurea.math.min
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.abs

/**
 * Represents a one dimensional numeric [Interval].
 * @author William Bittle
 * @version 3.1.9
 * @since 1.0.0
 */
class Interval {

    /** The minimum value  */
    var min: Double
        set(value) {
            if (value > max) throw IllegalArgumentException(message("geometry.interval.invalidMinimum"))
            field = value
        }

    /** The maximum value  */
    var max: Double
        set(value) {
            if (value < min) throw IllegalArgumentException(message("geometry.interval.invalidMaximum"))
            field = value
        }

    /**
     * Full constructor.
     * @param min the minimum value
     * @param max the maximum value
     * @throws IllegalArgumentException if min &gt; max
     */
    constructor(min: Double, max: Double) {
        if (min > max) throw IllegalArgumentException(message("geometry.interval.invalid"))
        this.min = min
        this.max = max
    }

    /**
     * Copy constructor.
     * @param interval the [Interval] to copy
     * @since 3.1.1
     */
    constructor(interval: Interval) {
        min = interval.min
        max = interval.max
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("[").append(min).append(", ").append(max).append("]")
        return sb.toString()
    }

    /**
     * Returns true if the given value is within this [Interval]
     * including the maximum and minimum.
     * @param value the test value
     * @return boolean
     */
    fun includesInclusive(value: Double): Boolean {
        return value <= max && value >= min
    }

    /**
     * Returns true if the given value is within this [Interval]
     * exlcuding the maximum and minimum.
     * @param value the test value
     * @return boolean
     */
    fun includesExclusive(value: Double): Boolean {
        return value < max && value > min
    }

    /**
     * Returns true if the given value is within this [Interval]
     * including the minimum and excluding the maximum.
     * @param value the test value
     * @return boolean
     */
    fun includesInclusiveMin(value: Double): Boolean {
        return value < max && value >= min
    }

    /**
     * Returns true if the given value is within this [Interval]
     * including the maximum and excluding the minimum.
     * @param value the test value
     * @return boolean
     */
    fun includesInclusiveMax(value: Double): Boolean {
        return value <= max && value > min
    }

    /**
     * Returns true if the two [Interval]s overlap.
     * @param interval the [Interval]
     * @return boolean
     */
    fun overlaps(interval: Interval): Boolean {
        return !(min > interval.max || interval.min > max)
    }

    /**
     * Returns the amount of overlap between this [Interval] and the given
     * [Interval].
     *
     *
     * This method tests to if the [Interval]s overlap first.  If they do then
     * the overlap is returned, if they do not then 0 is returned.
     * @param interval the [Interval]
     * @return double
     */
    fun getOverlap(interval: Interval): Double {
        // make sure they overlap
        return if (overlaps(interval)) {
            min(max, interval.max) - max(min, interval.min)
        } else 0.0
    }

    /**
     * If the value is within this [Interval], inclusive, then return the value, else
     * return either the max or minimum value.
     * @param value the value to clamp
     * @return double
     */
    fun clamp(value: Double): Double {
        return clamp(value, min, max)
    }

    /**
     * Returns true if this [Interval] is degenerate.
     *
     *
     * An [Interval] is degenerate if it equals [a, a].
     * @return boolean
     */
    val isDegenerate: Boolean
        get() = min == max

    /**
     * Returns true if this [Interval] is degenerate
     * given the allowed error.
     *
     *
     * An [Interval] is degenerate given some error if
     * max - min &lt;= error.
     * @param error the allowed error
     * @return boolean
     */
    fun isDegenerate(error: Double): Boolean {
        return abs(max - min) <= error
    }

    /**
     * Returns true if the given [Interval] is contained in this [Interval].
     * @param interval the [Interval]
     * @return boolean
     */
    operator fun contains(interval: Interval): Boolean {
        return interval.min > min && interval.max < max
    }

    /**
     * Sets this [Interval] to the union of this [Interval] and the given [Interval].
     *
     *
     * If the two [Interval]s are not overlapping then this method will
     * return one [Interval] that represents an [Interval] enclosing both
     * [Interval]s.
     * @param interval the [Interval]
     */
    fun union(interval: Interval) {
        min = min(interval.min, min)
        max = max(interval.max, max)
    }

    /**
     * Returns the union of the given [Interval] and this [Interval].
     * @see Interval.union
     * @param interval the [Interval]
     * @return [Interval]
     */
    fun getUnion(interval: Interval): Interval {
        return Interval(
            min(interval.min, min),
            max(interval.max, max)
        )
    }

    /**
     * Sets this [Interval] to the intersection of this [Interval] and the given [Interval].
     *
     *
     * If the two [Interval]s are not overlapping then this method will make this [Interval]
     * the a zero degenerate [Interval], [0, 0].
     * @param interval the [Interval]
     */
    fun intersection(interval: Interval) {
        if (overlaps(interval)) {
            min = max(interval.min, min)
            max = min(interval.max, max)
        } else {
            min = 0.0
            max = 0.0
        }
    }

    /**
     * Returns the intersection of the given [Interval] and this [Interval].
     * @see Interval.intersection
     * @param interval the [Interval]
     * @return [Interval]
     */
    fun getIntersection(interval: Interval): Interval {
        return if (overlaps(interval)) {
            Interval(
                max(interval.min, min),
                min(interval.max, max)
            )
        } else Interval(0.0, 0.0)
    }

    /**
     * Returns the distance between the two [Interval]s.
     *
     *
     * If the given interval overlaps this interval, zero is returned.
     * @param interval the [Interval]
     * @return double
     */
    fun distance(interval: Interval): Double {
        // make sure they arent overlapping
        return if (!overlaps(interval)) {
            // the distance is calculated by taking the max of one - the min of the other
            // the interval whose max will be used is determined by the interval with the max
            // less than the other's min
            if (max < interval.min) {
                interval.min - max
            } else {
                min - interval.max
            }
        } else 0.0
        // if they are overlapping then return 0
    }

    /**
     * Expands this [Interval] by half the given amount in both directions.
     *
     *
     * The value can be negative to shrink the interval.  However, if the value is
     * greater than the current length of the interval, the interval can become
     * invalid.  In this case, the interval will become a degenerate interval at
     * the mid point of the min and max.
     * @param value the value
     */
    fun expand(value: Double) {
        val e = value * 0.5
        min -= e
        max += e
        // verify the interval is still valid
        if (value < 0.0 && min > max) {
            // if its not then set the min/max to
            // the middle value of their current values
            val p = (min + max) * 0.5
            min = p
            max = p
        }
    }

    /**
     * Returns a new [Interval] of this interval expanded by half the given amount
     * in both directions.
     *
     *
     * The value can be negative to shrink the interval.  However, if the value is
     * greater than the current length of the interval, the interval will be
     * invalid.  In this case, the interval returned will be a degenerate interval at
     * the mid point of the min and max.
     * @param value the value
     * @return [Interval]
     * @since 3.1.1
     */
    fun getExpanded(value: Double): Interval {
        val e = value * 0.5
        var min = min - e
        var max = max + e
        // verify the interval is still valid
        if (value < 0.0 && min > max) {
            // if its not then set the min/max to
            // the middle value of their current values
            val p = (min + max) * 0.5
            min = p
            max = p
        }
        return Interval(min, max)
    }

    /**
     * Returns the length of this interval from its min to its max.
     * @return double
     * @since 3.1.1
     */
    val length: Double
        get() = max - min

    companion object {
        /**
         * Returns a number clamped between two other numbers.
         *
         *
         * This method assumes that min  max.
         * @param value the value to clamp
         * @param min the min value
         * @param max the max value
         * @return double
         */
        @JvmStatic
        fun clamp(value: Double, min: Double, max: Double): Double {
            return if (value <= max && value >= min) {
                value
            } else if (max < value) {
                max
            } else {
                min
            }
        }
    }
}