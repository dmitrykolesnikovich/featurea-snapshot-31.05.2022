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
import org.dyn4j.collision.narrowphase.NarrowphaseDetector
import org.dyn4j.geometry.*

/**
 * Represents a broad-phase collision detection algorithm.
 *
 *
 * A [BroadphaseDetector] should quickly determine the pairs of [Collidable]s and
 * [Fixture]s that possibly intersect.  These algorithms are used to filter out collision
 * pairs in the interest of sending less pairs to the [NarrowphaseDetector] which is generally
 * much more expensive.
 *
 *
 * [BroadphaseDetector]s require that the collidables are updated via the [.update]
 * or [.update] methods when the collidables move, rotate, or have their fixtures
 * changed.
 *
 *
 * **
 * NOTE: Special care must be taken when removing fixtures from a collidable.  Be sure to call the
 * [.remove] method to make sure its removed from the broad-phase.
 ** *
 *
 *
 * [BroadphaseDetector]s use a expansion value to expand a collidable's AABB width and height.  The
 * [.getAABB] returns the expanded [AABB].  This expansion is used to reduce the
 * number of updates to the broad-phase.  See the [.setAABBExpansion] for more details on
 * this value.
 *
 *
 * The [.detect], [.detect], [.raycast] methods
 * use the current state of all the collidables and fixtures that have been added.  Make sure that all
 * changes have been reflected to the broad-phase using the [.update] and
 * [.update] methods before calling these.
 *
 *
 * The [.detect] and [.detect] methods do not
 * use the current state of the broad-phase.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 * @param <E> the [Collidable] type
 * @param <T> the [Fixture] type
</T></E> */
interface BroadphaseDetector<E : Collidable<T>, T : Fixture> : Shiftable {
    /**
     * Adds a new [Collidable] to the broad-phase.
     *
     *
     * This will add all the given [Collidable]'s [Fixture]s to the broad-phase.
     *
     *
     * If the colliable has no fixtures, nothing will be added to this broad-phase.
     *
     *
     * If the [Collidable]'s [Fixture]s have already been added to this broad-phase
     * they will instead be updated.
     *
     *
     * If a fixture is removed from a [Collidable], the calling code must
     * call the [.remove] method for that fixture to
     * be removed from the broad-phase.  This method makes no effort to remove
     * fixtures no longer attached to the given collidable.
     * @param collidable the [Collidable]
     * @since 3.0.0
     */
    fun add(collidable: E)

    /**
     * Adds a new [Fixture] for the given [Collidable] to
     * the broad-phase.
     * @param collidable the collidable
     * @param fixture the fixture to add
     * @since 3.2.0
     */
    fun add(collidable: E, fixture: T)

    /**
     * Removes the given [Collidable] from the broad-phase.
     *
     *
     * This method removes all the [Fixture]s attached to the
     * given [Collidable] from the broad-phase.
     *
     *
     * If a fixture is removed from a [Collidable], the calling code must
     * call the [.remove] method for that fixture to
     * be removed from the broad-phase.  This method makes no effort to remove
     * fixtures no longer attached to the given collidable.
     * @param collidable the [Collidable]
     * @since 3.0.0
     */
    fun remove(collidable: E)

    /**
     * Removes the given [Fixture] for the given [Collidable] from
     * the broad-phase and returns true if it was found.
     * @param collidable the collidable
     * @param fixture the fixture to remove
     * @return boolean true if the fixture was found and removed
     * @since 3.2.0
     */
    fun remove(collidable: E, fixture: T?): Boolean

    /**
     * Updates all the [Fixture]s on the given [Collidable].
     *
     *
     * Used when the collidable or its fixtures have moved or rotated.
     *
     *
     * This method updates all the [Fixture]s attached to the
     * given [Collidable] from the broad-phase, if they exist. If the
     * fixtures on the given collidable do not exist in the broad-phase, they are
     * added.
     *
     *
     * If a fixture is removed from a [Collidable], the calling code must
     * call the [.remove] method for that fixture to
     * be removed from the broad-phase.  This method makes no effort to remove
     * fixtures no longer attached to the given collidable.
     * @param collidable the [Collidable]
     * @since 3.2.0
     */
    fun update(collidable: E)

    /**
     * Updates the given [Collidable]'s [Fixture].
     *
     *
     * Used when a fixture on a [Collidable] has moved or rotated.
     *
     *
     * This method will add the [Fixture] if it doesn't currently exist in
     * this broad-phase.
     * @param collidable the [Collidable]
     * @param fixture the [Fixture] that has moved
     * @since 3.2.0
     */
    fun update(collidable: E, fixture: T)

    /**
     * Returns the AABB for the given [Collidable].
     *
     *
     * The AABB returned is an AABB encompasing all fixtures on the
     * given [Collidable].  When possible, AABBs from the
     * broad-phase will be used to create this.
     *
     *
     * If the collidable doesn't have any fixtures a degenerate
     * AABB is returned.
     * @param collidable the [Collidable]
     * @return [AABB]
     * @since 3.2.0
     */
    fun getAABB(collidable: E): AABB?

    /**
     * Returns the AABB for the given [Collidable] [Fixture].
     *
     *
     * If the collidable and its fixture have not been added to this
     * broad-phase, a new AABB is created and returned (but not added to
     * broad-phase).
     * @param collidable the [Collidable]
     * @param fixture the [Fixture]
     * @return [AABB]
     * @since 3.2.0
     */
    fun getAABB(collidable: E, fixture: T): AABB?

