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

import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Transform

/**
 * Represents a delegating [NarrowphaseDetector] that uses a primary [NarrowphaseDetector] and
 * fallback [NarrowphaseDetector].
 *
 *
 * The fallback [NarrowphaseDetector] is used when **any** of the [FallbackCondition]s
 * added have been met.
 *
 *
 * [FallbackCondition]s will be checked in order and will stop on the first matched condition. By default the conditions
 * are ordered in the order they are added unless a condition specifies a sortIndex.
 *
 *
 * For example, when the [Sat] algorithm is used, some shapes are not supported. A [TypedFallbackCondition] can be
 * used to fallback to the [Gjk] algorithm:
 * <pre>
 * FallbackNarrowphaseDetector detector = new FallbackNarrowphaseDetector(new Sat(), new Gjk());
 * // any Slice collisions will be handled by Gjk instead of Sat
 * detector.addCondition(new SingleTypedFallbackCondition(Slice.class));</pre>
 * New condition types can be added by implementing the [FallbackCondition] interface. Doing so can lead to
 * interesting options like custom collision detectors for specific cases or custom shapes.
 *
 *
 * The primary and fallback detectors can also be [FallbackNarrowphaseDetector]s as well allowing for a chain of
 * fallbacks.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.1.5
 */
class FallbackNarrowphaseDetector : NarrowphaseDetector {

    /** The primary [NarrowphaseDetector]  */
    val primaryNarrowphaseDetector: NarrowphaseDetector

    /** The fallback [NarrowphaseDetector]  */
    val fallbackNarrowphaseDetector: NarrowphaseDetector

    /** The conditions for when to use the fallback [NarrowphaseDetector]  */
    var fallbackConditions: MutableList<FallbackCondition>

    /**
     * Minimal constructor.
     * @param primaryNarrowphaseDetector the primary [NarrowphaseDetector]
     * @param fallbackNarrowphaseDetector the fallback [NarrowphaseDetector]
     * @throws NullPointerException if either the primary or fallback [NarrowphaseDetector]s are null
     */
    constructor(primaryNarrowphaseDetector: NarrowphaseDetector, fallbackNarrowphaseDetector: NarrowphaseDetector)
            :this(primaryNarrowphaseDetector, fallbackNarrowphaseDetector, ArrayList())

    /**
     * Full constructor.
     * @param primaryNarrowphaseDetector the primary [NarrowphaseDetector]
     * @param fallbackNarrowphaseDetector the fallback [NarrowphaseDetector]
     * @param conditions the fallback conditions
     * @throws NullPointerException if either the primary or fallback [NarrowphaseDetector]s are null
     */
    constructor(
        primaryNarrowphaseDetector: NarrowphaseDetector,
        fallbackNarrowphaseDetector: NarrowphaseDetector,
        conditions: MutableList<FallbackCondition>?
    ) {
        this.primaryNarrowphaseDetector = primaryNarrowphaseDetector
        this.fallbackNarrowphaseDetector = fallbackNarrowphaseDetector
        if (conditions != null) {
            fallbackConditions = conditions
        } else {
            fallbackConditions = ArrayList()
        }
    }

    /**
     * Adds the given condition to the list of fallback conditions.
     * @param condition the condition
     */
    fun addCondition(condition: FallbackCondition) {
        fallbackConditions!!.add(condition)
        fallbackConditions!!.sort()
    }

    /**
     * Removes the given condition to the list of fallback conditions and
     * returns true if the operation was successful.
     * @param condition the condition
     * @return boolean
     */
    fun removeCondition(condition: FallbackCondition): Boolean {
        return fallbackConditions!!.remove(condition)
    }

    /**
     * Returns true if the given condition is contained in this detector.
     * @param condition the fallback condition
     * @return boolean
     */
    fun containsCondition(condition: FallbackCondition): Boolean {
        return fallbackConditions!!.contains(condition)
    }

    /**
     * Returns the number of fallback conditions.
     * @return int
     */
    fun getConditionCount(): Int {
        return fallbackConditions!!.size
    }

    /**
     * Returns the fallback condition at the given index.
     * @param index the index
     * @return [FallbackCondition]
     * @throws IndexOutOfBoundsException if index is not between 0 and [.getConditionCount]
     */
    fun getCondition(index: Int): FallbackCondition? {
        return fallbackConditions!![index]
    }

    /**
     * Returns true if the fallback [NarrowphaseDetector] should be used rather
     * than the primary.
     * @param convex1 the first convex
     * @param convex2 the second convex
     * @return boolean
     */
    fun isFallbackRequired(convex1: Convex?, convex2: Convex?): Boolean {
        val size = fallbackConditions!!.size
        for (i in 0 until size) {
            val condition = fallbackConditions!![i]
            if (condition != null && condition.isMatch(convex1, convex2)) {
                return true
            }
        }
        return false
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.narrowphase.NarrowphaseDetector#detect(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform)
	 */
    override fun detect(convex1: Convex, transform1: Transform, convex2: Convex, transform2: Transform): Boolean {
        return if (isFallbackRequired(convex1, convex2)) {
            fallbackNarrowphaseDetector!!.detect(convex1!!, transform1!!, convex2!!, transform2!!)
        } else primaryNarrowphaseDetector!!.detect(convex1!!, transform1!!, convex2!!, transform2!!)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.narrowphase.NarrowphaseDetector#detect(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.collision.narrowphase.Penetration)
	 */
    override fun detect(convex1: Convex, transform1: Transform, convex2: Convex, transform2: Transform, penetration: Penetration?): Boolean {
        return if (isFallbackRequired(convex1, convex2)) {
            fallbackNarrowphaseDetector!!.detect(
                convex1!!,
                transform1!!,
                convex2!!,
                transform2!!,
                penetration!!
            )
        } else primaryNarrowphaseDetector!!.detect(
            convex1!!,
            transform1!!,
            convex2!!,
            transform2!!,
            penetration!!
        )
    }

}