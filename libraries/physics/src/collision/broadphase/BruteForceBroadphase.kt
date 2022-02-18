/*
 * Copyright (c) 2010-2017 William Bittle http://www.dyn4j.org/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 * * Neither the name of dyn4j nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
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
import org.dyn4j.collision.Fixture
import org.dyn4j.geometry.AABB
import org.dyn4j.geometry.Ray
import org.dyn4j.geometry.Vector2

/**
 * This class implements the simplest possible broad-phase detector,
 * a brute-force algorithm for finding all pairs of collisions (and similar queries).
 * <p>
 * This implementation is not tuned for performance in any way and should <b>not</b> be used
 * except for testing purposes. One main reason this was developed is for automated testing of the other broad-phase detectors.
 * <p>
 * The logic of this class is simple: It holds a hash table of all the nodes and each time a query is made it scans linearly
 * all the nodes to find the answer.
 * <p>
 * Important note: This class must not use AABB expansion in order to always return the minimum set of pairs/items.
 * This property is used to test the other broad-phase detectors correctly.
 *
 * @author Manolis Tsamis
 * @version 3.4.0
 * @since 3.4.0
 * @param <E> the {@link Collidable} type
 * @param <T> the {@link Fixture} type
 */
class BruteForceBroadphase<E : Collidable<T>, T : Fixture> : AbstractBroadphaseDetector<E, T>(),
    BroadphaseDetector<E, T> {
    /** Id to node map for fast lookup */
    internal val map: MutableMap<BroadphaseKey, BruteForceBroadphaseNode<E, T>>

    /*
     * (non-Javadoc)
     * @see org.dyn4j.collision.broadphase.BroadphaseDetector#expansion
     */
    val aabbExpansion: Double
        get() {
            return 0.0
        }

    /**
     * Default constructor.
     */
    init {
        this.map = LinkedHashMap<BroadphaseKey, BruteForceBroadphaseNode<E, T>>()
    }

    /* (non-Javadoc)
     * @see org.dyn4j.collision.broadphase.BroadphaseDetector#add(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
     */
    override fun add(collidable: E, fixture: T) {
        val key = BroadphaseKey[collidable, fixture]
        val node = this.map[key]
        if (node != null) {
            // if the collidable-fixture has already been added just update it
            node.updateAABB()
        } else {
            // else add the new node
            this.map[key] = BruteForceBroadphaseNode<E, T>(collidable, fixture)
        }
    }

    /* (non-Javadoc)
     * @see org.dyn4j.collision.broadphase.BroadphaseDetector#remove(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
     */
	override fun remove(collidable: E, fixture: T?): Boolean {
        val key = BroadphaseKey.get(collidable, fixture)
        // find the node in the map
        val node = this.map.remove(key)
        return node != null
    }

    /* (non-Javadoc)
     * @see org.dyn4j.collision.broadphase.BroadphaseDetector#update(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
     */
	override fun update(collidable: E, fixture: T) {
        add(collidable, fixture)
    }

    /* (non-Javadoc)
     * @see org.dyn4j.collision.broadphase.BroadphaseDetector#getAABB(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
     */
	override fun getAABB(collidable: E, fixture: T): AABB? {
        val key = BroadphaseKey.get(collidable, fixture)
        val node = this.map.get(key)
        if (node != null) {
            return node.aabb
        }
        return fixture.shape.createAABB(collidable.transform)
    }

    /* (non-Javadoc)
     * @see org.dyn4j.collision.broadphase.BroadphaseDetector#contains(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
     */
    override fun contains(collidable: E, fixture: T): Boolean {
        val key = BroadphaseKey.get(collidable, fixture)
        return this.map.containsKey(key)
    }

    /* (non-Javadoc)
     * @see org.dyn4j.collision.broadphase.BroadphaseDetector#clear()
     */
    override fun clear() {
        this.map.clear()
    }

    /* (non-Javadoc)
     * @see org.dyn4j.collision.broadphase.BroadphaseDetector#size()
     */
    override fun size(): Int {
        return this.map.size
    }

    /* (non-Javadoc)
     * @see org.dyn4j.collision.broadphase.BroadphaseDetector#detect(org.dyn4j.collision.broadphase.BroadphaseFilter)
     */
    override fun detect(filter: BroadphaseFilter<E, T>): List<BroadphasePair<E, T>> {
        // clear all the tested flags on the nodes
        val size = this.map.size
        val nodes = this.map.values
        for (node in nodes) {
            // reset the flag
            node.tested = false
        }
        // the estimated size of the pair list
        val eSize = Collisions.getEstimatedCollisionPairs(size)
        val pairs = ArrayList<BroadphasePair<E, T>>(eSize)
        // test each collidable in the collection
        for (node in nodes) {
            for (other in nodes) {
                if (node.aabb.overlaps(other.aabb) && !other.tested && other.collidable !== node.collidable) {
                    // if they overlap and not already tested
                    if (filter.isAllowed(node.collidable, node.fixture, other.collidable, other.fixture)) {
                        val pair = BroadphasePair<E, T>(
                            node.collidable, // A
                            node.fixture,
                            other.collidable, // B
                            other.fixture
                        )
                        // add the pair to the list of pairs
                        pairs.add(pair)
                    }
                }
            }
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
        // the estimated size of the item list
        val eSize = Collisions.estimatedCollisionsPerObject
        val list = ArrayList<BroadphaseItem<E, T>>(eSize)
        val nodes = this.map.values
        // test each collidable in the collection
        for (node in nodes) {
            if (aabb.overlaps(node.aabb)) {
                if (filter.isAllowed(aabb, node.collidable, node.fixture)) {
                    list.add(BroadphaseItem<E, T>(node.collidable, node.fixture))
                }
            }
        }
        return list
    }

    /* (non-Javadoc)
     * @see org.dyn4j.collision.broadphase.BroadphaseDetector#raycast(org.dyn4j.geometry.Ray, double)
     */
    override fun raycast(ray: Ray, length: Double, filter: BroadphaseFilter<E, T>): List<BroadphaseItem<E, T>> {
        // check the size of the proxy list
        if (this.map.size == 0) {
            // return an empty list
            return emptyList<BroadphaseItem<E, T>>()
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
        // get the estimated collision count
        val eSize = Collisions.getEstimatedRaycastCollisions(this.map.size)
        val list = ArrayList<BroadphaseItem<E, T>>(eSize)
        val nodes = this.map.values
        for (node in nodes) {
            if (aabb.overlaps(node.aabb) && this.raycast(s, l, invDx, invDy, node.aabb)) {
                if (filter.isAllowed(ray, length, node.collidable, node.fixture)) {
                    list.add(BroadphaseItem<E, T>(node.collidable, node.fixture))
                }
            }
        }
        return list
    }

    /* (non-Javadoc)
     * @see org.dyn4j.geometry.Shiftable#shift(org.dyn4j.geometry.Vector2)
     */
    override fun shift(shift: Vector2) {
        val nodes = this.map.values
        for (node in nodes) {
            node.aabb.translate(shift)
        }
    }

    /*
     * (non-Javadoc)
     * @see org.dyn4j.collision.broadphase.BroadphaseDetector#supportsAABBExpansion()
     */
    override fun supportsAABBExpansion(): Boolean {
        return false
    }
}