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

import org.dyn4j.Epsilon
import org.dyn4j.geometry.*
import org.dyn4j.resources.Messages.getString
import kotlin.math.sqrt


/**
 * Implementation of the Gilbert-Johnson-Keerthi (GJK) algorithm for collision detection.
 *
 *
 * [Gjk] is an algorithm used to find minimum distance from one [Convex] [Shape]
 * to another, but can also be used to determine whether or not they intersect.
 *
 *
 * [Gjk] uses a specific mathematical construct called the [MinkowskiSum].  The
 * [MinkowskiSum] of two [Convex] [Shape]s create another [Convex]
 * [Shape].  If the shapes are labeled A and B, the [MinkowskiSum] is the convex hull
 * of adding every point in A to every point in B.
 *
 *
 * Now, if we subtract every point in A and every point in B, we still end up with a
 * [Convex] [Shape], but we also get another interesting property.  If the two [Convex]
 * [Shape]s are penetrating one another (overlapping) then the [MinkowskiSum], using the difference
 * operator, will contain the origin.
 *
 *
 * Computing the [MinkowskiSum] directly would not be very efficient and performance would be directly linked
 * to the number of vertices each shape contained (n*m subtractions).  In addition, curved shapes have an infinite
 * number of vertices.
 *
 *
 * That said, it's not necessary to compute the [MinkowskiSum]. Instead, to determine whether the origin is
 * contained in the [MinkowskiSum] we iteratively create a [Shape] inside the [MinkowskiSum] that
 * encloses the origin.  This is called the simplex and for 2D it will be a triangle.  If we can enclose the origin
 * using a shape contained within the [MinkowskiSum], then we can conclude that the origin is contained within
 * the [MinkowskiSum], and that the two shapes are penetrating. If we cannot, then the shapes are separated.
 *
 *
 * To create a shape inside the [MinkowskiSum], we use what is called a support function. The support function
 * returns a point on the edge of the [MinkowskiSum] farthest in a given direction.  This can be obtained by taking
 * the farthest point in shape A minus the farthest point in shape B in the opposite direction.
 *
 *
 * If the [MinkowskiSum] is:
 * <pre>
 * A - B
</pre> *
 * then the support function would be:
 * <pre>
 * (farthest point in direction D in A) - (farthest point in direction -D in B)
</pre> *
 * With this we can obtain a point which is on the edge of the [MinkowskiSum] shape in any direction.  Next we
 * iteratively create these points so that we create a shape (triangle in the 2d case) that encloses the origin.
 *
 *
 * Algorithm psuedo-code:
 * <pre>
 * // get a point farthest in the direction
 * // choose some random direction (selection of the initial direction can
 * // determine the speed at which the algorithm terminates)
 * Point p = support(A, B, direction);
 * // add it to the simplex
 * simplex.addPoint(p);
 * // negate the direction
 * direction = -direction;
 * // make sure the point we are about to add is actually past the origin
 * // if its not past the origin then that means we can never enclose the origin
 * // therefore its not in the Minkowski sum and therefore there is no penetration.
 * while (p = support(A, B, direction).dot(direction) &gt; 0) {
 * // if the point is past the origin then add it to the simplex
 * simplex.add(p);
 * // then check to see if the simplex contains the origin
 * // passing back a new search direction if it does not
 * if (check(simplex, direction)) {
 * return true;
 * }
 * }
 * return false;
</pre> *
 * The last method to discuss is the check method.  This method can be implemented in
 * any fashion, however, if the simplex points are stored in a way that we always know what point
 * was added last, many optimizations can be done.  For these optimizations please refer
 * to the source documentation on [Gjk.checkSimplex].
 *
 *
 * Once [Gjk] has found that the two [Collidable]s are penetrating it will exit
 * and hand off the resulting simplex to a [MinkowskiPenetrationSolver] to find the
 * collision depth and normal.
 *
 *
 * [Gjk]'s default [MinkowskiPenetrationSolver] is [Epa].
 *
 *
 * The [Gjk] algorithm's original intent was to find the minimum distance between two [Convex]
 * [Shape]s.  Refer to [Gjk.distance]
 * for details on the implementation.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 * @see Epa
 *
 * @see [GJK
 * @see [GJK - Distance &amp; Closest Points](http://www.dyn4j.org/2010/04/gjk-distance-closest-points/)
](http://www.dyn4j.org/2010/04/gjk-gilbert-johnson-keerthi/) */
class Gjk : NarrowphaseDetector, DistanceDetector, RaycastDetector {

