/*
 * Copyright (c) 2010-2017 William Bittle  http://www.dyn4j.org/
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

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.math.abs
import kotlin.math.exp

/**
 * This is an implementation of multi-precision decimals based on the original work by Jonathan Richard Shewchuk,
 * "Routines for Arbitrary Precision Floating-point Arithmetic and Fast Robust Geometric Predicates".
 *
 *
 * More information about the algorithms, the original code in C and proofs of correctness can all
 * be found at [http://www.cs.cmu.edu/~quake/robust.html](http://www.cs.cmu.edu/~quake/robust.html)
 *
 *
 * Short description:
 * The value of this [AdaptiveDecimal] is represented as the sum of some components,
 * where each component is a double value.
 * The components must be stored in increasing magnitude order, but there can be any amount of
 * zeros between components. The components must also satisfy the non-overlapping property, that is
 * the corresponding bit representation of adjacent components must not overlap. See [.checkInvariants]
 * and the corresponding paper for more info.
 *
 *
 * This code **requires** that the floating point model is IEEE-754 with round-to-even in order
 * to work properly in all cases and fulfill the above properties. This is not a problem because this
 * is the default and only model the Java specification describes.
 *
 * @author Manolis Tsamis
 * @version 3.4.0
 * @since 3.4.0
 */
class AdaptiveDecimal {
    /** The array storing this [AdaptiveDecimal]'s component values  */
    private val components: DoubleArray

    /** The number of components this [AdaptiveDecimal] currently contains  */
    private var size: Int

    /**
     * Creates a new [AdaptiveDecimal] with the specified length.
     * The initial [AdaptiveDecimal] created does not contains any components.
     *
     * @param length The maximum number of components this [AdaptiveDecimal] can store
     */
    constructor(length: Int) {
        if (length <= 0) {
            throw IllegalArgumentException()
        }
        components = DoubleArray(length)
        size = 0
    }

    /**
     * Deep copy constructor.
     * @param other the [AdaptiveDecimal] to copy from
     */
    constructor(other: AdaptiveDecimal) {
        components = other.components.copyOf()
        size = other.size
    }

    /**
     * Internal helper constructor to create a [AdaptiveDecimal] with two components
     * @param a0 the component with the smallest magnitude
     * @param a1 the component with the largest magnitude
     */
    protected constructor(a0: Double, a1: Double) {
        components = doubleArrayOf(a0, a1)
        size = 2
    }

    /**
     * @return The number of components this [AdaptiveDecimal] currently has
     */
    fun size(): Int {
        return size
    }

    /**
     * @return The maximum number of components this [AdaptiveDecimal] can hold
     */
    fun capacity(): Int {
        return components.size
    }

    /**
     * @return A deep copy of this [AdaptiveDecimal]
     */
    fun copy(): AdaptiveDecimal {
        return AdaptiveDecimal(this)
    }

    /**
     * Copies the components of another [AdaptiveDecimal] into this.
     * The capacity of the this [AdaptiveDecimal] is not modified and it should
     * be enough to hold all the components.
     *
     * @param other The [AdaptiveDecimal] to copy from
     */
    /*
    Object src,  int  srcPos, Object dest, int destPos, int length
    */
    fun copyFrom(other: AdaptiveDecimal) {
        other.components.copyInto(components)
        size = other.size
    }

    /**
     * @param index index of the component to return
     * @return the component at the specified position
     * @throws IndexOutOfBoundsException if the index is not in the range [0, size)
     */
    operator fun get(index: Int): Double {
        if (index < 0 || index >= size()) {
            throw IndexOutOfBoundsException()
        }
        return components[index]
    }

    /**
     * Appends a new component after all the existing components.
     *
     * @param value The component
     * @return this [AdaptiveDecimal]
     * @throws IndexOutOfBoundsException if this [AdaptiveDecimal] has no capacity for more components
     */
    fun append(value: Double): AdaptiveDecimal {
        if (size >= capacity()) {
            throw IndexOutOfBoundsException()
        }
        components[size++] = value
        return this
    }

