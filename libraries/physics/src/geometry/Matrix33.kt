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
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.math.abs

/**
 * Represents a 3x3 Matrix.
 *
 *
 * Used to solve 3x3 systems of equations.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 */
class Matrix33 {
    /** The element at 0,0  */
    @JvmField
    var m00 = 0.0

    /** The element at 0,1  */
    @JvmField
    var m01 = 0.0

    /** The element at 0,2  */
    @JvmField
    var m02 = 0.0

    /** The element at 1,0  */
    @JvmField
    var m10 = 0.0

    /** The element at 1,1  */
    @JvmField
    var m11 = 0.0

    /** The element at 1,2  */
    @JvmField
    var m12 = 0.0

    /** The element at 2,0  */
    @JvmField
    var m20 = 0.0

    /** The element at 2,1  */
    @JvmField
    var m21 = 0.0

    /** The element at 2,2  */
    @JvmField
    var m22 = 0.0

    /**
     * Default constructor.
     */
    constructor() {}

    /**
     * Full constructor.
     * @param m00 the element at 0,0
     * @param m01 the element at 0,1
     * @param m02 the element at 0,2
     * @param m10 the element at 1,0
     * @param m11 the element at 1,1
     * @param m12 the element at 1,2
     * @param m20 the element at 2,0
     * @param m21 the element at 2,1
     * @param m22 the element at 2,2
     */
    constructor(
        m00: Double, m01: Double, m02: Double,
        m10: Double, m11: Double, m12: Double,
        m20: Double, m21: Double, m22: Double
    ) {
        this.m00 = m00
        this.m01 = m01
        this.m02 = m02
        this.m10 = m10
        this.m11 = m11
        this.m12 = m12
        this.m20 = m20
        this.m21 = m21
        this.m22 = m22
    }

    /**
     * Full constructor.
     *
     *
     * The given array should be in the same order as the
     * [.Matrix33] constructor.
     * @param values the values array
     * @throws NullPointerException if values is null
     * @throws IllegalArgumentException if values is not length 9
     */
    constructor(values: DoubleArray?) {
        if (values == null) throw NullPointerException(message("geometry.matrix.nullArray"))
        if (values.size != 9) throw IndexOutOfBoundsException(message("geometry.matrix.invalidLength9"))
        m00 = values[0]
        m01 = values[1]
        m02 = values[2]
        m10 = values[3]
        m11 = values[4]
        m12 = values[5]
        m20 = values[6]
        m21 = values[7]
        m22 = values[8]
    }

    /**
     * Copy constructor.
     * @param matrix the [Matrix33] to copy
     */
    constructor(matrix: Matrix33) {
        m00 = matrix.m00
        m01 = matrix.m01
        m02 = matrix.m02
        m10 = matrix.m10
        m11 = matrix.m11
        m12 = matrix.m12
        m20 = matrix.m20
        m21 = matrix.m21
        m22 = matrix.m22
    }

