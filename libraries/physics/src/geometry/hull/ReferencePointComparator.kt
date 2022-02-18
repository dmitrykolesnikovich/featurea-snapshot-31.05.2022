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
package org.dyn4j.geometry.hull

import org.dyn4j.geometry.RobustGeometry.getLocation
import org.dyn4j.geometry.Vector2
import kotlin.math.sign

/**
 * Comparator class to compare points by their angle from the positive
 * x-axis with reference from a given point.
 * @author William Bittle
 * @version 3.4.0
 * @since 2.2.0
 */
internal class ReferencePointComparator(val reference: Vector2) : Comparator<Vector2> {

    /* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
    override fun compare(p1: Vector2, p2: Vector2): Int {
        // we can use the getLocation method to successfully sort by angle to the reference point
        // This is also must faster than using atan2 to compute the angles of the points.
        // The order of parameters here must match the one in GrahamScan
        // in order to obtain correct results and winding
        val sign = getLocation(p2, p1, reference).sign.toInt()
        return if (sign == 0) {
            // If the point are colinear we *must* choose the one that is more close to the reference point
            reference.distanceSquared(p1).compareTo(reference.distanceSquared(p2))
        } else sign
    }

}