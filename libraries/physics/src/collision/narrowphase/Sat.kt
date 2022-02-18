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
package org.dyn4j.collision.narrowphase

import org.dyn4j.geometry.*
import kotlin.math.abs

/**
 * Implementation of the Separating Axis Theorem (SAT) for collision detection.
 *
 *
 * [Sat] states that &quot;if two [Convex] objects are not penetrating, there exists an axis (vector)
 * for which the projection of the objects does not overlap.&quot;
 *
 *
 * The axes that must be tested are **all** the edge normals of both [Convex] [Shape]s.  For each
 * edge normal we project the [Convex] [Shape]s onto it yielding a 1 dimensional [Interval].  If any
 * [Interval] doesn't overlap, then we can conclude the [Convex] [Shape]s do not intersect.  If all the
 * [Interval]s overlap, then we can conclude that the [Convex] [Shape]s intersect.
 *
 *
 * If the [Convex] [Shape]s are penetrating, a [Penetration] object can be built from the [Interval]s
 * with the least overlap.  The normal will be the edge normal of the [Interval] and the depth will be the [Interval]
 * overlap.
 * @author William Bittle
 * @version 3.0.2
 * @since 1.0.0
 * @see [SAT
](http://www.dyn4j.org/2010/01/sat/) */
class Sat : NarrowphaseDetector {
    /* (non-Javadoc)
	 * @see org.dyn4j.collision.narrowphase.NarrowphaseDetector#detect(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.collision.narrowphase.Penetration)
	 */
    override fun detect(convex1: Convex, transform1: Transform, convex2: Convex, transform2: Transform, penetration: Penetration?): Boolean {
        val penetration = penetration!! // by design
        // check for circles
        if (convex1 is Circle && convex2 is Circle) {
            // if its a circle - circle collision use the faster method
            return CircleDetector.detect(convex1, transform1, convex2, transform2, penetration)
        }
        penetration.clear()
        var n: Vector2? = null
        var overlap = Double.MAX_VALUE

        // get the foci from both shapes, the foci are used to test any
        // voronoi regions of the other shape
        val foci1 = convex1.getFoci(transform1)
        val foci2 = convex2.getFoci(transform2)

        // get the vector arrays for the separating axes tests
        val axes1 = convex1.getAxes(foci2, transform1)
        val axes2 = convex2.getAxes(foci1, transform2)

        // loop through shape1 axes
        if (axes1 != null) {
            val size = axes1.size
            for (i in 0 until size) {
                val axis = axes1[i]
                // check for the zero vector
                if (!axis.isZero) {
                    // project both shapes onto the axis
                    val intervalA = convex1.project(axis, transform1)
                    val intervalB = convex2.project(axis, transform2)
                    // if the intervals do not overlap then the two shapes
                    // cannot be intersecting
                    if (!intervalA.overlaps(intervalB)) {
                        // the shapes cannot be intersecting so immediately return null
                        return false
                    } else {
                        // get the overlap
                        var o = intervalA.getOverlap(intervalB)
                        // check for containment
                        if (intervalA.contains(intervalB) || intervalB.contains(intervalA)) {
                            // if containment exists then get the overlap plus the distance
                            // to between the two end points that are the closest
                            val max: Double = abs(intervalA.max - intervalB.max)
                            val min: Double = abs(intervalA.min - intervalB.min)
                            o += if (max > min) {
                                // if the min differences is less than the max then we need
                                // to flip the penetration axis
                                axis.negate()
                                min
                            } else {
                                max
                            }
                        }
                        // if the intervals do overlap then get save the depth and axis
                        // get the magnitude of the overlap
                        // get the minimum penetration depth and axis
                        if (o < overlap) {
                            overlap = o
                            n = axis
                        }
                    }
                }
            }
        }

        // loop through shape2 axes
        if (axes2 != null) {
            val size = axes2.size
            for (i in 0 until size) {
                val axis = axes2[i]
                // check for the zero vector
                if (!axis.isZero) {
                    // project both shapes onto the axis
                    val intervalA = convex1.project(axis, transform1)
                    val intervalB = convex2.project(axis, transform2)
                    // if the intervals do not overlap then the two shapes
                    // cannot be intersecting
                    if (!intervalA.overlaps(intervalB)) {
                        // the shapes cannot be intersecting so immediately return null
                        return false
                    } else {
                        // if the intervals do overlap then get save the depth and axis
                        // get the magnitude of the overlap
                        var o = intervalA.getOverlap(intervalB)
                        // check for containment
                        if (intervalA.contains(intervalB) || intervalB.contains(intervalA)) {
                            // if containment exists then get the overlap plus the distance
                            // to between the two end points that are the closest
                            val max: Double = abs(intervalA.max - intervalB.max)
                            val min: Double = abs(intervalA.min - intervalB.min)
                            o += if (max > min) {
                                // if the min differences is less than the max then we need
                                // to flip the penetration axis
                                axis.negate()
                                min
                            } else {
                                max
                            }
                        }
                        // get the minimum penetration depth and axis
                        if (o < overlap) {
                            overlap = o
                            n = axis
                        }
                    }
                }
            }
        }

        // make sure the vector is pointing from shape1 to shape2
        val c1 = transform1.getTransformed(convex1.center)
        val c2 = transform2.getTransformed(convex2.center)
        val cToc = c1.to(c2)
        if (cToc.dot(n!!) < 0) {
            // negate the normal if its not
            n.negate()
        }

        // fill the penetration object
        penetration.normal = n
        penetration.depth = overlap
        // return true
        return true
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.narrowphase.NarrowphaseDetector#test(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform)
	 */
    override fun detect(convex1: Convex, transform1: Transform, convex2: Convex, transform2: Transform): Boolean {
        // check for circles
        if (convex1 is Circle && convex2 is Circle) {
            // if its a circle - circle collision use the faster method
            return CircleDetector.detect(convex1, transform1, convex2, transform2)
        }

        // get the foci from both shapes, the foci are used to test any
        // voronoi regions of the other shape
        val foci1 = convex1.getFoci(transform1)
        val foci2 = convex2.getFoci(transform2)

        // get the vector arrays for the separating axes tests
        val axes1 = convex1.getAxes(foci2, transform1)
        val axes2 = convex2.getAxes(foci1, transform2)

        // loop through shape1 axes
        if (axes1 != null) {
            val size = axes1.size
            for (i in 0 until size) {
                val axis = axes1[i]
                // check for the zero vector
                if (!axis!!.isZero) {
                    // project both shapes onto the axis
                    val intervalA = convex1.project(axis, transform1)
                    val intervalB = convex2.project(axis, transform2)
                    // if the intervals do not overlap then the two shapes
                    // cannot be intersecting
                    if (!intervalA.overlaps(intervalB)) {
                        // the shapes cannot be intersecting so immediately return
                        return false
                    }
                }
            }
        }

        // loop through shape2 axes
        if (axes2 != null) {
            val size = axes2.size
            for (i in 0 until size) {
                val axis = axes2[i]
                // check for the zero vector
                if (!axis.isZero) {
                    // project both shapes onto the axis
                    val intervalA = convex1.project(axis, transform1)
                    val intervalB = convex2.project(axis, transform2)
                    // if the intervals do not overlap then the two shapes
                    // cannot be intersecting
                    if (!intervalA.overlaps(intervalB)) {
                        // the shapes cannot be intersecting so immediately return
                        return false
                    }
                }
            }
        }

        // if we get here, then we have intersection
        return true
    }
}