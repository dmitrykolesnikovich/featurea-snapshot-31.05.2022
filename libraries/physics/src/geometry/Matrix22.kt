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
import org.dyn4j.resources.Messages
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.math.abs

/**
 * Represents a 2x2 Matrix.
 *
 *
 * Used to solve 2x2 systems of equations.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 */
class Matrix22 {
    /** The element at 0,0  */
    @JvmField
    var m00 = 0.0

    /** The element at 0,1  */
    @JvmField
    var m01 = 0.0

    /** The element at 1,0  */
    @JvmField
    var m10 = 0.0

    /** The element at 1,1  */
    @JvmField
    var m11 = 0.0

    /**
     * Default constructor.
     */
    constructor() {}

    /**
     * Full constructor.
     * @param m00 the element at 0,0
     * @param m01 the element at 0,1
     * @param m10 the element at 1,0
     * @param m11 the element at 1,1
     */
    constructor(m00: Double, m01: Double, m10: Double, m11: Double) {
        this.m00 = m00
        this.m01 = m01
        this.m10 = m10
        this.m11 = m11
    }

    /**
     * Full constructor.
     *
     *
     * The given array should be in the same order as the
     * [.Matrix22] constructor.
     * @param values the values array
     * @throws NullPointerException if values is null
     * @throws IllegalArgumentException if values is not of length 4
     */
    constructor(values: DoubleArray?) {
        if (values == null) throw NullPointerException(message("geometry.matrix.nullArray"))
        if (values.size != 4) throw IndexOutOfBoundsException(message("geometry.matrix.invalidLength4"))
        m00 = values[0]
        m01 = values[1]
        m10 = values[2]
        m11 = values[3]
    }

    /**
     * Copy constructor.
     * @param matrix the [Matrix22] to copy
     */
    constructor(matrix: Matrix22) {
        m00 = matrix.m00
        m01 = matrix.m01
        m10 = matrix.m10
        m11 = matrix.m11
    }