    /**
     * Returns a copy of this [Matrix33].
     * @return [Matrix33]
     * @since 3.4.0
     */
    fun copy(): Matrix33 {
        return Matrix33(this)
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
        temp = m02.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = m10.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = m11.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = m12.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = m20.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = m21.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = m22.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj === this) return true
        if (obj is Matrix33) {
            val other = obj
            if (other.m00 == m00 && other.m01 == m01 && other.m02 == m02 && other.m10 == m10 && other.m11 == m11 && other.m12 == m12 && other.m20 == m20 && other.m21 == m21 && other.m22 == m22
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
        sb.append("[").append(m00).append(" ").append(m01).append(" ").append(m02).append("][")
            .append(m10).append(" ").append(m11).append(" ").append(m12).append("][")
            .append(m20).append(" ").append(m21).append(" ").append(m22).append("]")
        return sb.toString()
    }

    /**
     * Adds the given [Matrix33] to this [Matrix33]
     * returning this [Matrix33].
     * <pre>
     * this = this + m
    </pre> *
     * @param matrix the [Matrix33] to add
     * @return [Matrix33] this matrix
     */
    fun add(matrix: Matrix33): Matrix33 {
        m00 += matrix.m00
        m01 += matrix.m01
        m02 += matrix.m02
        m10 += matrix.m10
        m11 += matrix.m11
        m12 += matrix.m12
        m20 += matrix.m20
        m21 += matrix.m21
        m22 += matrix.m22
        return this
    }

    /**
     * Returns a new [Matrix33] that is the sum of this [Matrix33]
     * and the given [Matrix33].
     * <pre>
     * r = this + m
    </pre> *
     * @param matrix the [Matrix33] to add
     * @return [Matrix33] a new matrix containing the result
     */
    fun sum(matrix: Matrix33): Matrix33 {
        // make a copy of this matrix and perform the addition
        return copy().add(matrix)
    }

    /**
     * Subtracts the given [Matrix33] from this [Matrix33]
     * returning this [Matrix33].
     * <pre>
     * this = this - m
    </pre> *
     * @param matrix the [Matrix33] to subtract
     * @return [Matrix33] this matrix
     */
    fun subtract(matrix: Matrix33): Matrix33 {
        m00 -= matrix.m00
        m01 -= matrix.m01
        m02 -= matrix.m02
        m10 -= matrix.m10
        m11 -= matrix.m11
        m12 -= matrix.m12
        m20 -= matrix.m20
        m21 -= matrix.m21
        m22 -= matrix.m22
        return this
    }

    /**
     * Returns a new [Matrix33] that is the difference of this [Matrix33]
     * and the given [Matrix33].
     * <pre>
     * r = this - m
    </pre> *
     * @param matrix the [Matrix33] to subtract
     * @return [Matrix33] a new matrix containing the result
     */
    fun difference(matrix: Matrix33): Matrix33 {
        // make a copy of this matrix and perform the subtraction
        return copy().subtract(matrix)
    }

    /**
     * Multiplies this [Matrix33] by the given matrix [Matrix33]
     * returning this [Matrix33].
     * <pre>
     * this = this * m
    </pre> *
     * @param matrix the [Matrix33] to subtract
     * @return [Matrix33] this matrix
     */
    fun multiply(matrix: Matrix33): Matrix33 {
        val m00 = m00
        val m01 = m01
        val m02 = m02
        val m10 = m10
        val m11 = m11
        val m12 = m12
        val m20 = m20
        val m21 = m21
        val m22 = m22
        // row 1
        this.m00 = m00 * matrix.m00 + m01 * matrix.m10 + m02 * matrix.m20
        this.m01 = m00 * matrix.m01 + m01 * matrix.m11 + m02 * matrix.m21
        this.m02 = m00 * matrix.m02 + m01 * matrix.m12 + m02 * matrix.m22
        // row 2
        this.m10 = m10 * matrix.m00 + m11 * matrix.m10 + m12 * matrix.m20
        this.m11 = m10 * matrix.m01 + m11 * matrix.m11 + m12 * matrix.m21
        this.m12 = m10 * matrix.m02 + m11 * matrix.m12 + m12 * matrix.m22
        // row 3
        this.m20 = m20 * matrix.m00 + m21 * matrix.m10 + m22 * matrix.m20
        this.m21 = m20 * matrix.m01 + m21 * matrix.m11 + m22 * matrix.m21
        this.m22 = m20 * matrix.m02 + m21 * matrix.m12 + m22 * matrix.m22
        return this
    }

    /**
     * Returns a new [Matrix33] that is the product of this [Matrix33]
     * and the given [Matrix33].
     * <pre>
     * r = this * m
    </pre> *
     * @param matrix the [Matrix33] to multiply
     * @return [Matrix33] a new matrix containing the result
     */
    fun product(matrix: Matrix33): Matrix33 {
        // make a copy of this matrix and perform the multiplication
        return copy().multiply(matrix)
    }

    /**
     * Multiplies this [Matrix33] by the given [Vector3] and
     * places the result in the given [Vector3].
     * <pre>
     * v = this * v
    </pre> *
     * @param vector the [Vector3] to multiply
     * @return [Vector3] the vector result
     */
    fun multiply(vector: Vector3): Vector3 {
        val x: Double = vector.x
        val y: Double = vector.y
        val z: Double = vector.z
        vector.x = m00 * x + m01 * y + m02 * z
        vector.y = m10 * x + m11 * y + m12 * z
        vector.z = m20 * x + m21 * y + m22 * z
        return vector
    }

    /**
     * Multiplies this [Matrix33] by the given [Vector3] returning
     * the result in a new [Vector3].
     * <pre>
     * r = this * v
    </pre> *
     * @param vector the [Vector3] to multiply
     * @return [Vector3] the vector result
     */
    fun product(vector: Vector3): Vector3 {
        return this.multiply(vector.copy())
    }

    /**
     * Multiplies the given [Vector3] by this [Matrix33] and
     * places the result in the given [Vector3].
     *
     *  v = v<sup>T</sup> * this
     * @param vector the [Vector3] to multiply
     * @return [Vector3] the vector result
     */
    fun multiplyT(vector: Vector3): Vector3 {
        val x: Double = vector.x
        val y: Double = vector.y
        val z: Double = vector.z
        vector.x = m00 * x + m10 * y + m20 * z
        vector.y = m01 * x + m11 * y + m21 * z
        vector.z = m02 * x + m12 * y + m22 * z
        return vector
    }

    /**
     * Multiplies the given [Vector3] by this [Matrix33] returning
     * the result in a new [Vector3].
     *
     *  r = v<sup>T</sup> * this
     * @param vector the [Vector3] to multiply
     * @return [Vector3] the vector result
     */
    fun productT(vector: Vector3): Vector3 {
        return multiplyT(vector.copy())
    }

    /**
     * Multiplies this [Matrix33] by the given scalar and places
     * the result in this [Matrix33].
     * <pre>
     * this = this * scalar
    </pre> *
     * @param scalar the scalar to multiply by
     * @return [Matrix33] this matrix
     */
    fun multiply(scalar: Double): Matrix33 {
        m00 *= scalar
        m01 *= scalar
        m02 *= scalar
        m10 *= scalar
        m11 *= scalar
        m12 *= scalar
        m20 *= scalar
        m21 *= scalar
        m22 *= scalar
        return this
    }

    /**
     * Multiplies this [Matrix33] by the given scalar returning a
     * new [Matrix33] containing the result.
     * <pre>
     * r = this * scalar
    </pre> *
     * @param scalar the scalar to multiply by
     * @return [Matrix33] a new matrix containing the result
     */
    fun product(scalar: Double): Matrix33 {
        // make a copy of this matrix and perform the scalar multiplication
        return copy().multiply(scalar)
    }

    /**
     * Sets this [Matrix33] to an identity [Matrix33].
     * @return [Matrix33] this matrix
     */
    fun identity(): Matrix33 {
        m00 = 1.0
        m01 = 0.0
        m02 = 0.0
        m10 = 0.0
        m11 = 1.0
        m12 = 0.0
        m20 = 0.0
        m21 = 0.0
        m22 = 1.0
        return this
    }

    /**
     * Sets this [Matrix33] to the transpose of this [Matrix33].
     * @return [Matrix33] this matrix
     */
    fun transpose(): Matrix33 {
        var s: Double
        // switch 01 and 10
        s = m01
        m01 = m10
        m10 = s
        // switch 02 and 20
        s = m02
        m02 = m20
        m20 = s
        // switch 12 and 21
        s = m12
        m12 = m21
        m21 = s
        return this
    }

    /**
     * Returns the the transpose of this [Matrix33] in a new [Matrix33].
     * @return [Matrix33] a new matrix contianing the transpose
     */
    fun getTranspose(): Matrix33 {
        val rm = Matrix33()
        rm.m00 = m00
        rm.m01 = m10
        rm.m02 = m20
        rm.m10 = m01
        rm.m11 = m11
        rm.m12 = m21
        rm.m20 = m02
        rm.m21 = m12
        rm.m22 = m22
        return rm
    }

    /**
     * Returns the determinant of this [Matrix33].
     * @return double
     */
    fun determinant(): Double {
        return m00 * m11 * m22 + m01 * m12 * m20 + m02 * m10 * m21 - m20 * m11 * m02 - m21 * m12 * m00 - m22 * m10 * m01
    }

    /**
     * Performs the inverse of this [Matrix33] and places the
     * result in this [Matrix33].
     * @return [Matrix33] this matrix
     */
    fun invert(): Matrix33 {
        // get the determinant
        var det = determinant()
        // check for zero determinant
        if (abs(det) > Epsilon.E) {
            det = 1.0 / det
        }

        // compute the cofactor determinants and apply the signs
        // and transpose the matrix and multiply by the inverse 
        // of the determinant
        val m00 = det * (m11 * m22 - m12 * m21)
        val m01 =
            -det * (m01 * m22 - m21 * m02) // actually m10 in the cofactor matrix
        val m02 =
            det * (this.m01 * m12 - m11 * m02) // actually m20 in the cofactor matrix
        val m10 =
            -det * (m10 * m22 - m20 * m12) // actually m01 in the cofactor matrix
        val m11 = det * (this.m00 * m22 - m20 * this.m02)
        val m12 =
            -det * (this.m00 * m12 - this.m10 * this.m02) // actually m21 in the cofactor matrix
        val m20 =
            det * (this.m10 * m21 - m20 * this.m11) // actually m02 in the cofactor matrix
        val m21 =
            -det * (this.m00 * m21 - this.m20 * this.m01) // actually m12 in the cofactor matrix
        val m22 = det * (this.m00 * this.m11 - this.m10 * this.m01)
        this.m00 = m00
        this.m01 = m01
        this.m02 = m02
        this.m10 = m10
        this.m11 = m11
        this.m12 = m12
        this.m20 = m20
        this.m21 = m21
        this.m22 = m22
        return this
    }// make a copy of this matrix and perform the inversion

    /**
     * Returns a new [Matrix33] containing the inverse of this [Matrix33].
     * @return [Matrix33] a new matrix containing the result
     */
    val inverse: Matrix33
        get() =// make a copy of this matrix and perform the inversion
            copy().invert()

    /**
     * Solves the system of linear equations:
     *
     *  Ax = b
     * Multiply by A<sup>-1</sup> on both sides
     * x = A<sup>-1</sup>b
     * @param b the b [Vector3]
     * @return [Vector3] the x vector
     */
    fun solve33(b: Vector3): Vector3 {
        // get the determinant
        var det = determinant()
        // check for zero determinant
        if (abs(det) > Epsilon.E) {
            det = 1.0 / det
        }
        val r = Vector3()
        val m00 = m11 * m22 - m12 * m21
        val m01 = -m01 * m22 + m21 * m02
        val m02 = this.m01 * m12 - m11 * m02
        val m10 = -m10 * m22 + m20 * m12
        val m11 = this.m00 * m22 - m20 * this.m02
        val m12 = -this.m00 * m12 + this.m10 * this.m02
        val m20 = this.m10 * m21 - m20 * this.m11
        val m21 = -this.m00 * m21 + this.m20 * this.m01
        val m22 = this.m00 * this.m11 - this.m10 * this.m01
        r.x = det * (m00 * b.x + m01 * b.y + m02 * b.z)
        r.y = det * (m10 * b.x + m11 * b.y + m12 * b.z)
        r.z = det * (m20 * b.x + m21 * b.y + m22 * b.z)
        return r
    }

    /**
     * Solves the system of linear equations:
     *
     *  Ax = b
     * Multiply by A<sup>-1</sup> on both sides
     * x = A<sup>-1</sup>b
     * @param b the b [Vector2]
     * @return [Vector2] the x vector
     */
    fun solve22(b: Vector2): Vector2 {
        // get the 2D determinant
        var det = m00 * m11 - m01 * m10
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