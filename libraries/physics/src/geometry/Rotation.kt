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

import featurea.math.toDegrees
import featurea.math.toRadians
import org.dyn4j.Epsilon
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.*

/**
 * This class represents a rotation (in 2D space).
 * The aim of this class is to reduce as much as possible the use of
 * trigonometric function calls (like Math.sin/cos or Math.atan2) because
 * the majority of those are very slow to compute.
 * This can be achieved by pre-computing the sin and cos of the angle
 * of the rotation.
 *
 * A Rotation object is essentially a vector with norm 1.
 *
 * This class encapsulates the above so the user need not directly use
 * and compute those trigonometric values.
 * This also provides implicit validation as the user cannot create a
 * Rotation with invalid values (values not derived from cos/sin of some angle).
 * The receiver of a Rotation object can be sure it always represents a valid rotation.
 *
 * @author Manolis Tsamis
 * @version 3.4.0
 * @since 3.4.0
 */
class Rotation {
    /**
     * Returns the value of cos() for this [Rotation].
     * @return double
     */
    /** The cosine of the angle described by this Rotation  */
    @JvmField
    var cost: Double

    /**
     * Returns the value of sin() for this [Rotation].
     * @return double
     */
    /** The sine of the angle described by this Rotation  */
    @JvmField
    var sint: Double

    /**
     * Internal constructor that directly sets the cost and sint fields
     * of the [Rotation] without additional validation.
     * @param cost The cosine of some angle
     * @param sint The sine of the same angle
     */
    constructor(cost: Double, sint: Double) {
        this.cost = cost
        this.sint = sint
    }

    /**
     * Default constructor. Creates an identity [Rotation].
     */
    constructor() {
        cost = 1.0 // cos(0)
        sint = 0.0 // sin(0)
    }

    /**
     * Copy constructor.
     * @param rotation the [Rotation] to copy from
     */
    constructor(rotation: Rotation) {
        cost = rotation.cost
        sint = rotation.sint
    }

    /**
     * Creates a [Rotation] from the given angle.
     * @param angle the angle in radians
     */
    constructor(angle: Double) {
        cost = cos(angle)
        sint = sin(angle)
    }

    /**
     * @return a copy of this [Rotation]
     */
    fun copy(): Rotation {
        return Rotation(cost, sint)
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    override fun hashCode(): Int {
        val prime = 31
        var result = 3
        var temp: Long
        temp = cost.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = sint.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj === this) return true
        if (obj is Rotation) {
            val rotation = obj
            return cost == rotation.cost && sint == rotation.sint
        }
        return false
    }

    /**
     * Returns true if the cos and sin components of this [Rotation]
     * are the same as the given [Rotation].
     * @param rotation the [Rotation] to compare to
     * @return boolean
     */
    fun equals(rotation: Rotation?): Boolean {
        return if (rotation == null) false else cost == rotation.cost && sint == rotation.sint
    }

    /**
     * Returns true if the cos and sin components of this [Rotation]
     * are the same as the given [Rotation] given the specified error.
     * @param rotation the [Rotation] to compare to
     * @param error the error
     * @return boolean
     */
    fun equals(rotation: Rotation?, error: Double): Boolean {
        return if (rotation == null) false else abs(cost - rotation.cost) < error && abs(
            sint - rotation.sint
        ) < error
    }

    /**
     * Returns true if the cos and sin components of this [Rotation]
     * are the same as the given angle
     * @param angle the angle in radians
     * @return boolean
     */
    fun equals(angle: Double): Boolean {
        return cost == cos(angle) && sint == sin(angle)
    }

