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

import org.dyn4j.Listener
import org.dyn4j.geometry.AABB
import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Transform

/**
 * Interface to listen for detection events from the [World].detect methods.
 *
 *
 * By default all methods should return true.
 * @author William Bittle
 * @version 3.1.9
 * @since 3.1.9
 */
interface DetectListener : Listener {
    /**
     * Called **before** the [BodyFixture] is tested against the
     * given [AABB].
     *
     *
     * Return false from this method to eliminate this fixture from the
     * list of results.  Return false also improves performance by reducing
     * the number of collision tests.
     *
     *
     * This method is only called by the `World.detect(AABB,...)` methods.
     * @param aabb the AABB given
     * @param body the [Body] whose AABB overlaps the given AABB
     * @param fixture the [BodyFixture] that is about to be tested
     * @return boolean true to allow this body to be included in the results
     */
    fun allow(aabb: AABB?, body: Body?, fixture: BodyFixture?): Boolean

    /**
     * Called after the given [Convex] has been found to overlap the
     * a [Body].
     *
     *
     * Return false from this method to eliminate this body from the
     * list of results.
     *
     *
     * This method is only called by the `World.detect(Convex,...)` methods.
     * @param convex the convex given to the World.detect method
     * @param transform the transform given to the World.detect method
     * @param body the [Body] whose AABB overlaps the given AABB
     * @return boolean true to allow this body to be included in the results
     */
    fun allow(convex: Convex?, transform: Transform?, body: Body?): Boolean

    /**
     * Called **before** the [BodyFixture] is tested against the
     * given [Convex].
     *
     *
     * Return false from this method to eliminate this fixture from the
     * list of results.  Return false also improves performance by reducing
     * the number of collision tests.
     *
     *
     * This method is only called by the `World.detect(Convex,...)` methods.
     * @param convex the convex given to the World.detect method
     * @param transform the transform given to the World.detect method
     * @param body the [Body] whose AABB overlaps the given AABB
     * @param fixture the [BodyFixture] that is about to be tested
     * @return boolean true to allow this body to be included in the results
     */
    fun allow(convex: Convex?, transform: Transform?, body: Body?, fixture: BodyFixture?): Boolean
}