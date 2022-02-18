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
package org.dyn4j

import kotlin.jvm.JvmStatic

/**
 * Class used to convert units.
 *
 *
 * dyn4j uses meters-kilograms-seconds (MKS) units by default.  This class can be used
 * to convert to and from MKS.
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 */
object UnitConversion {
    /** 1 foot = [.FOOT_TO_METER] meters  */
    const val FOOT_TO_METER = 0.0254 * 12.0

    /** 1 meter = [.METER_TO_FOOT] feet  */
    const val METER_TO_FOOT = 1.0 / FOOT_TO_METER

    /** 1 slug = [.SLUG_TO_KILOGRAM] kilograms  */
    const val SLUG_TO_KILOGRAM = 14.5939029

    /** 1 kilogram = [.KILOGRAM_TO_SLUG] slugs  */
    const val KILOGRAM_TO_SLUG = 1.0 / SLUG_TO_KILOGRAM

    /** 1 pound-mass = [.POUND_TO_KILOGRAM] kilograms  */
    const val POUND_TO_KILOGRAM = 0.45359237

    /** 1 kilogram = [.KILOGRAM_TO_POUND] pounds  */
    const val KILOGRAM_TO_POUND = 1.0 / POUND_TO_KILOGRAM

    /** 1 pound-force = [.POUND_TO_NEWTON] newtons  */
    const val POUND_TO_NEWTON = 4.448222

    /** 1 newton = [.NEWTON_TO_POUND] pound-force  */
    const val NEWTON_TO_POUND = 1.0 / POUND_TO_NEWTON

    /** 1 foot-pound = [.FOOT_POUND_TO_NEWTON_METER] newton-meters  */
    const val FOOT_POUND_TO_NEWTON_METER = 0.7375621

    /** 1 newton-meter = [.NEWTON_METER_TO_FOOT_POUND] foot-pounds  */
    const val NEWTON_METER_TO_FOOT_POUND = 1.0 / FOOT_POUND_TO_NEWTON_METER
    // FPS (mixture of Gravitational and Engineering approaches) to MKS
    // Length Conversions
    /**
     * Converts feet to meters.
     * @param feet the length value in feet
     * @return double the length value in meters
     */
    @JvmStatic
    fun feetToMeters(feet: Double): Double {
        return feet * FOOT_TO_METER
    }
    // Mass Conversions
    /**
     * Converts slugs to kilograms.
     * @param slugs the mass value in slugs
     * @return double the mass value in kilograms
     */
    @JvmStatic
    fun slugsToKilograms(slugs: Double): Double {
        return slugs * SLUG_TO_KILOGRAM
    }

    /**
     * Converts pound-mass to kilograms.
     * @param pound the mass value in pound-masses
     * @return double the mass value in kilograms
     */
    @JvmStatic
    fun poundsToKilograms(pound: Double): Double {
        return pound * POUND_TO_KILOGRAM
    }
    // Velocity Conversions
    /**
     * Converts feet per second to meters per second.
     * @param feetPerSecond the velocity in feet per second
     * @return double the velocity in meters per second
     */
    @JvmStatic
    fun feetPerSecondToMetersPerSecond(feetPerSecond: Double): Double {
        return feetPerSecond * METER_TO_FOOT
    }
    // Force Conversions
    /**
     * Converts pound-force to newtons.
     * @param pound the force value in pound-force
     * @return double the force value in newtons
     */
    @JvmStatic
    fun poundsToNewtons(pound: Double): Double {
        return pound * POUND_TO_NEWTON
    }
    // Torque Conversions
    /**
     * Converts foot-pounds to newton-meters.
     * @param footPound the torque value in foot-pounds
     * @return double the torque value in newton-meters
     */
    @JvmStatic
    fun footPoundsToNewtonMeters(footPound: Double): Double {
        return footPound * FOOT_POUND_TO_NEWTON_METER
    }
    // MKS to FPS (mixture of Gravitational and Engineering approaches)
    // Length Conversions
    /**
     * Converts meters to feet.
     * @param meters the length value in meters
     * @return double the length value in feet
     */
    @JvmStatic
    fun metersToFeet(meters: Double): Double {
        return meters * METER_TO_FOOT
    }
    // Mass Conversions
    /**
     * Converts kilograms to slugs.
     * @param kilograms the mass value in kilograms
     * @return double the mass value in slugs
     */
    @JvmStatic
    fun kilogramsToSlugs(kilograms: Double): Double {
        return kilograms * KILOGRAM_TO_SLUG
    }

    /**
     * Converts kilograms to pound-mass.
     * @param kilograms the mass value in kilograms
     * @return double the mass value in pound-masses
     */
    @JvmStatic
    fun kilogramsToPounds(kilograms: Double): Double {
        return kilograms * KILOGRAM_TO_POUND
    }
    // Velocity Conversions
    /**
     * Converts meters per second to feet per second.
     * @param metersPerSecond the velocity in meters per second
     * @return double the velocity in feet per second
     */
    @JvmStatic
    fun metersPerSecondToFeetPerSecond(metersPerSecond: Double): Double {
        return metersPerSecond * FOOT_TO_METER
    }
    // Force Conversions
    /**
     * Converts newtons to pound-force.
     * @param newtons the force value in newtons
     * @return double the force value in pound-force
     */
    @JvmStatic
    fun newtonsToPounds(newtons: Double): Double {
        return newtons * NEWTON_TO_POUND
    }
    // Torque Conversions
    /**
     * Converts newton-meters to foot-pounds.
     * @param newtonMeters the torque value in newton-meters
     * @return double the torque value in foot-pounds
     */
    @JvmStatic
    fun newtonMetersToFootPounds(newtonMeters: Double): Double {
        return newtonMeters * NEWTON_METER_TO_FOOT_POUND
    }
}