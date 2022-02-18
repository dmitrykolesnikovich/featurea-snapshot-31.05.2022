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
import org.dyn4j.geometry.Transform

/**
 * Represents a leaf node in a [LazyAABBTree].
 *
 *
 * The leaf nodes in a [LazyAABBTree] are the nodes that contain the [Fixture] AABBs.
 *
 * @author Manolis Tsamis
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
 * @version 3.4.0
 * @since 3.4.0
</T></E> */
class LazyAABBTreeLeaf<E : Collidable<T>, T : Fixture>(
    /** The [Collidable]  */
    var collidable: E,
    /** The [Fixture]  */
    var fixture: T
) :
    LazyAABBTreeNode() {

    /** Flag storing whether this leaf is in the tree currently  */
    private var onTree = false

    /** Mark for removal flag  */
    private var removed = false

    /**
     * Updates the AABB of this leaf
     */
    fun updateAABB() {
        val transform: Transform = collidable.transform
        this.aabb = fixture.shape.createAABB(transform)
    }

    /**
     * @return true if this leaf has been marked for removal, false otherwise
     */
    fun mustRemove(): Boolean {
        return removed
    }

    /**
     * Marks that this leaf must be removed
     */
    fun markForRemoval() {
        removed = true
    }

    /**
     * Change the flag denoting if this leaf is on the tree or not
     * @param onTree the new flag value
     */
    fun setOnTree(onTree: Boolean) {
        this.onTree = onTree
        if (!onTree) {
            // Clear possible leftovers
            this.left = null
            this.right = null
            this.parent = null
        }
    }

    /**
     * @return true if this leaf is on the tree, false otherwise
     */
    fun isOnTree(): Boolean {
        return onTree
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj === this) return true
        if (obj is LazyAABBTreeLeaf<*, *>) {
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
        sb.append("LazyAABBTreeLeaf[Collidable=").append(collidable.hashCode())
            .append("|Fixture=").append(fixture.hashCode())
            .append("|AABB=").append(this.aabb.toString())
            .append("|OnTree=").append(onTree)
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