    /**
     * Returns a copy of this [Matrix22].
     * @return [Matrix22]
     * @since 3.4.0
     */
    fun copy(): Matrix22 {
        return Matrix22(this)
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        var temp: Long
        temp = m00.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = m01.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = m10.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = m11.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj === this) return true
        if (obj is Matrix22) {
            val other = obj
            if (other.m00 == m00 && other.m01 == m01 && other.m10 == m10 && other.m11 == m11
            ) {
                return true
            }
        }
        return false
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("[").append(m00).append(" ").append(m01).append("][")
            .append(m10).append(" ").append(m11).append("]")
        return sb.toString()
    }

    /**
     * Adds the given [Matrix22] to this [Matrix22]
     * returning this [Matrix22].
     * <pre>
     * this = this + m
    </pre> *
     * @param matrix the [Matrix22] to add
     * @return [Matrix22] this matrix
     */
    fun add(matrix: Matrix22): Matrix22 {
        m00 += matrix.m00
        m01 += matrix.m01
        m10 += matrix.m10
        m11 += matrix.m11
        return this
    }

    /**
     * Returns a new [Matrix22] that is the sum of this [Matrix22]
     * and the given [Matrix22].
     * <pre>
     * r = this + m
    </pre> *
     * @param matrix the [Matrix22] to add
     * @return [Matrix22] a new matrix containing the result
     */
    fun sum(matrix: Matrix22): Matrix22 {
        // make a copy of this matrix and perform the addition
        return copy().add(matrix)
    }

    /**
     * Subtracts the given [Matrix22] from this [Matrix22]
     * returning this [Matrix22].
     * <pre>
     * this = this - m
    </pre> *
     * @param matrix the [Matrix22] to subtract
     * @return [Matrix22] this matrix
     */
    fun subtract(matrix: Matrix22): Matrix22 {
        m00 -= matrix.m00
        m01 -= matrix.m01
        m10 -= matrix.m10
        m11 -= matrix.m11
        return this
    }

    /**
     * Returns a new [Matrix22] that is the difference of this [Matrix22]
     * and the given [Matrix22].
     * <pre>
     * r = this - m
    </pre> *
     * @param matrix the [Matrix22] to subtract
     * @return [Matrix22] a new matrix containing the result
     */
    fun difference(matrix: Matrix22): Matrix22 {
        // make a copy of this matrix and perform the subtraction
        return copy().subtract(matrix)
    }

    /**
     * Multiplies this [Matrix22] by the given matrix [Matrix22]
     * returning this [Matrix22].
     * <pre>
     * this = this * m
    </pre> *
     * @param matrix the [Matrix22] to subtract
     * @return [Matrix22] this matrix
     */
    fun multiply(matrix: Matrix22): Matrix22 {
        val m00 = m00
        val m01 = m01
        val m10 = m10
        val m11 = m11
        this.m00 = m00 * matrix.m00 + m01 * matrix.m10
        this.m01 = m00 * matrix.m01 + m01 * matrix.m11
        this.m10 = m10 * matrix.m00 + m11 * matrix.m10
        this.m11 = m10 * matrix.m01 + m11 * matrix.m11
        return this
    }

    /**
     * Returns a new [Matrix22] that is the product of this [Matrix22]
     * and the given [Matrix22].
     * <pre>
     * r = this * m
    </pre> *
     * @param matrix the [Matrix22] to multiply
     * @return [Matrix22] a new matrix containing the result
     */
    fun product(matrix: Matrix22): Matrix22 {
        // make a copy of this matrix and perform the multiplication
        return copy().multiply(matrix)
    }

    /**
     * Multiplies this [Matrix22] by the given [Vector2] and
     * places the result in the given [Vector2].
     * <pre>
     * v = this * v
    </pre> *
     * @param vector the [Vector2] to multiply
     * @return [Vector2] the vector result
     */
    fun multiply(vector: Vector2): Vector2 {
        val x: Double = vector.x
        val y: Double = vector.y
        vector.x = m00 * x + m01 * y
        vector.y = m10 * x + m11 * y
        return vector
    }

    /**
     * Multiplies this [Matrix22] by the given [Vector2] returning
     * the result in a new [Vector2].
     * <pre>
     * r = this * v
    </pre> *
     * @param vector the [Vector2] to multiply
     * @return [Vector2] the vector result
     */
    fun product(vector: Vector2): Vector2 {
        return this.multiply(vector.copy())
    }

    /**
     * Multiplies the given [Vector2] by this [Matrix22] and
     * places the result in the given [Vector2].
     *
     *  v = v<sup>T</sup> * this
     * @param vector the [Vector2] to multiply
     * @return [Vector2] the vector result
     */
    fun multiplyT(vector: Vector2): Vector2 {
        val x: Double = vector.x
        val y: Double = vector.y
        vector.x = m00 * x + m10 * y
        vector.y = m01 * x + m11 * y
        return vector
    }

    /**
     * Multiplies the given [Vector2] by this [Matrix22] returning
     * the result in a new [Vector2].
     *
     *  r = v<sup>T</sup> * this
     * @param vector the [Vector2] to multiply
     * @return [Vector2] the vector result
     */
    fun productT(vector: Vector2): Vector2 {
        return multiplyT(vector.copy())
    }

    /**
     * Multiplies this [Matrix22] by the given scalar and places
     * the result in this [Matrix22].
     * <pre>
     * this = this * scalar
    </pre> *
     * @param scalar the scalar to multiply by
     * @return [Matrix22] this matrix
     */
    fun multiply(scalar: Double): Matrix22 {
        m00 *= scalar
        m01 *= scalar
        m10 *= scalar
        m11 *= scalar
        return this
    }

    /**
     * Multiplies this [Matrix22] by the given scalar returning a
     * new [Matrix22] containing the result.
     * <pre>
     * r = this * scalar
    </pre> *
     * @param scalar the scalar to multiply by
     * @return [Matrix22] a new matrix containing the result
     */
    fun product(scalar: Double): Matrix22 {
        // make a copy of this matrix and perform the scalar multiplication
        return copy().multiply(scalar)
    }

    /**
     * Sets this [Matrix22] to an identity [Matrix22].
     * @return [Matrix22] this matrix
     */
    fun identity(): Matrix22 {
        m00 = 1.0
        m01 = 0.0
        m10 = 0.0
        m11 = 1.0
        return this
    }

    /**
     * Sets this [Matrix22] to the transpose of this [Matrix22].
     * @return [Matrix22] this matrix
     */
    fun transpose(): Matrix22 {
        val m = m01
        m01 = m10
        m10 = m
        return this
    }

    /**
     * Returns the the transpose of this [Matrix22] in a new [Matrix22].
     * @return [Matrix22] a new matrix contianing the transpose
     */
    fun getTranspose(): Matrix22 = copy().transpose()

    /**
     * Returns the determinant of this [Matrix22].
     * @return double
     */
    fun determinant(): Double {
        return m00 * m11 - m01 * m10
    }

    /**
     * Performs the inverse of this [Matrix22] and places the
     * result in this [Matrix22].
     * @return [Matrix22] this matrix
     */
    fun invert(): Matrix22 {
        // get the determinant
        var det = determinant()
        // check for zero determinant
        if (abs(det) > Epsilon.E) {
            det = 1.0 / det
        }
        val a = m00
        val b = m01
        val c = m10
        val d = m11
        m00 = det * d
        m01 = -det * b
        m10 = -det * c
        m11 = det * a
        return this
    }// make a copy of this matrix and perform the inversion

    /**
     * Returns a new [Matrix22] containing the inverse of this [Matrix22].
     * @return [Matrix22] a new matrix containing the result
     */
    val inverse: Matrix22
        get() =// make a copy of this matrix and perform the inversion
            copy().invert()

    /**
     * Solves the system of linear equations:
     *
     *  Ax = b
     * Multiply by A<sup>-1</sup> on both sides
     * x = A<sup>-1</sup>b
     * @param b the b [Vector2]
     * @return [Vector2] the x vector
     */
    fun solve(b: Vector2): Vector2 {
        // get the determinant
        var det = determinant()
        // check for zero determinant
        if (abs(det) > Epsilon.E) {
            det = 1.0 / det
        }
        val r = Vector2()
        r.x = det * (m11 * b.x - m01 * b.y)
        r.y = det * (m00 * b.y - m10 * b.x)
        return r
    }
}