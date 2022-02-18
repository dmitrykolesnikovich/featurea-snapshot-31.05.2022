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
import kotlin.jvm.JvmField

/**
 * Represents the estimated number of objects of different types.
 *
 *
 * This class is used to initially size internal structures to improve performance.
 * These same structures will grow larger than the given sizes if necessary.
 * @author William Bittle
 * @version 3.2.0
 * @since 3.2.0
 */
class Capacity constructor(
    bodyCount: Int =
        DEFAULT_BODY_COUNT, jointCount: Int =
        DEFAULT_JOINT_COUNT, listenerCount: Int =
        DEFAULT_LISTENER_COUNT
) {
    // counts
    /**
     * Returns the estimated number of bodies.
     * @return int
     */
    /** The estimated [Body] count  */
    val bodyCount: Int

    /**
     * Returns the estimated number of joints.
     * @return int
     */
    /** The estimated [Joint] count  */
    val jointCount: Int

    /**
     * Returns the estimated number of listeners.
     * @return int
     */
    /** The estimated [Listener] (all listener types) count  */
    val listenerCount: Int

    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj === this) return true
        if (obj is Capacity) {
            val capacity = obj
            return capacity.bodyCount == bodyCount && capacity.jointCount == jointCount && capacity.listenerCount == listenerCount
        }
        return false
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    override fun hashCode(): Int {
        var hash = 17
        hash = hash * 31 + bodyCount
        hash = hash * 31 + jointCount
        hash = hash * 31 + listenerCount
        return hash
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Capacity[BodyCount=").append(bodyCount)
            .append("|JointCount=").append(jointCount)
            .append("|ListenerCount=").append(listenerCount)
            .append("]")
        return sb.toString()
    }

    companion object {
        /** The default [Body] count  */
        const val DEFAULT_BODY_COUNT = 32

        /** The default [Joint] count  */
        const val DEFAULT_JOINT_COUNT = 16

        /** The default [Listener] count  */
        const val DEFAULT_LISTENER_COUNT = 16

        /** The default capacity  */
        @JvmField
        val DEFAULT_CAPACITY = Capacity()
    }
    /**
     * Full constructor.
     * @param bodyCount the estimated number of bodies
     * @param jointCount the estimated number of joints
     * @param listenerCount the estimated number of listeners
     * @throws IllegalArgumentException if any count is less than zero
     */
    /**
     * Default constructor.
     *
     *
     * Creates a default capacity with the default counts.
     */
    init {
        this.bodyCount = if (bodyCount > 0) bodyCount else DEFAULT_BODY_COUNT
        this.jointCount = if (jointCount > 0) jointCount else DEFAULT_JOINT_COUNT
        this.listenerCount =
            if (listenerCount > 0) listenerCount else DEFAULT_LISTENER_COUNT
    }
}