    /**
     * Returns true if all the [Fixture]s on the given [Collidable]
     * have been added to this broad-phase.
     *
     *
     * If a collidable is added without any fixtures, this method will return
     * false, since the fixtures, not the collidable, are added to the
     * broad-phase.
     * @param collidable the [Collidable]
     * @return boolean
     * @since 3.2.0
     */
    operator fun contains(collidable: E): Boolean

    /**
     * Returns true if the given [Fixture] on the given [Collidable]
     * has been added to this broadphase.
     * @param collidable the [Collidable]
     * @param fixture the [Fixture]
     * @return boolean
     * @since 3.2.0
     */
    fun contains(collidable: E, fixture: T): Boolean

    /**
     * Clears all the [Collidable] [Fixture]s from this broad-phase.
     * @since 3.0.0
     */
    fun clear()

    /**
     * Returns the number of [Fixture]s that are being managed in this broad-phase.
     * @return int
     */
    fun size(): Int

    /**
     * Performs collision detection on all [Collidable] [Fixture]s that have
     * been added to this [BroadphaseDetector] and returns the list of potential
     * pairs.
     * @return List&lt;[BroadphasePair]&gt;
     * @since 3.0.0
     */
    fun detect(): List<BroadphasePair<E, T>>

    /**
     * Performs collision detection on all [Collidable] [Fixture]s that have
     * been added to this [BroadphaseDetector] and returns the list of potential
     * pairs.
     *
     *
     * Use the `filter` parameter to further reduce the number of potential pairs.
     * @param filter the broad-phase filter
     * @return List&lt;[BroadphasePair]&gt;
     * @since 3.2.0
     * @see .detect
     */
    fun detect(filter: BroadphaseFilter<E, T>): List<BroadphasePair<E, T>>

    /**
     * Performs a broad-phase collision test using the given [AABB] and returns
     * the items that overlap.
     * @param aabb the [AABB] to test
     * @return List&lt;[BroadphaseItem]&gt;
     * @since 3.0.0
     */
    fun detect(aabb: AABB): List<BroadphaseItem<E, T>>

    /**
     * Performs a broad-phase collision test using the given [AABB] and returns
     * the items that overlap.
     *
     *
     * Use the `filter` parameter to further reduce the number of items returned.
     * @param aabb the [AABB] to test
     * @param filter the broad-phase filter
     * @return List&lt;[BroadphaseItem]&gt;
     * @since 3.2.0
     * @see .detect
     */
    fun detect(aabb: AABB, filter: BroadphaseFilter<E, T>): List<BroadphaseItem<E, T>>

    /**
     * Performs a preliminary raycast over all the collidables in the broad-phase and returns the
     * items that intersect.
     * @param ray the [Ray]
     * @param length the length of the ray; 0.0 for infinite length
     * @return List&lt;[BroadphaseItem]&gt;
     * @since 3.0.0
     */
    fun raycast(ray: Ray, length: Double): List<BroadphaseItem<E, T>>

    /**
     * Performs a preliminary raycast over all the collidables in the broad-phase and returns the
     * items that intersect.
     *
     *
     * Use the `filter` parameter to further reduce the number of items returned.
     * @param ray the [Ray]
     * @param length the length of the ray; 0.0 for infinite length
     * @param filter the broad-phase filter
     * @return List&lt;[BroadphaseItem]&gt;
     * @since 3.2.0
     * @see .raycast
     */
    fun raycast(ray: Ray, length: Double, filter: BroadphaseFilter<E, T> ): List<BroadphaseItem<E, T>>

    /**
     * Returns true if this broad-phase detector considers the given collidables to be in collision.
     * @param a the first [Collidable]
     * @param b the second [Collidable]
     * @return boolean
     */
    fun detect(a: E, b: E): Boolean

    /**
     * Returns true if this broad-phase detector considers the given [Convex] [Shape]s to be in collision.
     *
     *
     * This method does not use the expansion value.
     * @param convex1 the first [Convex] [Shape]
     * @param transform1 the first [Convex] [Shape]'s [Transform]
     * @param convex2 the second [Convex] [Shape]
     * @param transform2 the second [Convex] [Shape]'s [Transform]
     * @return boolean
     */
    fun detect(convex1: Convex, transform1: Transform, convex2: Convex, transform2: Transform): Boolean

    /**
     * Returns whether this particular [BroadphaseDetector] supports expanding AABBs.
     * @return boolean
     */
    fun supportsAABBExpansion(): Boolean

    /**
     * Returns the [AABB] expansion value used to improve performance of broad-phase updates.
     *
     *
     * If supportsAABBExpansion() returns false the value returned is unspecified and should not be taken into account.
     * @return double
     * @see .setAABBExpansion
     */
    /**
     * Sets the [AABB] expansion value used to improve performance of broad-phase updates.
     *
     *
     * Increasing this value will cause less updates to the broad-phase but will cause more pairs
     * to be sent to the narrow-phase.
     *
     *
     * Note that a broadphase implementation may ignore this value, if supportsAABBExpansion() returns false.
     * @param expansion the expansion
     */
    var expansion: Double

    companion object {
        /** The default [AABB] expansion value  */
        const val DEFAULT_AABB_EXPANSION = 0.2

        /** The default initial capacity of fixtures  */
        const val DEFAULT_INITIAL_CAPACITY = 64
    }
}