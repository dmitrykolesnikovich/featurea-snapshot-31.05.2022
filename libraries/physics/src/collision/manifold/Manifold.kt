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

import org.dyn4j.collision.narrowphase.Penetration
import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Shape
import org.dyn4j.geometry.Vector2

/**
 * Represents a contact [Manifold] for a collision between two [Convex] [Shape]s.
 *
 *
 * A [Manifold] has a list of [ManifoldPoint]s for a given [Penetration] normal. In
 * two dimensions there will only be 1 or 2 contact points.
 *
 *
 * All [ManifoldPoint]s are in world space coordinates.
 * @author William Bittle
 * @version 3.0.2
 * @since 1.0.0
 */
class Manifold {

    /** The [ManifoldPoint] in world space  */
    var points: MutableList<ManifoldPoint>

    /** The penetration normal  */
    var normal: Vector2? = null

    /**
     * Default constructor.
     */
    constructor() {
        points = ArrayList(2)
    }

    /**
     * Full constructor.
     * @param points the manifold points
     * @param normal the manifold normal
     */
    constructor(points: MutableList<ManifoldPoint>, normal: Vector2?) {
        this.points = points
        this.normal = normal
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Manifold[Normal=").append(normal)
        sb.append("|Points={")
        val size = points.size
        for (i in 0 until size) {
            if (i != 0) sb.append(",")
            sb.append(points[i])
        }
        sb.append("}]")
        return sb.toString()
    }

    /**
     * Clears the [Manifold] information.
     */
    fun clear() {
        points.clear()
        normal = null
    }

}