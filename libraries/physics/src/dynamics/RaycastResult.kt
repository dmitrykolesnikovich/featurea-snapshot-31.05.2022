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

import org.dyn4j.collision.narrowphase.Raycast
import kotlin.math.sign

/**
 * Represents the result of a raycast.
 *
 *
 * Implements the Comparable interface to allow for sorting by the distance.
 *
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 * @author William Bittle
 * @version 3.2.0
 * @since 2.0.0
 */
class RaycastResult : Comparable<RaycastResult> {

    /** The [Body] detected  */
    var body: Body? = null

    /** The [BodyFixture] of the [Body] detected  */
    var fixture: BodyFixture? = null

    /**
     * Returns the [Raycast] result information.
     * @return [Raycast]
     */
    /**
     * Sets the [Raycast] result information.
     * @param raycast the [Raycast]
     */
    /** The [Raycast] result information  */
    var raycast: Raycast? = null

    /**
     * Default constructor.
     */
    constructor() {}

    /**
     * Full constructor.
     * @param body the body
     * @param fixture the fixture
     * @param raycast the raycast
     */
    constructor(body: Body?, fixture: BodyFixture?, raycast: Raycast?) {
        this.body = body
        this.fixture = fixture
        this.raycast = raycast
    }

    /* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
    override operator fun compareTo(o: RaycastResult): Int {
        return (raycast!!.distance - o.raycast!!.distance).sign.toInt()
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("RaycastResult[Body=").append(body)
            .append("|Fixture=").append(fixture)
            .append("|Raycast=").append(raycast)
            .append("]")
        return sb.toString()
    }

}