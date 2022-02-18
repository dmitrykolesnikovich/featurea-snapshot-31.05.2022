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
package org.dyn4j.collision.continuous

import featurea.math.cbrt
import org.dyn4j.Epsilon
import org.dyn4j.collision.narrowphase.DistanceDetector
import org.dyn4j.collision.narrowphase.Gjk
import org.dyn4j.collision.narrowphase.Separation
import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Transform
import org.dyn4j.geometry.Vector2
import org.dyn4j.resources.message
import kotlin.math.abs

/**
 * Implements the Conservative Advancement technique to solve for the time of impact.
 *
 *
 * This method assumes that translation and rotation are linear and computes the
 * time of impact within a given tolerance.
 *
 *
 * This method is described in "Continuous Collision Detection and Physics" by Erwin Coumans (Draft).
 * @author William Bittle
 * @version 3.1.5
 * @since 1.2.0
 */
class ConservativeAdvancement : TimeOfImpactDetector {

    /** The distance detector  */
    var distanceDetector: DistanceDetector = Gjk()

    /** The tolerance  */
    var distanceEpsilon =
        DEFAULT_DISTANCE_EPSILON

    /** The maximum number of iterations of the root finder  */
    var maxIterations =
        DEFAULT_MAX_ITERATIONS

    /**
     * Default constructor.
     *
     *
     * Uses [Gjk] as the [DistanceDetector].
     */
    constructor() {}

    /**
     * Optional constructor.
     * @param distanceDetector the distance detector
     * @throws NullPointerException if distanceDetector is null
     */
    constructor(distanceDetector: DistanceDetector?) {
        if (distanceDetector == null) throw NullPointerException(message("collision.continuous.conservativeAdvancement.nullDistanceDetector"))
        this.distanceDetector = distanceDetector
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.continuous.TimeOfImpactDetector#getTimeOfImpact(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Vector2, double, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Vector2, double, org.dyn4j.collision.continuous.TimeOfImpact)
	 */
    override fun getTimeOfImpact(
        convex1: Convex,
        transform1: Transform,
        dp1: Vector2,
        da1: Double,
        convex2: Convex,
        transform2: Transform,
        dp2: Vector2,
        da2: Double,
        toi: TimeOfImpact
    ): Boolean {
        return this.getTimeOfImpact(convex1, transform1, dp1, da1, convex2, transform2, dp2, da2, 0.0, 1.0, toi)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.continuous.TimeOfImpactDetector#getTimeOfImpact(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Vector2, double, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Vector2, double, double, double, org.dyn4j.collision.continuous.TimeOfImpact)
	 */
    override fun getTimeOfImpact(
        convex1: Convex,
        transform1: Transform,
        dp1: Vector2,
        da1: Double,
        convex2: Convex,
        transform2: Transform,
        dp2: Vector2,
        da2: Double,
        t1: Double,
        t2: Double,
        toi: TimeOfImpact
    ): Boolean {
        // count the number of iterations
        var iterations = 0

        // create some reusable transforms for interpolation
        val lerpTx1 = Transform()
        val lerpTx2 = Transform()

        // check for separation at the beginning of the interval
        val separation = Separation()
        var separated: Boolean = distanceDetector.distance(convex1, transform1, convex2, transform2, separation)
        // if they are not separated then there is nothing to do
        if (!separated) {
            return false
        }
        // get the distance
        var d: Double = separation.distance
        // check if the distance is less than the tolerance
        if (d < distanceEpsilon) {
            // fill up the toi
            toi.time = 0.0
            toi.separation = separation
            return true
        }
        // get the separation normal
        var n: Vector2 = separation.normal!!

        // get the rotation disc radius for the swept object
        val rmax1: Double = convex1.radius
        val rmax2: Double = convex2.radius

        // compute the relative linear velocity
        val rv: Vector2 = dp1.difference(dp2)
        // compute the relative linear velocity magnitude
        val rvl: Double = rv.magnitude
        // compute the maximum rotational velocity
        val amax: Double = rmax1 * abs(da1) + rmax2 * abs(da2)

        // check if the bodies are moving relative to one another
        if (rvl + amax == 0.0) {
            return false
        }

        // set the initial time
        var l = t1
        // set the previous time
        var l0 = l

        // loop until the distance is less than the tolerance
        while (d > distanceEpsilon && iterations < maxIterations) {
            // project the relative max velocity along the separation normal
            val rvDotN: Double = rv.dot(n)
            // compute the max relative velocity
            val drel = rvDotN + amax
            // is the relative velocity along the normal and the maximum
            // rotation velocity less than epsilon
            if (drel <= Epsilon.E) {
                return false
            } else {
                // compute the time to advance
                val dt = d / drel
                // advance the time
                l += dt
                // if l drops below the minimum time
                if (l < t1) {
                    return false
                }
                // if l goes above the maximum time
                if (l > t2) {
                    return false
                }
                // if l doesn't change significantly
                if (l <= l0) {
                    // l hasn't changed so just return with
                    // what we have now
                    break
                }
                // set the last time
                l0 = l
            }

            // increment the number of iterations
            iterations++

            // interpolate to time
            transform1.lerp(dp1, da1, l, lerpTx1)
            transform2.lerp(dp2, da2, l, lerpTx2)

            // find closest points
            separated = distanceDetector.distance(convex1, lerpTx1, convex2, lerpTx2, separation)
            d = separation.distance
            // check for intersection
            if (!separated) {
                // the shapes are intersecting.  This should
                // not happen because of the conservative nature
                // of the algorithm, however because of numeric
                // error it will.

                // back up to half the distance epsilon
                l -= 0.5 * distanceEpsilon / drel
                // interpolate
                transform1.lerp(dp1, da1, l, lerpTx1)
                transform2.lerp(dp2, da2, l, lerpTx2)
                // compute a new separation
                distanceDetector.distance(convex1, lerpTx1, convex2, lerpTx2, separation)
                // get the distance
                d = separation.distance
                // the separation here could still be close to zero if the
                // objects are rotating very fast, in which case just assume
                // this is as close as we can get

                // break from the loop since we have detected the
                // time of impact but had to fix the distance
                break
            }

            // set the new normal and distance
            n = separation.normal!!
            d = separation.distance
        }

        // fill up the separation object
        toi.time = l
        toi.separation = separation
        return true
    }

    companion object {
        /** The default distance epsilon  */
        val DEFAULT_DISTANCE_EPSILON: Double = cbrt(Epsilon.E)

        /** The default maximum number of iterations  */
        const val DEFAULT_MAX_ITERATIONS = 30
    }
}