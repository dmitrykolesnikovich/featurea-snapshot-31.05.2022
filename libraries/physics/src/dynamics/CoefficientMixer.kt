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
package org.dyn4j.dynamics

import kotlin.math.max
import kotlin.math.sqrt

/**
 * Interface used to customize the way friction and restitution coefficients are mixed.
 *
 *
 * The [.DEFAULT_MIXER] performs the following operations for friction and
 * restitution mixing respectively:
 * <pre> sqrt(friction1 * friction2)
 * max(restitution1, restitution2)</pre>
 * @author William Bittle
 * @version 2.0.0
 * @since 1.0.0
 */
interface CoefficientMixer {
    /**
     * Method used to mix the coefficients of friction of two [BodyFixture]s.
     * @param friction1 the coefficient of friction for the first [BodyFixture]
     * @param friction2 the coefficient of friction for the second [BodyFixture]
     * @return double
     */
    fun mixFriction(friction1: Double, friction2: Double): Double

    /**
     * Method used to mix the coefficients of restitution of two [BodyFixture]s.
     * @param restitution1 the coefficient of restitution for the first [BodyFixture]
     * @param restitution2 the coefficient of restitution for the second [BodyFixture]
     * @return double
     */
    fun mixRestitution(restitution1: Double, restitution2: Double): Double

    companion object {
        /** The default dynamics mixer  */
        val DEFAULT_MIXER: CoefficientMixer = object : CoefficientMixer {
            /* (non-Javadoc)
		 * @see org.dyn4j.dynamics.DynamicsMixer#mixFriction(double, double)
		 */
            override fun mixFriction(friction1: Double, friction2: Double): Double {
                return sqrt(friction1 * friction2)
            }

            /* (non-Javadoc)
		 * @see org.dyn4j.dynamics.DynamicsMixer#mixRestitution(double, double)
		 */
            override fun mixRestitution(
                restitution1: Double,
                restitution2: Double
            ): Double {
                return max(restitution1, restitution2)
            }
        }
    }
}