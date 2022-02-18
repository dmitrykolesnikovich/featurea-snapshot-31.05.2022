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

import org.dyn4j.DataContainer
import org.dyn4j.collision.Fixture
import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Mass
import org.dyn4j.resources.message

/**
 * Represents a piece of a [Body].
 *
 *
 * [BodyFixture] extends the [Fixture] class, adding physical features
 * like density and friction.
 * @author William Bittle
 * @version 3.4.1
 * @since 2.0.0
 * @see Fixture
 */
class BodyFixture(shape: Convex) : Fixture(shape), DataContainer {
    /** The density in kg/m<sup>2</sup>  */
    var density: Double = 0.0
        set(value) {
            if (value <= 0) throw IllegalArgumentException(message("dynamics.body.fixture.invalidDensity"))
            field = value
        }

    /** The coefficient of friction  */
    var friction: Double = 0.0
        set(value) {
            if (value < 0) throw IllegalArgumentException(message("dynamics.body.fixture.invalidFriction"))
            field = value
        }

    /** The coefficient of restitution  */
    var restitution: Double = 0.0
        set(value) {
            if (value < 0) throw IllegalArgumentException(message("dynamics.body.fixture.invalidRestitution"))
            field = value
        }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("BodyFixture[HashCode=").append(this.hashCode())
            .append("|Shape=").append(shape)
            .append("|Filter=").append(filter)
            .append("|IsSensor=").append(this.isSensor)
            .append("|Density=").append(density)
            .append("|Friction=").append(friction)
            .append("|Restitution=").append(restitution)
            .append("]")
        return sb.toString()
    }

    /**
     * Creates a new [Mass] object using the set density and shape.
     * @return [Mass]
     */
    fun createMass(): Mass {
        return shape.createMass(density)
    }

    companion object {
        /** The default coefficient of friction; value = [.DEFAULT_FRICTION]  */
        const val DEFAULT_FRICTION = 0.2

        /** The default coefficient of restitution; value = [.DEFAULT_RESTITUTION]  */
        const val DEFAULT_RESTITUTION = 0.0

        /** The default density in kg/m<sup>2</sup>; value = [.DEFAULT_DENSITY]  */
        const val DEFAULT_DENSITY = 1.0
    }

    /**
     * Minimal constructor.
     * @param shape the [Convex] [Shape] for this fixture
     */
    init {
        density = DEFAULT_DENSITY
        friction = DEFAULT_FRICTION
        restitution = DEFAULT_RESTITUTION
    }
}