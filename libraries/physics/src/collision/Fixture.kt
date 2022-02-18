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
package org.dyn4j.collision

import org.dyn4j.DataContainer
import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Shape
import kotlin.jvm.JvmField

/**
 * Represents a geometric piece of a [Collidable].
 *
 *
 * A [Fixture] has a one-to-one relationship with a [Convex] [Shape], storing
 * additional collision specific information.
 *
 *
 * A [Collidable] is composed of many [Fixture]s to represent its physical shape. While
 * the only shapes supported by the collision detection system are [Convex] shapes, the composition
 * of multiple [Fixture]s in a [Collidable] allows the collidables to be non-convex.
 *
 *
 * The [Fixture]'s [Shape] should be translated and rotated using the [Shape]'s methods
 * to move the shape relative to the containing [Collidable].  Other modifications to the shape is
 * not recommended after adding it to a [Fixture]. To change the shape of a fixture, remove the existing
 * [Fixture] from the [Collidable] and add a new [Fixture] with an updated shape instead.
 *
 *
 * There's no restriction on reusing [Shape]s and [Fixture]s between [Collidable]s, but
 * this is also discouraged to reduce confusion and unexpected behavior (primarily local translations and
 * rotations).
 *
 *
 * A [Fixture] can have a [Filter] assigned to enable filtering of collisions between it
 * and other fixtures.
 *
 *
 * A [Fixture] can be flagged as a sensor fixture to enable standard collision detection, but disable
 * collision resolution (response).
 * @author William Bittle
 * @version 3.4.1
 * @since 2.0.0
 */
open class Fixture : DataContainer {

    /** The convex shape for this fixture  */
    @JvmField
    val shape: Convex

    /** The collision filter  */
    @JvmField
    var filter: Filter? = null

    /** Whether the fixture only senses contact  */
    var isSensor = false

    /** The user data  */
    override var userData: Any? = null

    /**
     * Minimal constructor.
     * @param shape the [Convex] [Shape] for this fixture
     * @throws NullPointerException if shape is null
     */
    constructor(shape: Convex) {
        this.shape = shape
        filter = DEFAULT_FILTER
        isSensor = false
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Fixture[HashCode=").append(this.hashCode())
            .append("|Shape=").append(shape)
            .append("|Filter=").append(filter)
            .append("|IsSensor=").append(isSensor)
            .append("]")
        return sb.toString()
    }

}