    companion object {
        /** The origin point  */
        private val ORIGIN = Vector2()
        // defaults
        /** The default [Gjk] maximum iterations  */
        val DEFAULT_MAX_ITERATIONS = 30

        /** The default epsilon in meters for collision detection  */
        val DEFAULT_DETECT_EPSILON = 0.0

        /** The default epsilon in meters for distance checks  */
        val DEFAULT_DISTANCE_EPSILON: Double = sqrt(Epsilon.E)

        /** The default epsilon in meters for raycast checks  */
        val DEFAULT_RAYCAST_EPSILON = DEFAULT_DISTANCE_EPSILON
    }

    // members

    // members
    /** The penetration solver; defaults to [Epa]  */
    var minkowskiPenetrationSolver: MinkowskiPenetrationSolver = Epa()

    /** The maximum number of collision detection iterations  */
    var maxDetectIterations: Int = Gjk.DEFAULT_MAX_ITERATIONS
        set(value) {
            if (value < 5) throw IllegalArgumentException(getString("collision.narrowphase.gjk.invalidMaximumIterations"))
            field = value
        }

    /** The maximum number of distance check iterations  */
    var maxDistanceIterations: Int = Gjk.DEFAULT_MAX_ITERATIONS
        set(value) {
            if (value < 5) throw IllegalArgumentException(getString("collision.narrowphase.gjk.invalidMaximumIterations"))
            field = value
        }

    /** The maximum number of raycast iterations  */
    var maxRaycastIterations: Int = Gjk.DEFAULT_MAX_ITERATIONS

    /** The collision detection epsilon in meters   */
    var detectEpsilon: Double = Gjk.DEFAULT_DETECT_EPSILON

    /** The distance check epsilon in meters  */
    var distanceEpsilon: Double = Gjk.DEFAULT_DISTANCE_EPSILON

    /** The raycast check epsilon in meters  */
    var raycastEpsilon: Double = Gjk.DEFAULT_DISTANCE_EPSILON

    /**
     * Default constructor.
     */
    fun Gjk() {}

