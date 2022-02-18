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
package org.dyn4j.collision.broadphase

import org.dyn4j.collision.Collidable
import org.dyn4j.collision.Fixture
import org.dyn4j.geometry.AABB
import org.dyn4j.geometry.Ray

/**
 * Represents a class that defines rules to exclude results from a [BroadphaseDetector]'s query methods. Some examples
 * include the [BroadphaseDetector.detect], [BroadphaseDetector.raycast]
 * and [BroadphaseDetector.detect] methods.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.2.0
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
 * @see DefaultBroadphaseFilter
</T></E> */
interface BroadphaseFilter<E : Collidable<T>, T : Fixture> {
    /**
     * Returns true if this result should be added to the results list.
     *
     *
     * This method is called from the [BroadphaseDetector.detect] and
     * [BroadphaseDetector.detect] methods.
     * @param collidable1 the first [Collidable]
     * @param fixture1 the first [Collidable]s [Fixture]
     * @param collidable2 the second [Collidable]
     * @param fixture2 the second [Collidable]s [Fixture]
     * @return boolean
     */
    fun isAllowed(collidable1: E, fixture1: T, collidable2: E, fixture2: T): Boolean

    /**
     * Returns true if this result should be added to the results list.
     *
     *
     * This method is called from the [BroadphaseDetector.detect] and
     * [BroadphaseDetector.detect] methods.
     * @param aabb the AABB using to test
     * @param collidable the [Collidable]
     * @param fixture the [Collidable]s [Fixture]
     * @return boolean
     */
    fun isAllowed(aabb: AABB?, collidable: E, fixture: T): Boolean

    /**
     * Returns true if this result should be added to the results list.
     *
     *
     * This method is called from the [BroadphaseDetector.raycast] and
     * [BroadphaseDetector.raycast] methods.
     * @param ray the ray
     * @param length the length of the ray
     * @param collidable the [Collidable]
     * @param fixture the [Collidable]s [Fixture]
     * @return boolean
     */
    fun isAllowed(ray: Ray?, length: Double, collidable: E, fixture: T): Boolean
}