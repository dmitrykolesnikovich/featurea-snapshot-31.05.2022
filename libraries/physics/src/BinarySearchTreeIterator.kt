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

import featurea.Stack
import org.dyn4j.resources.message

/**
 * An iterator class for looping through the elements of a [BinarySearchTree]
 * in order or in reverse order.
 *
 *
 * The [.remove] method is unsupported.
 * @author William Bittle
 * @version 3.2.3
 * @since 2.2.0
 * @param <E> the comparable type
</E> */
internal class BinarySearchTreeIterator<E : Comparable<E>> private constructor(
    root: BinarySearchTreeNode<E>?, from: E?, to: E?, inOrder: Boolean
) : MutableIterator<E> {

    /** The node stack for iterative traversal  */
    val stack: Stack<BinarySearchTreeNode<E>>

    /** The root of the tree  */
    val root: BinarySearchTreeNode<E>

    /** The value to start iteration from; can be null  */
    val from: E?

    /** The value to end iteration; can be null  */
    val to: E?

    /** The traversal direction  */
    val inOrder: Boolean

    /**
     * Default constructor using in-order traversal.
     * @param root the root node of the subtree to traverse
     * @throws NullPointerException if node is null
     */
    constructor(root: BinarySearchTreeNode<E>?) : this(root, null, null, true) {}

    /**
     * Full constructor.
     * @param root the root node of the subtree to traverse
     * @param inOrder true to iterate in-order, false to iterate reverse order
     * @throws NullPointerException if node is null
     */
    constructor(root: BinarySearchTreeNode<E>?, inOrder: Boolean) : this(root, null, null, inOrder) {}

    /**
     * Full constructor.
     * @param root the root node of the subtree to traverse
     * @param from the value to start iterating from (inclusive)
     * @param to the value to stop iterating after (inclusive)
     * @throws NullPointerException if node is null
     * @since 3.2.3
     */
    constructor(root: BinarySearchTreeNode<E>?, from: E?, to: E?) : this(root, from, to, true)

    /**
     * Pushes the required nodes onto the stack to begin iterating
     * nodes in order starting from the given value.
     * @param from the value to start iterating from
     * @since 3.2.3
     */
    protected fun pushLeftFrom(from: E?) {
        var node: BinarySearchTreeNode<E>? = root
        while (node != null) {
            val cmp = from!!.compareTo(node.comparable)
            node = if (cmp < 0) {
                // go left
                stack.push(node)
                node.left
            } else if (cmp > 0) {
                // go right
                node.right
            } else {
                stack.push(node)
                break
            }
        }
    }

    /**
     * Pushes the left most nodes of the given subtree onto the stack.
     * @param node the root node of the subtree
     */
    protected fun pushLeft(node: BinarySearchTreeNode<E>?) {
        // loop until we don't have any more left nodes
        var node: BinarySearchTreeNode<E>? = node
        while (node != null) {
            // if we have a iterate to node, then only push nodes
            // to that are less than or equal to it
            if (to == null || to.compareTo(node.comparable) >= 0) {
                stack.push(node)
            }
            node = node.left
        }
    }

    /**
     * Pushes the right most nodes of the given subtree onto the stack.
     * @param node the root node of the subtree
     */
    protected fun pushRight(node: BinarySearchTreeNode<E>?) {
        // loop until we don't have any more right nodes
        var node: BinarySearchTreeNode<E>? = node
        while (node != null) {
            stack.push(node)
            node = node.right
        }
    }

    /* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
    override fun hasNext(): Boolean {
        return !stack.isEmpty()
    }

    /* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
    override fun next(): E {
        // if the stack is empty throw an exception
        if (stack.isEmpty()) throw NoSuchElementException()
        // get an element off the stack
        val node: BinarySearchTreeNode<E>? = stack.pop()
        if (inOrder) {
            // add all the left most nodes of the right subtree of this element 
            pushLeft(node!!.right)
        } else {
            // add all the right most nodes of the left subtree of this element 
            pushRight(node!!.left)
        }
        // return the comparable object
        return node.comparable
    }

    /**
     * Currently unsupported.
     */
    override fun remove() {
        throw UnsupportedOperationException()
    }

    /**
     * Full constructor.
     * @param root the root node of the subtree to traverse
     * @param from the value to start iterating from (inclusive)
     * @param to the value to stop iterating after (inclusive)
     * @param inOrder true to iterate in-order, false to iterate reverse order
     * @throws NullPointerException if node is null
     * @since 3.2.3
     */
    init {
        // check for null
        if (root == null) throw NullPointerException(message("binarySearchTree.nullSubTreeForIterator"))
        // set the direction
        this.inOrder = inOrder
        // create the node stack and initialize it
        stack = Stack()
        this.root = root
        this.from = from
        this.to = to
        // check the direction to determine how to initialize it
        if (inOrder) {
            if (this.from != null) {
                pushLeftFrom(from)
            } else {
                pushLeft(root)
            }
        } else {
            pushRight(root)
        }
    }
}