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

import org.dyn4j.Listener
import org.dyn4j.geometry.AABB
import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Transform

/**
 * Convenience class for implementing the [DetectListener] interface.
 *
 *
 * This class can be used to implement only the methods desired instead of all
 * the methods contained in the [DetectListener] interface.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.1.9
 */
class DetectAdapter : DetectListener, Listener {
    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.DetectListener#allow(org.dyn4j.geometry.AABB, org.dyn4j.dynamics.Body, org.dyn4j.dynamics.BodyFixture)
	 */
    override fun allow(aabb: AABB?, body: Body?, fixture: BodyFixture?): Boolean {
        return false
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.DetectListener#allow(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.dynamics.Body)
	 */
    override fun allow(convex: Convex?, transform: Transform?, body: Body?): Boolean {
        return true
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.dynamics.DetectListener#allow(org.dyn4j.geometry.Convex, org.dyn4j.geometry.Transform, org.dyn4j.dynamics.Body, org.dyn4j.dynamics.BodyFixture)
	 */
    override fun allow(convex: Convex?, transform: Transform?, body: Body?, fixture: BodyFixture?): Boolean {
        return true
    }
}