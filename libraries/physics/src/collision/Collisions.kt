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

import kotlin.math.max

/**
 * Class used to estimate collision counts and other one-off collision methods and values.
 *
 *
 * This class is transient and may get deprecated at any time.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.1.1
 */
object Collisions {// at this time it just returns the static field above
    // this could change at any time so we keep the original method
    // and make the static field private
    /**
     * Returns an estimate on the number of collisions per object.
     * @return int
     */
    /**
     * The estimated collisions per object.
     *
     *
     * Worst Case:
     * <pre>size * size(every object colliding with every object) - size(remove self collisions)</pre>
     * Which is just way too large.  Dividing by a factor is still grossly over estimated so
     * I opted to do an estimate on the test results found in the Sandbox application.
     *
     *
     * Test Results:
     * <pre>
     * +----------+--------------+-----------------+-------------------+
     * | Test     | Object Count | Collision Pairs | Collisions/Object |
     * +----------+--------------+-----------------+-------------------+
     * | Bucket   |          200 |            ~500 |               2.5 |
     * | Funnel   |          200 |            ~400 |               2.0 |
     * | Parallel |          300 |             600 |               2.0 |
     * +----------+--------------+-----------------+-------------------+</pre>
     * Therefore a good estimate could be 4 collisions per object.
     */
    const val estimatedCollisionsPerObject = 4

    /**
     * An estimate of the number of objects that will be hit when raycasting assuming uniform
     * distribution of objects.
     *
     *
     * This was computed from the Sandbox's Raycast Performance test.
     */
    private const val ESTIMATED_RAYCAST_DENSITY = 0.02

    /**
     * Returns an estimate on the number of collision pairs based on the number objects being simulated.
     * @param n the number of objects
     * @return int
     */
    fun getEstimatedCollisionPairs(n: Int): Int {
        return n * estimatedCollisionsPerObject
    }

    /**
     * Returns an estimate on the number of collisions per object.
     * @return int
     */
    fun getEstimatedCollisionsPerObject(): Int {
        // at this time it just returns the static field above
        // this could change at any time so we keep the original method
        // and make the static field private
        return estimatedCollisionsPerObject
    }

    /**
     * Returns an estimate on the number of raycast collisions given the total number
     * of objects to collide with.
     * @param n the number of objects
     * @return int
     * @since 3.2.0
     */
    fun getEstimatedRaycastCollisions(n: Int): Int {
        return max(1.0, n * ESTIMATED_RAYCAST_DENSITY).toInt()
    }
}