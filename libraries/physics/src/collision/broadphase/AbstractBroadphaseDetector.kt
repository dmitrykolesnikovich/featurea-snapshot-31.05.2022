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

import featurea.math.max
import featurea.math.min
import org.dyn4j.collision.Collidable
import org.dyn4j.collision.Fixture
import org.dyn4j.geometry.*

/**
 * Abstract implementation of a [BroadphaseDetector].
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
</T></E> */
abstract class AbstractBroadphaseDetector<E : Collidable<T>, T : Fixture> : BroadphaseDetector<E, T> {
    /** The default broadphase filter object  */
    val defaultFilter: BroadphaseFilter<E, T> = DefaultBroadphaseFilter<E, T>()

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#expansion
	 *//* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#setAABBExpansion(double)
	 */
    /** The [AABB] expansion value  */
    override var expansion: Double = BroadphaseDetector.DEFAULT_AABB_EXPANSION

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#add(org.dyn4j.collision.Collidable)
	 */
    override fun add(collidable: E) {
        val size: Int = collidable.fixtureCount
        // iterate over the new list
        for (i in 0 until size) {
            val fixture: T = collidable.getFixture(i)
            add(collidable, fixture)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#remove(org.dyn4j.collision.Collidable)
	 */
    override fun remove(collidable: E) {
        val size: Int = collidable.fixtureCount
        if (size == 0) return
        for (i in 0 until size) {
            val fixture: T = collidable.getFixture(i)
            this.remove(collidable, fixture)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#update(org.dyn4j.collision.Collidable)
	 */
    override fun update(collidable: E) {
        val size: Int = collidable.fixtureCount
        // iterate over the new list
        for (i in 0 until size) {
            val fixture: T = collidable.getFixture(i)
            update(collidable, fixture)
        }
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#detect(org.dyn4j.collision.Collidable, org.dyn4j.collision.Collidable)
	 */
    override fun detect(a: E, b: E): Boolean {
        // attempt to use this broadphase's cache
        val aAABB: AABB? = getAABB(a)
        val bAABB: AABB? = getAABB(b)
        // check for null
        if (aAABB == null || bAABB == null) return false
        // perform the test
        return if (aAABB.overlaps(bAABB)) {
            true
        } else false
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#getAABB(org.dyn4j.collision.Collidable)
	 */
    override fun getAABB(collidable: E): AABB {
        val size: Int = collidable.fixtureCount
        if (size == 0) return AABB(0.0, 0.0, 0.0, 0.0)
        val union: AABB = getAABB(collidable, collidable.getFixture(0))!!
        for (i in 1 until size) {
            val aabb: AABB = getAABB(collidable, collidable.getFixture(i))!!
            union.union(aabb)
        }
        return union
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#detect(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform)
	 */
    override fun detect(convex1: Convex, transform1: Transform, convex2: Convex, transform2: Transform): Boolean {
        // compute the shape's aabbs
        val a: AABB = convex1.createAABB(transform1)
        val b: AABB = convex2.createAABB(transform2)

        // if both sets of intervals overlap then we have a possible intersection
        return if (a.overlaps(b)) {
            true
        } else false
        // otherwise they definitely do not intersect
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#contains(org.dyn4j.collision.Collidable)
	 */
    override operator fun contains(collidable: E): Boolean {
        val size: Int = collidable.fixtureCount
        for (i in 0 until size) {
            val fixture: T = collidable.getFixture(i)
            if (!this.contains(collidable, fixture)) {
                return false
            }
        }
        return true
    }

    /**
     * Returns true if the ray and AABB intersect.
     *
     *
     * This method is ideally called for a number of AABBs where the invDx and invDy can
     * be computed once.
     * @param start the start position of the ray
     * @param length the length of the ray
     * @param invDx the inverse of the x component of the ray direction
     * @param invDy the inverse of the y component of the ray direction
     * @param aabb the AABB to test
     * @return true if the AABB and ray intersect
     */
    protected fun raycast(
        start: Vector2,
        length: Double,
        invDx: Double,
        invDy: Double,
        aabb: AABB
    ): Boolean {
        // see here for implementation details
        // http://tavianator.com/2011/05/fast-branchless-raybounding-box-intersections/
        val tx1: Double = (aabb.minX - start.x) * invDx
        val tx2: Double = (aabb.maxX - start.x) * invDx
        var tmin: Double = min(tx1, tx2)
        var tmax: Double = max(tx1, tx2)
        val ty1: Double = (aabb.minY - start.y) * invDy
        val ty2: Double = (aabb.maxY - start.y) * invDy
        tmin = max(tmin, min(ty1, ty2))
        tmax = min(tmax, max(ty1, ty2))
        // the ray is pointing in the opposite direction
        if (tmax < 0) return false
        // consider the ray length
        return if (tmin > length) false else tmax >= tmin
        // along the ray, tmax should be larger than tmin
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#detect()
	 */
    override fun detect(): List<BroadphasePair<E, T>> {
        return this.detect(defaultFilter)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#detect(org.dyn4j.geometry.AABB)
	 */
    override fun detect(aabb: AABB): List<BroadphaseItem<E, T>> {
        return this.detect(aabb, defaultFilter)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#raycast(org.dyn4j.geometry.Ray, double)
	 */
    override fun raycast(ray: Ray, length: Double): List<BroadphaseItem<E, T>> {
        return this.raycast(ray, length, defaultFilter)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseDetector#supportsAABBExpansion()
	 */
    override fun supportsAABBExpansion(): Boolean {
        return true
    }

}