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

import org.dyn4j.geometry.Vector2

/**
 * Represents a single contact point in a contact [Manifold].
 *
 *
 * The depth represents the distance along the [Manifold] normal to this
 * contact point. This can vary for every [ManifoldPoint] in a [Manifold].
 * @author William Bittle
 * @version 3.1.5
 * @since 1.0.0
 * @see Manifold
 */
class ManifoldPoint {

    /** The id for this manifold point  */
    val id: ManifoldPointId

    /** The point in world coordinates  */
    var point: Vector2? = null

    /**
     * Returns the collision depth of the manifold point.
     * @return double
     */
    /**
     * Sets the collision depth of the manifold point.
     * @param depth the depth
     * @since 3.1.5
     */
    /** The penetration depth  */
    var depth = 0.0

    /**
     * Minimal constructor.
     * @param id the id for this manifold point
     */
    constructor(id: ManifoldPointId) {
        this.id = id
    }

    /**
     * Full constructor.
     * @param id the id for this manifold point
     * @param point the manifold point in world coordinates
     * @param depth the penetration depth
     */
    constructor(id: ManifoldPointId, point: Vector2?, depth: Double) {
        this.id = id
        this.point = point
        this.depth = depth
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("ManifoldPoint[Id=").append(id)
            .append("|Point=").append(point)
            .append("|Depth=").append(depth)
            .append("]")
        return sb.toString()
    }

}