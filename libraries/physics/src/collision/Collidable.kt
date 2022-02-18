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
package org.dyn4j.collision

import org.dyn4j.DataContainer
import org.dyn4j.geometry.*

/**
 * Represents an object that can collide with other objects.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 * @param <T> the [Fixture] type
 * @see AbstractCollidable
</T> */
interface Collidable<T : Fixture> : Transformable, Shiftable, DataContainer {
    /**
     * Creates an [AABB] from this [Collidable]'s attached [Fixture]s.
     *
     *
     * If there are no fixtures attached, a degenerate AABB, (0.0, 0.0) to (0.0, 0.0), is returned.
     * @return [AABB]
     * @since 3.0.0
     */
    fun createAABB(): AABB

    /**
     * Creates an [AABB] from this [Collidable]'s attached [Fixture]s using the given
     * world space [Transform].
     *
     *
     * If there are no fixtures attached, a degenerate AABB, (0.0, 0.0) to (0.0, 0.0), is returned.
     * @param transform the world space [Transform]
     * @return [AABB]
     * @throws NullPointerException if the given transform is null
     * @since 3.2.0
     */
    fun createAABB(transform: Transform): AABB

    /**
     * Adds the given [Fixture] to this [Collidable].
     * @param fixture the [Fixture] to add
     * @return [Collidable] this collidable
     * @since 3.2.0
     * @throws NullPointerException if fixture is null
     */
    fun addFixture(fixture: T): Collidable<T>

    /**
     * Creates a [Fixture] for the given [Convex] [Shape],
     * adds it to this [Collidable], and returns it.
     * @param convex the [Convex] [Shape] to add
     * @return T the fixture created
     * @since 3.2.0
     * @throws NullPointerException if convex is null
     */
    fun addFixture(convex: Convex): T

    /**
     * Returns the [Fixture] at the given index.
     * @param index the index of the [Fixture]
     * @return T the fixture
     * @throws IndexOutOfBoundsException if index is out of bounds
     * @since 2.0.0
     */
    fun getFixture(index: Int): T

    /**
     * Returns true if this [Collidable] contains the given [Fixture].
     * @param fixture the fixture
     * @return boolean
     * @since 3.2.0
     */
    fun containsFixture(fixture: T): Boolean

    /**
     * Returns the first [Fixture] in this [Collidable], determined by the order in
     * which they were added, that contains the given point.
     *
     *
     * Returns null if the point is not contained in any fixture in this [Collidable].
     * @param point a world space point
     * @return T the fixture or null
     * @throws NullPointerException if point is null
     * @since 3.2.0
     */
    fun getFixture(point: Vector2): T?

    /**
     * Returns all the [Fixture]s in this [Collidable] that contain the given point.
     *
     *
     * Returns an empty list if the point is not contained in any fixture in this [Collidable].
     * @param point a world space point
     * @return List&lt;T&gt;
     * @throws NullPointerException if point is null
     * @since 3.2.0
     */
    fun getFixtures(point: Vector2): List<T>

    /**
     * Removes the given [Fixture] from this [Collidable].
     * @param fixture the [Fixture]
     * @return boolean true if the [Fixture] was removed from this [Collidable]
     * @since 3.2.0
     */
    fun removeFixture(fixture: T?): Boolean

    /**
     * Removes the [Fixture] at the given index.
     * @param index the index
     * @return T the fixture removed
     * @throws IndexOutOfBoundsException if index is out of bounds
     * @since 3.2.0
     */
    fun removeFixture(index: Int): T

    /**
     * Removes all fixtures from this [Collidable] and returns them.
     * @return List&lt;T&gt;
     * @since 3.2.0
     */
    fun removeAllFixtures(): List<T>

    /**
     * Removes the first [Fixture] in this [Collidable], determined by the order in
     * which they were added, that contains the given point and returns it.
     *
     *
     * Returns null if the point is not contained in any [Fixture] in this [Collidable].
     * @param point a world space point
     * @return T the fixture or null
     * @throws NullPointerException if point is null
     * @since 3.2.0
     */
    fun removeFixture(point: Vector2): T?

