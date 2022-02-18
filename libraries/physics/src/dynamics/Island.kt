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

import org.dyn4j.Epsilon
import org.dyn4j.collision.Collisions.getEstimatedCollisionPairs
import org.dyn4j.dynamics.contact.ContactConstraint
import org.dyn4j.dynamics.contact.ContactConstraintSolver
import org.dyn4j.dynamics.joint.Joint
import org.dyn4j.geometry.Interval
import org.dyn4j.geometry.Vector2
import org.dyn4j.resources.Messages
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Used to solve the contact constraints and joints for a group of interconnected bodies.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 */
internal class Island constructor(initialCapacity: Capacity? = Capacity.DEFAULT_CAPACITY) {
    /** The list of [Body]s on this [Island]  */
    @JvmField
    val bodies: MutableList<Body>

    /** The list of [Joint]s on this [Island]  */
    @JvmField
    val joints: MutableList<Joint>

    /** The list of [ContactConstraint]s on this [Island]  */
    @JvmField
    val contactConstraints: MutableList<ContactConstraint>

    /**
     * Clears the island.
     */
    fun clear() {
        bodies.clear()
        joints.clear()
        contactConstraints.clear()
    }

    /**
     * Adds the given [Body] to the [Body] list.
     * @param body the [Body]
     */
    fun add(body: Body) {
        bodies.add(body)
    }

    /**
     * Adds the given [ContactConstraint] to the [ContactConstraint] list.
     * @param contactConstraint the [ContactConstraint]
     */
    fun add(contactConstraint: ContactConstraint) {
        contactConstraints.add(contactConstraint)
    }

    /**
     * Adds the given [Joint] to the [Joint] list.
     * @param joint the [Joint]
     */
    fun add(joint: Joint) {
        joints.add(joint)
    }

