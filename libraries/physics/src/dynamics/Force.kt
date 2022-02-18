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

import org.dyn4j.geometry.Vector2
import org.dyn4j.resources.message
import kotlin.jvm.JvmField

/**
 * Represents a force.
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 */
open class Force {
    /** The force to apply  */
    @JvmField
    var force: Vector2

    /**
     * Default constructor.
     */
    constructor() {
        force = Vector2()
    }

    /**
     * Creates a new [Force] using the x and y components.
     * @param x the x component
     * @param y the y component
     */
    constructor(x: Double, y: Double) {
        force = Vector2(x, y)
    }

    /**
     * Creates a new [Force] using the given [Vector2].
     * @param force the force [Vector2]
     * @throws NullPointerException if force is null
     */
    constructor(force: Vector2?) {
        if (force == null) throw NullPointerException(message("dynamics.force.nullVector"))
        this.force = force
    }

    /**
     * Copy constructor.
     * @param force the [Force] to copy
     * @throws NullPointerException if force is null
     */
    constructor(force: Force?) {
        if (force == null) throw NullPointerException(message("dynamics.force.nullForce"))
        this.force = force.force.copy()
    }

    /**
     * Sets this [Force] to the given components.
     * @param x the x component
     * @param y the y component
     */
    operator fun set(x: Double, y: Double) {
        force.set(x, y)
    }

    /**
     * Sets this [Force] to the given force [Vector2].
     * @param force the force [Vector2]
     * @throws NullPointerException if force is null
     */
    fun set(force: Vector2?) {
        if (force == null) throw NullPointerException(message("dynamics.force.setNullVector"))
        this.force.set(force)
    }

    /**
     * Sets this [Force] to the given [Force].
     * @param force the [Force] to copy
     * @throws NullPointerException if force is null
     */
    fun set(force: Force?) {
        if (force == null) throw NullPointerException(message("dynamics.force.setNullForce"))
        this.force.set(force.force)
    }

    /**
     * Returns true if this force should be removed.
     *
     *
     * Implement this method to create [Force] objects
     * that are not cleared each iteration by the [World].
     *
     *
     * The default implementation always returns true.
     * @param elapsedTime the elapsed time since the last call to this method
     * @return boolean true if this force should be removed
     * @since 3.1.0
     */
    open fun isComplete(elapsedTime: Double): Boolean {
        return true
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        return force.toString()
    }

}