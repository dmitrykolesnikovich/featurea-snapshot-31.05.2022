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
import kotlin.math.*

/**
 * This class represents a vector or point in 2D space.
 *
 *
 * The operations [Vector2.setMagnitude], [Vector2.normalized],
 * [Vector2.project], and [Vector2.normalize] require the [Vector2]
 * to be non-zero in length.
 *
 *
 * Some methods also return the vector to facilitate chaining.  For example:
 * <pre>
 * Vector a = new Vector();
 * a.zero().add(1, 2).multiply(2);
</pre> *
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 */
// todo use `featurea.math.Vector2`
class Vector2 {

    /** The magnitude of the x component of this [Vector2]  */
    @JvmField
    var x: Double = 0.0

    /** The magnitude of the y component of this [Vector2]  */
    @JvmField
    var y: Double = 0.0

    /** Default constructor.  */
    constructor() {}

    /**
     * Copy constructor.
     * @param vector the [Vector2] to copy from
     */
    constructor(vector: Vector2) {
        x = vector.x
        y = vector.y
    }

    /**
     * Optional constructor.
     * @param x the x component
     * @param y the y component
     */
    constructor(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    /**
     * Creates a [Vector2] from the first point to the second point.
     * @param x1 the x coordinate of the first point
     * @param y1 the y coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y2 the y coordinate of the second point
     */
    constructor(x1: Double, y1: Double, x2: Double, y2: Double) {
        x = x2 - x1
        y = y2 - y1
    }

    /**
     * Creates a [Vector2] from the first point to the second point.
     * @param p1 the first point
     * @param p2 the second point
     */
    constructor(p1: Vector2, p2: Vector2) {
        x = p2.x - p1.x
        y = p2.y - p1.y
    }

    /**
     * Creates a unit length vector in the given direction.
     * @param direction the direction in radians
     * @since 3.0.1
     */
    constructor(direction: Double) {
        x = cos(direction)
        y = sin(direction)
    }

    /**
     * Returns a copy of this [Vector2].
     * @return [Vector2]
     */
    fun copy(): Vector2 {
        return Vector2(x, y)
    }

    /**
     * Returns the distance from this point to the given point.
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return double
     */
    fun distance(x: Double, y: Double): Double {
        //return Math.hypot(this.x - x, this.y - y);
        val dx = this.x - x
        val dy = this.y - y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Returns the distance from this point to the given point.
     * @param point the point
     * @return double
     */
    fun distance(point: Vector2): Double {
        //return Math.hypot(this.x - point.x, this.y - point.y);
        val dx = x - point.x
        val dy = y - point.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Returns the distance from this point to the given point squared.
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return double
     */
    fun distanceSquared(x: Double, y: Double): Double {
        //return (this.x - x) * (this.x - x) + (this.y - y) * (this.y - y);
        val dx = this.x - x
        val dy = this.y - y
        return dx * dx + dy * dy
    }

    /**
     * Returns the distance from this point to the given point squared.
     * @param point the point
     * @return double
     */
    fun distanceSquared(point: Vector2): Double {
        //return (this.x - point.x) * (this.x - point.x) + (this.y - point.y) * (this.y - point.y);
        val dx = x - point.x
        val dy = y - point.y
        return dx * dx + dy * dy
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
        return result
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj === this) return true
        if (obj is Vector2) {
            val vector = obj
            return x == vector.x && y == vector.y
        }
        return false
    }

    /**
     * Returns true if the x and y components of this [Vector2]
     * are the same as the given [Vector2].
     * @param vector the [Vector2] to compare to
     * @return boolean
     */
    fun equals(vector: Vector2?): Boolean {
        if (vector == null) return false
        return if (this === vector) {
            true
        } else {
            x == vector.x && y == vector.y
        }
    }

    /**
     * Returns true if the x and y components of this [Vector2]
     * are the same as the given x and y components.
     * @param x the x coordinate of the [Vector2] to compare to
     * @param y the y coordinate of the [Vector2] to compare to
     * @return boolean
     */
    fun equals(x: Double, y: Double): Boolean {
        return this.x == x && this.y == y
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
            .append(")")
        return sb.toString()
    }

    /**
     * Sets this [Vector2] to the given [Vector2].
     * @param vector the [Vector2] to set this [Vector2] to
     * @return [Vector2] this vector
     */
    fun set(vector: Vector2): Vector2 {
        x = vector.x
        y = vector.y
        return this
    }

    /**
     * Sets this [Vector2] to the given [Vector2].
     * @param x the x component of the [Vector2] to set this [Vector2] to
     * @param y the y component of the [Vector2] to set this [Vector2] to
     * @return [Vector2] this vector
     */
    operator fun set(x: Double, y: Double): Vector2 {
        this.x = x
        this.y = y
        return this
    }

    /**
     * Returns the x component of this [Vector2].
     * @return [Vector2]
     */
    val xComponent: Vector2
        get() = Vector2(x, 0.0)

    /**
     * Returns the y component of this [Vector2].
     * @return [Vector2]
     */
    val yComponent: Vector2
        get() = Vector2(0.0, y)// the magnitude is just the pythagorean theorem

    /**
     * Returns the magnitude of this [Vector2].
     * @return double
     */
    val magnitude: Double
        get() =// the magnitude is just the pythagorean theorem
            sqrt(x * x + y * y)

    /**
     * Returns the magnitude of this [Vector2] squared.
     * @return double
     */
    val magnitudeSquared: Double
        get() = x * x + y * y

    /**
     * Sets the magnitude of the [Vector2].
     * @param magnitude the magnitude
     * @return [Vector2] this vector
     */
    fun setMagnitude(magnitude: Double): Vector2 {
        // check the given magnitude
        if (abs(magnitude) <= Epsilon.E) {
            x = 0.0
            y = 0.0
            return this
        }
        // is this vector a zero vector?
        if (isZero) {
            return this
        }
        // get the magnitude
        var mag: Double = sqrt(x * x + y * y)
        // normalize and multiply by the new magnitude
        mag = magnitude / mag
        x *= mag
        y *= mag
        return this
    }

    /**
     * Returns the direction of this [Vector2]
     * as an angle in radians.
     * @return double angle in radians [-, ]
     */
    val direction: Double
        get() = atan2(y, x)

    /**
     * Sets the direction of this [Vector2].
     * @param angle angle in radians
     * @return [Vector2] this vector
     */
    fun setDirection(angle: Double): Vector2 {
        //double magnitude = Math.hypot(this.x, this.y);
        val magnitude: Double = sqrt(x * x + y * y)
        x = magnitude * cos(angle)
        y = magnitude * sin(angle)
        return this
    }

    /**
     * Adds the given [Vector2] to this [Vector2].
     * @param vector the [Vector2]
     * @return [Vector2] this vector
     */
    fun add(vector: Vector2): Vector2 {
        x += vector.x
        y += vector.y
        return this
    }

    /**
     * Adds the given [Vector2] to this [Vector2].
     * @param x the x component of the [Vector2]
     * @param y the y component of the [Vector2]
     * @return [Vector2] this vector
     */
    fun add(x: Double, y: Double): Vector2 {
        this.x += x
        this.y += y
        return this
    }

    /**
     * Adds this [Vector2] and the given [Vector2] returning
     * a new [Vector2] containing the result.
     * @param vector the [Vector2]
     * @return [Vector2]
     */
    fun sum(vector: Vector2): Vector2 {
        return Vector2(x + vector.x, y + vector.y)
    }

    /**
     * Adds this [Vector2] and the given [Vector2] returning
     * a new [Vector2] containing the result.
     * @param x the x component of the [Vector2]
     * @param y the y component of the [Vector2]
     * @return [Vector2]
     */
    fun sum(x: Double, y: Double): Vector2 {
        return Vector2(this.x + x, this.y + y)
    }

    /**
     * Subtracts the given [Vector2] from this [Vector2].
     * @param vector the [Vector2]
     * @return [Vector2] this vector
     */
    fun subtract(vector: Vector2): Vector2 {
        x -= vector.x
        y -= vector.y
        return this
    }

    /**
     * Subtracts the given [Vector2] from this [Vector2].
     * @param x the x component of the [Vector2]
     * @param y the y component of the [Vector2]
     * @return [Vector2] this vector
     */
    fun subtract(x: Double, y: Double): Vector2 {
        this.x -= x
        this.y -= y
        return this
    }

    /**
     * Subtracts the given [Vector2] from this [Vector2] returning
     * a new [Vector2] containing the result.
     * @param vector the [Vector2]
     * @return [Vector2]
     */
    fun difference(vector: Vector2): Vector2 {
        return Vector2(x - vector.x, y - vector.y)
    }

    /**
     * Subtracts the given [Vector2] from this [Vector2] returning
     * a new [Vector2] containing the result.
     * @param x the x component of the [Vector2]
     * @param y the y component of the [Vector2]
     * @return [Vector2]
     */
    fun difference(x: Double, y: Double): Vector2 {
        return Vector2(this.x - x, this.y - y)
    }

    /**
     * Creates a [Vector2] from this [Vector2] to the given [Vector2].
     * @param vector the [Vector2]
     * @return [Vector2]
     */
    fun to(vector: Vector2): Vector2 {
        return Vector2(vector.x - x, vector.y - y)
    }

    /**
     * Creates a [Vector2] from this [Vector2] to the given [Vector2].
     * @param x the x component of the [Vector2]
     * @param y the y component of the [Vector2]
     * @return [Vector2]
     */
    fun to(x: Double, y: Double): Vector2 {
        return Vector2(x - this.x, y - this.y)
    }

    /**
     * Multiplies this [Vector2] by the given scalar.
     * @param scalar the scalar
     * @return [Vector2] this vector
     */
    fun multiply(scalar: Double): Vector2 {
        x *= scalar
        y *= scalar
        return this
    }

    /**
     * Divides this [Vector2] by the given scalar.
     * @param scalar the scalar
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun divide(scalar: Double): Vector2 {
        x /= scalar
        y /= scalar
        return this
    }

    /**
     * Multiplies this [Vector2] by the given scalar returning
     * a new [Vector2] containing the result.
     * @param scalar the scalar
     * @return [Vector2]
     */
    fun product(scalar: Double): Vector2 {
        return Vector2(x * scalar, y * scalar)
    }

    /**
     * Divides this [Vector2] by the given scalar returning
     * a new [Vector2] containing the result.
     * @param scalar the scalar
     * @return [Vector2]
     * @since 3.4.0
     */
    fun quotient(scalar: Double): Vector2 {
        return Vector2(x / scalar, y / scalar)
    }

    /**
     * Returns the dot product of the given [Vector2]
     * and this [Vector2].
     * @param vector the [Vector2]
     * @return double
     */
    fun dot(vector: Vector2): Double {
        return x * vector.x + y * vector.y
    }

    /**
     * Returns the dot product of the given [Vector2]
     * and this [Vector2].
     * @param x the x component of the [Vector2]
     * @param y the y component of the [Vector2]
     * @return double
     */
    fun dot(x: Double, y: Double): Double {
        return this.x * x + this.y * y
    }

    /**
     * Returns the cross product of the this [Vector2] and the given [Vector2].
     * @param vector the [Vector2]
     * @return double
     */
    fun cross(vector: Vector2): Double {
        return x * vector.y - y * vector.x
    }

    /**
     * Returns the cross product of the this [Vector2] and the given [Vector2].
     * @param x the x component of the [Vector2]
     * @param y the y component of the [Vector2]
     * @return double
     */
    fun cross(x: Double, y: Double): Double {
        return this.x * y - this.y * x
    }

    /**
     * Returns the cross product of this [Vector2] and the z value of the right [Vector2].
     * @param z the z component of the [Vector2]
     * @return [Vector2]
     */
    fun cross(z: Double): Vector2 {
        return Vector2(-y * z, x * z)
    }

    /**
     * Returns true if the given [Vector2] is orthogonal (perpendicular)
     * to this [Vector2].
     *
     *
     * If the dot product of this vector and the given vector is
     * zero then we know that they are perpendicular
     * @param vector the [Vector2]
     * @return boolean
     */
    fun isOrthogonal(vector: Vector2): Boolean {
        return abs(x * vector.x + y * vector.y) <= Epsilon.E
    }

    /**
     * Returns true if the given [Vector2] is orthogonal (perpendicular)
     * to this [Vector2].
     *
     *
     * If the dot product of this vector and the given vector is
     * zero then we know that they are perpendicular
     * @param x the x component of the [Vector2]
     * @param y the y component of the [Vector2]
     * @return boolean
     */
    fun isOrthogonal(x: Double, y: Double): Boolean {
        return abs(this.x * x + this.y * y) <= Epsilon.E
    }

    /**
     * Returns true if this [Vector2] is the zero [Vector2].
     * @return boolean
     */
    val isZero: Boolean
        get() = abs(x) <= Epsilon.E && abs(y) <= Epsilon.E

    /**
     * Negates this [Vector2].
     * @return [Vector2] this vector
     */
    fun negate(): Vector2 {
        x = -x
        y = -y
        return this
    }

    /**
     * Returns a [Vector2] which is the negative of this [Vector2].
     * @return [Vector2]
     */
    val negative: Vector2
        get() = Vector2(-x, -y)

    /**
     * Sets the [Vector2] to the zero [Vector2]
     * @return [Vector2] this vector
     */
    fun zero(): Vector2 {
        x = 0.0
        y = 0.0
        return this
    }

    /**
     * Internal helper method that rotates about the origin by an angle .
     * @param cos cos()
     * @param sin sin()
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun rotate(cos: Double, sin: Double): Vector2 {
        val x = x
        val y = y
        this.x = x * cos - y * sin
        this.y = x * sin + y * cos
        return this
    }

    /**
     * Rotates about the origin.
     * @param theta the rotation angle in radians
     * @return [Vector2] this vector
     */
    fun rotate(theta: Double): Vector2 {
        return this.rotate(cos(theta), sin(theta))
    }

    /**
     * Rotates about the origin.
     * @param rotation the [Rotation]
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun rotate(rotation: Rotation): Vector2 {
        return this.rotate(rotation.cost, rotation.sint)
    }

    /**
     * Rotates about the origin by the inverse angle -.
     * @param theta the rotation angle in radians
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun inverseRotate(theta: Double): Vector2 {
        return this.rotate(cos(theta), -sin(theta))
    }

    /**
     * Rotates about the origin by the inverse angle -.
     * @param rotation the [Rotation]
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun inverseRotate(rotation: Rotation): Vector2 {
        return this.rotate(rotation.cost, -rotation.sint)
    }

    /**
     * Internal helper method that rotates about the given coordinates by an angle .
     * @param cos cos()
     * @param sin sin()
     * @param x the x coordinate to rotate about
     * @param y the y coordinate to rotate about
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun rotate(cos: Double, sin: Double, x: Double, y: Double): Vector2 {
        val tx = this.x - x
        val ty = this.y - y
        this.x = tx * cos - ty * sin + x
        this.y = tx * sin + ty * cos + y
        return this
    }

    /**
     * Rotates the [Vector2] about the given coordinates.
     * @param theta the rotation angle in radians
     * @param x the x coordinate to rotate about
     * @param y the y coordinate to rotate about
     * @return [Vector2] this vector
     */
    fun rotate(theta: Double, x: Double, y: Double): Vector2 {
        return this.rotate(cos(theta), sin(theta), x, y)
    }

    /**
     * Rotates the [Vector2] about the given coordinates.
     * @param rotation the [Rotation]
     * @param x the x coordinate to rotate about
     * @param y the y coordinate to rotate about
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun rotate(rotation: Rotation, x: Double, y: Double): Vector2 {
        return this.rotate(rotation.cost, rotation.sint, x, y)
    }

    /**
     * Rotates about the given coordinates by the inverse angle -.
     * @param theta the rotation angle in radians
     * @param x the x coordinate to rotate about
     * @param y the y coordinate to rotate about
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun inverseRotate(theta: Double, x: Double, y: Double): Vector2 {
        return this.rotate(cos(theta), -sin(theta), x, y)
    }

    /**
     * Rotates about the given coordinates by the inverse angle -.
     * @param rotation the [Rotation]
     * @param x the x coordinate to rotate about
     * @param y the y coordinate to rotate about
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun inverseRotate(rotation: Rotation, x: Double, y: Double): Vector2 {
        return this.rotate(rotation.cost, -rotation.sint, x, y)
    }

    /**
     * Rotates the [Vector2] about the given point.
     * @param theta the rotation angle in radians
     * @param point the point to rotate about
     * @return [Vector2] this vector
     */
    fun rotate(theta: Double, point: Vector2): Vector2 {
        return this.rotate(theta, point.x, point.y)
    }

    /**
     * Rotates the [Vector2] about the given point.
     * @param rotation the [Rotation]
     * @param point the point to rotate about
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun rotate(rotation: Rotation, point: Vector2): Vector2 {
        return this.rotate(rotation, point.x, point.y)
    }

    /**
     * Rotates the [Vector2] about the given point by the inverse angle -.
     * @param theta the rotation angle in radians
     * @param point the point to rotate about
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun inverseRotate(theta: Double, point: Vector2): Vector2 {
        return this.inverseRotate(theta, point.x, point.y)
    }

    /**
     * Rotates the [Vector2] about the given point by the inverse angle -.
     * @param rotation the [Rotation]
     * @param point the point to rotate about
     * @return [Vector2] this vector
     * @since 3.4.0
     */
    fun inverseRotate(rotation: Rotation, point: Vector2): Vector2 {
        return this.inverseRotate(rotation, point.x, point.y)
    }

    /**
     * Projects this [Vector2] onto the given [Vector2].
     * @param vector the [Vector2]
     * @return [Vector2] the projected [Vector2]
     */
    fun project(vector: Vector2): Vector2 {
        val dotProd = this.dot(vector)
        var denominator = vector.dot(vector)
        if (denominator <= Epsilon.E) return Vector2()
        denominator = dotProd / denominator
        return Vector2(denominator * vector.x, denominator * vector.y)
    }

    /**
     * Returns the right-handed normal of this vector.
     * @return [Vector2] the right hand orthogonal [Vector2]
     */
    val rightHandOrthogonalVector: Vector2
        get() = Vector2(-y, x)

    /**
     * Sets this vector to the right-handed normal of this vector.
     * @return [Vector2] this vector
     * @see .getRightHandOrthogonalVector
     */
    fun right(): Vector2 {
        val temp = x
        x = -y
        y = temp
        return this
    }

    /**
     * Returns the left-handed normal of this vector.
     * @return [Vector2] the left hand orthogonal [Vector2]
     */
    val leftHandOrthogonalVector: Vector2
        get() = Vector2(y, -x)

    /**
     * Sets this vector to the left-handed normal of this vector.
     * @return [Vector2] this vector
     * @see .getLeftHandOrthogonalVector
     */
    fun left(): Vector2 {
        val temp = x
        x = y
        y = -temp
        return this
    }

    val normalized: Vector2
        get() {
            var magnitude = magnitude
            if (magnitude <= Epsilon.E) return Vector2()
            magnitude = 1.0 / magnitude
            return Vector2(x * magnitude, y * magnitude)
        }

    /**
     * Converts this [Vector2] into a unit [Vector2] and returns
     * the magnitude before normalization.
     *
     *
     * This method requires the length of this [Vector2] is not zero.
     * @return double
     */
    fun normalize(): Double {
        val magnitude: Double = sqrt(x * x + y * y)
        if (magnitude <= Epsilon.E) return 0.0
        val m = 1.0 / magnitude
        x *= m
        y *= m
        //return 1.0 / m;
        return magnitude
    }

    /**
     * Returns the smallest angle between the given [Vector2]s.
     *
     *
     * Returns the angle in radians in the range - to .
     * @param vector the [Vector2]
     * @return angle in radians [-, ]
     */
    fun getAngleBetween(vector: Vector2): Double {
        val a: Double = atan2(vector.y, vector.x) - atan2(y, x)
        if (a > PI) return a - Geometry.TWO_PI
        return if (a < -PI) a + Geometry.TWO_PI else a
    }

    /**
     * Returns the smallest angle between the given [Vector2] and the given angle.
     *
     *
     * Returns the angle in radians in the range - to .
     * @param otherAngle the angle. Must be in the range - to
     * @return angle in radians [-, ]
     * @since 3.4.0
     */
    fun getAngleBetween(otherAngle: Double): Double {
        val a: Double = otherAngle - atan2(y, x)
        if (a > PI) return a - Geometry.TWO_PI
        return if (a < -PI) a + Geometry.TWO_PI else a
    }

    companion object {
        /** A vector representing the x-axis; this vector should not be changed at runtime; used internally  */
        @JvmField
        val X_AXIS = Vector2(1.0, 0.0)

        /** A vector representing the y-axis; this vector should not be changed at runtime; used internally  */
        @JvmField
        val Y_AXIS = Vector2(0.0, 1.0)

        /** A vector representing the inverse x-axis; this vector should not be changed at runtime; used internally  */
        @JvmField
        val INV_X_AXIS = Vector2(-1.0, 0.0)

        /** A vector representing the inverse y-axis; this vector should not be changed at runtime; used internally  */
        @JvmField
        val INV_Y_AXIS = Vector2(0.0, -1.0)

        /**
         * Returns a new [Vector2] given the magnitude and direction.
         * @param magnitude the magnitude of the [Vector2]
         * @param direction the direction of the [Vector2] in radians
         * @return [Vector2]
         */
        @JvmStatic
        fun create(magnitude: Double, direction: Double): Vector2 {
            val x: Double = magnitude * cos(direction)
            val y: Double = magnitude * sin(direction)
            return Vector2(x, y)
        }

        /**
         * The triple product of [Vector2]s is defined as:
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
         * @param a the a [Vector2] in the above equation
         * @param b the b [Vector2] in the above equation
         * @param c the c [Vector2] in the above equation
         * @return [Vector2]
         */
        @JvmStatic
        fun tripleProduct(a: Vector2, b: Vector2, c: Vector2): Vector2 {
            // expanded version of above formula
            val r = Vector2()

            /*
		 * In the following we can substitute ac and bc in r.x and r.y
		 * and with some rearrangement get a much more efficient version
		 * 
		 * double ac = a.x * c.x + a.y * c.y;
		 * double bc = b.x * c.x + b.y * c.y;
		 * r.x = b.x * ac - a.x * bc;
		 * r.y = b.y * ac - a.y * bc;
		 */
            val dot = a.x * b.y - b.x * a.y
            r.x = -c.y * dot
            r.y = c.x * dot
            return r
        }
    }
}