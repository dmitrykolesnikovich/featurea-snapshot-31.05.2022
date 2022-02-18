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

import org.dyn4j.collision.broadphase.BroadphaseFilter
import org.dyn4j.collision.broadphase.DefaultBroadphaseFilter

/**
 * Represents a [BroadphaseFilter] for the [BroadphaseDetector.detect] method.
 *
 *
 * This filter extends the [DefaultBroadphaseFilter] class and adds filtering for the additional information
 * in the [Body] class.
 *
 *
 * Extend this class to add additional filtering capabilities to the broad-phase.
 * @author William Bittle
 * @version 3.2.4
 * @since 3.2.0
 */
class DetectBroadphaseFilter : DefaultBroadphaseFilter<Body, BodyFixture>(), BroadphaseFilter<Body, BodyFixture> {
    /* (non-Javadoc)
	 * @see org.dyn4j.collision.broadphase.BroadphaseFilter#isAllowed(org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture, org.dyn4j.collision.Collidable, org.dyn4j.collision.Fixture)
	 */
    override fun isAllowed(body1: Body, fixture1: BodyFixture, body2: Body, fixture2: BodyFixture): Boolean {
        // inactive objects don't have collision detection/response
        if (!body1.isActive() || !body2.isActive()) return false
        // one body must be dynamic (unless one is a sensor)
        if (!body1.isDynamic() && !body2.isDynamic() && !fixture1.isSensor && !fixture2.isSensor) return false
        // check for connected pairs who's collision is not allowed
        return if (body1.isConnected(body2, false)) false else super.isAllowed(body1, fixture1, body2, fixture2)
    }

}
