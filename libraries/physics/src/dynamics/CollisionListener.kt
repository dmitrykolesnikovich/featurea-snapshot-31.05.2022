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
import org.dyn4j.collision.manifold.Manifold
import org.dyn4j.collision.narrowphase.Penetration
import org.dyn4j.dynamics.contact.ContactConstraint
import org.dyn4j.geometry.AABB
import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Shape

/**
 * Interface to listen for collision events.
 *
 *
 * Events for a pair of bodies (as long as they pass the criteria for the event to be called)
 * will be called in the following order:
 *
 *  1. Collision detected by the broadphase: [.collision]
 *  1. Collision detected by the narrowphase: [.collision]
 *  1. Contact manifold created by the manifold solver:[.collision]
 *  1. Contact constraint created: [.collision]
 *
 * Returning false from any of the listener methods will halt processing of that event.  Other
 * [CollisionListener]s will still be notified of that event, but subsequent events will
 * not occur (this indicates that you didn't want the collision to be resolved later).
 *
 *
 * Modification of the [World] is permitted in these methods.  Modification of the [Body]'s
 * fixtures is not permitted (adding/removing will cause a runtime exception).
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 */
interface CollisionListener : Listener {
    /**
     * Called when two [BodyFixture]s are colliding as determined by the [BroadphaseDetector].
     * *
     *
     *
     * [Body] objects can have many [Convex] [Shape]s that make up their geometry.  Because
     * of this, this method may be called multiple times if two multi-fixtured [Body]s are colliding.
     *
     *
     * This method is called when the two [BodyFixture]'s expanded [AABB]s are overlapping.
     * Visually the bodies may not appear to be colliding (which is a valid case).  If you need to
     * make sure the [Body]s are colliding, and not just their AABBs, use the
     * [.collision] method.
     *
     *
     * Return false from this method to stop processing of this collision.  Other
     * [CollisionListener]s will still be notified of this event, however, no further
     * collision or contact events will occur for this pair.
     *
     *
     * The [.collision] method is next
     * in the sequence of collision events.
     * @param body1 the first [Body]
     * @param fixture1 the first [Body]'s [BodyFixture]
     * @param body2 the second [Body]
     * @param fixture2 the second [Body]'s [BodyFixture]
     * @return boolean true if processing should continue for this collision
     * @since 3.2.0
     */
    fun collision(body1: Body?, fixture1: BodyFixture?, body2: Body?, fixture2: BodyFixture?): Boolean

    /**
     * Called when two [BodyFixture]s are colliding as determined by the [NarrowphaseDetector].
     *
     *
     * [Body] objects can have many [Convex] [Shape]s that make up their geometry.  Because
     * of this, this method may be called multiple times if two multi-fixtured [Body]s are colliding.
     *
     *
     * Modification of the [Penetration] object is allowed.  The [Penetration] object passed
     * will be used to generate the contact manifold in the [ManifoldSolver].
     *
     *
     * Return false from this method to stop processing of this collision.  Other
     * [CollisionListener]s will still be notified of this event, however, no further
     * collision or contact events will occur for this pair.
     *
     *
     * The [.collision] method is next
     * in the sequence of collision events.
     * @param body1 the first [Body]
     * @param fixture1 the first [Body]'s [BodyFixture]
     * @param body2 the second [Body]
     * @param fixture2 the second [Body]'s [BodyFixture]
     * @param penetration the [Penetration] between the [Shape]s
     * @return boolean true if processing should continue for this collision
     */
    fun collision(
        body1: Body?,
        fixture1: BodyFixture?,
        body2: Body?,
        fixture2: BodyFixture?,
        penetration: Penetration?
    ): Boolean

    /**
     * Called when two [BodyFixture]s are colliding and a contact [Manifold] has been found.
     *
     *
     * [Body] objects can have many [Convex] [Shape]s that make up their geometry.  Because
     * of this, this method may be called multiple times if two multi-fixtured [Body]s are colliding.
     *
     *
     * Modification of the [Manifold] object is allowed.  The [Manifold] is used to create contact
     * constraints.
     *
     *
     * Return false from this method to stop processing of this collision.  Other
     * [CollisionListener]s will still be notified of this event, however, no further
     * collision or contact events will occur for this pair.
     *
     *
     * The [.collision] method is next in the sequence of collision events.
     * @param body1 the first [Body]
     * @param fixture1 the first [Body]'s [BodyFixture]
     * @param body2 the second [Body]
     * @param fixture2 the second [Body]'s [BodyFixture]
     * @param manifold the contact [Manifold] for the collision
     * @return boolean true if processing should continue for this collision
     */
    fun collision(
        body1: Body?,
        fixture1: BodyFixture?,
        body2: Body?,
        fixture2: BodyFixture?,
        manifold: Manifold?
    ): Boolean

    /**
     * Called after a [ContactConstraint] has been created for a collision.
     *
     *
     * [Body] objects can have many [Convex] [Shape]s that make up their geometry.  Because
     * of this, this method may be called multiple times if two multi-fixtured [Body]s are colliding.
     *
     *
     * Modification of the friction and restitution (both computed using the [CoefficientMixer]
     * and sensor fields is allowed.
     *
     *
     * Setting the tangent velocity of the [ContactConstraint] can create a conveyor effect.
     *
     *
     * Return false from this method to stop processing of this collision.  Other
     * [CollisionListener]s will still be notified of this event, however, no further
     * collision or contact events will occur for this pair.
     *
     *
     * This is the last collision event before contact processing (via [ContactListener]s) occur.
     * @param contactConstraint the contact constraint
     * @return boolean true if processing should continue for this collision
     * @since 3.0.2
     */
    fun collision(contactConstraint: ContactConstraint?): Boolean
}