    /**
     * Optional constructor.
     * @param minkowskiPenetrationSolver the [MinkowskiPenetrationSolver] to use
     * @throws NullPointerException if minkowskiPenetrationSolver is null
     */
    fun Gjk(minkowskiPenetrationSolver: MinkowskiPenetrationSolver?) {
        if (minkowskiPenetrationSolver == null) throw NullPointerException(getString("collision.narrowphase.gjk.nullMinkowskiPenetrationSolver"))
        this.minkowskiPenetrationSolver = minkowskiPenetrationSolver
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.narrowphase.NarrowphaseDetector#detect(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.collision.narrowphase.Penetration)
	 */
    override fun detect(
        convex1: Convex, transform1: Transform, convex2: Convex,
        transform2: Transform, penetration: Penetration?
    ): Boolean {
        // check for circles
        if (convex1 is Circle && convex2 is Circle) {
            // if its a circle - circle collision use the faster method
            return CircleDetector.detect(
                convex1,
                transform1,
                convex2,
                transform2,
                penetration!!
            )
        }

        // define the simplex
        val simplex: MutableList<Vector2> = ArrayList(3)

        // create a Minkowski sum
        val ms = MinkowskiSum(convex1, transform1, convex2, transform2)

        // choose some search direction
        val d = getInitialDirection(convex1, transform1, convex2, transform2)

        // perform the detection
        if (this.detect(ms, simplex, d)) {
            minkowskiPenetrationSolver.getPenetration(simplex, ms, penetration!!)
            return true
        }
        return false
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.narrowphase.NarrowphaseDetector#detect(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform)
	 */
    override fun detect(
        convex1: Convex,
        transform1: Transform,
        convex2: Convex,
        transform2: Transform
    ): Boolean {
        // check for circles
        if (convex1 is Circle && convex2 is Circle) {
            // if its a circle - circle collision use the faster method
            return CircleDetector.detect(
                convex1,
                transform1,
                convex2,
                transform2
            )
        }

        // define the simplex
        val simplex: MutableList<Vector2> =
            ArrayList(3)

        // create a Minkowski sum
        val ms = MinkowskiSum(convex1, transform1, convex2, transform2)

        // choose some search direction
        val d = getInitialDirection(convex1, transform1, convex2, transform2)

        // perform the detection
        return detect(ms, simplex, d)
    }

    /**
     * Returns a vector for the initial direction for the GJK algorithm in world coordinates.
     *
     *
     * This implementation returns the vector from the center of the first convex to the center of the second.
     * @param convex1 the first convex
     * @param transform1 the first convex's transform
     * @param convex2 the second convex
     * @param transform2 the second convex's transform
     * @return Vector2
     */
    protected fun getInitialDirection(
        convex1: Convex,
        transform1: Transform,
        convex2: Convex,
        transform2: Transform
    ): Vector2 {
        // transform into world space if transform is not null
        val c1 = transform1.getTransformed(convex1.center)
        val c2 = transform2.getTransformed(convex2.center)
        // choose some search direction
        return c2.subtract(c1)
    }

    /**
     * The main [Gjk] algorithm loop.
     *
     *
     * Returns true if a collision was detected and false otherwise.
     *
     *
     * The simplex and direction parameters will reflect the state of the algorithm at termination, whether
     * a collision was found or not.  This is useful for subsequent algorithms that use the GJK simplex to
     * find the collision information ([Epa] for example).
     * @param ms the [MinkowskiSum]
     * @param simplex the simplex; should be an empty list
     * @param d the initial direction
     * @return boolean
     */
    protected fun detect(
        ms: MinkowskiSum,
        simplex: MutableList<Vector2>,
        d: Vector2
    ): Boolean {
        // check for a zero direction vector
        if (d.isZero) d[1.0] = 0.0
        // add the first point
        simplex.add(ms.getSupportPoint(d))
        // is the support point past the origin along d?
        if (simplex[0].dot(d) <= 0.0) {
            return false
        }
        // negate the search direction
        d.negate()
        // start the loop
        for (i in 0 until maxDetectIterations) {
            // always add another point to the simplex at the beginning of the loop
            val supportPoint = ms.getSupportPoint(d)
            simplex.add(supportPoint)

            // make sure that the last point we added was past the origin
            if (supportPoint.dot(d) <= detectEpsilon) {
                // a is not past the origin so therefore the shapes do not intersect
                // here we treat the origin on the line as no intersection
                // immediately return with null indicating no penetration
                return false
            } else {
                // if it is past the origin, then test whether the simplex contains the origin
                if (checkSimplex(simplex, d)) {
                    // if the simplex contains the origin then we know that there is an intersection.
                    // if we broke out of the loop then we know there was an intersection
                    return true
                }
                // if the simplex does not contain the origin then we need to loop using the new
                // search direction and simplex
            }
        }
        return false
    }

    /**
     * Determines whether the given simplex contains the origin.  If it does contain the origin,
     * then this method will return true.  If it does not, this method will update both the given
     * simplex and also the given search direction.
     *
     *
     * This method only handles the line segment and triangle simplex cases, however, these two cases
     * should be the only ones needed for 2 dimensional [Gjk].  The single point case is handled
     * in [.detect].
     *
     *
     * This method also assumes that the last point in the simplex is the most recently added point.
     * This matters because optimizations are available when you know this information.
     * @param simplex the simplex
     * @param direction the search direction
     * @return boolean true if the simplex contains the origin
     */
    protected fun checkSimplex(simplex: MutableList<Vector2>, direction: Vector2): Boolean {
        // this method should never be supplied anything other than 2 or 3 points for the simplex
        // get the last point added (a)
        val a = simplex[simplex.size - 1]
        // this is the same as a.to(ORIGIN);
        val ao = a.negative
        // check to see what type of simplex we have
        if (simplex.size == 3) {
            // then we have a triangle
            val b = simplex[1]
            val c = simplex[0]
            // get the edges
            val ab = a.to(b)
            val ac = a.to(c)
            // get the edge normal

            // inline Vector2.tripleProduct(ab, ac, ac) so we can use the
            // immidiate calculations for Vector2.tripleProduct(ac, ab, ab) too
            val acPerp = Vector2()
            val dot = ab.x * ac.y - ac.x * ab.y
            acPerp.x = -ac.y * dot
            acPerp.y = ac.x * dot

            // see where the origin is at
            val acLocation = acPerp.dot(ao)
            if (acLocation >= 0.0) {
                // the origin lies on the right side of A->C
                // because of the condition for the gjk loop to continue the origin 
                // must lie between A and C so remove B and set the
                // new search direction to A->C perpendicular vector
                simplex.removeAt(1)
                // this used to be direction.set(Vector.tripleProduct(ac, ao, ac));
                // but was changed since the origin may lie on the segment created
                // by a -> c in which case would produce a zero vector normal
                // calculating ac's normal using b is more robust
                direction.set(acPerp)
            } else {
                // inlined Vector2.tripleProduct(ac, ab, ab) because
                // it can use dot from the tripleProduct(ab, ab, ac) above
                // see Vector2.tripleProduct implementation
                val abPerp = Vector2()
                abPerp.x = ab.y * dot
                abPerp.y = -ab.x * dot
                val abLocation = abPerp.dot(ao)
                // the origin lies on the left side of A->C
                if (abLocation < 0.0) {
                    // the origin lies on the right side of A->B and therefore in the
                    // triangle, we have an intersection
                    return true
                } else {
                    // the origin lies between A and B so remove C and set the
                    // search direction to A->B perpendicular vector
                    simplex.removeAt(0)
                    // this used to be direction.set(Vector.tripleProduct(ab, ao, ab));
                    // but was changed since the origin may lie on the segment created
                    // by a -> b in which case would produce a zero vector normal
                    // calculating ab's normal using c is more robust
                    direction.set(abPerp)
                }
            }
        } else {
            // get the b point
            val b = simplex[0]
            val ab = a.to(b)
            // otherwise we have 2 points (line segment)
            // because of the condition for the gjk loop to continue the origin 
            // must lie in between A and B, so keep both points in the simplex and
            // set the direction to the perp of the line segment towards the origin
            direction.set(Vector2.tripleProduct(ab, ao, ab))
            // check for degenerate cases where the origin lies on the segment
            // created by a -> b which will yield a zero edge normal
            if (direction.magnitudeSquared <= Epsilon.E) {
                // in this case just choose either normal (left or right)
                direction.set(ab.left())
            }
        }
        return false
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.narrowphase.DistanceDetector#distance(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.collision.narrowphase.Separation)
	 */
    override fun distance(
        convex1: Convex,
        transform1: Transform,
        convex2: Convex,
        transform2: Transform,
        separation: Separation
    ): Boolean {
        // check for circles
        if (convex1 is Circle && convex2 is Circle) {
            // if its a circle - circle collision use the faster method
            return CircleDetector.distance(
                convex1,
                transform1,
                convex2,
                transform2,
                separation
            )
        }
        // create a Minkowski sum
        val ms = MinkowskiSum(convex1, transform1, convex2, transform2)
        // define some Minkowski points
        var a: MinkowskiSumPoint? = null
        var b: MinkowskiSumPoint? = null
        var c: MinkowskiSumPoint? = null
        // transform into world space if transform is not null
        val c1 = transform1.getTransformed(convex1.center)
        val c2 = transform2.getTransformed(convex2.center)
        // choose some search direction
        var d = c1.to(c2)
        // check for a zero direction vector
        // a zero direction vector indicates that the center's are coincident
        // which guarantees that the convex shapes are overlapping
        if (d.isZero) return false
        // add the first point 
        a = ms.getSupportPoints(d)
        // negate the direction
        d.negate()
        // get a second support point
        b = ms.getSupportPoints(d)
        // find the point on the simplex (segment) closest to the origin
        // and use that as the new search direction
        d = Segment.getPointOnSegmentClosestToPoint(ORIGIN, b.point, a.point)
        for (i in 0 until maxDistanceIterations) {
            // the vector from the point we found to the origin is the new search direction
            d.negate()
            // check if d is zero
            if (d.magnitudeSquared <= Epsilon.E) {
                // if the closest point is the origin then the shapes are not separated
                return false
            }

            // get the farthest point along d
            c = ms.getSupportPoints(d)

            // test if the triangle made by a, b, and c contains the origin
            if (containsOrigin(a!!.point, b!!.point, c.point)) {
                // if it does then return false;
                return false
            }

            // see if the new point is far enough along d
            val projection = c.point.dot(d)
            if (projection - a.point.dot(d) < distanceEpsilon) {
                // then the new point we just made is not far enough
                // in the direction of n so we can stop now
                // normalize d
                d.normalize()
                separation.normal = d
                // compute the real distance
                separation.distance = -c.point.dot(d)
                // get the closest points
                findClosestPoints(a, b, separation)
                // return true to indicate separation
                return true
            }

            // get the closest point on each segment to the origin
            val p1: Vector2 = Segment.getPointOnSegmentClosestToPoint(ORIGIN, a.point, c.point)
            val p2: Vector2 = Segment.getPointOnSegmentClosestToPoint(ORIGIN, c.point, b.point)


            // get the distance to the origin
            val p1Mag = p1.magnitudeSquared
            val p2Mag = p2.magnitudeSquared

            // check if the origin lies close enough to either edge
            if (p1Mag <= Epsilon.E) {
                // if so then we have a separation (although its
                // nearly zero separation)
                d.normalize()
                separation.distance = p1.normalize()
                separation.normal = d
                findClosestPoints(a, c, separation)
                return true
            } else if (p2Mag <= Epsilon.E) {
                // if so then we have a separation (although its
                // nearly zero separation)
                d.normalize()
                separation.distance = p2.normalize()
                separation.normal = d
                findClosestPoints(c, b, separation)
                return true
            }

            // test which point is closer and replace the one that is farthest
            // with the new point c and set the new search direction
            if (p1Mag < p2Mag) {
                // a was closest so replace b with c
                b = c
                d = p1
            } else {
                // b was closest so replace a with c
                a = c
                d = p2
            }
        }
        // if we made it here then we know that we hit the maximum number of iterations
        // this is really a catch all termination case
        d.normalize()
        separation.normal = d
        separation.distance = -c!!.point.dot(d)
        // get the closest points
        findClosestPoints(a, b, separation)
        // return true to indicate separation
        return true
    }

    /**
     * Finds the closest points on A and B given the termination simplex and places
     * them into point1 and point2 of the given [Separation] object.
     *
     *
     * The support points used to obtain a and b are not good enough since the support
     * methods of [Convex] [Shape]s only return the farthest *vertex*, not
     * necessarily the farthest point.
     * @param a the first simplex point
     * @param b the second simplex point
     * @param separation the [Separation] object to populate
     * @see [GJK - Distance &amp; Closest Points](http://www.dyn4j.org/2010/04/gjk-distance-closest-points/)
     */
    protected fun findClosestPoints(
        a: MinkowskiSumPoint?,
        b: MinkowskiSumPoint?,
        separation: Separation
    ) {
        val p1 = Vector2()
        val p2 = Vector2()

        // find lambda1 and lambda2
        val l = a!!.point.to(b!!.point)

        // check if a and b are the same point
        if (l.isZero) {
            // then the closest points are a or b support points
            p1.set(a.supportPoint1)
            p2.set(a.supportPoint2)
        } else {
            // otherwise compute lambda1 and lambda2
            val ll = l.dot(l)
            val l2 = -l.dot(a.point) / ll

            // check if either lambda1 or lambda2 is less than zero
            if (l2 > 1) {
                // if lambda1 is less than zero then that means that
                // the support points of the Minkowski point B are
                // the closest points
                p1.set(b.supportPoint1)
                p2.set(b.supportPoint2)
            } else if (l2 < 0) {
                // if lambda2 is less than zero then that means that
                // the support points of the Minkowski point A are
                // the closest points
                p1.set(a.supportPoint1)
                p2.set(a.supportPoint2)
            } else {
                // compute the closest points using lambda1 and lambda2
                // this is the expanded version of
                // p1 = a.p1.multiply(1 - l2).add(b.p1.multiply(l2));
                // p2 = a.p2.multiply(1 - l2).add(b.p2.multiply(l2));
                p1.x = a.supportPoint1.x + l2 * (b.supportPoint1.x - a.supportPoint1.x)
                p1.y = a.supportPoint1.y + l2 * (b.supportPoint1.y - a.supportPoint1.y)
                p2.x = a.supportPoint2.x + l2 * (b.supportPoint2.x - a.supportPoint2.x)
                p2.y = a.supportPoint2.y + l2 * (b.supportPoint2.y - a.supportPoint2.y)
            }
        }
        // set the new points in the separation object
        separation.point1 = p1
        separation.point2 = p2
    }

    /**
     * Returns true if the origin is within the triangle given by
     * a, b, and c.
     *
     *
     * If the origin lies on the same side of all the points then we
     * know that the origin is in the triangle.
     * <pre> sign(location(origin, a, b)) == sign(location(origin, b, c)) == sign(location(origin, c, a))</pre>
     * The [Segment.getLocation] method
     * can be simplified because we are using the origin as the search point:
     * <pre> = (b.x - a.x) * (origin.y - a.y) - (origin.x - a.x) * (b.y - a.y)
     * = (b.x - a.x) * (-a.y) - (-a.x) * (b.y - a.y)
     * = -a.y * b.x + a.y * a.x + a.x * b.y - a.x * a.y
     * = -a.y * b.x + a.x * b.y
     * = a.x * b.y - a.y * b.x
     * = a.cross(b)</pre>
     * @param a the first point
     * @param b the second point
     * @param c the third point
     * @return boolean
     */
    protected fun containsOrigin(
        a: Vector2,
        b: Vector2,
        c: Vector2
    ): Boolean {
        val sa = a.cross(b)
        val sb = b.cross(c)
        val sc = c.cross(a)
        // this is sufficient (we do not need to test sb * sc)
        return sa * sb > 0 && sa * sc > 0
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.narrowphase.RaycastDetector#raycast(org.dyn4j.geometry.Ray, double, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.collision.narrowphase.Raycast)
	 */
    override fun raycast(
        ray: Ray,
        maxLength: Double,
        convex: Convex,
        transform: Transform,
        raycast: Raycast
    ): Boolean {
        // check for circle
        if (convex is Circle) {
            // if the convex is a circle then use the more efficient method
            return CircleDetector.raycast(ray, maxLength, convex, transform, raycast)
        }
        // check for segment
        if (convex is Segment) {
            // if the convex is a segment then use the more efficient method
            return SegmentDetector.raycast(ray, maxLength, convex, transform, raycast)
        }

        // otherwise proceed with GJK raycast
        var lambda = 0.0

        // do we need to check against the max length?
        val lengthCheck = maxLength > 0

        // create the holders for the simplex
        var a: Vector2? = null
        var b: Vector2? = null

        // get the start point of the ray
        val start = ray.start!!
        // x is the current closest point on the ray
        var x = start
        // r is the ray direction
        val r = ray.directionVector
        // n is the normal at the hit point
        val n = Vector2()

        // is the start point contained in the convex?
        if (convex.contains(start, transform)) {
            // return false if the start of the ray is inside the convex
            return false
        }

        // get an arbitrary point within the convex shape
        // we can use the center point
        val c = transform.getTransformed(convex.center)
        // the center to the start point
        var d = c.to(x)

        // define an epsilon to compare the distance with
        var distanceSqrd = Double.MAX_VALUE
        var iterations = 0
        // loop until we have found the correct distance
        while (distanceSqrd > raycastEpsilon) {
            // get a point on the edge of the convex in the direction of d
            val p = convex.getFarthestPoint(d, transform)
            // get the vector from the current closest point to the edge point
            val w = p.to(x)
            // is the current point on the ray to the new point
            // in the same direction as d?
            val dDotW = d.dot(w)
            if (dDotW > 0.0) {
                // is the ray direction in the same direction as d?
                val dDotR = d.dot(r)
                if (dDotR >= 0.0) {
                    // immediately return false since this indicates that the
                    // ray is moving in the opposite direction
                    return false
                } else {
                    // otherwise compute the new closest point on the
                    // ray to the edge point
                    lambda = lambda - dDotW / dDotR
                    // check if l is larger than the length
                    if (lengthCheck && lambda > maxLength) {
                        // then return false
                        return false
                    }
                    x = r.product(lambda).add(start)
                    // set d as the best normal we have so far
                    // d will be normalized when the loop terminates
                    n.set(d)
                }
            }
            // now reduce the simplex to two points such that we keep the
            // two points that form a segment that is closest to x
            if (a != null) {
                if (b != null) {
                    // reduce the set to two points
                    // get the closest point on each segment to the origin
                    val p1: Vector2 = Segment.getPointOnSegmentClosestToPoint(x, a, p)
                    val p2: Vector2 = Segment.getPointOnSegmentClosestToPoint(x, p, b)
                    // test which point is closer and replace the one that is farthest
                    // with the new point p and set the new search direction
                    distanceSqrd = if (p1.distanceSquared(x) < p2.distanceSquared(x)) {
                        // a was closest so replace b with p
                        b.set(p)
                        // update the distance
                        p1.distanceSquared(x)
                    } else {
                        // b was closest so replace a with p
                        a.set(p)
                        // update the distance
                        p2.distanceSquared(x)
                    }
                    // get the new search direction
                    val ab: Vector2 = a.to(b)
                    val ax: Vector2 = a.to(x)
                    d = Vector2.tripleProduct(ab, ax, ab)
                } else {
                    // b is null so just set b
                    b = p
                    // get the new search direction
                    val ab: Vector2 = a.to(b)
                    val ax: Vector2 = a.to(x)
                    d = Vector2.tripleProduct(ab, ax, ab)
                }
            } else {
                // both a and b are null so just set a and use -d as the
                // new direction
                a = p
                d.negate()
            }

            // check for the maximum number of iterations
            if (iterations == maxRaycastIterations) {
                // we have hit the maximum number of iterations and
                // still are not close enough to the ray, in this case
                // just exit returning false
                return false
            }

            // increment the number of iterations
            iterations++
        }

        // set the raycast result values
        raycast.point = x
        raycast.normal = n
        n.normalize()
        raycast.distance = lambda

        // return true to indicate that we were successful
        return true
    }

}