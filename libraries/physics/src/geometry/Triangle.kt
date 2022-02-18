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

/**
 * Implementation of a Triangle [Convex] [Shape].
 *
 *
 * A [Triangle] must have one vertex which is not colinear with the other two.
 *
 *
 * This class is provided to enhance performance of some of the methods contained in
 * the [Convex] and [Shape] interfaces.
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 */
class Triangle : Polygon, Convex, Wound, Shape, Transformable, DataContainer {

    constructor(point1: Vector2, point2: Vector2, point3: Vector2) :
            super(point1, point2, point3)

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Wound#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Triangle[").append(super.toString()).append("]")
        return sb.toString()
    }

    /**
     * Returns true if the point is inside the [Triangle].
     *
     *
     * The equation of a plane is:
     *
     *  N  (P - A) = 0
     * Where A is any point on the plane. <br></br>
     * Create two axes ([Vector2]s), we will choose V<sub>ab</sub> and V<sub>ac</sub>.
     *
     *  V<sub>ac</sub> = C - A
     * V<sub>ab</sub> = B - A
     * Where A, B, and C are the
     * of the [Triangle].<br></br>
     * From this we can say that you can get to any point on the
     * plane by going some u distance on V<sub>ac</sub> and some v distance on V<sub>ab</sub>
     * where A is the origin.
     *
     *  P = A + u * V<sub>ac</sub> + v * V<sub>ab</sub>
     * Simplifing P - A
     *
     *  V<sub>pa</sub> = u * V<sub>ac</sub> + v * V<sub>ab</sub>
     * We still need another equation to solve for u and v:<br></br>
     * Dot the equation by V<sub>ac</sub> to get
     *
     *  V<sub>pa</sub>  V<sub>ac</sub> = (u * V<sub>ac</sub> + v * V<sub>ab</sub>)  V<sub>ac</sub>
     * Dot the equation by V<sub>ab</sub> to get the other
     *
     *  V<sub>pa</sub>  V<sub>ab</sub> = (u * V<sub>ac</sub> + v * V<sub>ab</sub>)  V<sub>ab</sub>
     * Distribute out both equations
     *
     *  V<sub>pa</sub>  V<sub>ac</sub> = u * V<sub>ac</sub>  V<sub>ac</sub> + v * V<sub>ab</sub>  V<sub>ac</sub>
     * V<sub>pa</sub>  V<sub>ab</sub> = u * V<sub>ac</sub>  V<sub>ab</sub> + v * V<sub>ab</sub>  V<sub>ab</sub>
     * Solving the first equation for u:
     *
     *  u = (V<sub>pa</sub>  V<sub>ac</sub> - v * V<sub>ab</sub>  V<sub>ac</sub>) / (V<sub>ac</sub>  V<sub>ac</sub>)
     * Substitute one into the other:
     *
     *  V<sub>pa</sub>  V<sub>ab</sub> = (V<sub>pa</sub>  V<sub>ac</sub> - v * V<sub>ab</sub>  V<sub>ac</sub>) / (V<sub>ac</sub>  V<sub>ac</sub>) * V<sub>ac</sub>  V<sub>ab</sub> + v * V<sub>ab</sub>  V<sub>ab</sub>
     * V<sub>pa</sub>  V<sub>ab</sub> = (V<sub>pa</sub>  V<sub>ac</sub> / V<sub>ac</sub>  V<sub>ac</sub>) * V<sub>ac</sub>  V<sub>ab</sub> - v * (V<sub>ab</sub>  V<sub>ac</sub> / V<sub>ac</sub>  V<sub>ac</sub>) * V<sub>ac</sub>  V<sub>ab</sub> + v * V<sub>ab</sub>  V<sub>ab</sub>
     * V<sub>pa</sub>  V<sub>ab</sub> = (V<sub>pa</sub>  V<sub>ac</sub> / V<sub>ac</sub>  V<sub>ac</sub>) * V<sub>ac</sub>  V<sub>ab</sub> + v * (V<sub>ab</sub>  V<sub>ab</sub> - (V<sub>ab</sub>  V<sub>ac</sub> / V<sub>ac</sub>  V<sub>ac</sub>) * V<sub>ac</sub>  V<sub>ab</sub>)
     * v = (V<sub>pa</sub>  V<sub>ab</sub> - (V<sub>pa</sub>  V<sub>ac</sub> / V<sub>ac</sub>  V<sub>ac</sub>) * V<sub>ac</sub>  V<sub>ab</sub>) / (V<sub>ab</sub>  V<sub>ab</sub> - (V<sub>ab</sub>  V<sub>ac</sub> / V<sub>ac</sub>  V<sub>ac</sub>) * V<sub>ac</sub>  V<sub>ab</sub>)
     * Which reduces to:
     *
     *  v = ((V<sub>pa</sub>  V<sub>ab</sub>) * (V<sub>ac</sub>  V<sub>ac</sub>) - (V<sub>pa</sub>  V<sub>ac</sub>) * (V<sub>ac</sub>  V<sub>ab</sub>)) / ((V<sub>ab</sub>  V<sub>ab</sub>) * (V<sub>ac</sub>  V<sub>ac</sub>) - (V<sub>ab</sub>  V<sub>ac</sub>) * (V<sub>ac</sub>  V<sub>ab</sub>))
     * Once v is obtained use either equation to obtain u:
     *
     *  u = (v * V<sub>ab</sub>  V<sub>ab</sub> - V<sub>pa</sub>  V<sub>ab</sub>) / V<sub>ac</sub>  V<sub>ab</sub>
     * We know that the point is inside the [Triangle] if u and v are greater than
     * zero and u + v is less than one.
     * @param point world space point
     * @param transform [Transform] the [Shape]'s transform
     * @return boolean
     */
    override fun contains(point: Vector2, transform: Transform): Boolean {
        val u: Double
        val v: Double
        // put the point in local coordinates
        val p = transform.getInverseTransformed(point)
        // get the vertices
        val p1: Vector2 = this.vertices.get(0)
        val p2: Vector2 = vertices.get(1)
        val p3: Vector2 = vertices.get(2)
        // create a vector representing edge ab
        val ab = p1.to(p2)
        // create a vector representing edge ac
        val ac = p1.to(p3)
        // create a vector from a to the point
        val pa = p1.to(p)
        val dot00 = ac.dot(ac)
        val dot01 = ac.dot(ab)
        val dot02 = ac.dot(pa)
        val dot11 = ab.dot(ab)
        val dot12 = ab.dot(pa)
        val denominator = dot00 * dot11 - dot01 * dot01
        val invD = 1.0 / denominator
        u = (dot11 * dot02 - dot01 * dot12) * invD

        // don't bother going any farther if u is less than zero
        if (u <= 0) return false
        v = (dot00 * dot12 - dot01 * dot02) * invD
        return  /*u > 0 && */v > 0 && u + v <= 1
    }

}
