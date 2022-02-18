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
package org.dyn4j.collision.narrowphase

import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Link
import org.dyn4j.geometry.Transform


/**
 * A [NarrowphasePostProcessor] specifically for the [Link] class to solve the
 * internal edge problem when using a chain of segments.
 * @author Willima Bittle
 * @version 3.2.2
 * @since 3.2.2
 * @see [Slides 46-54](https://bullet.googlecode.com/files/GDC10_Coumans_Erwin_Contact.pdf)
 */
class LinkPostProcessor : NarrowphasePostProcessor {

    override fun process(
        convex1: Convex,
        transform1: Transform,
        convex2: Convex,
        transform2: Transform,
        penetration: Penetration
    ) {
        if (convex1 is Link) {
            process(convex1, transform1, convex2, transform2, penetration)
        } else if (convex2 is Link) {
            // for this case we convert the parameters to match the order specified
            // by the other method and negate the incoming and outgoing normal to match
            penetration.normal!!.negate()
            process(convex2, transform2, convex1, transform1, penetration)
            penetration.normal!!.negate()
        }
    }


}