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

import kotlin.jvm.JvmField

/**
 * Class encapsulating the timestep information.
 *
 *
 * The [World] class maintains an instance of this class to perform various functions.
 *
 *
 * A time step represents the elapsed time since the last update.
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 */
class Step {

    /** The last elapsed time  */
    var dt0 = 0.0

    /** The last inverse elapsed time  */
    var invdt0 = 0.0

    /** The elapsed time  */
    var dt = 0.0

    /** The inverse elapsed time  */
    @JvmField
    var invdt = 0.0

    /** The elapsed time ratio from the last to the current  */
    var dtRatio = 0.0

    /**
     * Default constructor.
     * @param dt the initial delta time; in seconds<sup>-1</sup>
     */
    constructor(dt: Double) {
        // 1.0 / hz
        this.dt = dt
        invdt = 1.0 / dt
        dt0 = this.dt
        invdt0 = invdt
        dtRatio = 1.0
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Step[DeltaTime=").append(dt)
            .append("|InverseDeltaTime=").append(invdt)
            .append("|PreviousDeltaTime=").append(dt0)
            .append("|PreviousInverseDeltaTime=").append(invdt0)
            .append("|DeltaTimeRatio=").append(dtRatio)
            .append("]")
        return sb.toString()
    }

    /**
     * Updates the current [Step] using the new elapsed time.
     * @param dt in seconds.
     */
    fun update(dt: Double) {
        dt0 = this.dt
        invdt0 = invdt
        this.dt = dt
        invdt = 1.0 / dt
        dtRatio = invdt0 * dt
    }

    /**
     * Returns the elapsed time since the last time step in seconds.
     * @return double
     */
    val deltaTime: Double
        get() = dt

    /**
     * Returns the inverse of the elapsed time (in seconds) since the last time step.
     * @return double
     */
    val inverseDeltaTime: Double
        get() = invdt

    /**
     * Returns the ratio of the last elapsed time to the current
     * elapsed time.
     *
     *
     * This is used to cope with a variable time step.
     * @return double
     */
    val deltaTimeRatio: Double
        get() = dtRatio

    /**
     * Returns the previous frame's elapsed time in seconds.
     * @return double
     */
    val prevousDeltaTime: Double
        get() = dt0

    /**
     * Returns the previous frame's inverse elapsed time (in seconds).
     * @return double
     */
    val previousInverseDeltaTime: Double
        get() = invdt0

}