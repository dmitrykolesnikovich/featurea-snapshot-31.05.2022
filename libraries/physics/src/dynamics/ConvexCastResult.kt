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

import org.dyn4j.collision.continuous.TimeOfImpact
import kotlin.math.sign

/**
 * Represents the result of a convex cast.
 *
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.1.5
 */
class ConvexCastResult : Comparable<ConvexCastResult> {
    /** The body  */
    var body: Body? = null

    /** The body fixture with the smallest time of impact  */
    var fixture: BodyFixture? = null

    /**
     * Returns the time of impact information.
     * @return [TimeOfImpact]
     */
    /**
     * Sets the time of impact information.
     * @param timeOfImpact the time of impact
     */
    /** The time of impact information  */
    var timeOfImpact: TimeOfImpact? = null

    /**
     * Default constructor.
     */
    constructor() {}

    /**
     * Full constructor.
     * @param body the body
     * @param fixture the fixture
     * @param timeOfImpact the time of impact
     */
    constructor(body: Body?, fixture: BodyFixture?, timeOfImpact: TimeOfImpact?) {
        this.body = body
        this.fixture = fixture
        this.timeOfImpact = timeOfImpact
    }

    /* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
    override operator fun compareTo(o: ConvexCastResult): Int {
        return (timeOfImpact!!.time - o.timeOfImpact!!.time).sign.toInt()
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("ConvexCastResult[Body=").append(body)
            .append("|Fixture=").append(fixture)
            .append("|TimeOfImpact=").append(timeOfImpact)
            .append("]")
        return sb.toString()
    }

}
