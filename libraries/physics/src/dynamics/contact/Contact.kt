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

import org.dyn4j.collision.manifold.ManifoldPointId
import org.dyn4j.geometry.Vector2


/**
 * Represents a contact point between two {@link Body} objects.
 * @author William Bittle
 * @version 3.2.0
 * @since 1.0.0
 */
class Contact {
    /** The manifold point id for warm starting  */
    lateinit var id: ManifoldPointId

    /** The contact point in world space  */
    lateinit var p: Vector2

    /** The contact penetration depth  */
    var depth = 0.0

    /** The contact point in [Body]1 space  */
    var p1: Vector2? = null

    /** The contact point in [Body]2 space  */
    var p2: Vector2? = null

    /** The [Vector2] from the center of [Body]1 to the contact point  */
    var r1: Vector2? = null

    /** The [Vector2] from the center of [Body]2 to the contact point  */
    var r2: Vector2? = null

    /** The accumulated normal impulse  */
    var jn = 0.0

    /** The accumulated tangent impulse  */
    var jt = 0.0

    /** The accumulated position impulse  */
    var jp = 0.0

    /** The mass normal  */
    var massN = 0.0

    /** The mass tangent  */
    var massT = 0.0

    /** The velocity bias  */
    var vb = 0.0

    /**
     * Full constructor.
     * @param id the manifold point id used for warm starting
     * @param point the world space collision point
     * @param depth the penetration depth of this point
     * @param p1 the collision point in [Body]1's local space
     * @param p2 the collision point in [Body]2's local space
     */
    constructor(id: ManifoldPointId, point: Vector2, depth: Double, p1: Vector2, p2: Vector2) {
        this.id = id
        p = point
        this.depth = depth
        this.p1 = p1
        this.p2 = p2
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Contact[Id=").append(id)
            .append("|Point=").append(p)
            .append("|Depth=").append(depth)
            .append("|NormalImpulse=").append(jn)
            .append("|TangentImpulse=").append(jt)
            .append("]")
        return sb.toString()
    }

    /**
     * Returns the world space collision point.
     * @return [Vector2] the collision point in world space
     */
    fun getPoint(): Vector2? {
        return p
    }

    /**
     * Returns the accumulated normal impulse applied at this point.
     * @return double the accumulated normal impulse
     */
    fun getNormalImpulse(): Double {
        return jn
    }

    /**
     * Returns the accumulated tangential impulse applied at this point.
     * @return double the accumulated tangential impulse
     */
    fun getTangentialImpulse(): Double {
        return jt
    }
}