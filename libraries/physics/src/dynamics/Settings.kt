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

import featurea.math.toRadians
import org.dyn4j.resources.Messages
import org.dyn4j.resources.message
import kotlin.jvm.JvmField
import kotlin.math.PI

/**
 * Responsible for housing all of the dynamics engine's settings.
 * @author William Bittle
 * @version 3.1.1
 * @since 1.0.0
 */
class Settings
/** Default constructor  */
{
    /** The step frequency of the dynamics engine  */
    private var stepFrequency = DEFAULT_STEP_FREQUENCY

    /** The maximum translation a [Body] can have in one time step  */
    private var maximumTranslation = DEFAULT_MAXIMUM_TRANSLATION

    /**
     * Returns the maximum translation squared.
     * @see .getMaximumTranslation
     * @see .setMaximumTranslation
     * @return double
     */
    /** The squared value of [.maximumTranslation]  */
    var maximumTranslationSquared =
        DEFAULT_MAXIMUM_TRANSLATION * DEFAULT_MAXIMUM_TRANSLATION
        private set

    /** The maximum rotation a [Body] can have in one time step  */
    private var maximumRotation = DEFAULT_MAXIMUM_ROTATION

    /**
     * Returns the max rotation squared.
     * @see .getMaximumRotation
     * @see .setMaximumRotation
     * @return double
     */
    /** The squared value of [.maximumRotation]  */
    var maximumRotationSquared =
        DEFAULT_MAXIMUM_ROTATION * DEFAULT_MAXIMUM_ROTATION
        private set

    /**
     * Returns true if the engine automatically puts [Body]s to sleep.
     * @return boolean
     */
    /**
     * Sets whether the engine automatically puts [Body]s to sleep.
     * @param flag true if [Body]s should be put to sleep automatically
     */
    /** Whether on an engine level [Body]s are automatically put to sleep  */
    var isAutoSleepingEnabled = true

    /** The maximum linear velocity before a [Body] is considered to sleep  */
    private var sleepLinearVelocity = DEFAULT_SLEEP_LINEAR_VELOCITY

    /**
     * Returns the sleep linear velocity squared.
     * @see .getSleepLinearVelocity
     * @see .setSleepLinearVelocity
     * @return double
     */
    /** The squared value of [.sleepLinearVelocity]  */
    var sleepLinearVelocitySquared =
        DEFAULT_SLEEP_LINEAR_VELOCITY * DEFAULT_SLEEP_LINEAR_VELOCITY
        private set

    /** The maximum angular velocity before a [Body] is considered to sleep  */
    private var sleepAngularVelocity =
        DEFAULT_SLEEP_ANGULAR_VELOCITY

    /**
     * Returns the sleep angular velocity squared.
     * @see .getSleepAngularVelocity
     * @see .setSleepAngularVelocity
     * @return double
     */
    /** The squared value of [.sleepAngularVelocity]  */
    var sleepAngularVelocitySquared =
        DEFAULT_SLEEP_ANGULAR_VELOCITY * DEFAULT_SLEEP_ANGULAR_VELOCITY
        private set

    /** The time required for a [Body] to stay motionless before going to sleep  */
    private var sleepTime = DEFAULT_SLEEP_TIME

    /** The number of iterations used to solve velocity constraints  */
    private var velocityConstraintSolverIterations =
        DEFAULT_SOLVER_ITERATIONS

    /** The maximum number of iterations used to solve position constraints  */
    private var positionConstraintSolverIterations =
        DEFAULT_SOLVER_ITERATIONS

    /** The warm start distance  */
    private var warmStartDistance = DEFAULT_WARM_START_DISTANCE

    /**
     * Returns the warm start distance squared.
     * @see .getWarmStartDistance
     * @see .setWarmStartDistance
     * @return double
     */
    /** The squared value of [.warmStartDistance]  */
    var warmStartDistanceSquared =
        DEFAULT_WARM_START_DISTANCE * DEFAULT_WARM_START_DISTANCE
        private set

    /** The restitution velocity  */
    private var restitutionVelocity = DEFAULT_RESTITUTION_VELOCITY

    /**
     * Returns the restitution velocity squared.
     * @see .getRestitutionVelocity
     * @see .setRestitutionVelocity
     * @return double
     */
    /** The squared value of [.restitutionVelocity]  */
    var restitutionVelocitySquared =
        DEFAULT_RESTITUTION_VELOCITY * DEFAULT_RESTITUTION_VELOCITY
        private set

    /** The allowed linear tolerance  */
    private var linearTolerance = DEFAULT_LINEAR_TOLERANCE

    /**
     * Returns the linear tolerance squared.
     * @see .getLinearTolerance
     * @see .setLinearTolerance
     * @return double
     */
    /** The squared value of [.linearTolerance]  */
    var linearToleranceSquared =
        DEFAULT_LINEAR_TOLERANCE * DEFAULT_LINEAR_TOLERANCE
        private set

    /** The allowed angular tolerance  */
    private var angularTolerance = DEFAULT_ANGULAR_TOLERANCE

    /**
     * Returns the angular tolerance squared.
     * @see .getAngularTolerance
     * @see .setAngularTolerance
     * @return double
     */
    /** The squared value of [.angularTolerance]  */
    var angularToleranceSquared =
        DEFAULT_ANGULAR_TOLERANCE * DEFAULT_ANGULAR_TOLERANCE
        private set

    /** The maximum linear correction  */
    private var maximumLinearCorrection =
        DEFAULT_MAXIMUM_LINEAR_CORRECTION

    /**
     * Returns the maximum linear correction squared.
     * @see .getMaximumLinearCorrection
     * @see .setMaximumLinearCorrection
     * @return double
     */
    /** The squared value of [.maximumLinearCorrection]  */
    var maximumLinearCorrectionSquared =
        DEFAULT_MAXIMUM_LINEAR_CORRECTION * DEFAULT_MAXIMUM_LINEAR_CORRECTION
        private set

    /** The maximum angular correction  */
    private var maximumAngularCorrection =
        DEFAULT_MAXIMUM_ANGULAR_CORRECTION

    /**
     * Returns the maximum angular correction squared.
     * @see .getMaximumAngularCorrection
     * @see .setMaximumAngularCorrection
     * @return double
     */
    /** The squared value of [.maximumAngularCorrection]  */
    var maximumAngularCorrectionSquared =
        DEFAULT_MAXIMUM_ANGULAR_CORRECTION * DEFAULT_MAXIMUM_ANGULAR_CORRECTION
        private set

    /** The baumgarte factor  */
    private var baumgarte = DEFAULT_BAUMGARTE

    /** The continuous collision detection flag  */
    private var continuousDetectionMode = ContinuousDetectionMode.ALL

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("Settings[StepFrequency=").append(stepFrequency)
            .append("|MaximumTranslation=").append(maximumTranslation)
            .append("|MaximumRotation=").append(maximumRotation)
            .append("|AutoSleepingEnabled=").append(isAutoSleepingEnabled)
            .append("|SleepLinearVelocity=").append(sleepLinearVelocity)
            .append("|SleepAngularVelocity=").append(sleepAngularVelocity)
            .append("|SleepTime=").append(sleepTime)
            .append("|VelocityConstraintSolverIterations=").append(velocityConstraintSolverIterations)
            .append("|PositionConstraintSolverIterations=").append(positionConstraintSolverIterations)
            .append("|WarmStartDistance=").append(warmStartDistance)
            .append("|RestitutionVelocity=").append(restitutionVelocity)
            .append("|LinearTolerance=").append(linearTolerance)
            .append("|AngularTolerance=").append(angularTolerance)
            .append("|MaximumLinearCorrection=").append(maximumLinearCorrection)
            .append("|MaximumAngularCorrection=").append(maximumAngularCorrection)
            .append("|Baumgarte=").append(baumgarte)
            .append("|ContinuousDetectionMode=").append(continuousDetectionMode)
            .append("]")
        return sb.toString()
    }

    /**
     * Resets the settings back to defaults.
     */
    fun reset() {
        stepFrequency = DEFAULT_STEP_FREQUENCY
        maximumTranslation = DEFAULT_MAXIMUM_TRANSLATION
        maximumTranslationSquared =
            DEFAULT_MAXIMUM_TRANSLATION * DEFAULT_MAXIMUM_TRANSLATION
        maximumRotation = DEFAULT_MAXIMUM_ROTATION
        maximumRotationSquared =
            DEFAULT_MAXIMUM_ROTATION * DEFAULT_MAXIMUM_ROTATION
        isAutoSleepingEnabled = true
        sleepLinearVelocity = DEFAULT_SLEEP_LINEAR_VELOCITY
        sleepLinearVelocitySquared =
            DEFAULT_SLEEP_LINEAR_VELOCITY * DEFAULT_SLEEP_LINEAR_VELOCITY
        sleepAngularVelocity = DEFAULT_SLEEP_ANGULAR_VELOCITY
        sleepAngularVelocitySquared =
            DEFAULT_SLEEP_ANGULAR_VELOCITY * DEFAULT_SLEEP_ANGULAR_VELOCITY
        sleepTime = DEFAULT_SLEEP_TIME
        velocityConstraintSolverIterations = DEFAULT_SOLVER_ITERATIONS
        positionConstraintSolverIterations = DEFAULT_SOLVER_ITERATIONS
        warmStartDistance = DEFAULT_WARM_START_DISTANCE
        warmStartDistanceSquared =
            DEFAULT_WARM_START_DISTANCE * DEFAULT_WARM_START_DISTANCE
        restitutionVelocity = DEFAULT_RESTITUTION_VELOCITY
        restitutionVelocitySquared =
            DEFAULT_RESTITUTION_VELOCITY * DEFAULT_RESTITUTION_VELOCITY
        linearTolerance = DEFAULT_LINEAR_TOLERANCE
        linearToleranceSquared =
            DEFAULT_LINEAR_TOLERANCE * DEFAULT_LINEAR_TOLERANCE
        maximumLinearCorrection = DEFAULT_MAXIMUM_LINEAR_CORRECTION
        maximumLinearCorrectionSquared =
            DEFAULT_MAXIMUM_LINEAR_CORRECTION * DEFAULT_MAXIMUM_LINEAR_CORRECTION
        angularTolerance = DEFAULT_ANGULAR_TOLERANCE
        angularToleranceSquared =
            DEFAULT_ANGULAR_TOLERANCE * DEFAULT_ANGULAR_TOLERANCE
        baumgarte = DEFAULT_BAUMGARTE
        continuousDetectionMode = ContinuousDetectionMode.ALL
    }

    /**
     * Returns the step frequency of the dynamics engine in seconds.
     *
     *
     * @return double the step frequency
     * @see .setStepFrequency
     */
    fun getStepFrequency(): Double {
        return stepFrequency
    }

    /**
     * Sets the step frequency of the dynamics engine.  This value determines how often to
     * update the dynamics engine in seconds (every 1/60th of a second for example).
     *
     *
     * Valid values are in the range (0, ] seconds.
     *
     *
     * Versions before 3.1.1 would convert the stepFrequency parameter from seconds<sup>-1</sup> to
     * seconds (60 to 1/60 for example) automatically.  This automatic conversion has been removed
     * in versions 3.1.1 and higher.  Instead pass in the value in seconds (1/60 for example).
     * @param stepFrequency the step frequency
     * @throws IllegalArgumentException if stepFrequency is less than or equal to zero
     */
    fun setStepFrequency(stepFrequency: Double) {
        if (stepFrequency <= 0.0) throw IllegalArgumentException(message("dynamics.settings.invalidStepFrequency"))
        this.stepFrequency = stepFrequency
    }

    /**
     * Returns the maximum translation a [Body] can have in one time step.
     * @return double the maximum translation in meters
     * @see .setMaximumTranslation
     */
    fun getMaximumTranslation(): Double {
        return maximumTranslation
    }

    /**
     * Sets the maximum translation a [Body] can have in one time step.
     *
     *
     * Valid values are in the range [0, ] meters
     * @param maximumTranslation the maximum translation
     * @throws IllegalArgumentException if maxTranslation is less than zero
     */
    fun setMaximumTranslation(maximumTranslation: Double) {
        if (maximumTranslation < 0) throw IllegalArgumentException(message("dynamics.settings.invalidMaximumTranslation"))
        this.maximumTranslation = maximumTranslation
        maximumTranslationSquared = maximumTranslation * maximumTranslation
    }

    /**
     * Returns the maximum rotation a [Body] can have in one time step.
     * @return double the maximum rotation in radians
     * @see .setMaximumRotation
     */
    fun getMaximumRotation(): Double {
        return maximumRotation
    }

    /**
     * Sets the maximum rotation a [Body] can have in one time step.
     *
     *
     * Valid values are in the range [0, ] radians
     * @param maximumRotation the maximum rotation
     * @throws IllegalArgumentException if maxRotation is less than zero
     */
    fun setMaximumRotation(maximumRotation: Double) {
        if (maximumRotation < 0) throw IllegalArgumentException(message("dynamics.settings.invalidMaximumRotation"))
        this.maximumRotation = maximumRotation
        maximumRotationSquared = maximumRotation * maximumRotation
    }

    /**
     * Returns the sleep linear velocity.
     * @return double the sleep velocity.
     * @see .setSleepLinearVelocity
     */
    fun getSleepLinearVelocity(): Double {
        return sleepLinearVelocity
    }

    /**
     * Sets the sleep linear velocity.
     *
     *
     * The sleep linear velocity is the maximum velocity a [Body] can have
     * to be put to sleep.
     *
     *
     * Valid values are in the range [0, ] meters/second
     * @param sleepLinearVelocity the sleep linear velocity
     * @throws IllegalArgumentException if sleepLinearVelocity is less than zero
     */
    fun setSleepLinearVelocity(sleepLinearVelocity: Double) {
        if (sleepLinearVelocity < 0) throw IllegalArgumentException(message("dynamics.settings.invalidSleepLinearVelocity"))
        this.sleepLinearVelocity = sleepLinearVelocity
        sleepLinearVelocitySquared = sleepLinearVelocity * sleepLinearVelocity
    }

    /**
     * Returns the sleep angular velocity.
     * @return double the sleep angular velocity.
     * @see .setSleepAngularVelocity
     */
    fun getSleepAngularVelocity(): Double {
        return sleepAngularVelocity
    }

    /**
     * Sets the sleep angular velocity.
     *
     *
     * The sleep angular velocity is the maximum angular velocity a [Body] can have
     * to be put to sleep.
     *
     *
     * Valid values are in the range [0, ] radians/second
     * @param sleepAngularVelocity the sleep angular velocity
     * @throws IllegalArgumentException if sleepAngularVelocity is less than zero
     */
    fun setSleepAngularVelocity(sleepAngularVelocity: Double) {
        if (sleepAngularVelocity < 0) throw IllegalArgumentException(message("dynamics.settings.invalidSleepAngularVelocity"))
        this.sleepAngularVelocity = sleepAngularVelocity
        sleepAngularVelocitySquared = sleepAngularVelocity * sleepAngularVelocity
    }

    /**
     * Returns the sleep time.
     * @return double the sleep time
     * @see .setSleepTime
     */
    fun getSleepTime(): Double {
        return sleepTime
    }

    /**
     * Sets the sleep time.
     *
     *
     * The sleep time is the amount of time a body must be motionless
     * before being put to sleep.
     *
     *
     * Valid values are in the range [0, ] seconds
     * @param sleepTime the sleep time
     * @throws IllegalArgumentException if sleepTime is less than zero
     */
    fun setSleepTime(sleepTime: Double) {
        if (sleepTime < 0) throw IllegalArgumentException(message("dynamics.settings.invalidSleepTime"))
        this.sleepTime = sleepTime
    }

    /**
     * Returns the number of iterations used to solve velocity constraints.
     * @return int
     */
    fun getVelocityConstraintSolverIterations(): Int {
        return velocityConstraintSolverIterations
    }

    /**
     * Sets the number of iterations used to solve velocity constraints.
     *
     *
     * Increasing the number will increase accuracy but decrease performance.
     *
     *
     * Valid values are in the range [1, ]
     * @param velocityConstraintSolverIterations the number of iterations used to solve velocity constraints
     * @throws IllegalArgumentException if velocityConstraintSolverIterations is less than 5
     */
    fun setVelocityConstraintSolverIterations(velocityConstraintSolverIterations: Int) {
        if (velocityConstraintSolverIterations < 1) throw IllegalArgumentException(message("dynamics.settings.invalidVelocityIterations"))
        this.velocityConstraintSolverIterations = velocityConstraintSolverIterations
    }

    /**
     * Returns the number of iterations used to solve position constraints.
     * @return int
     */
    fun getPositionConstraintSolverIterations(): Int {
        return positionConstraintSolverIterations
    }

    /**
     * Sets the number of iterations used to solve position constraints.
     *
     *
     * Increasing the number will increase accuracy but decrease performance.
     *
     *
     * Valid values are in the range [1, ]
     * @param positionConstraintSolverIterations the number of iterations used to solve position constraints
     * @throws IllegalArgumentException if positionConstraintSolverIterations is less than 5
     */
    fun setPositionConstraintSolverIterations(positionConstraintSolverIterations: Int) {
        if (positionConstraintSolverIterations < 1) throw IllegalArgumentException(message("dynamics.settings.invalidPositionIterations"))
        this.positionConstraintSolverIterations = positionConstraintSolverIterations
    }

    /**
     * Returns the warm start distance.
     * @return double the warm start distance
     * @see .setWarmStartDistance
     */
    fun getWarmStartDistance(): Double {
        return warmStartDistance
    }

    /**
     * Sets the warm start distance.
     *
     *
     * The maximum distance from one point to another to consider the points to be the
     * same.  This distance is used to determine if the points can carry over another
     * point's accumulated impulses to be used for warm starting the constraint solver.
     *
     *
     * Valid values are in the range [0, ] meters
     * @param warmStartDistance the warm start distance
     * @throws IllegalArgumentException if warmStartDistance is less than zero
     */
    fun setWarmStartDistance(warmStartDistance: Double) {
        if (warmStartDistance < 0) throw IllegalArgumentException(message("dynamics.settings.invalidWarmStartDistance"))
        this.warmStartDistance = warmStartDistance
        warmStartDistanceSquared = this.warmStartDistance * this.warmStartDistance
    }

    /**
     * Returns the restitution velocity.
     * @return double the restitution velocity
     * @see .setRestitutionVelocity
     */
    fun getRestitutionVelocity(): Double {
        return restitutionVelocity
    }

    /**
     * Sets the restitution velocity.
     *
     *
     * The relative velocity in the direction of the contact normal which determines
     * whether to handle the collision as an inelastic or elastic collision.
     *
     *
     * Valid values are in the range [0, ] meters/second
     * @param restitutionVelocity the restitution velocity
     * @throws IllegalArgumentException if restitutionVelocity is less than zero
     */
    fun setRestitutionVelocity(restitutionVelocity: Double) {
        if (restitutionVelocity < 0) throw IllegalArgumentException(message("dynamics.settings.invalidRestitutionVelocity"))
        this.restitutionVelocity = restitutionVelocity
        restitutionVelocitySquared = restitutionVelocity * restitutionVelocity
    }

    /**
     * Returns the linear tolerance.
     * @return double the allowed penetration
     * @see .setLinearTolerance
     */
    fun getLinearTolerance(): Double {
        return linearTolerance
    }

    /**
     * Sets the linear tolerance.
     *
     *
     * Used to avoid jitter and facilitate stacking.
     *
     *
     * Valid values are in the range (0, ] meters
     * @param linearTolerance the linear tolerance
     * @throws IllegalArgumentException if linearTolerance is less than zero
     */
    fun setLinearTolerance(linearTolerance: Double) {
        if (linearTolerance < 0) throw IllegalArgumentException(message("dynamics.settings.invalidLinearTolerance"))
        this.linearTolerance = linearTolerance
        linearToleranceSquared = linearTolerance * linearTolerance
    }

    /**
     * Returns the angular tolerance.
     * @see .setAngularTolerance
     * @return double
     */
    fun getAngularTolerance(): Double {
        return angularTolerance
    }

    /**
     * Sets the angular tolerance.
     *
     *
     * Used to avoid jitter and facilitate stacking.
     *
     *
     * Valid values are in the range (0, ] radians
     * @param angularTolerance the angular tolerance
     * @throws IllegalArgumentException if angularTolerance is less than zero
     */
    fun setAngularTolerance(angularTolerance: Double) {
        if (angularTolerance < 0) throw IllegalArgumentException(message("dynamics.settings.invalidAngularTolerance"))
        this.angularTolerance = angularTolerance
        angularToleranceSquared = angularTolerance * angularTolerance
    }

    /**
     * Returns the maximum linear correction.
     * @return double the maximum linear correction
     * @see .setMaximumLinearCorrection
     */
    fun getMaximumLinearCorrection(): Double {
        return maximumLinearCorrection
    }

    /**
     * Sets the maximum linear correction.
     *
     *
     * The maximum linear correction used when estimating the current penetration depth
     * during the position constraint solving step.
     *
     *
     * This is used to avoid large corrections.
     *
     *
     * Valid values are in the range (0, ] meters
     * @param maximumLinearCorrection the maximum linear correction
     * @throws IllegalArgumentException if maxLinearCorrection is less than zero
     */
    fun setMaximumLinearCorrection(maximumLinearCorrection: Double) {
        if (maximumLinearCorrection < 0) throw IllegalArgumentException(message("dynamics.settings.invalidMaximumLinearCorrection"))
        this.maximumLinearCorrection = maximumLinearCorrection
        maximumLinearCorrectionSquared = maximumLinearCorrection * maximumLinearCorrection
    }

    /**
     * Returns the maximum angular correction.
     * @see .setMaximumAngularCorrection
     * @return double
     */
    fun getMaximumAngularCorrection(): Double {
        return maximumAngularCorrection
    }

    /**
     * Sets the maximum angular correction.
     *
     *
     * This is used to prevent large angular corrections.
     *
     *
     * Valid values are in the range [0, ] radians
     * @param maximumAngularCorrection the maximum angular correction
     * @throws IllegalArgumentException if maxAngularCorrection is less than zero
     */
    fun setMaximumAngularCorrection(maximumAngularCorrection: Double) {
        if (maximumAngularCorrection < 0) throw IllegalArgumentException(message("dynamics.settings.invalidMaximumAngularCorrection"))
        this.maximumAngularCorrection = maximumAngularCorrection
        maximumAngularCorrectionSquared = maximumAngularCorrection * maximumAngularCorrection
    }

    /**
     * Returns the baumgarte factor.
     * @return double baumgarte
     * @see .setBaumgarte
     */
    fun getBaumgarte(): Double {
        return baumgarte
    }

    /**
     * Sets the baumgarte factor.
     *
     *
     * The position correction bias factor that determines the rate at which the position constraints are solved.
     *
     *
     * Valid values are in the range [0, ].
     * @param baumgarte the baumgarte factor
     * @throws IllegalArgumentException if baumgarte is less than zero
     */
    fun setBaumgarte(baumgarte: Double) {
        if (baumgarte < 0) throw IllegalArgumentException(message("dynamics.settings.invalidBaumgarte"))
        this.baumgarte = baumgarte
    }

    /**
     * Returns the continuous collision detection mode.
     * @return [ContinuousDetectionMode]
     * @since 2.2.3
     */
    fun getContinuousDetectionMode(): ContinuousDetectionMode {
        return continuousDetectionMode
    }

    /**
     * Sets the continuous collision detection mode.
     * @param mode the CCD mode
     * @throws NullPointerException if mode is null
     * @since 2.2.3
     */
    fun setContinuousDetectionMode(mode: ContinuousDetectionMode?) {
        // make sure its not null
        if (mode == null) throw NullPointerException(message("dynamics.settings.invalidCCDMode"))
        // set the mode
        continuousDetectionMode = mode
    }

    companion object {
        /** The default step frequency of the dynamics engine; in seconds  */
        const val DEFAULT_STEP_FREQUENCY = 1.0 / 60.0

        /** The default maximum translation a [Body] can have in one time step; in meters  */
        const val DEFAULT_MAXIMUM_TRANSLATION = 2.0

        /** The default maximum rotation a [Body] can have in one time step; in radians  */
        @JvmField
        val DEFAULT_MAXIMUM_ROTATION: Double = 0.5 * PI

        /** The default maximum velocity for a [Body] to go to sleep; in meters/second  */
        const val DEFAULT_SLEEP_LINEAR_VELOCITY = 0.01

        /** The default maximum angular velocity for a [Body] to go to sleep; in radians/second  */
        @JvmField
        val DEFAULT_SLEEP_ANGULAR_VELOCITY: Double = 2.0.toRadians()

        /** The default required time a [Body] must maintain small motion so that its put to sleep; in seconds  */
        const val DEFAULT_SLEEP_TIME = 0.5

        /** The default number of solver iterations  */
        const val DEFAULT_SOLVER_ITERATIONS = 10

        /** The default warm starting distance; in meters<sup>2</sup>  */
        const val DEFAULT_WARM_START_DISTANCE = 1.0e-2

        /** The default restitution velocity; in meters/second  */
        const val DEFAULT_RESTITUTION_VELOCITY = 1.0

        /** The default linear tolerance; in meters  */
        const val DEFAULT_LINEAR_TOLERANCE = 0.005

        /** The default angular tolerance; in radians  */
        @JvmField
        val DEFAULT_ANGULAR_TOLERANCE: Double = 2.0.toRadians()

        /** The default maximum linear correction; in meters  */
        const val DEFAULT_MAXIMUM_LINEAR_CORRECTION = 0.2

        /** The default maximum angular correction; in radians  */
        @JvmField
        val DEFAULT_MAXIMUM_ANGULAR_CORRECTION: Double = 8.0.toRadians()

        /** The default baumgarte  */
        const val DEFAULT_BAUMGARTE = 0.2
    }
}