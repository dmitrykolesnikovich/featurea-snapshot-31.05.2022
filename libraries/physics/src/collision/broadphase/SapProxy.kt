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
import kotlin.math.sign

/**
 * Represents a sortable proxy for a [Collidable] [Fixture] in the [Sap] [BroadphaseDetector].
 *
 *
 * Note: This class has a natural ordering that is inconsistent with equals.
 * @author William Bittle
 * @since 3.2.3
 * @version 3.4.0
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
</T></E> */
class SapProxy<E : Collidable<T>, T : Fixture>(val collidable: E, val fixture: T, aabb: AABB) :
    Comparable<SapProxy<E, T>> {

    /** The collidable's aabb  */
    var aabb: AABB

    /** Whether the proxy has been tested or not  */
    var tested = false

    /* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
    override operator fun compareTo(o: SapProxy<E, T>): Int {
        // check if the objects are the same instance
        if (this === o) return 0
        // compute the difference in the minimum x values of the aabbs
        var diff: Double = aabb.minX - o.aabb.minX
        return if (diff != 0.0) {
            diff.sign.toInt()
        } else {
            // if the x values are the same then compare on the y values
            diff = aabb.minY - o.aabb.minY
            diff.sign.toInt()
        }
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj === this) return true
        if (obj is SapProxy<*, *>) {
            val pair =
                obj
            if (pair.collidable === collidable &&
                pair.fixture === fixture
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
        hash = hash * 31 + collidable.hashCode()
        hash = hash * 31 + fixture.hashCode()
        return hash
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("SapProxy[Collidable=").append(if (collidable != null) collidable.hashCode() else "null")
            .append("|Fixture=").append(if (fixture != null) fixture.hashCode() else "null")
            .append("|AABB=").append(aabb.toString())
            .append("|Tested=").append(tested)
            .append("]")
        return sb.toString()
    }

    /**
     * Full constructor.
     * @param collidable the collidable
     * @param fixture the fixture
     * @param aabb the aabb
     */
    init {
        this.aabb = aabb
    }
}