/*
 * Copyright (c) 2010-2017 William Bittle  http://www.dyn4j.org/
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
import org.dyn4j.collision.Collisions
import org.dyn4j.collision.Collisions.getEstimatedCollisionPairs
import org.dyn4j.collision.Collisions.getEstimatedRaycastCollisions
import org.dyn4j.collision.Fixture
import org.dyn4j.geometry.AABB
import org.dyn4j.geometry.Ray
import org.dyn4j.geometry.Vector2
import kotlin.math.max

/**
 * Implementation of a self-balancing axis-aligned bounding box tree broad-phase collision detection algorithm.
 *
 *
 * This class uses a self-balancing binary tree to store the AABBs.  The AABBs are sorted using the perimeter.
 * The perimeter hueristic is better than area for 2D because axis aligned segments would have zero area.
 * @author William Bittle
 * @version 3.4.0
 * @since 3.0.0
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
</T></E> */
class DynamicAABBTree<E : Collidable<T>, T : Fixture> constructor(initialCapacity: Int = BroadphaseDetector.DEFAULT_INITIAL_CAPACITY) :
    AbstractBroadphaseDetector<E, T>(), BroadphaseDetector<E, T> {
    
    /** The root node of the tree  */
    var root: DynamicAABBTreeNode? = null

    /** Id to node map for fast lookup  */
    val map: MutableMap<BroadphaseKey, DynamicAABBTreeLeaf<E, T>>

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#add(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun add(collidable: E, fixture: T) {
        val key = BroadphaseKey.get(collidable, fixture)
        // see if the collidable-fixture has already been added
        val node = map[key]
        if (node != null) {
            this.update(key, node, collidable, fixture)
        } else {
            this.add(key, collidable, fixture)
        }
    }

    /**
     * Internal add method.
     *
     *
     * This method assumes the given arguments are all non-null and that the
     * [Collidable] [Fixture] is not currently in this broad-phase.
     * @param key the key for the collidable-fixture pair
     * @param collidable the collidable
     * @param fixture the fixture
     */
    fun add(key: BroadphaseKey, collidable: E, fixture: T) {
        val tx = collidable!!.transform
        val aabb = fixture!!.shape.createAABB(tx)
        // expand the aabb
        aabb.expand(this.expansion)
        // create a new node for the collidable
        val node = DynamicAABBTreeLeaf<E, T>(collidable, fixture)
        node.aabb = aabb
        // add the proxy to the map
        map[key] = node
        // insert the node into the tree
        insert(node)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#remove(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun remove(collidable: E, fixture: T?): Boolean {
        val key = BroadphaseKey.get(collidable, fixture)
        // find the node in the map
        val node = map.remove(key)
        // make sure it was found
        if (node != null) {
            // remove the node from the tree
            this.remove(node)
            return true
        }
        return false
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#update(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun update(collidable: E, fixture: T) {
        val key = BroadphaseKey.get(collidable, fixture)
        // get the node from the map
        val node = map[key]
        // make sure we found it
        if (node != null) {
            // update the node
            this.update(key, node, collidable, fixture)
        } else {
            // add the node
            this.add(key, collidable, fixture)
        }
    }

    /**
     * Internal update method.
     *
     *
     * This method assumes the given arguments are all non-null.
     * @param key the key for the collidable-fixture pair
     * @param node the current node in the tree
     * @param collidable the collidable
     * @param fixture the fixture
     */
    fun update(key: BroadphaseKey?, node: DynamicAABBTreeLeaf<E, T>, collidable: E, fixture: T) {
        val tx = collidable!!.transform
        // create the new aabb
        val aabb = fixture!!.shape.createAABB(tx)
        // see if the old aabb contains the new one
        if (node.aabb.contains(aabb)) {
            // if so, don't do anything
            return
        }
        // otherwise expand the new aabb
        aabb.expand(this.expansion)
        // remove the current node from the tree
        this.remove(node)
        // set the new aabb
        node.aabb = aabb
        // reinsert the node
        insert(node)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#getAABB(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun getAABB(collidable: E, fixture: T): AABB? {
        val key = BroadphaseKey.get(collidable, fixture)
        val node = map[key]
        return node?.aabb ?: fixture!!.shape.createAABB(collidable!!.transform)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#contains(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun contains(collidable: E, fixture: T): Boolean {
        val key = BroadphaseKey.get(collidable, fixture)
        return map.containsKey(key)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#clear()
	 */
    override fun clear() {
        map.clear()
        root = null
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#size()
	 */
    override fun size(): Int {
        return map.size
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#detect(org.dyn4j.collision.broadphase.BroadphaseFilter)
	 */
    override fun detect(filter: BroadphaseFilter<E, T>): List<BroadphasePair<E, T>> {
        // clear all the tested flags on the nodes
        val size = map.size
        val nodes: Collection<DynamicAABBTreeLeaf<E, T>> = map.values
        for (node in nodes) {
            // reset the flag
            node.tested = false
        }

        // the estimated size of the pair list
        val eSize = getEstimatedCollisionPairs(size)
        val pairs: MutableList<BroadphasePair<E, T>> = ArrayList(eSize)

        // test each collidable in the list
        for (node in nodes) {
            // perform a stackless detection routine
            detectNonRecursive(node, root, filter, pairs)
            // update the tested flag
            node.tested = true
        }

        // return the list of pairs
        return pairs
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#detect(org.dyn4j.geometry.AABB)
	 */
    override fun detect(aabb: AABB, filter: BroadphaseFilter<E, T>): List<BroadphaseItem<E, T>> {
        return this.detectNonRecursive(aabb, root, filter)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#raycast(org.dyn4j.geometry.Ray, double)
	 */
    override fun raycast(
        ray: Ray,
        length: Double,
        filter: BroadphaseFilter<E, T>
    ): List<BroadphaseItem<E, T>> {
        // check the size of the proxy list
        if (map.size == 0) {
            // return an empty list
            return emptyList()
        }

        // create an aabb from the ray
        val s = ray.start!!
        val d = ray.directionVector

        // get the length
        var l = length
        if (length <= 0.0) l = Double.MAX_VALUE

        // compute the coordinates
        val x1 = s.x
        val x2 = s.x + d.x * l
        val y1 = s.y
        val y2 = s.y + d.y * l

        // create the aabb
        val aabb = AABB.createAABBFromPoints(x1, y1, x2, y2)

        // precompute
        val invDx = 1.0 / d.x
        val invDy = 1.0 / d.y
        var node = root

        // get the estimated collision count
        val eSize = getEstimatedRaycastCollisions(map.size)
        val list: MutableList<BroadphaseItem<E, T>> = ArrayList(eSize)
        // perform a iterative, stack-less, traversal of the tree
        while (node != null) {
            // check if the current node overlaps the desired node
            if (aabb.overlaps(node.aabb)) {
                // if they do overlap, then check the left child node
                if (node.left != null) {
                    // if the left is not null, then check that subtree
                    node = node.left
                    continue
                } else if (this.raycast(s, l, invDx, invDy, node.aabb)) {
                    // if both are null, then this is a leaf node
                    val leaf = node as DynamicAABBTreeLeaf<E, T>
                    if (filter.isAllowed(ray, length, leaf.collidable, leaf.fixture)) {
                        list.add(BroadphaseItem(leaf.collidable, leaf.fixture))
                    }
                    // if its a leaf node then we need to go back up the
                    // tree and test nodes we haven't yet
                }
            }
            // if the current node is a leaf node or doesnt overlap the
            // desired aabb, then we need to go back up the tree until we
            // find the first left node who's right node is not null
            var nextNodeFound = false
            while (node!!.parent != null) {
                // check if the current node the left child of its parent
                if (node === node.parent!!.left) {
                    // it is, so check if the right node is non-null
                    // NOTE: not need since the tree is a complete tree (every node has two children)
                    //if (node.parent.right != null) {
                    // it isn't so the sibling node is the next node
                    node = node.parent!!.right
                    nextNodeFound = true
                    break
                    //}
                }
                // if the current node isn't a left node or it is but its
                // sibling is null, go to the parent node
                node = node.parent
            }
            // if we didn't find it then we are done
            if (!nextNodeFound) break
        }
        return list
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shiftable#shift(org.dyn4j.geometry.Vector2)
	 */
    override fun shift(shift: Vector2) {
        // we need to update all nodes in the tree (not just the
        // nodes that contain the bodies)
        var node = root
        // perform a iterative, stack-less, in order traversal of the tree
        while (node != null) {
            // traverse down the left most tree first
            if (node.left != null) {
                node = node.left
            } else if (node.right != null) {
                // if the left sub tree is null then go
                // down the right sub tree
                node.aabb.translate(shift)
                node = node.right
            } else {
                // if both sub trees are null then go back
                // up the tree until we find the first left
                // node who's right node is not null
                node.aabb.translate(shift)
                var nextNodeFound = false
                while (node!!.parent != null) {
                    if (node === node.parent!!.left) {
                        if (node.parent!!.right != null) {
                            node.parent!!.aabb.translate(shift)
                            node = node.parent!!.right
                            nextNodeFound = true
                            break
                        }
                    }
                    node = node.parent
                }
                if (!nextNodeFound) break
            }
        }
    }

    /**
     * Internal recursive detection method.
     * @param node the node to test
     * @param root the root node of the subtree
     * @param filter the broadphase filter
     * @param pairs the list of pairs to add to
     */
    fun detect(
        node: DynamicAABBTreeLeaf<E, T>,
        root: DynamicAABBTreeNode?,
        filter: BroadphaseFilter<E, T>,
        pairs: MutableList<BroadphasePair<E, T>?>
    ) {
        // test the node itself
        if (node.aabb.overlaps(root!!.aabb)) {
            // check for leaf node
            // non-leaf nodes always have a left child
            if (root.left == null) {
                val leaf = root as DynamicAABBTreeLeaf<E, T>?
                if (!leaf!!.tested && leaf.collidable !== node.collidable) {
                    // its a leaf so add the pair
                    if (filter.isAllowed(node.collidable, node.fixture, leaf.collidable, leaf.fixture)) {
                        val pair = BroadphasePair(
                            node.collidable,  // A
                            node.fixture,
                            leaf.collidable,  // B
                            leaf.fixture
                        )
                        // add the pair to the list of pairs
                        pairs.add(pair)
                    }
                }
                // return and check other limbs
                return
            }
            // they overlap so descend into both children
            if (root.left != null) detect(node, root.left, filter, pairs)
            if (root.right != null) detect(node, root.right, filter, pairs)
        }
    }

    /**
     * Internal non-recursive detection method.
     * @param node the node to test
     * @param root the root node of the subtree
     * @param filter the broadphase filter
     * @param pairs the list of pairs to add to
     */
    fun detectNonRecursive(
        node: DynamicAABBTreeLeaf<E, T>,
        root: DynamicAABBTreeNode?,
        filter: BroadphaseFilter<E, T>,
        pairs: MutableList<BroadphasePair<E, T>>
    ) {
        // start at the root node
        var test = root
        // perform a iterative, stack-less, traversal of the tree
        while (test != null) {
            // check if the current node overlaps the desired node
            if (test.aabb.overlaps(node.aabb)) {
                // if they do overlap, then check the left child node
                if (test.left != null) {
                    // if the left is not null, then check that subtree
                    test = test.left
                    continue
                } else {
                    val leaf = test as DynamicAABBTreeLeaf<E, T>
                    // if both are null, then this is a leaf node
                    // check the tested flag to avoid duplicates and
                    // verify we aren't testing the same collidable against
                    // itself
                    if (!leaf.tested && leaf.collidable !== node.collidable) {
                        // its a leaf so add the pair
                        if (filter.isAllowed(node.collidable, node.fixture, leaf.collidable, leaf.fixture)) {
                            val pair = BroadphasePair(
                                node.collidable,  // A
                                node.fixture,
                                leaf.collidable,  // B
                                leaf.fixture
                            )
                            // add the pair to the list of pairs
                            pairs.add(pair)
                        }
                    }
                    // if its a leaf node then we need to go back up the
                    // tree and test nodes we haven't yet
                }
            }
            // if the current node is a leaf node or doesnt overlap the
            // desired aabb, then we need to go back up the tree until we
            // find the first left node who's right node is not null
            var nextNodeFound = false
            while (test!!.parent != null) {
                // check if the current node the left child of its parent
                if (test === test.parent!!.left) {
                    // it is, so check if the right node is non-null
                    // NOTE: not need since the tree is a complete tree (every node has two children)
                    //if (n.parent.right != null) {
                    // it isn't so the sibling node is the next node
                    test = test.parent!!.right
                    nextNodeFound = true
                    break
                    //}
                }
                // if the current node isn't a left node or it is but its
                // sibling is null, go to the parent node
                test = test.parent
            }
            // if we didn't find it then we are done
            if (!nextNodeFound) break
        }
    }

    /**
     * Internal recursive [AABB] detection method.
     * @param aabb the [AABB] to test
     * @param node the root node of the subtree
     * @param filter the broadphase filter
     * @param list the list to contain the results
     */
    fun detect(
        aabb: AABB,
        node: DynamicAABBTreeNode?,
        filter: BroadphaseFilter<E, T>,
        list: MutableList<BroadphaseItem<E, T>?>
    ) {
        // test the node itself
        if (aabb.overlaps(node!!.aabb)) {
            // check for leaf node
            // non-leaf nodes always have a left child
            if (node.left == null) {
                val leaf = node as DynamicAABBTreeLeaf<E, T>?
                // its a leaf so add the collidable
                if (filter.isAllowed(aabb, leaf!!.collidable, leaf.fixture)) {
                    list.add(BroadphaseItem(leaf.collidable, leaf.fixture))
                }
                // return and check other limbs
                return
            }
            // they overlap so descend into both children
            if (node.left != null) detect(aabb, node.left, filter, list)
            if (node.right != null) detect(aabb, node.right, filter, list)
        }
    }

    /**
     * Internal non-recursive [AABB] detection method.
     * @param aabb the [AABB] to test
     * @param node the root node of the subtree
     * @param filter the broadphase filter
     * @return List a list containing the results
     */
    fun detectNonRecursive(aabb: AABB, node: DynamicAABBTreeNode?, filter: BroadphaseFilter<E, T>): List<BroadphaseItem<E, T>> {
        // get the estimated collision count
        var node = node
        val eSize: Int = Collisions.getEstimatedCollisionsPerObject()
        val list: MutableList<BroadphaseItem<E, T>> = ArrayList(eSize)
        // perform a iterative, stack-less, traversal of the tree
        while (node != null) {
            // check if the current node overlaps the desired node
            if (aabb.overlaps(node.aabb)) {
                // if they do overlap, then check the left child node
                if (node.left != null) {
                    // if the left is not null, then check that subtree
                    node = node.left
                    continue
                } else {
                    // if both are null, then this is a leaf node
                    val leaf = node as DynamicAABBTreeLeaf<E, T>
                    if (filter.isAllowed(aabb, leaf.collidable, leaf.fixture)) {
                        list.add(BroadphaseItem(leaf.collidable, leaf.fixture))
                    }
                    // if its a leaf node then we need to go back up the
                    // tree and test nodes we haven't yet
                }
            }
            // if the current node is a leaf node or doesnt overlap the
            // desired aabb, then we need to go back up the tree until we
            // find the first left node who's right node is not null
            var nextNodeFound = false
            while (node!!.parent != null) {
                // check if the current node the left child of its parent
                if (node === node.parent!!.left) {
                    // it is, so check if the right node is non-null
                    // NOTE: not need since the tree is a complete tree (every node has two children)
                    //if (node.parent.right != null) {
                    // it isn't so the sibling node is the next node
                    node = node.parent!!.right
                    nextNodeFound = true
                    break
                    //}
                }
                // if the current node isn't a left node or it is but its
                // sibling is null, go to the parent node
                node = node.parent
            }
            // if we didn't find it then we are done
            if (!nextNodeFound) break
        }
        return list
    }

    /**
     * Internal method to insert a node into the tree.
     * @param item the node to insert
     */
    fun insert(item: DynamicAABBTreeNode) {
        // make sure the root is not null
        if (root == null) {
            // if it is then set this node as the root
            root = item
            // return from the insert method
            return
        }
        val temp = AABB(0.0, 0.0, 0.0, 0.0)

        // get the new node's aabb
        val itemAABB = item.aabb

        // start looking for the insertion point at the root
        var node = root
        // loop until node is a leaf or we find a better location
        while (!node!!.isLeaf) {
            // get the current node's aabb
            val aabb = node.aabb

            // the perimeter heuristic is better than area for 2D because
            // a line segment aligned with the x or y axis will generate
            // zero area

            // get its perimeter
            val perimeter = aabb.perimeter

            // union the new node's aabb and the current aabb
            // get the union's perimeter
            val unionPerimeter = temp.set(aabb).union(itemAABB).perimeter

            // compute the cost of creating a new parent for the new
            // node and the current node
            val cost = 2 * unionPerimeter

            // compute the minimum cost of descending further down the tree
            val descendCost = 2 * (unionPerimeter - perimeter)

            // get the left and right nodes
            val left = node.left
            val right = node.right

            // compute the cost of descending to the left
            var costl = 0.0
            costl = if (left!!.isLeaf) {
                temp.set(left.aabb).union(itemAABB).perimeter + descendCost
            } else {
                val oldPerimeter = left.aabb.perimeter
                val newPerimeter = temp.set(left.aabb).union(itemAABB).perimeter
                newPerimeter - oldPerimeter + descendCost
            }
            // compute the cost of descending to the right
            var costr = 0.0
            costr = if (right!!.isLeaf) {
                temp.set(right.aabb).union(itemAABB).perimeter + descendCost
            } else {
                val oldPerimeter = right.aabb.perimeter
                val newPerimeter = temp.set(right.aabb).union(itemAABB).perimeter
                newPerimeter - oldPerimeter + descendCost
            }

            // see if the cost to create a new parent node for the new
            // node and the current node is better than the children of
            // this node
            if (cost < costl && cost < costr) {
                break
            }

            // if not then choose the next best node to try
            node = if (costl < costr) {
                left
            } else {
                right
            }
        }

        // now that we have found a suitable place, insert a new root
        // node for node and item
        val parent = node.parent
        val newParent = DynamicAABBTreeNode()
        newParent.parent = node.parent
        newParent.aabb = node.aabb.getUnion(itemAABB)
        newParent.height = node.height + 1
        if (parent != null) {
            // node is not the root node
            if (parent.left === node) {
                parent.left = newParent
            } else {
                parent.right = newParent
            }
            newParent.left = node
            newParent.right = item
            node.parent = newParent
            item.parent = newParent
        } else {
            // node is the root item
            newParent.left = node
            newParent.right = item
            node.parent = newParent
            item.parent = newParent
            root = newParent
        }

        // fix the heights and aabbs
        node = item.parent
        while (node != null) {
            // balance the current tree
            node = balance(node)
            val left = node!!.left
            val right = node.right

            // neither node should be null
            node.height = 1 + max(left!!.height, right!!.height)
            //node.aabb = left.aabb.getUnion(right.aabb);
            node.aabb.set(left!!.aabb).union(right!!.aabb)
            node = node.parent
        }
    }

    /**
     * Internal method to remove a node from the tree.
     * @param node the node to remove
     */
    fun remove(node: DynamicAABBTreeNode) {
        // check for an empty tree
        if (root == null) return
        // check the root node
        if (node === root) {
            // set the root to null
            root = null
            // return from the remove method
            return
        }

        // get the node's parent, grandparent, and sibling
        val parent = node.parent
        val grandparent = parent!!.parent
        val other: DynamicAABBTreeNode?
        other = if (parent.left === node) {
            parent.right
        } else {
            parent.left
        }

        // check if the grandparent is null
        // indicating that the parent is the root
        if (grandparent != null) {
            // remove the node by overwriting the parent node
            // reference in the grandparent with the sibling
            if (grandparent.left === parent) {
                grandparent.left = other
            } else {
                grandparent.right = other
            }
            // set the siblings parent to the grandparent
            other!!.parent = grandparent

            // finally rebalance the tree
            var n = grandparent
            while (n != null) {
                // balance the current subtree
                n = balance(n)
                val left = n!!.left
                val right = n.right

                // neither node should be null
                n.height = 1 + max(left!!.height, right!!.height)
                // n.aabb = left.aabb.getUnion(right.aabb);
                n.aabb.set(left!!.aabb).union(right!!.aabb)
                n = n.parent
            }
        } else {
            // the parent is the root so set the root to the sibling
            root = other
            // set the siblings parent to null
            other!!.parent = null
        }
    }

    /**
     * Balances the subtree using node as the root.
     * @param node the root node of the subtree to balance
     * @return [DynamicAABBTreeNode] the new root of the subtree
     */
    fun balance(node: DynamicAABBTreeNode): DynamicAABBTreeNode? {

        // see if the node is a leaf node or if
        // it doesn't have enough children to be unbalanced
        if (node.isLeaf || node.height < 2) {
            // return since there isn't any work to perform
            return node
        }

        // get the nodes left and right children
        val b = node.left
        val c = node.right

        // compute the balance factor for node a
        val balance = c!!.height - b!!.height

        // if the balance is off on the right side
        if (balance > 1) {
            // get the c's left and right nodes
            val f = c.left
            val g = c.right

            // switch a and c
            c.left = node
            c.parent = node.parent
            node.parent = c

            // update c's parent to point to c instead of a
            if (c.parent != null) {
                if (c.parent!!.left === node) {
                    c.parent!!.left = c
                } else {
                    c.parent!!.right = c
                }
            } else {
                root = c
            }

            // compare the balance of the children of c
            if (f!!.height > g!!.height) {
                // rotate left
                c.right = f
                node.right = g
                g.parent = node
                // update the aabb
//				a.aabb = b.aabb.getUnion(g.aabb);
                node.aabb.set(b.aabb).union(g.aabb)
                //				c.aabb = a.aabb.getUnion(f.aabb);
                c.aabb.set(node.aabb).union(f.aabb)
                // update the heights
                node.height = 1 + max(b.height, g.height)
                c.height = 1 + max(node.height, f.height)
            } else {
                // rotate right
                c.right = g
                node.right = f
                f.parent = node
                // update the aabb
//				a.aabb = b.aabb.getUnion(f.aabb);
                node.aabb.set(b.aabb).union(f.aabb)
                //				c.aabb = a.aabb.getUnion(g.aabb);
                c.aabb.set(node.aabb).union(g.aabb)
                // update the heights
                node.height = 1 + max(b.height, f.height)
                c.height = 1 + max(node.height, g.height)
            }
            // c is the new root node of the subtree
            return c
        }
        // if the balance is off on the left side
        if (balance < -1) {
            // get b's children
            val d = b.left
            val e = b.right

            // switch a and b
            b.left = node
            b.parent = node.parent
            node.parent = b

            // update b's parent to point to b instead of a
            if (b.parent != null) {
                if (b.parent!!.left === node) {
                    b.parent!!.left = b
                } else {
                    b.parent!!.right = b
                }
            } else {
                root = b
            }

            // compare the balance of the children of b
            if (d!!.height > e!!.height) {
                // rotate left
                b.right = d
                node.left = e
                e.parent = node
                // update the aabb
//				a.aabb = c.aabb.getUnion(e.aabb);
                node.aabb.set(c.aabb).union(e.aabb)
                //				b.aabb = a.aabb.getUnion(d.aabb);
                b.aabb.set(node.aabb).union(d.aabb)
                // update the heights
                node.height = 1 + max(c.height, e.height)
                b.height = 1 + max(node.height, d.height)
            } else {
                // rotate right
                b.right = e
                node.left = d
                d.parent = node
                // update the aabb
//				a.aabb = c.aabb.getUnion(d.aabb);
                node.aabb.set(c.aabb).union(d.aabb)
                //				b.aabb = a.aabb.getUnion(e.aabb);
                b.aabb.set(node.aabb).union(e.aabb)
                // update the heights
                node.height = 1 + max(c.height, d.height)
                b.height = 1 + max(node.height, e.height)
            }
            // b is the new root node of the subtree
            return b
        }
        // no balancing required so return the original subtree root node
        return node
    }

    /**
     * Internal recursive method used to validate the state of the
     * subtree with the given node as the root.
     *
     *
     * Used for testing only.  Test using the -ea flag on the command line.
     * @param node the root of the subtree to validate
     */
    fun validate(node: DynamicAABBTreeNode?) {
        // just return if the given node is null
        if (node == null) {
            return
        }
        // check if the node is the root node
        if (node === root) {
            // if so, then make sure its parent is null
            check(node.parent == null)
        }

        // get the left and right children
        val left = node.left
        val right = node.right

        // check if the node is a leaf
        if (node.isLeaf) {
            val leaf = node as DynamicAABBTreeLeaf<E, T>
            check(node.left == null)
            check(node.right == null)
            check(node.height == 0)
            check(leaf.collidable != null)
            return
        }
        check(node.aabb.contains(left!!.aabb))
        if (right != null) check(node.aabb.contains(right.aabb))
        check(left!!.parent === node)
        check(right!!.parent === node)

        // validate the child subtrees
        validate(left)
        validate(right)
    }
    /**
     * Optional constructor.
     *
     *
     * Allows fine tuning of the initial capacity of local storage for faster running times.
     * @param initialCapacity the initial capacity of local storage
     * @throws IllegalArgumentException if initialCapacity is less than zero
     * @since 3.1.1
     */
    /**
     * Default constructor.
     */
    init {
        // 0.75 = 3/4, we can garuantee that the hashmap will not need to be rehashed
        // if we take capacity / load factor
        // the default load factor is 0.75 according to the javadocs, but lets assign it to be sure
        map = LinkedHashMap(initialCapacity * 4 / 3 + 1, 0.75f)
    }
}