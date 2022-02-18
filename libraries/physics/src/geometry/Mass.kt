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
import kotlin.jvm.JvmStatic

/**
 * Represents [Mass] data for an object about a given point.
 *
 *
 * Stores the center of mass, the mass, and inertia.
 *
 *
 * The center point may be something other than the origin (0, 0).  In this case, the mass and
 * inertia are about this point, not the origin.
 *
 *
 * A [Mass] can also take on special [MassType]s.  These mass types allow for interesting
 * effects during interaction.
 *
 *
 * When the mass type is changed, the original mass and inertia values are not lost. This allows the
 * swapping of mass types without recomputing the mass.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 * @see MassType
 */
class Mass {

    /** The mass type  */
    var type: MassType?
        set(value) {
            if (value == null) throw NullPointerException(message("geometry.mass.nullMassType"))
            field = value
        }

    /** The center of mass  */
    @JvmField
    val center: Vector2?

    /** The mass in kg  */
    val mass: Double get() = if (type === MassType.INFINITE || type === MassType.FIXED_LINEAR_VELOCITY) 0.0 else field

    /** The inertia tensor in kg  m<sup>2</sup>  */
    val inertia: Double get() = if (type === MassType.INFINITE || type === MassType.FIXED_ANGULAR_VELOCITY) 0.0 else field

    /** The inverse mass  */
    val inverseMass: Double get() = if (type === MassType.INFINITE || type === MassType.FIXED_LINEAR_VELOCITY) 0.0 else field


    /** The inverse inertia tensor  */
    val inverseInertia: Double get() = if (type === MassType.INFINITE || type === MassType.FIXED_ANGULAR_VELOCITY) 0.0 else field

    /**
     * Default constructor.
     *
     *
     * Creates an infinite mass centered at the origin.
     */
    constructor() {
        type = MassType.INFINITE
        center = Vector2()
        mass = 0.0
        inertia = 0.0
        inverseMass = 0.0
        inverseInertia = 0.0
    }

    /**
     * Full Constructor.
     *
     *
     * The `center` parameter will be copied.
     * @param center center of [Mass] in local coordinates
     * @param mass mass in kg
     * @param inertia inertia tensor in kg  m<sup>2</sup>
     * @throws NullPointerException if center is null
     * @throws IllegalArgumentException if mass or inertia is less than zero
     */
    constructor(center: Vector2?, mass: Double, inertia: Double) {
        // validate the input
        if (center == null) throw NullPointerException(message("geometry.mass.nullCenter"))
        if (mass < 0.0) throw IllegalArgumentException(message("geometry.mass.invalidMass"))
        if (inertia < 0.0) throw IllegalArgumentException(message("geometry.mass.invalidInertia"))
        // create the mass
        type = MassType.NORMAL
        this.center = center.copy()
        this.mass = mass
        this.inertia = inertia
        // set the inverse mass
        if (mass > Epsilon.E) {
            inverseMass = 1.0 / mass
        } else {
            inverseMass = 0.0
            type = MassType.FIXED_LINEAR_VELOCITY
        }
        // set the inverse inertia
        if (inertia > Epsilon.E) {
            inverseInertia = 1.0 / inertia
        } else {
            inverseInertia = 0.0
            type = MassType.FIXED_ANGULAR_VELOCITY
        }
        // check if both the mass and inertia are zero
        if (mass <= Epsilon.E && inertia <= Epsilon.E) {
            type = MassType.INFINITE
        }
    }

    /**
     * Copy constructor.
     *
     *
     * Performs a deep copy.
     * @param mass the [Mass] to copy
     * @throws NullPointerException if mass is null
     */
    constructor(mass: Mass?) {
        // validate the input
        if (mass == null) throw NullPointerException(message("geometry.mass.nullMass"))
        // setup the mass
        type = mass.type
        center = mass.center!!.copy()
        this.mass = mass.mass
        inertia = mass.inertia
        inverseMass = mass.inverseMass
        inverseInertia = mass.inverseInertia
    }

    /**
     * Copies this [Mass].
     * @return [Mass]
     */
    fun copy(): Mass {
        return Mass(this)
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Mass[Type=").append(type)
            .append("|Center=").append(center)
            .append("|Mass=").append(mass)
            .append("|Inertia=").append(inertia)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (center == null) 0 else center.hashCode()
        var temp: Long
        temp = inertia.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = inverseInertia.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = inverseMass.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        temp = mass.toBits()
        result = prime * result + (temp xor (temp ushr 32)).toInt()
        result = prime * result + if (type == null) 0 else type.hashCode()
        return result
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other is Mass) {
            val o = other
            if (type === o.type && mass == o.mass && inertia == o.inertia && center!!.equals(o.center)
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Returns true if this [Mass] object is of type [MassType.INFINITE].
     *
     *
     * A mass will still be treated as an infinite mass in physical modeling if the
     * mass and inertia are zero. This method simply checks the mass type.
     * @return boolean
     */
    val isInfinite: Boolean
        get() = type === MassType.INFINITE

    companion object {
        /**
         * Creates a [Mass] object from the given array of masses.
         *
         *
         * Uses the Parallel Axis Theorem to obtain the inertia tensor about
         * the center of all the given masses:
         *
         *  I<sub>dis</sub> = I<sub>cm</sub> + mr<sup>2</sup>
         * I<sub>total</sub> =  I<sub>dis</sub>
         * The center for the resulting mass will be a mass weighted center.
         *
         *
         * This method will produce unexpected results if any mass contained in the
         * list is infinite.
         * @param masses the list of [Mass] objects to combine
         * @return [Mass] the combined [Mass]
         * @throws NullPointerException if masses is null or contains null elements
         * @throws IllegalArgumentException if masses is empty
         */
        @JvmStatic
        fun create(masses: List<Mass>?): Mass {
            // check the list for null or empty
            if (masses == null) {
                throw NullPointerException(message("geometry.mass.nullMassList"))
            }
            if (masses.size == 0) {
                throw IllegalArgumentException(message("geometry.mass.invalidMassListSize"))
            }
            // get the length of the masses array
            val size = masses.size

            // check for a list of one
            if (size == 1) {
                // check for null item
                val m = masses[0]
                return if (m != null) {
                    Mass(masses[0])
                } else {
                    throw NullPointerException(message("geometry.mass.nullMassListElement"))
                }
            }

            // initialize the new mass values
            val c = Vector2()
            var m = 0.0
            var I = 0.0

            // loop over the masses
            for (i in 0 until size) {
                val mass =
                    masses[i] ?: throw NullPointerException(message("geometry.mass.nullMassListElement"))
                // check for null mass
                // add the center's up (weighting them by their respective mass)
                c.add(mass.center!!.product(mass.mass))
                // sum the masses
                m += mass.mass
            }
            // the mass will never be negative but could be zero
            // if all the masses are infinite
            if (m > 0.0) {
                // compute the center by dividing by the total mass
                c.divide(m)
            }
            // after obtaining the new center of mass we need
            // to compute the interia tensor about the center
            // using the parallel axis theorem:
            // Idis = Icm + mr^2 where r is the perpendicular distance
            // between the two parallel axes
            for (i in 0 until size) {
                // get the mass 
                val mass = masses[i]
                // compute the distance from the new center to
                // the current mass's center
                val d2: Double = mass.center!!.distanceSquared(c)
                // compute Idis
                val Idis = mass.inertia + mass.mass * d2
                // add it to the sum
                I += Idis
            }
            // finally create the mass
            return Mass(c, m, I)
        }
    }
}