    /**
     * Removes all the [Fixture]s in this [Collidable] that contain the given point and
     * returns them.
     *
     *
     * Returns an empty list if the point is not contained in any [Fixture] in this [Collidable].
     * @param point a world space point
     * @return List&lt;T&gt;
     * @throws NullPointerException if point is null
     * @since 3.2.0
     */
    fun removeFixtures(point: Vector2): List<T>

    /**
     * Returns the number of [Fixture]s attached
     * to this [Collidable] object.
     * @return int
     * @since 2.0.0
     */
    val fixtureCount: Int

    /**
     * Returns an unmodifiable list containing the [Fixture]s attached to this [Collidable].
     *
     *
     * The returned list is backed by the internal list, therefore adding or removing fixtures while
     * iterating through the returned list is not permitted.  Use the [.getFixtureIterator]
     * method instead.
     * @return List&lt;T&gt;
     * @since 3.1.5
     * @see .getFixtureIterator
     */
    val fixtures: List<T>

    /**
     * Returns an iterator for this collidable's fixtures.
     *
     *
     * The returned iterator supports the `remove` method.
     * @return Iterator&lt;T&gt;
     * @since 3.2.0
     */
    val fixtureIterator: Iterator<T>

    /**
     * Returns true if the given world space point is contained in this [Collidable].
     *
     *
     * The point is contained in this [Collidable] if and only if the point is contained
     * in one of this [Collidable]'s [Fixture]s.
     * @param point the world space test point
     * @return boolean
     * @throws NullPointerException if point is null
     * @since 3.2.0
     */
    operator fun contains(point: Vector2): Boolean

    /**
     * Returns the center for this [Collidable] in local coordinates.
     * @return [Vector2] the center in local coordinates
     * @since 3.2.0
     */
    val localCenter: Vector2

    /**
     * Returns the center for this [Collidable] in world coordinates.
     * @return [Vector2] the center in world coordinates
     * @since 3.2.0
     */
    val worldCenter: Vector2

    /**
     * Returns a new point in local coordinates of this [Collidable] given
     * a point in world coordinates.
     * @param worldPoint a world space point
     * @return [Vector2] local space point
     * @throws NullPointerException if the given point is null
     * @since 3.2.0
     */
    fun getLocalPoint(worldPoint: Vector2): Vector2

    /**
     * Returns a new point in world coordinates given a point in the
     * local coordinates of this [Collidable].
     * @param localPoint a point in the local coordinates of this [Collidable]
     * @return [Vector2] world space point
     * @throws NullPointerException if the given point is null
     * @since 3.2.0
     */
    fun getWorldPoint(localPoint: Vector2): Vector2

    /**
     * Returns a new vector in local coordinates of this [Collidable] given
     * a vector in world coordinates.
     * @param worldVector a world space vector
     * @return [Vector2] local space vector
     * @throws NullPointerException if the given vector is null
     * @since 3.2.0
     */
    fun getLocalVector(worldVector: Vector2): Vector2

    /**
     * Returns a new vector in world coordinates given a vector in the
     * local coordinates of this [Collidable].
     * @param localVector a vector in the local coordinates of this [Collidable]
     * @return [Vector2] world space vector
     * @throws NullPointerException if the given vector is null
     * @since 3.2.0
     */
    fun getWorldVector(localVector: Vector2): Vector2

    /**
     * Returns the maximum radius of the disk that the
     * [Collidable] creates if rotated 360 degrees about its center.
     * @return double the maximum radius of the rotation disk
     */
    val rotationDiscRadius: Double

    /**
     * Returns the local to world space [Transform] of this [Collidable].
     * @return [Transform]
     */
    /**
     * Sets this [Collidable]'s local to world space [Transform].
     *
     *
     * If the given transform is null, this method returns immediately.
     * @param transform the transform
     * @since 3.2.0
     */
    var transform: Transform

    /**
     * Rotates the [Collidable] about its center.
     * @param theta the angle of rotation in radians
     */
    fun rotateAboutCenter(theta: Double)

    /**
     * Translates the center of the [Collidable] to the world space origin (0,0).
     *
     *
     * This method is useful if bodies have a number of fixtures and the center
     * is not at the origin.  This method will reposition this [Collidable] so
     * that the center is at the origin.
     * @since 3.2.0
     */
    fun translateToOrigin()

    companion object {
        /** Number of fixtures typically attached to a [Collidable]  */
        const val TYPICAL_FIXTURE_COUNT = 1
    }
}