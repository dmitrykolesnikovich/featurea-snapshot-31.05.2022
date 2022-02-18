package org.dyn4j.collision.broadphase

import org.dyn4j.collision.Collidable
import org.dyn4j.collision.Collisions
import org.dyn4j.collision.Collisions.getEstimatedCollisionsPerObject
import org.dyn4j.collision.Collisions.getEstimatedRaycastCollisions
import org.dyn4j.collision.Fixture
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.AABB
import org.dyn4j.geometry.Ray
import org.dyn4j.geometry.Vector2
import kotlin.math.max


/**
 * Implementation of a self-balancing axis-aligned bounding box tree broad-phase collision detection algorithm.
 *
 *
 * This class implements a aabb tree broad-phase detector that is based on ideas from [DynamicAABBTree] but with some very critical improvements.
 * This data structure is lazy in the sense that it will build the actual tree as late as possible (hence the name).
 * Performance is optimized for fast detection of collisions, as required by the [World] class. Raycasting and other functionalities should see no big improvements.
 * Insertion is O(1), update is O(logn) but batch update (update of all bodies) is O(n), remove is O(logn) average but O(n) worse.
 *
 *
 * The class will rebuild the whole tree at each detection and will detect the collisions at the same time in an efficient manner.
 *
 *
 * This structure keeps the bodies sorted by the radius of their fixtures and rebuilds the tree each time in order to construct better trees.
 *
 * @author Manolis Tsamis
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
 * @version 3.4.0
 * @since 3.4.0
</T></E> */
class LazyAABBTree<E : Collidable<T>, T : Fixture> constructor(initialCapacity: Int = BroadphaseDetector.DEFAULT_INITIAL_CAPACITY) :
    AbstractBroadphaseDetector<E, T>(), BatchBroadphaseDetector<E, T> {
    /** The root node of the tree  */
    var root: LazyAABBTreeNode? = null

    /** Id to leaf map for fast lookup in tree of list  */
    val elementMap: MutableMap<BroadphaseKey, LazyAABBTreeLeaf<E, T>>

    /** List of all leafs, either on tree or not  */
    val elements: MutableList<LazyAABBTreeLeaf<E, T>>

    /** Whether there's new data to sort  */
    var sorted = true

    /** Whether there are leafs waiting to be batch-removed  */
    var pendingRemoves = false

    /** Whether there are leafs waiting to be batch-inserted  */
    var pendingInserts = false
    /**
     * Optional constructor.
     *
     *
     * Allows fine tuning of the initial capacity of local storage for faster running times.
     *
     * @param initialCapacity the initial capacity of local storage
     * @throws IllegalArgumentException if initialCapacity is less than zero
     */
    /**
     * Default constructor.
     */
    init {
        elements = ArrayList(initialCapacity)
        elementMap = HashMap(initialCapacity)
    }

    /**
     * Destroys the existing tree in O(n) time and prepares for batch-detection,
     * but does not update the AABBs of elements.
     */
    fun batchRebuild() {
        for (node in elements) {
            node.setOnTree(false)
        }
        root = null
        pendingInserts = true
    }

    /**
     * Destroys the existing tree in O(n) time and prepares for batch-detection while
     * also updating all AABBs. Called by [World] in each step before detection.
     */
    override fun batchUpdate() {
        for (node in elements) {
            node.setOnTree(false)
            node.updateAABB()
        }
        root = null
        pendingInserts = true
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#add(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun add(collidable: E, fixture: T) {
        // create a new node for the collidable
        val key = BroadphaseKey[collidable, fixture]
        val existing: LazyAABBTreeLeaf<E, T>? = elementMap.get(key)
        if (existing != null) {
            // update existing node
            if (existing.isOnTree()) {
                this.remove(existing)
                existing.setOnTree(false)
            }
            existing.updateAABB()
        } else {
            // add new node
            val node = LazyAABBTreeLeaf(collidable, fixture)
            elementMap[key] = node
            elements.add(node)
            sorted = false
        }
        pendingInserts = true
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#remove(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun remove(collidable: E, fixture: T?): Boolean {
        val key = BroadphaseKey[collidable, fixture]
        // find the node in the map
        val node: LazyAABBTreeLeaf<E, T>? = elementMap.remove(key)
        // make sure it was found
        if (node != null) {
            if (node.isOnTree()) {
                // remove the node from the tree
                // since the node is on the tree we know that the root is not null
                // so we can safely call removeImpl
                this.remove(node)
            }
            node.markForRemoval()
            pendingRemoves = true
            return true
        }
        return false
    }

    /**
     * Internal method to remove a leaf from the tree.
     * Assumes the root is not null.
     *
     * @param leaf the leaf to remove
     */
    fun remove(leaf: LazyAABBTreeLeaf<E, T>?) {
        // check the root node
        if (leaf === root) {
            // set the root to null
            root = null
            // return from the remove method
            return
        }

        // get the node's parent, grandparent, and sibling
        val parent = leaf!!.parent
        val grandparent = parent!!.parent
        val other = leaf.sibling

        // check if the grandparent is null
        // indicating that the parent is the root
        if (grandparent != null) {
            // remove the node by overwriting the parent node
            // reference in the grandparent with the sibling
            grandparent.replaceChild(parent, other)
            // set the siblings parent to the grandparent
            other!!.parent = grandparent

            // finally rebalance the tree
            this.balanceAll(grandparent)
        } else {
            // the parent is the root so set the root to the sibling
            root = other
            // set the siblings parent to null
            other!!.parent = null
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#update(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun update(collidable: E, fixture: T) {
        // In the way the add and update are described in BroadphaseDetector, their functionallity is identical
        // so just redirect the work to add for less duplication.
        this.add(collidable, fixture)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#getAABB(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun getAABB(collidable: E, fixture: T): AABB? {
        val key = BroadphaseKey[collidable, fixture]
        val node: LazyAABBTreeLeaf<E, T>? = elementMap.get(key)
        return if (node != null && !node.mustRemove()) {
            node.aabb
        } else fixture.shape.createAABB(collidable.transform)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#contains(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun contains(collidable: E, fixture: T): Boolean {
        val key = BroadphaseKey[collidable, fixture]
        return elementMap.containsKey(key)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#clear()
	 */
    override fun clear() {
        elementMap.clear()
        elements.clear()
        root = null

        // Important: since everything is removed there's no pending work to do
        sorted = true
        pendingRemoves = false
        pendingInserts = false
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#size()
	 */
    override fun size(): Int {
        return elements.size
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#detect(org.dyn4j.collision.broadphase.BroadphaseFilter)
	 */
    override fun detect(filter: BroadphaseFilter<E, T>): MutableList<BroadphasePair<E, T>> {
        val eSize: Int = Collisions.getEstimatedCollisionPairs(size())
        val pairs: MutableList<BroadphasePair<E, T>> = ArrayList(eSize)

        // this will not happen, unless the user makes more detect calls outside of the World class
        // so it can be considered rare
        if (root != null) {
            batchRebuild()
        }
        this.buildAndDetect(filter, pairs)
        return pairs
    }

    /**
     * Internal method to actually remove all leafs marked for removal.
     * If there are any (see pendingRemoves) performs all deletions in O(n) time, else no work is done.
     * This mechanism is used to solve the O(n) time for removing an element from the elements ArrayList.
     * Although worst case is the same, in various scenarios this will perform better.
     * Assumes all leafs marked for removal are **not** on the tree.
     */
    fun doPendingRemoves() {
        // We use the check (this.pendingRemoves) to avoid scanning all elements (O(n)) when
        // detect(AABB) or raycast are called a lot of times consecutively
        if (pendingRemoves) {
            // Important: because we're removing elements this.elements.size() can change in the loop
            // so we must call it in each loop iteration
            var i = 0
            while (i < elements.size) {
                val node: LazyAABBTreeLeaf<E, T> = elements[i]
                if (node.mustRemove()) {
                    // removed nodes are not on the tree, no need to check.
                    // Just remove the element from the list by swapping it with the last one and removing the last

                    // Note that works ok for the case elements.size() == 1
                    // so no need to have an extra branch for that case
                    val lastIndex = elements.size - 1

                    // Swap with the last
                    elements[i] = elements[lastIndex]

                    // And remove the last
                    // No copying involved here, just a size decrease
                    elements.removeAt(lastIndex)

                    // don't forget to check the new element that was moved into position i
                    i--
                }
                i++
            }
            pendingRemoves = false

            // Due to the swapping we need to restore the sorting order
            sorted = false
        }
    }

    /**
     * Internal method that sorts the elements if needed.
     * Note that the sorting routines in array list are optimized for partially sorted data
     * and we can expect the sorting to happen very fast if just a few changes did happen from the last sorting.
     */
    fun ensureSorted() {
        if (!sorted) {
            elements.sortedWith(object : Comparator<LazyAABBTreeLeaf<E, T>> {
                override fun compare(o1: LazyAABBTreeLeaf<E, T>, o2: LazyAABBTreeLeaf<E, T>): Int {
                    // Important heuristic: sort by size of fixtures.
                    // Radius is used here because the AABBs are not yet computed,
                    // but this works just as fine.
                    return o1.fixture.shape.radius.compareTo(o2.fixture.shape.radius)
                }
            })
            // NOTE: use this instead if dyn4j moves to Java 8+
//			elements.sort(new Comparator<LazyAABBTreeLeaf<E, T>>() {
//				@Override
//				public int compare(LazyAABBTreeLeaf<E, T> o1, LazyAABBTreeLeaf<E, T> o2) {
//					// Important heuristic: sort by size of fixtures.
//					// Radius is used here because the AABBs are not yet computed,
//					// but this works just as fine.
//					return Double.compare(o1.fixture.shape.radius, o2.fixture.shape.radius);
//				}
//			});
            sorted = true
        }
    }

    /**
     * Internal method to ensure that all nodes are on the tree.
     */
    fun ensureOnTree() {
        // We use the check (this.pendingInserts) to avoid scanning all elements (O(n)) when
        // detect(AABB) or raycast are called a lot of times consecutively
        if (pendingInserts) {
            val size = elements.size
            for (i in 0 until size) {
                val node = elements[i]
                if (!node.isOnTree()) {
                    this.insert(node)
                }
            }
            pendingInserts = false
        }
    }

    /**
     * Internal method that ensures the whole tree is built. This just creates the tree and performs no detection.
     * This is used to support raycasting and single AABB queries.
     */
    fun build() {
        doPendingRemoves()
        ensureSorted()
        ensureOnTree()
    }

    /**
     * The heart of the LazyAABBTree batch detection.
     * Assumes no tree exists and in performs all the broad-phase detection while building the tree from scratch.
     *
     * @param filter the broadphase filter
     * @param pairs List a list containing the results
     */
    fun buildAndDetect(filter: BroadphaseFilter<E, T>?, pairs: MutableList<BroadphasePair<E, T>>) {
        doPendingRemoves()
        ensureSorted()
        val size = elements.size
        for (i in 0 until size) {
            val node = elements[i]
            this.insertAndDetect(node, filter, pairs)
        }
    }

    /**
     * Cost function for descending to a particular node.
     * The cost equals the enlargement caused in the [AABB] of the node.
     * More specifically, descendCost(node, aabb) = (perimeter(union(node.aabb, aabb)) - perimeter(node.aabb)) / 2
     *
     * @param node the node to descend
     * @param itemAABB the AABB of the item being inserted
     * @return the cost of descending to node
     */
    fun descendCost(node: LazyAABBTreeNode, itemAABB: AABB): Double {
        val nodeAABB = node.aabb

        // The positive values indicate enlargement
        var enlargement = 0.0

        // Calculate enlargement in x axis
        val enlargementMinX = nodeAABB!!.minX - itemAABB.minX
        val enlargementMaxX = itemAABB.maxX - nodeAABB.maxX
        if (enlargementMinX > 0) enlargement += enlargementMinX
        if (enlargementMaxX > 0) enlargement += enlargementMaxX

        // Calculate enlargement in y axis
        val enlargementMinY = nodeAABB.minY - itemAABB.minY
        val enlargementMaxY = itemAABB.maxY - nodeAABB.maxY
        if (enlargementMinY > 0) enlargement += enlargementMinY
        if (enlargementMaxY > 0) enlargement += enlargementMaxY
        return enlargement
    }

    /**
     * Internal method to insert a leaf in the tree
     *
     * @param item the leaf to insert
     */
    fun insert(item: LazyAABBTreeLeaf<E, T>) {
        insert(item, false, null, null)
    }

    /**
     * Internal method to insert a leaf in the tree and also perform all the collision detection required for that tree
     *
     * @param item the leaf to insert
     * @param filter the broadphase filter
     * @param pairs a list containing the results
     */
    fun insertAndDetect(item: LazyAABBTreeLeaf<E, T>, filter: BroadphaseFilter<E, T>?, pairs: MutableList<BroadphasePair<E, T>>) {
        insert(item, true, filter, pairs)
    }

    /**
     * The implementation routine for the tree. In order to avoid code duplication this method performs either insertion with detection
     * or just insertion, as requested by the 'detect' parameter. The actual insertion algorithm is the same with that in [DynamicAABBTree]
     * but with a variety of optimizations and clean-ups.
     *
     * @param item The leaf to insert
     * @param detect Whether to also perform collision detection
     * @param filter the broadphase filter
     * @param pairs List a list containing the results
     */
    fun insert(item: LazyAABBTreeLeaf<E, T>, detect: Boolean, filter: BroadphaseFilter<E, T>?, pairs: MutableList<BroadphasePair<E, T>>?) {
        // Mark that this leaf is now on the tree
        item.setOnTree(true)

        // Make sure the root is not null
        if (root == null) {
            // If it is then set this node as the root
            root = item
            return
        }

        // Get the new node's AABB
        val itemAABB = item.aabb!!

        // Start looking for the insertion point at the root
        var node = root

        // loop until node is a leaf
        while (!node!!.isLeaf) {
            var other: LazyAABBTreeNode?
            val costLeft: Double = this.descendCost(node.left!!, itemAABB)
            if (costLeft == 0.0) {
                // Fast path: if (costLeft == 0) then this means that
                // itemAABB is contained inside node.left.aabb (zero enlargement).
                // This is optimal so we don't need to check the right child.
                // We also need not to enlarge node.aabb: since itemAABB is contained
                // in one of node's children then node.aabb already contains itemAABB as well.
                // This 'fast path' is beneficial because as the tree get's larger the first
                // levels of the tree have comparably large AABBs so this helps sink in the tree faster.
                other = node.right
                node = node.left
            } else {
                val costRight: Double = this.descendCost(node.right!!, itemAABB)
                // Although we could check if (costRight == 0) and make a similar case as above
                // there are not many gains, one fast path is enough

                // Enlarge the AABB of this node as needed
                node.aabb!!.union(itemAABB!!)
                if (costLeft < costRight) {
                    other = node.right
                    node = node.left
                } else {
                    other = node.left
                    node = node.right
                }
            }

            // perform collision detection to the child that we did not descend if needed
            if (detect && other!!.aabb!!.overlaps(itemAABB!!)) {
                detectWhileBuilding(item, other, filter!!, pairs)
            }
        }

        // We also need to perform collision detection for the leaf where we ended
        if (detect && node.aabb!!.overlaps(itemAABB!!)) {
            detectWhileBuilding(item, node, filter!!, pairs)
        }

        // Now that we have found a suitable place, insert a new node here for the new item
        val parent = node.parent
        val newParent = LazyAABBTreeNode()
        newParent.parent = parent
        newParent.aabb = node.aabb!!.getUnion(itemAABB!!)

        // Since node is always a leaf, newParent has height 1
        newParent.height = 1
        if (parent != null) {
            // Node is not the root node
            parent.replaceChild(node, newParent)
        } else {
            // Node is the root item
            root = newParent
        }
        newParent.left = node
        newParent.right = item
        node.parent = newParent
        item.parent = newParent

        // Fix the heights and balance the tree
        balanceAll(newParent.parent)
    }


    private fun detectWhileBuilding(
        node: LazyAABBTreeLeaf<E, T>, root: LazyAABBTreeNode?,
        filter: BroadphaseFilter<E, T>, pairs: MutableList<BroadphasePair<E, T>>?
    ) {
        // test the node itself
        // check for leaf node
        // non-leaf nodes always have a left child
        if (root!!.isLeaf) {
            val leaf = root as LazyAABBTreeLeaf<E, T>?
            if (filter.isAllowed(node.collidable, node.fixture, leaf!!.collidable, leaf.fixture)) {
                val pair = BroadphasePair(
                    node.collidable,  // A
                    node.fixture,
                    leaf.collidable,  // B
                    leaf.fixture
                )
                // add the pair to the list of pairs
                pairs!!.add(pair)
            }
        } else {
            // they overlap so descend into both children
            if (node.aabb!!.overlaps(root.left!!.aabb!!)) detectWhileBuilding(node, root.left, filter, pairs)
            if (node.aabb!!.overlaps(root.right!!.aabb!!)) detectWhileBuilding(node, root.right, filter, pairs)
        }
    }


    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#detect(org.dyn4j.geometry.AABB)
	 */
    override fun detect(aabb: AABB, filter: BroadphaseFilter<E, T>): List<BroadphaseItem<E, T>> {
        build()
        if (root == null) {
            return emptyList()
        }
        val eSize = getEstimatedCollisionsPerObject()
        val list: MutableList<BroadphaseItem<E, T>> = ArrayList(eSize)
        if (aabb.overlaps(root!!.aabb!!)) {
            this.detect(aabb, root, filter, list)
        }
        return list
    }

    /**
     * Internal recursive method used to implement BroadphaseDetector#detect.
     * @param aabb the aabb to test with
     * @param node the node to begin at
     * @param filter the filter
     * @param list the results list
     */
    private fun detect(
        aabb: AABB,
        node: LazyAABBTreeNode?,
        filter: BroadphaseFilter<E, T>,
        list: MutableList<BroadphaseItem<E, T>>
    ) {
        // test the node itself
        // check for leaf node
        // non-leaf nodes always have a left child
        if (node!!.isLeaf) {
            val leaf = node as LazyAABBTreeLeaf<E, T>?
            // its a leaf so add the collidable
            if (filter.isAllowed(aabb, leaf!!.collidable, leaf.fixture)) {
                list.add(BroadphaseItem(leaf.collidable, leaf.fixture))
            }
            // return and check other limbs
        } else {
            // they overlap so descend into both children
            if (aabb.overlaps(node.left!!.aabb!!)) this.detect(aabb, node.left, filter, list)
            if (aabb.overlaps(node.right!!.aabb!!)) this.detect(aabb, node.right, filter, list)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#raycast(org.dyn4j.geometry.Ray, double)
	 */
    override fun raycast(ray: Ray, length: Double, filter: BroadphaseFilter<E, T>): List<BroadphaseItem<E, T>> {
        var length = length
        build()
        if (root == null) {
            return emptyList()
        }

        // create an aabb from the ray
        val s = ray.start!!
        val d = ray.directionVector

        // get the length
        if (length <= 0.0) {
            length = Double.MAX_VALUE
        }

        // create the aabb
        val w = d.x * length
        val h = d.y * length
        val aabb = AABB.createAABBFromPoints(s.x, s.y, s.x + w, s.y + h)
        if (!root!!.aabb!!.overlaps(aabb)) {
            return emptyList()
        }

        // precompute
        val invDx = 1.0 / d.x
        val invDy = 1.0 / d.y

        // get the estimated collision count
        val eSize = getEstimatedRaycastCollisions(elementMap.size)
        val items: MutableList<BroadphaseItem<E, T>> = ArrayList(eSize)
        var node = root

        // perform a iterative, stack-less, traversal of the tree
        while (node != null) {
            // check if the current node overlaps the desired node
            if (aabb.overlaps(node.aabb!!)) {
                // if they do overlap, then check the left child node
                if (node.isLeaf) {
                    if (this.raycast(s, length, invDx, invDy, node.aabb!!)) {
                        // if both are null, then this is a leaf node
                        val leaf = node as LazyAABBTreeLeaf<E, T>
                        if (filter.isAllowed(ray, length, leaf.collidable, leaf.fixture)) {
                            items.add(BroadphaseItem(leaf.collidable, leaf.fixture))
                        }
                        // if its a leaf node then we need to go back up the
                        // tree and test nodes we haven't yet
                    }
                } else {
                    // if the left is not null, then check that subtree
                    node = node.left
                    continue
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
        return items
    }

    override fun shift(shift: Vector2) {
        // make sure the tree is built
        build()

        // Left intact from DynamicAABBTree

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
                node.aabb!!.translate(shift)
                node = node.right
            } else {
                // if both sub trees are null then go back
                // up the tree until we find the first left
                // node who's right node is not null
                node.aabb!!.translate(shift)
                var nextNodeFound = false
                while (node!!.parent != null) {
                    if (node === node.parent!!.left) {
                        if (node.parent!!.right != null) {
                            node.parent!!.aabb!!.translate(shift)
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

    fun balanceAll(node: LazyAABBTreeNode?) {
        var node = node
        while (node != null) {
            // balance the current tree
            this.balance(node)
            node = node.parent
        }
    }

    fun balance(a: LazyAABBTreeNode) {
        // see if the node is a leaf node or if
        // it doesn't have enough children to be unbalanced
        if (a.height < 2) {
            // return since there isn't any work to perform
            a.height = 1 + max(a.left!!.height, a.right!!.height)
            return
        }

        // get the nodes left and right children
        val b: LazyAABBTreeNode?
        val c: LazyAABBTreeNode?

        // compute the balance factor for node a
        val balance = a.right!!.height - a.left!!.height
        if (balance > 1) {
            b = a.left
            c = a.right
        } else if (balance < -1) {
            b = a.right
            c = a.left
        } else {
            a.height = 1 + kotlin.math.max(a.left!!.height, a.right!!.height)
            return
        }

        // get the c's left and right nodes
        var d = c!!.left
        var e = c.right

        // switch a and c
        c.left = a
        c.parent = a.parent
        a.parent = c

        // update c's parent to point to c instead of a
        if (c.parent != null) {
            c.parent!!.replaceChild(a, c)
        } else {
            root = c
        }
        if (d!!.height <= e!!.height) {
            val temp = d
            d = e
            e = temp
        }
        if (balance > 1) {
            a.right = e
        } else {
            a.left = e
        }
        c.right = d
        e.parent = a

        // update the aabb
        a.aabb!!.set(b!!.aabb!!).union(e.aabb!!)
        c.aabb!!.set(a.aabb!!).union(d.aabb!!)

        // update the heights
        a.height = 1 + max(b.height, e.height)
        c.height = 1 + max(a.height, d.height)

        // c is the new root node of the subtree
    }

    override var expansion: Double = 0.0

    /*
	 * (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#supportsAABBExpansion()
	 */
    override fun supportsAABBExpansion(): Boolean {
        return false
    }

}