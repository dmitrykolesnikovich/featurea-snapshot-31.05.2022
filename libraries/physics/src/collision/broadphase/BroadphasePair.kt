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

/**
 * Represents a pair of [Collidable] [Fixture]s that have been detected as
 * colliding in a [BroadphaseDetector].
 * @author William Bittle
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
 * @version 3.2.0
 * @since 1.0.0
</T></E> */
class BroadphasePair<E : Collidable<T>, T : Fixture>
/**
 * Minimal constructor.
 * @param collidable1 the first collidable
 * @param fixture1 the first collidable's fixture
 * @param collidable2 the second collidable
 * @param fixture2 the second collidable's fixture
 */(
    /** The first [Collidable]  */
    val collidable1: E,
    /** The first [Collidable]'s [Fixture]  */
    val fixture1: T,
    /** The second [Collidable]  */
    val collidable2: E,
    /** The second [Collidable]'s [Fixture]  */
    val fixture2: T
) {
    // the first
    /**
     * Returns the first [Collidable].
     * @return E
     */

    /**
     * Returns the first [Fixture].
     * @return T
     */
    // the second
    /**
     * Returns the second [Collidable].
     * @return E
     */

    /**
     * Returns the second [Fixture].
     * @return T
     */

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj === this) return true
        if (obj is BroadphasePair<*, *>) {
            val pair =
                obj
            if (pair.collidable1 === collidable1 && pair.fixture1 === fixture1 && pair.collidable2 === collidable2 && pair.fixture2 === fixture2
            ) {
                return true
            }
        }
        return false
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    override fun hashCode(): Int {
        var hash = 17
        hash = hash * 31 + collidable1.hashCode()
        hash = hash * 31 + fixture1.hashCode()
        hash = hash * 31 + collidable2.hashCode()
        hash = hash * 31 + fixture2.hashCode()
        return hash
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("BroadphasePair[Collidable1=").append(collidable1.hashCode())
            .append("|Fixture1=").append(fixture1.hashCode())
            .append("|Collidable2=").append(collidable2.hashCode())
            .append("|Fixture2=").append(fixture2.hashCode())
            .append("]")
        return sb.toString()
    }

}