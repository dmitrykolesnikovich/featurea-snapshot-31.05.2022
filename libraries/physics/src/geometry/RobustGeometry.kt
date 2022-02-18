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

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.math.abs

/**
 * This class provides geometric routines that have guarantees about some properties
 * of their floating point results and operations.
 *
 * @author Manolis Tsamis
 * @version 3.4.0
 * @since 3.4.0
 */
object RobustGeometry {
    /** Constant that [AdaptiveDecimal] uses to split doubles when calculation multiplication error  */
    var SPLITTER: Double = 0.0

    /** Error bounds used to adaptively use as much precision is required for a correct result  */
    private var RESULT_ERROR_BOUND = 0.0
    private var ERROR_BOUND_A = 0.0
    private var ERROR_BOUND_B = 0.0
    private var ERROR_BOUND_C = 0.0
    /**
     * Performs the cross product of two vectors a, b, that is ax * by - ay * bx but with extended precision
     * and stores the 4 component result in the given [AdaptiveDecimal] `result`.
     * In the same way as with [AdaptiveDecimal.sum] if `result` is null
     * a new one is allocated, otherwise the existing is cleared and used.
     *
     * @param ax The x value of the vector a
     * @param ay The y value of the vector a
     * @param bx The x value of the vector b
     * @param by The y value of the vector b
     * @param result The [AdaptiveDecimal] in which the cross product is stored
     * @return The result
     */
    /**
     * Performs cross product on four primitives and also allocates a new [AdaptiveDecimal]
     * with the appropriate capacity to store the result.
     *
     * @param ax The x value of the vector a
     * @param ay The y value of the vector a
     * @param bx The x value of the vector b
     * @param by The y value of the vector b
     * @return The result
     * @see .cross
     */
    @JvmStatic
    @JvmOverloads
    fun cross(ax: Double, ay: Double, bx: Double, by: Double, result: AdaptiveDecimal = AdaptiveDecimal(4)): AdaptiveDecimal {
        val axby = ax * by
        val aybx = bx * ay
        val axbyTail = AdaptiveDecimal.getErrorComponentFromProduct(ax, by, axby)
        val aybxTail = AdaptiveDecimal.getErrorComponentFromProduct(bx, ay, aybx)

        // result can be null in which case AdaptiveDecimal.fromDiff will allocate a new one
        return AdaptiveDecimal.fromDiff(axbyTail, axby, aybxTail, aybx, result)
    }

    /**
     * Robust side-of-line test.
     * Computes the same value with [Segment.getLocation] but with
     * enough precision so the sign of the result is correct for any [Vector2]s pa, pb, pc.
     * This implementation uses more precision as-needed only for the hardest cases.
     * For the majority of inputs this will be only slightly slower than the corresponding call
     * to [Segment.getLocation] but in the hard cases can be 5-25 times slower.
     *
     * @param point the point
     * @param linePoint1 the first point of the line
     * @param linePoint2 the second point of the line
     * @return double
     * @see Segment.getLocation
     */
    @JvmStatic
    fun getLocation(point: Vector2, linePoint1: Vector2, linePoint2: Vector2): Double {
        // This code is based on the original code by Jonathan Richard Shewchuk
        // For more details about the correctness and error bounds check the note
        // in the AdaptiveDecimal class and the corresponding paper of the author.

        // In the beginning try the simple-straightforward computation with floating point values
        // and no extra precision, as in Segment#getLocation
        val detLeft: Double = (point.x - linePoint2.x) * (linePoint1.y - linePoint2.y)
        val detRight: Double = (point.y - linePoint2.y) * (linePoint1.x - linePoint2.x)
        val det = detLeft - detRight
        if (detLeft == 0.0 || detRight == 0.0 || detLeft > 0 != detRight > 0) {
            return det
        }
        val detSum: Double = abs(detLeft + detRight)
        return if (abs(det) >= ERROR_BOUND_A * detSum) {
            // This will cover the vast majority of cases
            det
        } else getLocationAdaptive(
            point,
            linePoint1,
            linePoint2,
            detSum
        )

        // For the few harder cases we need to use the adaptive precision implementation
    }