    /**
     * Appends a new component after all the existing components, but only
     * if it has a non zero value.
     *
     * @param value The component
     * @return this [AdaptiveDecimal]
     */
    fun appendNonZero(value: Double): AdaptiveDecimal {
        if (value != 0.0) {
            this.append(value)
        }
        return this
    }

    /**
     * Returns a boolean value describing if this [AdaptiveDecimal] is a valid
     * representation as described in the header of this class.
     * Checks for the magnitude and non-overlapping property.
     * The invariants can be violated if bad input components are appended to this [AdaptiveDecimal].
     * The append methods do not check for those conditions because there is a big overhead for the check.
     * The output of the exposed operations must satisfy the invariants, given that their input also does so.
     *
     * @return true iff this [AdaptiveDecimal] satisfies the described invariants
     */
    @OptIn(kotlin.ExperimentalStdlibApi::class)
    fun checkInvariants(): Boolean {
        if (size == 0) {
            return true
        }

        // Holds the last value that needs to be checked
        // This skips all 0 except for mabe the first component (which is ok to be 0)
        var lastValue = this[0]
        for (i in 1 until size) {
            val currentValue = this[i]
            if (currentValue != 0.0) {
                // the magnitude of previous non-zero elements must be smaller
                if (abs(currentValue) < abs(lastValue)) {
                    return false
                }

                // A number n in the floating point representation can be written as
                // n = +/- 0.1xxxxx...x * 2 ^ exp
                //   mantissa ^~~~~~~~^       ^~~ exponent
                // where the above x are the binary digits either 0 or 1 except for the first digit after the decimal point
                // which is always 1. The exponent part is essentially a shift of the decimal point

                // If we have two numbers a, b with a > b then they're non-overlapping if a's lower set bit to
                // be to the left of b's higher set bit *after those have been scaled by their exponents accordingly*.
                // The sign is irrelevant for this.
                // a = -0.10101011 * 2^5 = -10101.011
                // b = 0.111001 * 2^-1 = 0.0111001

                // if we align a and b we can see that they overlap
                // -10101.011
                //      0.0111001
                //         ^^ overlap
                // They would also overlap if b's exponent was -2 (for a single bit)
                // only if b's exponent where less than -2 then there would be no overlap

                // get the value of the exponents
                val exp1: Int = exp(lastValue).toInt()
                val exp2: Int = exp(currentValue).toInt()

                // get the significants (the binary representation of the mantissa part)
                // The first, always 1 bit is not actually stored, so we'll add it ourselves
                val mantissa1: Long = lastValue.toBits() and SIGNIF_BIT_MASK or IMPLICIT_MANTISSA_BIT
                val mantissa2: Long = currentValue.toBits() and SIGNIF_BIT_MASK or IMPLICIT_MANTISSA_BIT

                // We want to find the logical location of the most significant bit in the smallest component
                // and of the least significant bit in the largest component, accounting for the exponents as well
                // In the following convention bit numbering is done from the higher to the lowest bit.
                // Note that the first bit of the double representation won't be the first in the long below.
                // This is logical since the mantissa is fewer bits wide than a long, but it's not a problem
                // since both the msd and lsd will have the same difference.
                var msd1: Int = mantissa1.countLeadingZeroBits()
                var lsd2: Int = Long.SIZE_BITS - mantissa2.countTrailingZeroBits() - 1

                // Apply the exponents
                // The exponents are essentially shifts in the bit positions
                msd1 -= exp1
                lsd2 -= exp2

                // Finally check for the non-overlapping property
                // We want the lower bit of the currentValue's representation to be higher than
                // lastValue's higher bit
                if (lsd2 >= msd1) {
                    return false
                }

                // Update the last non-zero value
                lastValue = currentValue
            }
        }
        return true
    }

    /**
     * @throws IllegalStateException iff [.checkInvariants] returns false
     */
    fun ensureInvariants() {
        check(checkInvariants())
    }

    /**
     * Removes the components of this [AdaptiveDecimal].
     *
     * @return this [AdaptiveDecimal]
     */
    fun clear(): AdaptiveDecimal {
        size = 0
        return this
    }

