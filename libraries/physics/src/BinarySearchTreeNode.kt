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
package org.dyn4j

import org.dyn4j.resources.message
import kotlin.jvm.JvmField

/**
 * Node class for the [BinarySearchTree].
 * @author William Bittle
 * @version 3.1.9
 * @since 2.2.0
 * @param <E> the comparable type
</E> */
class BinarySearchTreeNode<E : Comparable<E>>constructor(
    comparable: E?,
    parent: BinarySearchTreeNode<E>? = null,
    left: BinarySearchTreeNode<E>? = null,
    right: BinarySearchTreeNode<E>? = null
) : Comparable<BinarySearchTreeNode<E>> {
    /**
     * Returns the comparable object.
     * @return E
     */
    /** The comparable data  */
    @JvmField
    val comparable: E
    // the parent and children will change as the tree evolves
    /** The parent node of this node  */
    var parent: BinarySearchTreeNode<E>?

    /** The node to the left; the left node is greater than this node  */
    var left: BinarySearchTreeNode<E>?

    /** The node to the right; the right node is greater than this node  */
    var right: BinarySearchTreeNode<E>?

    /**
     * Minimal constructor.
     * @param comparable the comparable object
     */
    constructor(comparable: E) : this(comparable, null, null, null)


    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        return comparable.toString()
    }

    /* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
    override operator fun compareTo(other: BinarySearchTreeNode<E>): Int {
        return comparable!!.compareTo(other.comparable)
    }

    /**
     * Returns true if this node is the left child of
     * its parent node.
     *
     *
     * Returns false if this node does not have a parent.
     * @return boolean
     */
    val isLeftChild: Boolean
        get() = if (parent == null) false else parent!!.left == this
    /**
     * Full constructor.
     * @param comparable the comparable object
     * @param parent the parent node
     * @param left the left node
     * @param right the right node
     * @throws NullPointerException if comparable is null
     */
    /**
     * Minimal constructor.
     * @param comparable the comparable object
     */
    init {
        if (comparable == null) throw NullPointerException(message("binarySearchTree.nullComparable"))
        this.comparable = comparable
        this.parent = parent
        this.left = left
        this.right = right
    }
}