    /**
     * Returns true if the cos and sin components of this [Rotation]
     * are the same as the given angle given the specified error.
     * @param angle the angle in radians
     * @param error the error
     * @return boolean
     */
    fun equals(angle: Double, error: Double): Boolean {
        return abs(cost - cos(angle)) < error && abs(
            sint - sin(
                angle
            )
        ) < error
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Rotation(")
            .append(cost)
            .append(", ")
            .append(sint)
            .append(")")
        return sb.toString()
    }

    /**
     * Sets this [Rotation] to the given [Rotation].
     * @param rotation the [Rotation] to set this [Rotation] to
     * @return [Rotation] this rotation
     */
    fun set(rotation: Rotation): Rotation {
        cost = rotation.cost
        sint = rotation.sint
        return this
    }

    /**
     * Sets this [Rotation] to be the identity.
     * @return [Rotation] this rotation
     */
    fun setIdentity(): Rotation {
        cost = 1.0
        sint = 0.0
        return this
    }

    /**
     * Sets this [Rotation] to the given angle.
     * @param angle the angle in radians
     * @return [Rotation] this rotation
     */
    fun set(angle: Double): Rotation {
        cost = cos(angle)
        sint = sin(angle)
        return this
    }

    /**
     * Returns the angle in radians for this [Rotation].
     * @return double
     */
    fun toRadians(): Double {
        // Since we have the cos and sin values computed we can use
        // the Math.acos function which is much faster than Math.atan2

        // We can find the angle in the range [0, &pi;] with Math.acos
        // and then we'll use the sign of the sin value to find in which
        // semicircle we are and extend the result to [-&pi;, &pi;]

        // Apart from being quite faster this is also more precise
        // (see the documentation of Math.acos and Math.atan2)
        val acos: Double = acos(cost)
        return if (sint >= 0) acos else -acos
    }

    /**
     * Returns the angle in degrees for this [Rotation].
     * @return double
     */
    fun toDegrees(): Double {
        return toRadians().toDegrees()
    }

    /**
     * Returns this [Rotation] as a unit length direction vector.
     * @return [Vector2]
     */
    fun toVector(): Vector2 {
        return Vector2(cost, sint)
    }

    /**
     * Returns this [Rotation] as a direction vector with the given magnitude.
     * @param magnitude the magnitude
     * @return [Vector2]
     */
    fun toVector(magnitude: Double): Vector2 {
        return Vector2(cost * magnitude, sint * magnitude)
    }

    /**
     * Internal helper method to perform rotations consisting of a 45 degree.
     * @param cost the cos of the angle
     * @param sint the sin of the angle
     * @return This [Rotation] after being set to (cost, sint) and rotated 45 degrees
     */
    fun rotate45Helper(cost: Double, sint: Double): Rotation {
        this.cost = SQRT_2_INV * (cost - sint)
        this.sint = SQRT_2_INV * (cost + sint)
        return this
    }

    /**
     * Internal helper method to perform rotations consisting of a 45 degree.
     * Returns a new [Rotation] object.
     * @param cost the cos of the angle
     * @param sint the sin of the angle
     * @return A new [Rotation] with initial values (cost, sint) and then rotated 45 degrees
     */
    fun getRotated45Helper(cost: Double, sint: Double): Rotation {
        return Rotation(
            SQRT_2_INV * (cost - sint),
            SQRT_2_INV * (cost + sint)
        )
    }
    /* ************************************
	 * Methods to rotate by common angles *
	 ************************************ */
    /**
     * Rotates this rotation 45 degrees and returns this rotation.
     * @return [Rotation]
     */
    fun rotate45(): Rotation {
        return rotate45Helper(cost, sint)
    }

    /**
     * Rotates this rotation 45 degrees and returns a new rotation.
     * @return [Rotation]
     */
    val rotated45: Rotation
        get() = getRotated45Helper(cost, sint)

    /**
     * Rotates this rotation 90 degrees and returns this rotation.
     * @return [Rotation]
     */
    fun rotate90(): Rotation {
        val temp = cost
        cost = -sint
        sint = temp
        return this
    }

    /**
     * Rotates this rotation 90 degrees and returns a new rotation.
     * @return [Rotation]
     */
    val rotated90: Rotation
        get() = Rotation(-sint, cost)

    /**
     * Rotates this rotation 135 degrees and returns this rotation.
     * @return [Rotation]
     */
    fun rotate135(): Rotation {
        // Rotate by 90 and another 45
        return rotate45Helper(-sint, cost)
    }// Rotate by 90 and another 45

    /**
     * Rotates this rotation 135 degrees and returns a new rotation.
     * @return [Rotation]
     */
    val rotated135: Rotation
        get() =// Rotate by 90 and another 45
            getRotated45Helper(-sint, cost)

    /**
     * Rotates this rotation 180 degrees and returns this rotation.
     * @return [Rotation]
     */
    fun rotate180(): Rotation {
        cost = -cost
        sint = -sint
        return this
    }

    /**
     * Rotates this rotation 180 degrees and returns a new rotation.
     * @return [Rotation]
     */
    val rotated180: Rotation
        get() = Rotation(-cost, -sint)

    /**
     * Rotates this rotation 225 degrees and returns this rotation.
     * @return [Rotation]
     */
    fun rotate225(): Rotation {
        // Rotate by 180 and another 45
        return rotate45Helper(-cost, -sint)
    }// Rotate by 180 and another 45

    /**
     * Rotates this rotation 225 degrees and returns a new rotation.
     * @return [Rotation]
     */
    val rotated225: Rotation
        get() =// Rotate by 180 and another 45
            getRotated45Helper(-cost, -sint)

    /**
     * Rotates this rotation 270 degrees and returns this rotation.
     * @return [Rotation]
     */
    fun rotate270(): Rotation {
        val temp = cost
        cost = sint
        sint = -temp
        return this
    }

    /**
     * Rotates this rotation 270 degrees and returns a new rotation.
     * @return [Rotation]
     */
    val rotated270: Rotation
        get() = Rotation(sint, -cost)

    /**
     * Rotates this rotation 315 degrees and returns this rotation.
     * @return [Rotation]
     */
    fun rotate315(): Rotation {
        // Rotate by 270 and another 45
        return rotate45Helper(sint, -cost)
    }// Rotate by 270 and another 45

    /**
     * Rotates this rotation 315 degrees and returns a new rotation.
     * @return [Rotation]
     */
    val rotated315: Rotation
        get() =// Rotate by 270 and another 45
            getRotated45Helper(sint, -cost)

    /**
     * Negates this rotation and returns this rotation.
     *
     *
     * Let  be the rotation, then - is the inverse rotation.
     * @return [Rotation]
     */
    fun inverse(): Rotation {
        sint = -sint
        return this
    }

    /**
     * Negates this rotation and returns a new rotation.
     *
     *
     * Let  be the rotation, then - is the inverse rotation.
     * @return [Rotation]
     */
    val inversed: Rotation
        get() = Rotation(cost, -sint)

    /**
     * Internal method that rotates this [Rotation] by an angle  and
     * returns this rotation.
     * @param c cos()
     * @param s sin()
     * @return [Rotation]
     */
    fun rotate(c: Double, s: Double): Rotation {
        val cost = cost
        val sint = sint
        this.cost = cost * c - sint * s
        this.sint = cost * s + sint * c
        return this
    }

    /**
     * Internal method that return a new [Rotation] representing
     * this [Rotation] after being rotated by an angle .
     * @param c cos()
     * @param s sin()
     * @return [Rotation]
     */
    fun getRotated(c: Double, s: Double): Rotation {
        return Rotation(
            cost * c - sint * s,
            cost * s + sint * c
        )
    }

    /**
     * Rotates this rotation by the given rotation and returns this rotation.
     * @param rotation the [Rotation]
     * @return [Rotation]
     */
    fun rotate(rotation: Rotation): Rotation {
        return this.rotate(rotation.cost, rotation.sint)
    }

    /**
     * Rotates this rotation by the given rotation and returns a new rotation.
     * @param rotation the [Rotation]
     * @return [Rotation]
     */
    fun getRotated(rotation: Rotation): Rotation {
        return this.getRotated(rotation.cost, rotation.sint)
    }

    /**
     * Rotates this rotation by the given angle and returns this rotation.
     * @param angle the rotation in radians
     * @return [Rotation]
     */
    fun rotate(angle: Double): Rotation {
        return this.rotate(cos(angle), sin(angle))
    }

    /**
     * Rotates this rotation by the given angle and returns a new rotation.
     * @param angle the rotation in radians
     * @return [Rotation]
     */
    fun getRotated(angle: Double): Rotation {
        return this.getRotated(cos(angle), sin(angle))
    }

    /**
     * Returns true if this rotation is an identity rotation.
     * @return boolean
     */
    val isIdentity: Boolean
        get() = cost == 1.0

    /**
     * Returns true if this rotation is an identity rotation within the given error.
     * @param error the error
     * @return boolean
     */
    fun isIdentity(error: Double): Boolean {
        return abs(cost - 1) < error
    }

    /**
     * Returns the dot product of the this [Rotation] and the given [Rotation]
     * which is essentially the sine of the angle between those rotations.
     * @param rotation the [Rotation]
     * @return double
     */
    fun dot(rotation: Rotation): Double {
        return cost * rotation.cost + sint * rotation.sint
    }

    /**
     * Returns the cross product of the this [Rotation] and the given [Rotation]
     * which is essentially the sine of the angle between those rotations.
     * @param rotation the [Rotation]
     * @return double
     */
    fun cross(rotation: Rotation): Double {
        return cost * rotation.sint - sint * rotation.cost
    }

    /**
     * Returns the dot product of the this [Rotation] and the given [Vector2].
     * For internal use.
     * @param vector the [Vector2]
     * @return double
     */
    fun dot(vector: Vector2): Double {
        return cost * vector.x + sint * vector.y
    }

    /**
     * Returns the cross product of the this [Rotation] and the given [Vector2].
     * For internal use.
     * @param vector the [Vector2]
     * @return double
     */
    fun cross(vector: Vector2): Double {
        return cost * vector.y - sint * vector.x
    }

    /**
     * Compares this [Rotation] with another one, based on the angle between them (The one with -    )
     * Returns 1 if  &gt; 0, -1 if  &lt; 0 and 0 otherwise
     * @param other the [Rotation] to compare to
     * @return int
     */
    fun compare(other: Rotation): Int {
        // cmp = sin(&thetasym;) where &thetasym; is the angle between this rotation and the other
        // So we can decide what to return based on the sign of cmp
        val cmp = this.cross(other)
        return if (cmp > 0.0) {
            1
        } else if (cmp < 0.0) {
            -1
        } else {
            0
        }
    }

    /**
     * Compares this [Rotation] with a [Vector2], based on the angle between them (The one with -    )
     * Returns 1 if  &gt; 0, -1 if  &lt; 0 and 0 otherwise
     * @param other the [Vector2] to compare to
     * @return int
     */
    fun compare(other: Vector2): Int {
        // cmp = |v| * sin(&thetasym;) where &thetasym; is the angle between this rotation and the other
        // |v| is always positive and does not affect the result so we can decide what to return based just on the sign of cmp
        val cmp: Double = this.cross(other)
        return if (cmp > 0.0) {
            1
        } else if (cmp < 0.0) {
            -1
        } else {
            0
        }
    }

    /**
     * Returns the angle between this and the given [Rotation]
     * represented as a new [Rotation].
     * @param rotation the [Rotation]
     * @return [Rotation]
     */
    fun getRotationBetween(rotation: Rotation): Rotation {
        return Rotation(this.dot(rotation), this.cross(rotation))
    }

    /**
     * Returns the angle between this [Rotation] and the
     * given [Vector2] represented as a new [Rotation].
     * @param vector the [Vector2]
     * @return [Rotation]
     */
    fun getRotationBetween(vector: Vector2): Rotation {
        return this.getRotationBetween(of(vector))
    }

    companion object {
        private val SQRT_2_INV: Double = 1.0 / sqrt(2.0)

        /**
         * Alternative way to create a new [Rotation] from a given angle.
         * @param angle in radians
         * @return A [Rotation] for that angle
         */
        @JvmStatic
        fun of(angle: Double): Rotation {
            return Rotation(angle)
        }

        /**
         * Alternative way to create a new {@link Rotation} from a given angle, in degrees.
         * @param angle in degrees
         * @return A {@link Rotation} for that angle
         */
        @JvmStatic
        fun ofDegrees(angle: Double): Rotation? {
            return Rotation(angle.toRadians())
        }

        /**
         * Static method to create a [Rotation] object from the direction
         * of a given [Vector2].
         * @param direction The [Vector2] describing a direction
         * @return A [Rotation] with the same direction
         */
        @JvmStatic
        fun of(direction: Vector2): Rotation {
            // Normalize the vector
            val magnitude: Double = sqrt(direction.x * direction.x + direction.y * direction.y)
            return if (magnitude <= Epsilon.E) {
                // The zero vector has no direction, return the Identity rotation
                Rotation()
            } else Rotation(direction.x / magnitude, direction.y / magnitude)

            // Avoid multipying by the inverse in order to achieve better numerical accuracy
            // double m = 1.0 / magnitude;

            // The rotation is the normalized vector
        }

        /**
         * Static method to create a [Rotation] from a pair of values that lie on the unit circle;
         * That is a pair of values (x, y) such that x = cos(), y = sin() for some value
         * This method is provided for the case where the cos and sin values are already computed and
         * the overhead can be avoided.
         * This method will check whether those values are indeed on the unit circle and otherwise throw an [IllegalArgumentException].
         * @param cost The x value = cos()
         * @param sint The y value = sin()
         * @throws IllegalArgumentException if (cost, sint) is not on the unit circle
         * @return A [Rotation] defined by (cost, sint)
         */
        @JvmStatic
        fun of(cost: Double, sint: Double): Rotation {
            val magnitude = cost * cost + sint * sint
            if (abs(magnitude - 1) > Epsilon.E) {
                throw IllegalArgumentException(message("geometry.rotation.invalidPoint"))
            }
            return Rotation(cost, sint)
        }

        /**
         * Creates a new [Rotation] representing the same rotation
         * of a [Transform] object.
         * @param transform The [Transform]
         * @return A [Rotation] representing the same rotation
         */
        @JvmStatic
        fun of(transform: Transform): Rotation {
            // The cos and sin values are already computed internally in Transform
            return Rotation(transform.cost, transform.sint)
        }

        /**
         * Creates a new [Rotation] of 0 degrees.
         * @return [Rotation]
         */
        @JvmStatic
        fun rotation0(): Rotation {
            return Rotation()
        }

        /**
         * Creates a new [Rotation] of 90 degrees.
         * @return [Rotation]
         */
        @JvmStatic
        fun rotation90(): Rotation {
            return Rotation(0.0, 1.0)
        }

        /**
         * Creates a new [Rotation] of 180 degrees.
         * @return [Rotation]
         */
        @JvmStatic
        fun rotation180(): Rotation {
            return Rotation(-1.0, 0.0)
        }

        /**
         * Creates a new [Rotation] of 270 degrees.
         * @return [Rotation]
         */
        @JvmStatic
        fun rotation270(): Rotation {
            return Rotation(0.0, -1.0)
        }

        /**
         * Creates a new [Rotation] of 45 degrees.
         * @return [Rotation]
         */
        @JvmStatic
        fun rotation45(): Rotation {
            return Rotation(
                SQRT_2_INV,
                SQRT_2_INV
            )
        }

        /**
         * Creates a new [Rotation] of 135 degrees.
         * @return [Rotation]
         */
        @JvmStatic
        fun rotation135(): Rotation {
            return Rotation(
                -SQRT_2_INV,
                SQRT_2_INV
            )
        }

        /**
         * Creates a new [Rotation] of 225 degrees.
         * @return [Rotation]
         */
        @JvmStatic
        fun rotation225(): Rotation {
            return Rotation(
                -SQRT_2_INV,
                -SQRT_2_INV
            )
        }

        /**
         * Creates a new [Rotation] of 315 degrees.
         * @return [Rotation]
         */
        @JvmStatic
        fun rotation315(): Rotation {
            return Rotation(
                SQRT_2_INV,
                -SQRT_2_INV
            )
        }
    }
}