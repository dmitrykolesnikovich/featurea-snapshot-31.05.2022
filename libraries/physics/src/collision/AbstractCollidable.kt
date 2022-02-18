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
 * A base implementation of the [Collidable] interface.
 * @author William Bittle
 * @version 3.4.1
 * @since 3.2.0
 * @param <T> the [Fixture] type
</T> */
abstract class AbstractCollidable<T : Fixture> : Collidable<T>, Transformable, DataContainer {

    /** The current [Transform]  */
    override lateinit var transform: Transform

    /** The [Fixture] list  */
    override lateinit var fixtures: MutableList<T>

    /** The the rotation disk radius  */
    var radius = 0.0

    /** The user data associated to this [Collidable]  */
    override var userData: Any? = null

    /**
     * Default constructor.
     */
    constructor() : this(Collidable.TYPICAL_FIXTURE_COUNT)

    /**
     * Optional constructor.
     *
     *
     * Creates a new [AbstractCollidable] using the given estimated fixture count.
     * Assignment of the initial fixture count allows sizing of internal structures
     * for optimal memory/performance.  This estimated fixture count is **not** a
     * limit on the number of fixtures.
     * @param fixtureCount the estimated number of fixtures
     */
    constructor(fixtureCount: Int) {
        val size = if (fixtureCount <= 0) Collidable.TYPICAL_FIXTURE_COUNT else fixtureCount
        fixtures = ArrayList(size)
        radius = 0.0
        transform = Transform()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#removeFixture(org.dyn4j.collision.Fixture)
	 */
    override fun removeFixture(fixture: T?): Boolean {
        // because the fixture list contains no nulls, this handles the case fixture == null as well
        return fixtures.remove(fixture)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#removeFixture(int)
	 */
    override fun removeFixture(index: Int): T {
        return fixtures.removeAt(index)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#removeAllFixtures()
	 */
    override fun removeAllFixtures(): List<T> {
        // return the current list
        val fixtures: List<T>? = fixtures
        // create a new list to replace the current list
        this.fixtures = ArrayList(fixtures!!.size)
        // return the current list
        return fixtures
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#containsFixture(org.dyn4j.collision.Fixture)
	 */
    override fun containsFixture(fixture: T): Boolean {
        // because the fixture list contains no nulls, this handles the case fixture == null as well
        return fixtures!!.contains(fixture)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(double, double, double)
	 */
    override fun rotate(theta: Double, x: Double, y: Double) {
        transform!!.rotate(theta, x, y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(org.dyn4j.geometry.Rotation, double, double)
	 */
    override fun rotate(rotation: Rotation, x: Double, y: Double) {
        transform!!.rotate(rotation!!, x, y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(double, org.dyn4j.geometry.Vector)
	 */
    override fun rotate(theta: Double, point: Vector2) {
        transform!!.rotate(theta, point!!)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(org.dyn4j.geometry.Rotation, org.dyn4j.geometry.Vector)
	 */
    override fun rotate(rotation: Rotation, point: Vector2) {
        transform!!.rotate(rotation!!, point!!)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(double)
	 */
    override fun rotate(theta: Double) {
        transform!!.rotate(theta)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(org.dyn4j.geometry.Rotation)
	 */
    override fun rotate(rotation: Rotation) {
        transform!!.rotate(rotation!!)
    }

    /**
     * Rotates the [Collidable] about its center of mass.
     * @param theta the angle of rotation in radians
     */
    override fun rotateAboutCenter(theta: Double) {
        val center = worldCenter
        this.rotate(theta, center)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#translate(double, double)
	 */
    override fun translate(x: Double, y: Double) {
        transform!!.translate(x, y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#translate(org.dyn4j.geometry.Vector)
	 */
    override fun translate(vector: Vector2) {
        transform!!.translate(vector!!)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#translateToOrigin()
	 */
    override fun translateToOrigin() {
        // get the world space center of mass
        val wc = worldCenter
        // translate the collidable negative that much to put it at the origin
        transform!!.translate(-wc.x, -wc.y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Shiftable#shift(org.dyn4j.geometry.Vector2)
	 */
    override fun shift(shift: Vector2) {
        transform!!.translate(shift!!)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#getFixture(int)
	 */
    override fun getFixture(index: Int): T {
        return fixtures!![index]
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#getFixture(org.dyn4j.geometry.Vector2)
	 */
    override fun getFixture(point: Vector2): T? {
        val size = fixtures!!.size
        for (i in 0 until size) {
            val fixture = fixtures!![i]
            val convex = fixture.shape
            if (convex.contains(point, transform)) {
                return fixture
            }
        }
        return null
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#getFixtures(org.dyn4j.geometry.Vector2)
	 */
    override fun getFixtures(point: Vector2): List<T> {
        val fixtures: MutableList<T> = ArrayList()
        val size = this.fixtures!!.size
        for (i in 0 until size) {
            val fixture = this.fixtures!![i]
            val convex = fixture.shape
            if (convex.contains(point, transform)) {
                fixtures.add(fixture)
            }
        }
        return fixtures
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#removeFixture(org.dyn4j.geometry.Vector2)
	 */
    override fun removeFixture(point: Vector2): T? {
        val size = fixtures!!.size
        for (i in 0 until size) {
            val fixture = fixtures!![i]
            val convex = fixture.shape
            if (convex.contains(point, transform)) {
                fixtures!!.removeAt(i)
                return fixture
            }
        }
        return null
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#removeFixtures(org.dyn4j.geometry.Vector2)
	 */
    override fun removeFixtures(point: Vector2): List<T> {
        val fixtures: MutableList<T> = ArrayList()
        val it = this.fixtures!!.iterator()
        while (it.hasNext()) {
            val fixture = it.next()
            val convex = fixture.shape
            if (convex.contains(point, transform)) {
                it.remove()
                fixtures.add(fixture)
            }
        }
        return fixtures
    }

    override val fixtureCount: Int
        get() = fixtures!!.size

    override val fixtureIterator: Iterator<T>
        get() = FixtureIterator(this)


    override val rotationDiscRadius: Double
        get() = radius

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#createAABB()
	 */
    override fun createAABB(): AABB {
        return this.createAABB(transform)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#createAABB(org.dyn4j.geometry.Transform)
	 */
    override fun createAABB(transform: Transform): AABB {
        // get the number of fixtures
        val size = fixtures!!.size
        // make sure there is at least one
        if (size > 0) {
            // create the aabb for the first fixture
            val aabb = fixtures!![0].shape.createAABB(transform)
            // loop over the remaining fixtures, unioning the aabbs
            for (i in 1 until size) {
                // create the aabb for the current fixture
                val faabb = fixtures!![i].shape.createAABB(transform)
                // union the aabbs
                aabb.union(faabb)
            }
            // return the aabb
            return aabb
        }
        return AABB(0.0, 0.0, 0.0, 0.0)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#getLocalPoint(org.dyn4j.geometry.Vector2)
	 */
    override fun getLocalPoint(worldPoint: Vector2): Vector2 {
        return transform!!.getInverseTransformed(worldPoint!!)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#getWorldPoint(org.dyn4j.geometry.Vector2)
	 */
    override fun getWorldPoint(localPoint: Vector2): Vector2 {
        return transform!!.getTransformed(localPoint!!)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#getLocalVector(org.dyn4j.geometry.Vector2)
	 */
    override fun getLocalVector(worldVector: Vector2): Vector2 {
        return transform!!.getInverseTransformedR(worldVector!!)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#getWorldVector(org.dyn4j.geometry.Vector2)
	 */
    override fun getWorldVector(localVector: Vector2): Vector2 {
        return transform!!.getTransformedR(localVector!!)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Collidable#contains(org.dyn4j.geometry.Vector2)
	 */
    override fun contains(point: Vector2): Boolean {
        val size = fixtures!!.size
        for (i in 0 until size) {
            val fixture = fixtures!![i]
            val convex = fixture.shape
            if (convex.contains(point, transform)) {
                return true
            }
        }
        return false
    }

}