    /**
     * Removes all the components with zero value from this [AdaptiveDecimal].
     *
     * @return this [AdaptiveDecimal]
     */
    fun removeZeros(): AdaptiveDecimal {
        val oldSize = size
        this.clear()
        for (i in 0 until oldSize) {
            appendNonZero(components[i])
        }
        return this
    }

    /**
     * Ensures this [AdaptiveDecimal] has at least one component.
     * That is, appends the zero value if there are currently zero components.
     *
     * @return this [AdaptiveDecimal]
     */
    fun normalize(): AdaptiveDecimal {
        if (size == 0) {
            append(0.0)
        }
        return this
    }

    /**
     * Negates the logical value of this [AdaptiveDecimal].
     * This can be used with sum to perform subtraction .
     *
     * @return this [AdaptiveDecimal]
     */
    fun negate(): AdaptiveDecimal {
        for (i in 0 until size) {
            components[i] = -components[i]
        }
        return this
    }

    /**
     * Computes an approximation for the value of this [AdaptiveDecimal] that fits in a double.
     *
     * @return The approximation
     */
    val estimation: Double
        get() {
            var value = 0.0
            for (i in 0 until size) {
                value += components[i]
            }
            return value
        }

    /**
     * Helper method to implement the sum procedure.
     * Sums the remaining components of a single [AdaptiveDecimal] to the result
     * and the initial carry value from previous computations
     *
     * @param carry The carry from previous computations
     * @param e The [AdaptiveDecimal] that probably has more components
     * @param eIndex The index to the next component of e that has to be examined
     * @param result The [AdaptiveDecimal] in which the result is stored
     * @return The result
     */
    fun sumEpilogue(
        carry: Double,
        e: AdaptiveDecimal,
        eIndex: Int,
        result: AdaptiveDecimal
    ): AdaptiveDecimal {
        var carry = carry
        var eIndex = eIndex
        while (eIndex < e.size()) {
            val enow = e[eIndex]
            val sum = carry + enow
            val error =
                getErrorComponentFromSum(carry, enow, sum)
            carry = sum
            result.appendNonZero(error)
            eIndex++
        }
        result.appendNonZero(carry)
        result.normalize()
        return result
    }
    /**
     * Performs the addition of this [AdaptiveDecimal] with the given [AdaptiveDecimal] f
     * and stores the result in the provided [AdaptiveDecimal] `result`.
     * If `result` is null it allocates a new [AdaptiveDecimal] with the
     * appropriate capacity to store the result. Otherwise the components of `result`
     * are cleared and the resulting value is stored there, assuming there is enough capacity.
     *
     * Be careful that it must be `f`  `result`  `this`.
     *
     * @param f The [AdaptiveDecimal] to sum with this [AdaptiveDecimal]
     * @param result The [AdaptiveDecimal] in which the sum is stored or null to allocate a new one
     * @return The result
     */
    /**
     * Performs addition and also allocates a new [AdaptiveDecimal] with the
     * appropriate capacity to store the result.
     *
     * @param f The [AdaptiveDecimal] to sum with this [AdaptiveDecimal]
     * @return A new [AdaptiveDecimal] that holds the result of the addition
     * @see .sum
     */
    @JvmOverloads
    fun sum(f: AdaptiveDecimal, result: AdaptiveDecimal = AdaptiveDecimal(size() + f.size())): AdaptiveDecimal {
        // The following algorithm performs addition of two AdaptiveDecimals
        // It is based on the original fast_expansion_sum_zeroelim function written
        // by the author of the said paper

        // allocate a new instance of sufficient size if result is null or just clear
        result?.clear()
        val e = this

        // eIndex and fIndex are used to iterate the components of e and f accordingly
        var eIndex = 0
        var fIndex = 0
        // enow = e[eIndex] and fnow = f[fIndex] is the current component examined for e and f
        var enow = e[eIndex]
        var fnow = f[fIndex]

        // sum will be used to store the sum needed for the getErrorComponentFromSum method
        // error will store the error as returned from getErrorComponentFromSum method
        // carry will store the value that will be summed in the next sum
        var carry: Double
        var sum: Double
        var error: Double

        // each time we need the next component in increasing magnitude
        // (fnow > enow) == (fnow > -enow)
        if (abs(enow) <= abs(fnow)) {
            carry = enow
            eIndex++
            if (eIndex >= e.size()) {
                return sumEpilogue(carry, f, fIndex, result)
            }
            enow = e[eIndex]
        } else {
            carry = fnow
            fIndex++
            if (fIndex >= f.size()) {
                return sumEpilogue(carry, e, eIndex, result)
            }
            fnow = f[fIndex]
        }
        while (true) {
            if (abs(enow) <= abs(fnow)) {
                // perform the addition with the carry from the previous iterarion
                error = getErrorComponentFromSum(
                    carry,
                    enow,
                    carry + enow.also { sum = it }
                )
                eIndex++
                carry = sum

                // append + zero elimination
                result.appendNonZero(error)

                // if this AdaptiveDecimal has no more components then move to the epilogue
                if (eIndex >= e.size()) {
                    return sumEpilogue(carry, f, fIndex, result)
                }
                enow = e[eIndex]
            } else {
                // perform the addition with the carry from the previous iterarion
                error = getErrorComponentFromSum(
                    carry,
                    fnow,
                    carry + fnow.also { sum = it }
                )
                fIndex++
                carry = sum

                // append + zero elimination
                result.appendNonZero(error)

                // if this AdaptiveDecimal has no more components then move to the epilogue
                if (fIndex >= f.size()) {
                    return sumEpilogue(carry, e, eIndex, result)
                }
                fnow = f[fIndex]
            }
        }
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder(size * 10)
        sb.append('[')
        for (i in 0 until size) {
            sb.append(components[i])
            if (i < size - 1) {
                sb.append(", ")
            }
        }
        sb.append("] ~= ")
        sb.append(estimation)
        return sb.toString()
    }

