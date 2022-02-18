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
package org.dyn4j.collision

import org.dyn4j.geometry.AABB
import org.dyn4j.geometry.Translatable
import org.dyn4j.geometry.Vector2
import org.dyn4j.resources.Messages
import org.dyn4j.resources.message

/**
 * Represents a bounding region that is an Axis-Aligned bounding box.
 *
 *
 * This class compares its AABB with the AABB of the given body and returns true
 * if they do not overlap.
 * @author William Bittle
 * @version 3.4.0
 * @since 3.1.1
 */
class AxisAlignedBounds(width: Double, height: Double) : AbstractBounds(), Bounds, Translatable {

    /** The local coordinates AABB  */
    val aabb: AABB

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("AxisAlignedBounds[Width=").append(aabb.width)
            .append("|Height=").append(aabb.height)
            .append("|Translation=").append(translation)
            .append("]")
        return sb.toString()
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.collision.Bounds#isOutside(org.dyn4j.collision.Collidable)
	 */
    override fun isOutside(collidable: Collidable<*>): Boolean {
        val tx: Vector2 = transform.translation
        val aabbBounds: AABB = aabb.getTranslated(tx)
        val aabbBody: AABB = collidable.createAABB()!!

        // test the projections for overlap
        return !aabbBounds.overlaps(aabbBody)
    }// return the AABB in world coordinates

    /**
     * Returns the world space Axis-Aligned bounding box for this
     * bounds object.
     * @return [AABB]
     */
    val bounds: AABB
        get() =// return the AABB in world coordinates
            aabb.getTranslated(transform.translation)

    /**
     * Returns the width of the bounds.
     * @return double
     */
    val width: Double
        get() = aabb.width

    /**
     * Returns the height of the bounds.
     * @return double
     */
    val height: Double
        get() = aabb.height

    /**
     * Minimal constructor.
     * @param width the width of the bounds; must be greater than zero
     * @param height the height of the bounds; must be greater than zero
     * @throws IllegalArgumentException if either width or height are less than or equal to zero
     */
    init {
        if (width <= 0.0 || height <= 0.0) throw IllegalArgumentException(message("collision.bounds.axisAligned.invalidArgument"))
        val w2 = width * 0.5
        val h2 = height * 0.5
        aabb = AABB(-w2, -h2, w2, h2)
    }
}