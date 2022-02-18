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
 * Represents a leaf node in a [DynamicAABBTree].
 *
 *
 * The leaf nodes in a [DynamicAABBTree] are the nodes that contain the [Fixture] AABBs.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.2.0
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
</T></E> */
class DynamicAABBTreeLeaf<E : Collidable<T>, T : Fixture>
/**
 * Minimal constructor.
 * @param collidable the collidable
 * @param fixture the fixture
 */(
    /** The [Collidable]  */
    val collidable: E,
    /** The [Fixture]  */
    val fixture: T
) :
    DynamicAABBTreeNode() {

    /** Flag used to determine if a node has been tested before  */
    var tested = false

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj === this) return true
        if (obj is DynamicAABBTreeLeaf<*, *>) {
            val leaf =
                obj
            if (leaf.collidable === collidable &&
                leaf.fixture === fixture
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
        sb.append("DynamicAABBTreeLeaf[Collidable=").append(collidable.hashCode())
            .append("|Fixture=").append(fixture.hashCode())
            .append("|AABB=").append(this.aabb.toString())
            .append("|Height=").append(this.height)
            .append("|Tested=").append(tested)
            .append("]")
        return sb.toString()
    }

}