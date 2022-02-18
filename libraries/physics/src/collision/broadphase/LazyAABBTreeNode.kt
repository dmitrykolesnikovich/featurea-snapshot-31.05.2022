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

import org.dyn4j.geometry.AABB

/**
 * Represents a basic node in a [LazyAABBTree].
 *
 *
 * The AABB of the node should be the union of all the AABBs below this node.
 * @author Manolis Tsamis
 * @version 3.4.0
 * @since 3.4.0
 */
open class LazyAABBTreeNode {
    /** The left child  */
    var left: LazyAABBTreeNode? = null

    /** The right child  */
    var right: LazyAABBTreeNode? = null

    /** The parent node  */
    var parent: LazyAABBTreeNode? = null

    /** The height of this subtree  */
    var height = 0

    /** The aabb containing all children  */
    var aabb: AABB? = null

    /**
     * Replace oldChild with newChild. oldChild must be a child of this node before the replacement.
     * Children are compared with the equality operator.
     *
     * @param oldChild The child to replace in this node
     * @param newChild The replacement
     * @throws IllegalArgumentException if oldChild is not a child of this node
     */
    fun replaceChild(
        oldChild: LazyAABBTreeNode,
        newChild: LazyAABBTreeNode?
    ) {
        if (left === oldChild) {
            left = newChild
        } else if (right === oldChild) {
            right = newChild
        } else {
            throw IllegalArgumentException("$oldChild is not a child of node $this")
        }
    }

    /**
     * Returns the sibling of this node, that is the other child of this node's parent.
     *
     * @return The sibling node
     * @throws NullPointerException if this node has no parent
     * @throws IllegalStateException if this node is not a child of it's parent
     */
    val sibling: LazyAABBTreeNode?
        get() = if (parent!!.left === this) {
            parent!!.right
        } else if (parent!!.right === this) {
            parent!!.left
        } else {
            throw IllegalStateException("Invalid parent pointer for node $this")
        }

    /**
     * Returns true if this node is a leaf node.
     * @return boolean true if this node is a leaf node
     */
    val isLeaf: Boolean
        get() = left == null

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("LazyAABBTreeNode[AABB=").append(aabb.toString())
            .append("|Height=").append(height)
            .append("]")
        return sb.toString()
    }
}