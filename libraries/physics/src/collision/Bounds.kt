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

import org.dyn4j.geometry.Rotatable
import org.dyn4j.geometry.Shiftable
import org.dyn4j.geometry.Translatable
import org.dyn4j.geometry.Vector2

/**
 * Represents the [Bounds] of a simulation.
 *
 *
 * By default all bounds are [Translatable] but not [Rotatable].
 *
 *
 * Though not part of the bounds contract, a bounds object should only return true
 * from the [.isOutside] method when a [Collidable] is
 * **fully** outside the bounds.
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 */
interface Bounds : Translatable, Shiftable {
    /**
     * Returns the translation of the bounds.
     * @return [Vector2]
     * @since 3.2.0
     */
    val translation: Vector2

    /**
     * Returns true if the given [Collidable] is **fully** outside the bounds.
     *
     *
     * If the [Collidable] contains zero [Fixture]s then
     * [Collidable] is considered to be outside the bounds.
     * @param collidable the [Collidable] to test
     * @return boolean true if outside the bounds
     */
    fun isOutside(collidable: Collidable<*>): Boolean
}