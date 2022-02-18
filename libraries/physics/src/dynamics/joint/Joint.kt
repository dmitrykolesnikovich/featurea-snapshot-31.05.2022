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
package org.dyn4j.dynamics.joint

import org.dyn4j.DataContainer
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.Constraint
import org.dyn4j.dynamics.Settings
import org.dyn4j.dynamics.Step
import org.dyn4j.geometry.Shiftable
import org.dyn4j.geometry.Vector2

/**
 * Represents constrained motion between two [Body]s.
 * @author William Bittle
 * @version 3.4.1
 * @since 1.0.0
 */
abstract class Joint(body1: Body, body2: Body) :
    Constraint(body1, body2), Shiftable, DataContainer {

    open var isCollisionAllowed: Boolean = false
        set(value) {
            // is it different than the current value
            if (field != value) {
                // wake up both bodies
                this.body1.setAsleep(false)
                this.body2.setAsleep(false)
                // set the new value
                field = value
            }
        }

    /* (non-Javadoc)
	 * @see org.dyn4j.DataContainer#getUserData()
	 *//* (non-Javadoc)
	 * @see org.dyn4j.DataContainer#setUserData(java.lang.Object)
	 */
    /** The user data  */
    override var userData: Any? = null

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("HashCode=").append(this.hashCode())
        // body1, body2, island
        sb.append("|").append(super.toString())
            .append("|IsCollisionAllowed=").append(isCollisionAllowed)
        return sb.toString()
    }

    /**
     * Performs any initialization of the velocity and position constraints.
     * @param step the time step information
     * @param settings the current world settings
     */
    abstract fun initializeConstraints(step: Step, settings: Settings)

    /**
     * Solves the velocity constraints.
     * @param step the time step information
     * @param settings the current world settings
     */
    abstract fun solveVelocityConstraints(step: Step, settings: Settings)

    /**
     * Solves the position constraints.
     * @param step the time step information
     * @param settings the current world settings
     * @return boolean true if the position constraints were solved
     */
    abstract fun solvePositionConstraints(step: Step, settings: Settings): Boolean

    /**
     * Returns the anchor point on the first [Body] in
     * world coordinates.
     * @return [Vector2]
     */
    abstract val anchor1: Vector2?

    /**
     * Returns the anchor point on the second [Body] in
     * world coordinates.
     * @return [Vector2]
     */
    abstract val anchor2: Vector2?

    /**
     * Returns the force applied to the [Body]s in order
     * to satisfy the constraint in newtons.
     * @param invdt the inverse delta time
     * @return [Vector2]
     */
    abstract fun getReactionForce(invdt: Double): Vector2?

    /**
     * Returns the torque applied to the [Body]s in order
     * to satisfy the constraint in newton-meters.
     * @param invdt the inverse delta time
     * @return double
     */
    abstract fun getReactionTorque(invdt: Double): Double

    /**
     * Returns true if this [Joint] is active.
     *
     *
     * A joint is only active if both joined [Body]s are active.
     * @return boolean
     */
    val isActive: Boolean
        get() = this.body1.isActive() && this.body2.isActive()
}