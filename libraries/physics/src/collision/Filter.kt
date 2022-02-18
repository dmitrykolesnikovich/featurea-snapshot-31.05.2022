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

import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * Interface representing a filter for collision detection.
 *
 *
 * Filters allow the collision detection system to skip expensive operations
 * between [Fixture]s that shouldn't be colliding at all.
 *
 *
 * The [.DEFAULT_FILTER] allows all fixture collisions.
 * @author William Bittle
 * @version 3.0.2
 * @since 1.0.0
 */
interface Filter {
    /**
     * Returns true if the given [Filter] and this [Filter]
     * allow the objects to interact.
     *
     *
     * If the given [Filter] is not the same type as this [Filter]
     * its up to the implementing class to specify the behavior.
     *
     *
     * In addition, if the given [Filter] is null its up to the implementing
     * class to specify the behavior.
     * @param filter the other [Filter]
     * @return boolean
     */
    fun isAllowed(filter: Filter?): Boolean

}

/** The default filter which always returns true  */
@JvmField
val DEFAULT_FILTER: Filter = object : Filter {
    /* (non-Javadoc)
 * @see org.dyn4j.collision.Filter#isAllowed(org.dyn4j.collision.Filter)
 */
    override fun isAllowed(filter: Filter?): Boolean {
        // always return true
        return true
    }

    /* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
    override fun toString(): String {
        return "DefaultFilter[]"
    }
}