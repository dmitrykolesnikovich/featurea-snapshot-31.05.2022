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

import org.dyn4j.collision.continuous.TimeOfImpact
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.Settings
import org.dyn4j.geometry.Interval
import org.dyn4j.geometry.Vector2

/**
 * Represents a position solver for a pair of [Body]s who came in
 * contact during a time step but where not detected by the discrete
 * collision detectors.
 *
 *
 * This class will translate and rotate the [Body]s into a collision.
 * @author William Bittle
 * @version 3.2.0
 * @since 2.0.0
 */
class TimeOfImpactSolver {
    /**
     * Moves the given [Body]s into collision given the [TimeOfImpact]
     * information.
     * @param body1 the first [Body]
     * @param body2 the second [Body]
     * @param timeOfImpact the [TimeOfImpact]
     * @param settings the current world settings
     */
    fun solve(
        body1: Body,
        body2: Body,
        timeOfImpact: TimeOfImpact,
        settings: Settings
    ) {
        val linearTolerance = settings.getLinearTolerance()
        val maxLinearCorrection = settings.getMaximumLinearCorrection()
        val c1 = body1.worldCenter
        val c2 = body2.worldCenter
        val m1 = body1.mass
        val m2 = body2.mass
        val mass1 = m1!!.mass
        val mass2 = m2!!.mass
        val invMass1 = mass1 * m1.inverseMass
        val invI1 = mass1 * m1.inverseInertia
        val invMass2 = mass2 * m2.inverseMass
        val invI2 = mass2 * m2.inverseInertia
        val separation = timeOfImpact.separation

        // solve the constraint
        val p1w = separation!!.point1!!
        val p2w = separation.point2!!
        val r1: Vector2 = c1.to(p1w)
        val r2: Vector2 = c2.to(p2w)
        val n = separation.normal!!
        val d = separation.distance
        val C = Interval.clamp(d - linearTolerance, -maxLinearCorrection, 0.0)
        val rn1 = r1.cross(n)
        val rn2 = r2.cross(n)
        val K = invMass1 + invMass2 + invI1 * rn1 * rn1 + invI2 * rn2 * rn2
        var impulse = 0.0
        if (K > 0.0) {
            impulse = -C / K
        }
        val J = n.product(impulse)

        // translate and rotate the objects
        body1.translate(J.product(invMass1))
        body1.rotate(invI1 * r1.cross(J), c1.x, c1.y)
        body2.translate(J.product(-invMass2))
        body2.rotate(-invI2 * r2.cross(J), c2.x, c2.y)
    }
}