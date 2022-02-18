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
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * This class represents a vector or point in 3D space.
 *
 *
 * Used to solve 3x3 systems of equations.
 * @see Vector2
 *
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 */
// todo use `featurea.math.Vector3`
class Vector3 {
    /** The magnitude of the x component of this [Vector3]  */
    @JvmField
    var x = 0.0

    /** The magnitude of the y component of this [Vector3]  */
    @JvmField
    var y = 0.0

    /** The magnitude of the z component of this [Vector3]  */
    @JvmField
    var z = 0.0

    /** Default constructor.  */
    constructor() {}

    /**
     * Copy constructor.
     * @param vector the [Vector3] to copy from
     */
    constructor(vector: Vector3) {
        x = vector.x
        y = vector.y
        z = vector.z
    }

    /**
     * Optional constructor.
     * @param x the x component
     * @param y the y component
     * @param z the z component
     */
    constructor(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Creates a [Vector3] from the first point to the second point.
     * @param x1 the x coordinate of the first point
     * @param y1 the y coordinate of the first point
     * @param z1 the z coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y2 the y coordinate of the second point
     * @param z2 the z coordinate of the second point
     */
    constructor(
        x1: Double,
        y1: Double,
        z1: Double,
        x2: Double,
        y2: Double,
        z2: Double
    ) {
        x = x2 - x1
        y = y2 - y1
        z = z2 - z1
    }

    /**
     * Creates a [Vector3] from the first point to the second point.
     * @param p1 the first point
     * @param p2 the second point
     */
    constructor(p1: Vector3, p2: Vector3) {
        x = p2.x - p1.x
        y = p2.y - p1.y
        z = p2.z - p1.z
    }

    /**
     * Returns a copy of this [Vector3].
     * @return [Vector3]
     */
    fun copy(): Vector3 {
        return Vector3(x, y, z)
    }

    /**
     * Returns the distance from this point to the given point.
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param z the z coordinate of the point
     * @return double
     */
    fun distance(x: Double, y: Double, z: Double): Double {
        val xd = this.x - x
        val yd = this.y - y
        val zd = this.z - z
        return sqrt(xd * xd + yd * yd + zd * zd)
    }

    /**
     * Returns the distance from this point to the given point.
     * @param point the point
     * @return double
     */
    fun distance(point: Vector3): Double {
        val xd = x - point.x
        val yd = y - point.y
        val zd = z - point.z
        return sqrt(xd * xd + yd * yd + zd * zd)
    }

    /**
     * Returns the distance from this point to the given point squared.
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param z the z coordinate of the point
     * @return double
     */
    fun distanceSquared(x: Double, y: Double, z: Double): Double {
        val xd = this.x - x
        val yd = this.y - y
        val zd = this.z - z
        return xd * xd + yd * yd + zd * zd
    }

    /**
     * Returns the distance from this point to the given point squared.
     * @param point the point
     * @return double
     */
    fun distanceSquared(point: Vector3): Double {
        val xd = x - point.x
        val yd = y - point.y
        val zd = z - point.z
        return xd * xd + yd * yd + zd * zd
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        var temp: Long
        temp = x.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = y.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = z.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj === this) return true
        if (obj is Vector3) {
            val other = obj
            if (x == other.x && y == other.y && z == other.z
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Returns true if the x and y components of this [Vector3]
     * are the same as the given [Vector3].
     * @param vector the [Vector3] to compare to
     * @return boolean
     */
    fun equals(vector: Vector3?): Boolean {
        if (vector == null) return false
        return if (this === vector) {
            true
        } else {
            x == vector.x && y == vector.y && z == vector.z
        }
    }

    /**
     * Returns true if the x, y and z components of this [Vector3]
     * are the same as the given x, y and z components.
     * @param x the x coordinate of the [Vector3] to compare to
     * @param y the y coordinate of the [Vector3] to compare to
     * @param z the z coordinate of the [Vector3] to compare to
     * @return boolean
     */
    fun equals(x: Double, y: Double, z: Double): Boolean {
        return this.x == x && this.y == y && this.z == z
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("(")
            .append(x)
            .append(", ")
            .append(y)
            .append(", ")
            .append(z)
            .append(")")
        return sb.toString()
    }

    /**
     * Sets this [Vector3] to the given [Vector3].
     * @param vector the [Vector3] to set this [Vector3] to
     * @return [Vector3] this vector
     */
    fun set(vector: Vector3): Vector3 {
        x = vector.x
        y = vector.y
        z = vector.z
        return this
    }

    /**
     * Sets this [Vector3] to the given [Vector3].
     * @param x the x component of the [Vector3] to set this [Vector3] to
     * @param y the y component of the [Vector3] to set this [Vector3] to
     * @param z the z component of the [Vector3] to set this [Vector3] to
     * @return [Vector3] this vector
     */
    operator fun set(x: Double, y: Double, z: Double): Vector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    /**
     * Returns the x component of this [Vector3].
     * @return [Vector3]
     */
    val xComponent: Vector3
        get() = Vector3(x, 0.0, 0.0)

    /**
     * Returns the y component of this [Vector3].
     * @return [Vector3]
     */
    val yComponent: Vector3
        get() = Vector3(0.0, y, 0.0)

    /**
     * Returns the z component of this [Vector3].
     * @return [Vector3]
     */
    val zComponent: Vector3
        get() = Vector3(0.0, 0.0, z)// the magnitude is just the pathagorean theorem

    /**
     * Returns the magnitude of this [Vector3].
     * @return double
     */
    val magnitude: Double
        get() =// the magnitude is just the pathagorean theorem
            sqrt(x * x + y * y + z * z)

    /**
     * Returns the magnitude of this [Vector3] squared.
     * @return double
     */
    val magnitudeSquared: Double
        get() = x * x + y * y + z * z

    /**
     * Sets the magnitude of the [Vector3].
     * @param magnitude  the magnitude
     * @return [Vector3] this vector
     */
    fun setMagnitude(magnitude: Double): Vector3 {
        // check the given magnitude
        if (abs(magnitude) <= Epsilon.E) {
            x = 0.0
            y = 0.0
            z = 0.0
            return this
        }
        // is this vector a zero vector?
        if (isZero) {
            return this
        }
        // get the magnitude
        var mag: Double = sqrt(x * x + y * y + z * z)
        // normalize and multiply by the new magnitude
        mag = magnitude / mag
        x *= mag
        y *= mag
        z *= mag
        return this
    }

    /**
     * Adds the given [Vector3] to this [Vector3].
     * @param vector the [Vector3]
     * @return [Vector3] this vector
     */
    fun add(vector: Vector3): Vector3 {
        x += vector.x
        y += vector.y
        z += vector.z
        return this
    }

    /**
     * Adds the given [Vector3] to this [Vector3].
     * @param x the x component of the [Vector3]
     * @param y the y component of the [Vector3]
     * @param z the z component of the [Vector3]
     * @return [Vector3] this vector
     */
    fun add(x: Double, y: Double, z: Double): Vector3 {
        this.x += x
        this.y += y
        this.z += z
        return this
    }

    /**
     * Adds this [Vector3] and the given [Vector3] returning
     * a new [Vector3] containing the result.
     * @param vector the [Vector3]
     * @return [Vector3]
     */
    fun sum(vector: Vector3): Vector3 {
        return Vector3(x + vector.x, y + vector.y, z + vector.z)
    }

    /**
     * Adds this [Vector3] and the given [Vector3] returning
     * a new [Vector3] containing the result.
     * @param x the x component of the [Vector3]
     * @param y the y component of the [Vector3]
     * @param z the z component of the [Vector3]
     * @return [Vector3]
     */
    fun sum(x: Double, y: Double, z: Double): Vector3 {
        return Vector3(this.x + x, this.y + y, this.z + z)
    }

    /**
     * Subtracts the given [Vector3] from this [Vector3].
     * @param vector the [Vector3]
     * @return [Vector3] this vector
     */
    fun subtract(vector: Vector3): Vector3 {
        x -= vector.x
        y -= vector.y
        z -= vector.z
        return this
    }

    /**
     * Subtracts the given [Vector3] from this [Vector3].
     * @param x the x component of the [Vector3]
     * @param y the y component of the [Vector3]
     * @param z the z component of the [Vector3]
     * @return [Vector3] this vector
     */
    fun subtract(x: Double, y: Double, z: Double): Vector3 {
        this.x -= x
        this.y -= y
        this.z -= z
        return this
    }

    /**
     * Subtracts the given [Vector3] from this [Vector3] returning
     * a new [Vector3] containing the result.
     * @param vector the [Vector3]
     * @return [Vector3]
     */
    fun difference(vector: Vector3): Vector3 {
        return Vector3(x - vector.x, y - vector.y, z - vector.z)
    }

    /**
     * Subtracts the given [Vector3] from this [Vector3] returning
     * a new [Vector3] containing the result.
     * @param x the x component of the [Vector3]
     * @param y the y component of the [Vector3]
     * @param z the z component of the [Vector3]
     * @return [Vector3]
     */
    fun difference(x: Double, y: Double, z: Double): Vector3 {
        return Vector3(this.x - x, this.y - y, this.z - z)
    }

    /**
     * Creates a [Vector3] from this [Vector3] to the given [Vector3].
     * @param vector the [Vector3]
     * @return [Vector3]
     */
    fun to(vector: Vector3): Vector3 {
        return Vector3(vector.x - x, vector.y - y, vector.z - z)
    }

    /**
     * Creates a [Vector3] from this [Vector3] to the given [Vector3].
     * @param x the x component of the [Vector3]
     * @param y the y component of the [Vector3]
     * @param z the z component of the [Vector3]
     * @return [Vector3]
     */
    fun to(x: Double, y: Double, z: Double): Vector3 {
        return Vector3(x - this.x, y - this.y, z - this.z)
    }

    /**
     * Multiplies this [Vector3] by the given scalar.
     * @param scalar the scalar
     * @return [Vector3] this vector
     */
    fun multiply(scalar: Double): Vector3 {
        x *= scalar
        y *= scalar
        z *= scalar
        return this
    }

    /**
     * Multiplies this [Vector3] by the given scalar returning
     * a new [Vector3] containing the result.
     * @param scalar the scalar
     * @return [Vector3]
     */
    fun product(scalar: Double): Vector3 {
        return Vector3(x * scalar, y * scalar, z * scalar)
    }

    /**
     * Returns the dot product of the given [Vector3]
     * and this [Vector3].
     * @param vector the [Vector3]
     * @return double
     */
    fun dot(vector: Vector3): Double {
        return x * vector.x + y * vector.y + z * vector.z
    }

    /**
     * Returns the dot product of the given [Vector3]
     * and this [Vector3].
     * @param x the x component of the [Vector3]
     * @param y the y component of the [Vector3]
     * @param z the z component of the [Vector3]
     * @return double
     */
    fun dot(x: Double, y: Double, z: Double): Double {
        return this.x * x + this.y * y + this.z * z
    }

    /**
     * Returns the cross product of the this [Vector3] and the given [Vector3].
     * @param vector the [Vector3]
     * @return [Vector3]
     */
    fun cross(vector: Vector3): Vector3 {
        return Vector3(
            y * vector.z - z * vector.y,
            z * vector.x - x * vector.z,
            x * vector.y - y * vector.x
        )
    }

    /**
     * Returns the cross product of the this [Vector3] and the given [Vector3].
     * @param x the x component of the [Vector3]
     * @param y the y component of the [Vector3]
     * @param z the z component of the [Vector3]
     * @return [Vector3]
     */
    fun cross(x: Double, y: Double, z: Double): Vector3 {
        return Vector3(
            this.y * z - this.z * y,
            this.z * x - this.x * z,
            this.x * y - this.y * x
        )
    }

    /**
     * Returns true if the given [Vector3] is orthogonal (perpendicular)
     * to this [Vector3].
     *
     *
     * If the dot product of this vector and the given vector is
     * zero then we know that they are perpendicular
     * @param vector the [Vector3]
     * @return boolean
     */
    fun isOrthogonal(vector: Vector3): Boolean {
        return if (abs(x * vector.x + y * vector.y + z * vector.z) <= Epsilon.E) true else false
    }

    /**
     * Returns true if the given [Vector3] is orthogonal (perpendicular)
     * to this [Vector3].
     *
     *
     * If the dot product of this vector and the given vector is
     * zero then we know that they are perpendicular
     * @param x the x component of the [Vector3]
     * @param y the y component of the [Vector3]
     * @param z the z component of the [Vector3]
     * @return boolean
     */
    fun isOrthogonal(x: Double, y: Double, z: Double): Boolean {
        return abs(this.x * x + this.y * y + this.z * z) <= Epsilon.E
    }

    /**
     * Returns true if this [Vector3] is the zero [Vector3].
     * @return boolean
     */
    val isZero: Boolean
        get() = abs(x) <= Epsilon.E && abs(y) <= Epsilon.E && abs(
            z
        ) <= Epsilon.E

    /**
     * Negates this [Vector3].
     * @return [Vector3] this vector
     */
    fun negate(): Vector3 {
        x = -x
        y = -y
        z = -z
        return this
    }

    /**
     * Returns a [Vector3] which is the negative of this [Vector3].
     * @return [Vector3]
     */
    val negative: Vector3
        get() = Vector3(-x, -y, -z)

    /**
     * Sets the [Vector3] to the zero [Vector3]
     * @return [Vector3] this vector
     */
    fun zero(): Vector3 {
        x = 0.0
        y = 0.0
        z = 0.0
        return this
    }

    /**
     * Projects this [Vector3] onto the given [Vector3].
     *
     *
     * This method requires the length of the given [Vector3] is not zero.
     * @param vector the [Vector3]
     * @return [Vector3] the projected [Vector3]
     */
    fun project(vector: Vector3): Vector3 {
        val dotProd = this.dot(vector)
        var denominator = vector.dot(vector)
        if (denominator <= Epsilon.E) return Vector3()
        denominator = dotProd / denominator
        return Vector3(denominator * vector.x, denominator * vector.y, denominator * vector.z)
    }

    /**
     * Returns a unit [Vector3] of this [Vector3].
     *
     *
     * This method requires the length of this [Vector3] is not zero.
     * @return [Vector3]
     */
    val normalized: Vector3
        get() {
            var magnitude: Double = sqrt(x * x + y * y + z * z)
            if (magnitude <= Epsilon.E) return Vector3()
            magnitude = 1.0 / magnitude
            return Vector3(x * magnitude, y * magnitude, z * magnitude)
        }

    /**
     * Converts this [Vector3] into a unit [Vector3] and returns
     * the magnitude before normalization.
     *
     *
     * This method requires the length of this [Vector3] is not zero.
     * @return double
     */
    fun normalize(): Double {
        val magnitude: Double = sqrt(x * x + y * y + z * z)
        if (magnitude <= Epsilon.E) return 0.0
        val m = 1.0 / magnitude
        x *= m
        y *= m
        z *= m
        return magnitude
    }

    companion object {
        /**
         * The triple product of [Vector3]s is defined as:
         * <pre>
         * a x (b x c)
        </pre> *
         * However, this method performs the following triple product:
         * <pre>
         * (a x b) x c
        </pre> *
         * this can be simplified to:
         * <pre>
         * -a * (b  c) + b * (a  c)
        </pre> *
         * or:
         * <pre>
         * b * (a  c) - a * (b  c)
        </pre> *
         * @param a the a [Vector3] in the above equation
         * @param b the b [Vector3] in the above equation
         * @param c the c [Vector3] in the above equation
         * @return [Vector3]
         */
        @JvmStatic
        fun tripleProduct(a: Vector3, b: Vector3, c: Vector3): Vector3 {
            // expanded version of above formula
            val r = Vector3()
            // perform a.dot(c)
            val ac = a.x * c.x + a.y * c.y + a.z * c.z
            // perform b.dot(c)
            val bc = b.x * c.x + b.y * c.y + b.z * c.z
            // perform b * a.dot(c) - a * b.dot(c)
            r.x = b.x * ac - a.x * bc
            r.y = b.y * ac - a.y * bc
            r.z = b.z * ac - a.z * bc
            return r
        }
    }

}