    /**
     * The extended precision implementation for the side-of-line test.
     *
     * @param point the point
     * @param linePoint1 the first point of the line
     * @param linePoint2 the second point of the line
     * @return double
     * @see .getLocation
     */
    @JvmStatic
    private fun getLocationAdaptive(point: Vector2, linePoint1: Vector2, linePoint2: Vector2, detSum: Double): Double {
        val acx: Double = point.x - linePoint2.x
        val acy: Double = point.y - linePoint2.y
        val bcx: Double = linePoint1.x - linePoint2.x
        val bcy: Double = linePoint1.y - linePoint2.y

        // Calculate the cross product but with more precision than before
        // But don't bother yet to perform the differences acx, acy, bcx, bcy
        // with full precision
        val B = cross(acx, acy, bcx, bcy)
        var det = B.estimation
        var errorBound = ERROR_BOUND_B * detSum
        if (abs(det) >= errorBound) {
            return det
        }

        // Since we need more precision to produce the result at this point
        // we have to calculate the differences with full precision
        val acxTail = AdaptiveDecimal.getErrorComponentFromDifference(point.x, linePoint2.x, acx)
        val acyTail = AdaptiveDecimal.getErrorComponentFromDifference(point.y, linePoint2.y, acy)
        val bcxTail = AdaptiveDecimal.getErrorComponentFromDifference(linePoint1.x, linePoint2.x, bcx)
        val bcyTail = AdaptiveDecimal.getErrorComponentFromDifference(linePoint1.y, linePoint2.y, bcy)
        if (acxTail == 0.0 && acyTail == 0.0 && bcxTail == 0.0 && bcyTail == 0.0) {
            // trivial case: the extra precision was not needed after all
            return det
        }
        errorBound =
            ERROR_BOUND_C * detSum + RESULT_ERROR_BOUND * abs(
                det
            )
        // But don't use full precision to calculate the following cross products with the tail values
        det += acx * bcyTail + bcy * acxTail - (acy * bcxTail + bcx * acyTail)
        if (abs(det) >= errorBound) {
            return det
        }

        // This case is so rare that we don't know if there are any inputs going into it
        // At this point we have to go full out and calculate all the products with full precision

        // Re-usable buffer to store the results of the 3 cross products needed below
        val buffer = AdaptiveDecimal(4)
        cross(acxTail, bcx, acyTail, bcy, buffer)
        val C1 = B.sum(buffer)
        cross(acx, bcxTail, acy, bcyTail, buffer)
        val C2 = C1.sum(buffer)
        cross(acxTail, bcxTail, acyTail, bcyTail, buffer)
        val D = C2.sum(buffer)

        // return the most significant component of the last buffer D.
        // reminder: components are non-overlapping so this is ok
        return D[D.size() - 1]
    }

    /**
     * Initializer that computes the necessary splitter value and error bounds based on the machine epsilon.
     * Also instantiates the internal [AdaptiveDecimal] variables.
     */
    init {
        // calculate the splitter and epsilon as described in the paper
        var everyOther = true
        var epsilon = 1.0
        var splitterMut = 1.0
        while (1.0 + epsilon > 1.0) {
            if (everyOther) {
                splitterMut *= 2
            }
            epsilon *= 0.5
            everyOther = !everyOther
        }
        splitterMut += 1.0
        SPLITTER = splitterMut

        // compute bounds as described in the paper
        RESULT_ERROR_BOUND = (3 + 8 * epsilon) * epsilon
        ERROR_BOUND_A = (3 + 16 * epsilon) * epsilon
        ERROR_BOUND_B = (2 + 12 * epsilon) * epsilon
        ERROR_BOUND_C = (9 + 64 * epsilon) * epsilon * epsilon
    }
}