    /**
     * Integrates the [Body]s, solves all [ContactConstraint]s and
     * [Joint]s, and attempts to sleep motionless [Body]s.
     * @param solver the contact constraint solver
     * @param gravity the gravity vector
     * @param step the time step information
     * @param settings the current world settings
     */
    fun solve(
        solver: ContactConstraintSolver,
        gravity: Vector2,
        step: Step,
        settings: Settings
    ) {
        // the number of solver iterations
        val velocitySolverIterations = settings.getVelocityConstraintSolverIterations()
        val positionSolverIterations = settings.getPositionConstraintSolverIterations()
        // the sleep settings
        val sleepAngularVelocity = settings.getSleepAngularVelocity()
        val sleepLinearVelocitySquared = settings.sleepLinearVelocitySquared
        val sleepTime = settings.getSleepTime()
        val size = bodies.size
        val jSize = joints.size
        val dt: Double = step.dt
        var invM: Double
        var invI: Double

        // integrate the velocities
        for (i in 0 until size) {
            val body: Body = bodies[i]
            // check if the body has infinite mass and infinite inertia
            if (!body.isDynamic()) continue
            // accumulate the forces and torques
            body.accumulate(dt)
            // get the mass properties
            invM = body.mass!!.inverseMass
            invI = body.mass!!.inverseInertia
            // integrate force and torque to modify the velocity and
            // angular velocity (sympletic euler)
            // v1 = v0 + (f / m) + g) * dt
            if (invM > Epsilon.E) {
                // only perform this step if the body does not have
                // a fixed linear velocity
                body.velocity.x += (body.force!!.x * invM + gravity.x * body.gravityScale) * dt
                body.velocity.y += (body.force!!.y * invM + gravity.y * body.gravityScale) * dt
            }
            // av1 = av0 + (t / I) * dt
            if (invI > Epsilon.E) {
                // only perform this step if the body does not have
                // a fixed angular velocity
                body.angularVelocity += dt * invI * body.torque
            }

            // apply linear damping
            if (body.linearDamping !== 0.0) {
                // Because DEFAULT_LINEAR_DAMPING is 0.0 apply linear damping only if needed
                var linear: Double = 1.0 - dt * body.linearDamping
                linear = Interval.clamp(linear, 0.0, 1.0)

                // inline body.velocity.multiply(linear);
                body.velocity.x *= linear
                body.velocity.y *= linear
            }

            // apply angular damping
            var angular: Double = 1.0 - dt * body.angularDamping
            angular = Interval.clamp(angular, 0.0, 1.0)
            body.angularVelocity *= angular
        }

        // initialize the solver
        solver.initialize(contactConstraints, step, settings)

        // initialize joint constraints
        for (i in 0 until jSize) {
            val joint = joints[i]
            joint.initializeConstraints(step, settings)
        }
        if (!contactConstraints.isEmpty() || !joints.isEmpty()) {
            // solve the velocity constraints if needed
            for (i in 0 until velocitySolverIterations) {
                // solve the joint velocity constraints
                for (j in 0 until jSize) {
                    val joint = joints[j]
                    joint.solveVelocityConstraints(step, settings)
                }
                solver.solveVelocityContraints(contactConstraints, step, settings)
            }
        }

        // the max settings
        val maxTranslation = settings.getMaximumTranslation()
        val maxRotation = settings.getMaximumRotation()
        val maxTranslationSqrd = settings.maximumTranslationSquared

        // integrate the positions
        for (i in 0 until size) {
            val body: Body = bodies[i]
            if (body.isStatic()) continue

            // compute the translation and rotation for this time step
            var translationX: Double = body.velocity.x * dt
            var translationY: Double = body.velocity.y * dt
            val translationMagnitudeSquared = translationX * translationX + translationY * translationY

            // make sure the translation is not over the maximum
            if (translationMagnitudeSquared > maxTranslationSqrd) {
                val translationMagnitude: Double = sqrt(translationMagnitudeSquared)
                val ratio = maxTranslation / translationMagnitude
                body.velocity.multiply(ratio)
                translationX *= ratio
                translationY *= ratio
            }
            var rotation: Double = body.angularVelocity * dt

            // make sure the rotation is not over the maximum
            if (rotation > maxRotation) {
                val ratio: Double = maxRotation / abs(rotation)
                body.angularVelocity *= ratio
                rotation *= ratio
            }

            // recompute the translation/rotation in case we hit the maximums
            // inline body.translate(body.velocity.product(dt));
            body.translate(translationX, translationY)
            body.rotateAboutCenter(rotation)
        }

        // solve the position constraints
        var positionConstraintsSolved = false
        for (i in 0 until positionSolverIterations) {
            val contactsSolved = solver.solvePositionContraints(contactConstraints, step, settings)

            // solve the joint position constraints
            var jointsSolved = true
            for (j in 0 until jSize) {
                val joint = joints[j]
                val jointSolved = joint.solvePositionConstraints(step, settings)
                jointsSolved = jointsSolved && jointSolved
            }
            if (contactsSolved && jointsSolved) {
                positionConstraintsSolved = true
                break
            }
        }

        // see if sleep is enabled
        if (settings.isAutoSleepingEnabled) {
            var minSleepTime = Double.MAX_VALUE
            // check for sleep-able bodies
            for (i in 0 until size) {
                val body: Body = bodies[i]
                // just skip static bodies
                if (body.isStatic()) continue
                // see if the body is allowed to sleep
                if (body.isAutoSleepingEnabled()) {
                    // check the linear and angular velocity
                    if (body.velocity.magnitudeSquared > sleepLinearVelocitySquared || body.angularVelocity > sleepAngularVelocity) {
                        // if either the linear or angular velocity is above the 
                        // threshold then reset the sleep time
                        body.sleepTime = 0.0
                        minSleepTime = 0.0
                    } else {
                        // then increment the sleep time
                        body.sleepTime += step.dt
                        minSleepTime = min(minSleepTime, body.sleepTime)
                    }
                } else {
                    body.sleepTime = 0.0
                    minSleepTime = 0.0
                }
            }

            // check the min sleep time
            if (minSleepTime >= sleepTime && positionConstraintsSolved) {
                for (i in 0 until size) {
                    val body: Body = bodies[i]
                    body.setAsleep(true)
                }
            }
        }
    }
    /**
     * Full constructor.
     * @param initialCapacity the initial capacity of the island
     * @throws NullPointerException if initialCapacity is null
     * @since 3.2.0
     */
    /**
     * Default constructor.
     *
     *
     * Uses a default [Capacity] for the initial capacity.
     * @since 3.2.0
     */
    init {
        // check for null capacity
        if (initialCapacity == null) throw NullPointerException(message("dynamics.nullCapacity"))
        bodies = ArrayList(initialCapacity.bodyCount)
        joints = ArrayList(initialCapacity.jointCount)
        // estimated the number of contacts
        val eSize = getEstimatedCollisionPairs(initialCapacity.bodyCount)
        contactConstraints = ArrayList(eSize)
    }
}