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
import org.dyn4j.geometry.Transform

/**
 * Simple helper class that holds information for each item in the [BruteForceBroadphase].
 *
 * @author Manolis Tsamis
 * @version 3.4.0
 * @since 3.4.0
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
</T></E> */
internal class BruteForceBroadphaseNode<E : Collidable<T>, T : Fixture>(
    /** The collidable  */
    val collidable: E,
    /** The fixture  */
    val fixture: T
) {

    /** The AABB  */
    lateinit var aabb: AABB

    /** Whether the node has been tested or not  */
    var tested = false

    /**
     * Updates the AABB of this node
     */
    fun updateAABB() {
        // Remember, we don't expand the AABB
        val tx: Transform = collidable.transform
        aabb = fixture.shape.createAABB(tx)
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("PlainBroadphaseNode[AABB=").append(aabb.toString())
            .append("|Fixture=").append(fixture.hashCode())
            .append("]")
        return sb.toString()
    }

    /**
     * Minimal constructor.
     * @param collidable the collidable
     * @param fixture the fixture
     */
    init {

        // calculate the initial AABB
        updateAABB()
    }
}