    companion object {
        /** The mask to get the mantissa of a double as per the standard; Taken from [DoubleConsts.SIGN_BIT_MASK]  */
        private const val SIGNIF_BIT_MASK = 0x000FFFFFFFFFFFFFL

        /** The implicit bit in the mantissa of a double  */
        private const val IMPLICIT_MANTISSA_BIT = 0x0010000000000000L

        /**
         * Creates a [AdaptiveDecimal] with only a single component.
         *
         * @param value The component
         * @return [AdaptiveDecimal]
         */
        @JvmStatic
        fun valueOf(value: Double): AdaptiveDecimal {
            return AdaptiveDecimal(1).append(value)
        }

        /**
         * Creates a [AdaptiveDecimal] that holds the result of the
         * addition of two double values.
         *
         * @param a The first value
         * @param b The second value
         * @return A new [AdaptiveDecimal] that holds the resulting sum
         */
        @JvmStatic
        fun fromSum(a: Double, b: Double): AdaptiveDecimal {
            val sum = a + b
            return AdaptiveDecimal(
                getErrorComponentFromSum(
                    a,
                    b,
                    sum
                ), sum
            )
        }

        /**
         * Creates a [AdaptiveDecimal] that holds the result of the
         * difference of two double values.
         *
         * @param a The first value
         * @param b The second value
         * @return A new [AdaptiveDecimal] that holds the resulting difference
         */
        @JvmStatic
        fun fromDiff(a: Double, b: Double): AdaptiveDecimal {
            val diff = a - b
            return AdaptiveDecimal(
                getErrorComponentFromDifference(
                    a,
                    b,
                    diff
                ), diff
            )
        }

        /**
         * Given two unrolled expansions (a0, a1) and (b0, b1) performs the difference
         * (a0, a1) - (b0, b1) and stores the 4 component result in the given [AdaptiveDecimal] `result`.
         * In the same way as with [AdaptiveDecimal.sum] if `result` is null
         * a new one is allocated, otherwise the existing is cleared and used.
         * Does not perform zero elimination.
         * This is also a helper method to allow fast computation of the cross product
         * without the overhead of creating new [AdaptiveDecimal] and performing
         * the generalized sum procedure.
         *
         * @param a0 The first component of a
         * @param a1 The second component of a
         * @param b0 The first component of b
         * @param b1 The second component of b
         * @param result The [AdaptiveDecimal] in which the difference is stored or null to allocate a new one
         * @return The result
         */
        @JvmStatic
        fun fromDiff(
            a0: Double,
            a1: Double,
            b0: Double,
            b1: Double,
            result: AdaptiveDecimal = AdaptiveDecimal(4)
        ): AdaptiveDecimal {
            // the exact order of those operations is necessary for correct functionality 
            // This is a rewrite of the corresponding Two_Two_Diff macro in the original code

            // allocate a new instance of sufficient size if result is null or just clear
            result.clear()

            // x0-x1-x2-x3 store the resulting components with increasing magnitude
            val x0: Double
            val x1: Double
            val x2: Double
            var x3: Double

            // variable to store immediate results for each pair of Diff/Sum
            var imm: Double

            // variables to store immediate results across the two pairs 
            val imm1: Double
            var imm2: Double

            // Diff (a0, a1) - b0, result = (x0, imm1, imm2)
            x0 = getErrorComponentFromDifference(
                a0,
                b0,
                a0 - b0.also { imm = it }
            )
            imm1 = getErrorComponentFromSum(
                a1,
                imm,
                a1 + imm.also { imm2 = it }
            )

            // Diff (imm1, imm2) - b1, result = (x1, x2, x3)
            x1 = getErrorComponentFromDifference(imm1, b1, imm1 - b1.also { imm = it })
            x2 = getErrorComponentFromSum(imm2, imm, imm2 + imm.also { x3 = it })
            result.append(x0)
            result.append(x1)
            result.append(x2)
            result.append(x3)
            return result!!
        }

        /**
         * Creates a [AdaptiveDecimal] that holds the result of the
         * product of two double values.
         *
         * @param a The first value
         * @param b The second value
         * @return A new [AdaptiveDecimal] that holds the resulting product
         */
        @JvmStatic
        fun fromProduct(a: Double, b: Double): AdaptiveDecimal {
            val product = a * b
            return AdaptiveDecimal(getErrorComponentFromProduct(a, b, product), a * b)
        }

        /**
         * Given two values a, b and their sum = fl(a + b) calculates the value error for which
         * fl(a) + fl(b) = fl(a + b) + fl(error).
         *
         * @param a The first value
         * @param b The second value
         * @param sum Their sum, must always be sum = fl(a + b)
         * @return The error described above
         */
        @JvmStatic
        fun getErrorComponentFromSum(a: Double, b: Double, sum: Double): Double {
            // the exact order of those operations is necessary for correct functionality 
            val bvirt = sum - a
            val avirt = sum - bvirt
            val bround = b - bvirt
            val around = a - avirt
            return around + bround
        }

        /**
         * Given two values a, b and their difference = fl(a - b) calculates the value error for which
         * fl(a) - fl(b) = fl(a - b) + fl(error).
         *
         * @param a The first value
         * @param b The second value
         * @param diff Their difference, must always be diff = fl(a - b)
         * @return The error described above
         */
        @JvmStatic
        fun getErrorComponentFromDifference(a: Double, b: Double, diff: Double): Double {
            // the exact order of those operations is necessary for correct functionality 
            val bvirt = a - diff
            val avirt = diff + bvirt
            val bround = bvirt - b
            val around = a - avirt
            return around + bround
        }

        /**
         * Given two values a, b and their product = fl(a * b) calculates the value error for which
         * fl(a) * fl(b) = fl(a * b) + fl(error).
         *
         * @param a The first value
         * @param b The second value
         * @param product Their product, must always be product = fl(a * b)
         * @return The error described above
         */
        @JvmStatic
        fun getErrorComponentFromProduct(a: Double, b: Double, product: Double): Double {
            // the exact order of those operations is necessary for correct functionality 

            // split a in two parts
            val ac: Double = RobustGeometry.SPLITTER * a
            val abig = ac - a
            val ahi = ac - abig
            val alo = a - ahi

            // split b in two parts
            val bc: Double = RobustGeometry.SPLITTER * b
            val bbig = bc - b
            val bhi = bc - bbig
            val blo = b - bhi
            val error1 = product - ahi * bhi
            val error2 = error1 - alo * bhi
            val error3 = error2 - ahi * blo
            return alo * blo - error3
        }
    }
}