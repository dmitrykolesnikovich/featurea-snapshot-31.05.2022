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
package org.dyn4j.dynamics

import org.dyn4j.collision.Filter
import org.dyn4j.collision.broadphase.BroadphaseFilter
import org.dyn4j.collision.broadphase.BroadphaseFilterAdapter
import org.dyn4j.geometry.AABB

/**
 * Represents a [BroadphaseFilter] for the [BroadphaseDetector.detect] method.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.2.0
 */
class AABBBroadphaseFilter
/**
 * Full constructor.
 * @param ignoreInactive true to ignore inactive bodies
 * @param ignoreSensors true to ignore sensor fixtures
 * @param filter the fixture filter
 */(
    /** True to ignore inactive bodies  */
    val isIgnoreInactive: Boolean,
    /** True to ignore sensor fixtures  */
    val isIgnoreSensors: Boolean,
    /** The fixture filter  */
    val filter: Filter?
) : BroadphaseFilterAdapter<Body, BodyFixture>(), BroadphaseFilter<Body, BodyFixture> {
    /**
     * Returns true if inactive bodies are ignored.
     * @return boolean
     */

    /**
     * Returns true if sensor fixtures are ignored.
     * @return boolean
     */

    /**
     * Returns the filter used in filtering [Fixture]s.
     * @return [Filter]
     */

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.DefaultBroadphaseFilter#isAllowed(org.dyn4j.geometry.AABB, org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun isAllowed(aabb: AABB?, body: Body, fixture: BodyFixture): Boolean {
        // check for inactive
        if (isIgnoreInactive && !body.isActive()) return false
        // check for sensor
        if (isIgnoreSensors && fixture.isSensor) {
            // skip this fixture
            return false
        }
        // check against the filter
        return if (filter != null && !filter.isAllowed(fixture.filter)) {
            false
        } else super.isAllowed(
            aabb,
            body,
            fixture
        )
    }

}