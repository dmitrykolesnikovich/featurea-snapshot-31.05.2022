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
package org.dyn4j.dynamics.contact

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.Settings
import org.dyn4j.geometry.Shiftable

/**
 * Maintains [ContactConstraint]s between [Body]s and notifies [ContactListener]s
 * of various events related to the life-cycle of a contact.
 * @author William Bittle
 * @version 3.3.0
 * @since 3.2.0
 */
interface ContactManager : Shiftable {
    // preparation stage
    /**
     * Queues a new [ContactConstraint] to be added to this [ContactManager].
     *
     *
     * The [.updateAndNotify] method should be called after all [ContactConstraint]s
     * have been queued.
     * @param constraint the [ContactConstraint]
     */
    fun queue(constraint: ContactConstraint)
    // notification stage
    /**
     * Updates this [ContactManager] with the queued [ContactConstraint]s and notifying
     * the given [ContactListener]s of the respective events.
     *
     *
     * This method does not notify the [ContactListener.preSolve] or
     * [ContactListener.postSolve] events.
     *
     *
     * If any [ContactListener] method returns false, the contact will not continue to the
     * next stage.  In the event that all the contacts of a [ContactConstraint] do not
     * continue to the next stage, the [ContactConstraint] itself will not continue.
     * @param listeners the [ContactListener] to notify
     * @param settings the world [Settings]
     * @see ContactListener
     */
    fun updateAndNotify(listeners: List<ContactListener>?, settings: Settings)

    /**
     * Notifies the given [ContactListener]s of the pre-solve event for all
     * [ContactConstraint]s that reached this stage.
     *
     *
     * If any [ContactListener] method returns false, the contact will not continue to the
     * next stage.  In the event that all the contacts of a [ContactConstraint] do not
     * continue to the next stage, the [ContactConstraint] itself will not continue.
     * @param listeners the [ContactListener] to notify
     */
    fun preSolveNotify(listeners: List<ContactListener>?)

    /**
     * Notifies the given [ContactListener]s of the post-solve event for all
     * [ContactConstraint]s that reached this stage.
     * @param listeners the [ContactListener] to notify
     */
    fun postSolveNotify(listeners: List<ContactListener>?)
    // post-notification stage
    /**
     * Manually ends the contacts associated with the given [ContactConstraint].
     *
     *
     * This method does not call the [ContactListener.end] method for
     * the contacts in the given [ContactConstraint].
     * @param constraint the [ContactConstraint]
     * @return true if the [ContactConstraint] was found
     */
    fun end(constraint: ContactConstraint): Boolean

    /**
     * Clears the contact manager.
     */
    fun clear()

    /**
     * Returns the number of contact constraints in the queue.
     * @return int
     * @since 3.3.0
     */
    val queueCount: Int

    /**
     * Returns the number of contact constraints in the manager.
     * @return int
     * @since 3.3.0
     */
    val contactCount: Int
}