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

import org.dyn4j.collision.narrowphase.Penetration

/**
 * Represents the result of a static detection of the world.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.1.9
 */
class DetectResult {
    /** The overlapping [Body]  */
    var body: Body? = null

    /** The overlapping [BodyFixture]  */
    var fixture: BodyFixture? = null

    /**
     * Returns the overlap penetration (collision data).
     *
     *
     * This will return null if the collision data was flagged to not be included.
     * @return [Penetration]
     */
    /**
     * Sets the overlap penetration (collision data).
     * @param penetration the [Penetration]; can be null
     */
    /** The overlap [Penetration]; may be null  */
    var penetration: Penetration? = null

    /**
     * Default constructor.
     */
    constructor() {}

    /**
     * Optional constructor.
     * @param body the body
     * @param fixture the fixture
     */
    constructor(body: Body?, fixture: BodyFixture?) : this(body, fixture, null) {}

    /**
     * Full constructor.
     * @param body the body
     * @param fixture the fixture
     * @param penetration the penetration; can be null
     */
    constructor(body: Body?, fixture: BodyFixture?, penetration: Penetration?) {
        this.body = body
        this.fixture = fixture
        this.penetration = penetration
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("DetectResult[Body=").append(body.hashCode())
            .append("|Fixture=").append(fixture.hashCode())
            .append("|Penetration=").append(penetration)
            .append("]")
        return sb.toString()
    }

}
