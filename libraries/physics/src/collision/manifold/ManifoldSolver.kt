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
package org.dyn4j.collision.manifold

import org.dyn4j.collision.narrowphase.NarrowphaseDetector
import org.dyn4j.collision.narrowphase.Penetration
import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Shape
import org.dyn4j.geometry.Transform

/**
 * Finds a contact [Manifold] for two given [Convex] [Shape]s that are in collision.
 *
 *
 * A contact [Manifold] is a collection of contact points for a collision. For two dimensions, this will never
 * be more than two contacts.
 *
 *
 * A [ManifoldSolver] relies on the [Penetration] object returned from a [NarrowphaseDetector] to
 * determine the contact [Manifold]. The [Manifold]s have ids to facilitate caching of contact information.
 *
 *
 * It's possible that no contact points are returned, in which case the [.getManifold]
 * method will return false.
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 * @see Manifold
 */
interface ManifoldSolver {
    /**
     * Returns true if there exists a valid contact manifold between the two [Convex] [Shape]s.
     *
     *
     * When returning true, this method fills in the [Manifold] object with the points, depth, and normal.
     *
     *
     * The given [Manifold] object will be cleared using the [Manifold.clear] method. This allows reuse of the
     * [Manifold] if desired.
     *
     *
     * The [Penetration] object will be left unchanged by this method.
     * @param penetration the [Penetration]
     * @param convex1 the first [Convex] [Shape]
     * @param transform1 the first [Shape]'s [Transform]
     * @param convex2 the second [Convex] [Shape]
     * @param transform2 the second [Shape]'s [Transform]
     * @param manifold the [Manifold] object to fill
     * @return boolean
     */
    fun getManifold(
        penetration: Penetration,
        convex1: Convex,
        transform1: Transform,
        convex2: Convex,
        transform2: Transform,
        manifold: Manifold
    ): Boolean
}