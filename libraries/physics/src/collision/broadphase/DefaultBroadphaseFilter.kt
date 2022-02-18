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
import org.dyn4j.collision.Filter
import org.dyn4j.collision.Fixture

/**
 * A default filter for the [BroadphaseDetector.detect] method that
 * filters [Fixture]s by their [Filter]s.
 *
 *
 * This is the default [BroadphaseFilter] used in the [BroadphaseDetector]s.  Use the methods
 * in the [BroadphaseDetector] that accept [BroadphaseFilter]a to override the filter.
 *
 *
 * It's recommended that this class be extended when creating custom [BroadphaseFilter]s to ensure
 * the default functionality is retained.
 * @author William Bittle
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
 * @version 3.2.0
 * @since 3.2.0
</T></E> */
open class DefaultBroadphaseFilter<E : Collidable<T>, T : Fixture> : BroadphaseFilterAdapter<E, T>(),
    BroadphaseFilter<E, T> {
    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseFilter#isAllowed(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture, org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun isAllowed(collidable1: E, fixture1: T, collidable2: E, fixture2: T): Boolean {
        val filter1: Filter = fixture1.filter!!
        val filter2: Filter = fixture2.filter!!
        return filter1.isAllowed(filter2)
    }
}