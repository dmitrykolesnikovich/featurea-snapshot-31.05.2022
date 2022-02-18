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

import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.Ray
import org.dyn4j.geometry.Transform
import org.dyn4j.geometry.Vector2
import kotlin.math.sqrt

/**
 * Class devoted to [Circle] detection queries.
 * @author William Bittle
 * @version 3.2.0
 * @since 2.0.0
 */
object CircleDetector {
    /**
     * Fast method for determining a collision between two [Circle]s.
     *
     *
     * Returns true if the given [Circle]s are intersecting and places the
     * penetration vector and depth in the given [Penetration] object.
     *
     *
     * If the [Circle] centers are coincident then the penetration [Vector2]
     * will be the zero [Vector2], however, the penetration depth will be
     * correct.  In this case its up to the caller to determine a reasonable penetration
     * [Vector2].
     * @param circle1 the first [Circle]
     * @param transform1 the first [Circle]'s [Transform]
     * @param circle2 the second [Circle]
     * @param transform2 the second [Circle]'s [Transform]
     * @param penetration the [Penetration] object to fill
     * @return boolean
     */
    fun detect(circle1: Circle, transform1: Transform, circle2: Circle, transform2: Transform, penetration: Penetration): Boolean {
        // get their world centers
        val ce1: Vector2 = transform1.getTransformed(circle1.center)
        val ce2: Vector2 = transform2.getTransformed(circle2.center)
        // create a vector from one center to the other
        val v: Vector2 = ce2.subtract(ce1)
        // check the magnitude against the sum of the radii
        val radii: Double = circle1.radius + circle2.radius
        // get the magnitude squared
        val mag: Double = v.magnitudeSquared
        // check difference
        if (mag < radii * radii) {
            // then we have a collision
            penetration.normal = v
            penetration.depth = radii - v.normalize()
            return true
        }
        return false
    }

    /**
     * Fast method for determining a collision between two [Circle]s.
     *
     *
     * Returns true if the given [Circle]s are intersecting.
     * @param circle1 the first [Circle]
     * @param transform1 the first [Circle]'s [Transform]
     * @param circle2 the second [Circle]
     * @param transform2 the second [Circle]'s [Transform]
     * @return boolean true if the two circles intersect
     */
    fun detect(circle1: Circle, transform1: Transform, circle2: Circle, transform2: Transform): Boolean {
        // get their world centers
        val ce1: Vector2 = transform1.getTransformed(circle1.center)
        val ce2: Vector2 = transform2.getTransformed(circle2.center)
        // create a vector from one center to the other
        val v: Vector2 = ce2.subtract(ce1)
        // check the magnitude against the sum of the radii
        val radii: Double = circle1.radius + circle2.radius
        // get the magnitude squared
        val mag: Double = v.magnitudeSquared
        // check difference
        return mag < radii * radii
    }

    /**
     * Fast method for determining the distance between two [Circle]s.
     *
     *
     * Returns true if the given [Circle]s are separated and places the
     * separating vector and distance in the given [Separation] object.
     * @param circle1 the first [Circle]
     * @param transform1 the first [Circle]'s [Transform]
     * @param circle2 the second [Circle]
     * @param transform2 the second [Circle]'s [Transform]
     * @param separation the [Separation] object to fill
     * @return boolean
     */
    fun distance(circle1: Circle, transform1: Transform, circle2: Circle, transform2: Transform, separation: Separation): Boolean {
        // get their world centers
        val ce1: Vector2 = transform1.getTransformed(circle1.center)
        val ce2: Vector2 = transform2.getTransformed(circle2.center)
        // get the radii
        val r1: Double = circle1.radius
        val r2: Double = circle2.radius
        // create a vector from one center to the other
        val v: Vector2 = ce1.to(ce2)
        // check the magnitude against the sum of the radii
        val radii = r1 + r2
        // get the magnitude squared
        val mag: Double = v.magnitudeSquared
        // check difference
        if (mag >= radii * radii) {
            // then the circles are separated
            separation.normal = v
            separation.distance = v.normalize() - radii
            separation.point1 = ce1.add(v.x * r1, v.y * r1)
            separation.point2 = ce2.add(-v.x * r2, -v.y * r2)
            return true
        }
        return false
    }

    /**
     * Performs a ray cast against the given circle.
     * @param ray the [Ray]
     * @param maxLength the maximum ray length
     * @param circle the [Circle]
     * @param transform the [Circle]'s [Transform]
     * @param raycast the [Raycast] result
     * @return boolean true if the ray intersects the circle
     * @since 2.0.0
     */
    fun raycast(ray: Ray, maxLength: Double, circle: Circle, transform: Transform, raycast: Raycast): Boolean {
        // solve the problem algebraically
        val s: Vector2 = ray.start!!
        val d: Vector2 = ray.directionVector
        val ce: Vector2 = transform.getTransformed(circle.center)
        val r: Double = circle.radius

        // make sure the start of the ray is not contained in the circle
        if (circle.contains(s, transform)) return false

        // any point on a ray can be found by the parametric equation:
        // P = tD + S
        // any point on a circle can be found by:
        // (x - h)^2 + (y - k)^2 = r^2 where h and k are the x and y center coordinates
        // substituting the first equation into the second yields a quadratic equation:
        // |D|^2t^2 + 2D.dot(S - C)t + (S - C)^2 - r^2 = 0
        // using the quadratic equation we can solve for t where
        // a = |D|^2
        // b = 2D.dot(S - C)
        // c = (S - C)^2 - r^2
        val sMinusC: Vector2 = s.difference(ce)

        // mag(d)^2
        val a: Double = d.dot(d)
        // 2d.dot(s - c)
        val b: Double = 2 * d.dot(sMinusC)
        // (s - c)^2 - r^2
        val c: Double = sMinusC.dot(sMinusC) - r * r

        // precompute
        val inv2a = 1.0 / (2.0 * a)
        val b24ac = b * b - 4 * a * c
        // check for negative inside the sqrt
        if (b24ac < 0.0) {
            // if the computation inside the sqrt is
            // negative then this indicates that the
            // ray is parallel to the circle
            return false
        }
        val sqrt: Double = sqrt(b24ac)
        // compute the two values of t
        val t0 = (-b + sqrt) * inv2a
        val t1 = (-b - sqrt) * inv2a

        // find the correct t
        // t cannot be negative since this would make the point
        // in the opposite direction of the ray's direction
        var t = 0.0
        // check for negative value
        t = if (t0 < 0.0) {
            // check for negative value
            if (t1 < 0.0) {
                // return the ray does not intersect the circle
                return false
            } else {
                // t1 is the answer
                t1
            }
        } else {
            // check for negative value
            if (t1 < 0.0) {
                // t0 is the answer
                t0
            } else if (t0 < t1) {
                // t0 is the answer
                t0
            } else {
                // t1 is the answer
                t1
            }
        }

        // check the value of t
        if (maxLength > 0.0 && t > maxLength) {
            // if the smallest non-negative t is larger
            // than the maximum length then return false
            return false
        }

        // compute the hit point
        val p: Vector2 = d.product(t).add(s)
        // compute the normal
        val n: Vector2 = ce.to(p)
        n.normalize()

        // populate the raycast result
        raycast.point = p
        raycast.normal = n
        raycast.distance = t

        // return success
        return true
    }
}