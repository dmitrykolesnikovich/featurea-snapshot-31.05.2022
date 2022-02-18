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
 * Represents a key for a [BroadphaseItem] used for fast look ups in
 * the [BroadphaseDetector]s.
 * @author William Bittle
 * @version 3.4.0
 * @since 3.2.0
 */
class BroadphaseKey(collidable: Collidable<*>, fixture: Fixture?) {
    /** The [Collidable]  */
    val collidable: Collidable<*>

    /** The [Fixture]  */
    val fixture: Fixture?

    /** The pre-computed hashcode  */
    private val hashCode: Int

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj === this) return true
        if (obj is BroadphaseKey) {
            val key =
                obj as BroadphaseKey?
            return key!!.collidable === collidable &&
                    key!!.fixture === fixture
        }
        return false
    }

    /**
     * Computes the hashcode from the collidable and fixture ids.
     * @return int
     */
    protected fun computeHashCode(): Int {
        var hash = 17
        hash = hash * 31 + collidable.hashCode()
        hash = hash * 31 + fixture.hashCode()
        return hash
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    override fun hashCode(): Int {
        return hashCode
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("BroadphaseKey[Collidable=").append(collidable.hashCode())
            .append("|Fixture=").append(fixture.hashCode())
            .append("]")
        return sb.toString()
    }

    companion object {
        /**
         * Creates and returns a new key for the given [Collidable] and [Fixture].
         * @param collidable the [Collidable]
         * @param fixture the [Fixture]
         * @return [BroadphaseKey]
         */
        operator fun get(collidable: Collidable<*>, fixture: Fixture?): BroadphaseKey {
            return BroadphaseKey(collidable, fixture)
        }
    }

    /**
     * Minimal constructor.
     * @param collidable the [Collidable]
     * @param fixture the [Fixture]
     */
    init {
        this.collidable = collidable
        this.fixture = fixture
        // pre compute the hash
        hashCode = computeHashCode()
    }
}