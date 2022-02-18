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
package org.dyn4j.geometry

import kotlin.jvm.JvmField
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents a transformation matrix.
 *
 *
 * Supported operations are rotation and translation.
 * @author William Bittle
 * @version 3.4.0
 * @since 1.0.0
 */
class Transform : Transformable {
    /** the cosine of the rotation angle  */
    @JvmField
    var cost: Double = 1.0

    /** the sine of the rotation angle  */
    @JvmField
    var sint: Double = 0.0

    /**
     * Returns the x translation.
     * @return double
     */
    /**
     * Sets the translation along the x axis.
     * @param x the translation along the x axis
     * @since 1.2.0
     */
    /** The x translation  */
    @JvmField
    var x = 0.0

    /**
     * Returns the x translation.
     * @return double
     */
    /**
     * Sets the translation along the y axis.
     * @param y the translation along the y axis
     * @since 1.2.0
     */
    /** The y translation  */
    @JvmField
    var y = 0.0

    /**
     * Default public constructor
     */
    constructor() {}

    /**
     * Public copy constructor constructor
     * @param transform the transform to copy
     * @since 3.4.0
     */
    constructor(transform: Transform) {
        cost = transform.cost
        sint = transform.sint
        x = transform.x
        y = transform.y
    }

    /**
     * Private constructor for some copy and internal operations
     * @param cost the cosine
     * @param sint the negative sine
     * @param x the x translation
     * @param y the y translation
     * @since 3.4.0
     */
    private constructor(cost: Double, sint: Double, x: Double, y: Double) {
        this.cost = cost
        this.sint = sint
        this.x = x
        this.y = y
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("[").append(cost).append(" ").append(-sint).append(" | ").append(x).append("]")
            .append("[").append(sint).append(" ").append(cost).append(" | ").append(y).append("]")
        return sb.toString()
    }

    /**
     * Internal helper method to rotate this [Transform] by an angle
     * @param c cos()
     * @param s sin()
     * @since 3.4.0
     */
    fun rotate(c: Double, s: Double) {
        // perform an optimized version of matrix multiplication
        val cost = c * cost - s * sint
        val sint = s * this.cost + c * sint
        val x = c * x - s * y
        val y = s * this.x + c * y

        // set the new values
        this.cost = cost
        this.sint = sint
        this.x = x
        this.y = y
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(double)
	 */
    override fun rotate(theta: Double) {
        this.rotate(cos(theta), sin(theta))
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(org.dyn4j.geometry.Rotation)
	 */
    override fun rotate(rotation: Rotation) {
        this.rotate(rotation.cost, rotation.sint)
    }

    /**
     * Internal helper method to rotate this [Transform] by an angle  around a point
     * @param c cos()
     * @param s sin()
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @since 3.4.0
     */
    fun rotate(c: Double, s: Double, x: Double, y: Double) {
        // perform an optimized version of the matrix multiplication:
        // M(new) = inverse(T) * R * T * M(old)
        val cost = c * cost - s * sint
        val sint = s * this.cost + c * sint
        this.cost = cost
        this.sint = sint
        val cx = this.x - x
        val cy = this.y - y
        this.x = c * cx - s * cy + x
        this.y = s * cx + c * cy + y
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(double, double, double)
	 */
    override fun rotate(theta: Double, x: Double, y: Double) {
        this.rotate(cos(theta), sin(theta), x, y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(org.dyn4j.geometry.Rotation, double, double)
	 */
    override fun rotate(rotation: Rotation, x: Double, y: Double) {
        this.rotate(rotation.cost, rotation.sint, x, y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(double, org.dyn4j.geometry.Vector)
	 */
    override fun rotate(theta: Double, point: Vector2) {
        this.rotate(theta, point.x, point.y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#rotate(org.dyn4j.geometry.Rotation, org.dyn4j.geometry.Vector)
	 */
    override fun rotate(rotation: Rotation, point: Vector2) {
        this.rotate(rotation, point.x, point.y)
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#translate(double, double)
	 */
    override fun translate(x: Double, y: Double) {
        this.x += x
        this.y += y
    }

    /* (non-Javadoc)
	 * @see org.dyn4j.geometry.Transformable#translate(org.dyn4j.geometry.Vector)
	 */
    override fun translate(vector: Vector2) {
        x += vector.x
        y += vector.y
    }

    /**
     * Copies this [Transform].
     * @return [Transform]
     */
    fun copy(): Transform {
        return Transform(this)
    }

    /**
     * Sets this transform to the given transform.
     * @param transform the transform to copy
     * @since 1.1.0
     */
    fun set(transform: Transform) {
        cost = transform.cost
        sint = transform.sint
        x = transform.x
        y = transform.y
    }

    /**
     * Sets this [Transform] to the identity.
     */
    fun identity() {
        cost = 1.0
        sint = 0.0
        x = 0.0
        y = 0.0
    }

    /**
     * Transforms only the x coordinate of the given [Vector2] and returns the result.
     * @param vector the [Vector2] to transform
     * @return the transformed x coordinate
     * @since 3.4.0
     */
    fun getTransformedX(vector: Vector2): Double {
        return cost * vector.x - sint * vector.y + x
    }

    /**
     * Transforms only the y coordinate of the given [Vector2] and returns the result.
     * @param vector the [Vector2] to transform
     * @return the transformed y coordinate
     * @since 3.4.0
     */
    fun getTransformedY(vector: Vector2): Double {
        return sint * vector.x + cost * vector.y + y
    }

    /**
     * Transforms only the x coordinate of the given [Vector2] and places the result in the x field of the given [Vector2].
     * @param vector the [Vector2] to transform
     * @since 3.4.0
     */
    fun transformX(vector: Vector2) {
        vector.x = cost * vector.x - sint * vector.y + x
    }

    /**
     * Transforms only the y coordinate of the given [Vector2] and places the result in the y field of the given [Vector2].
     * @param vector the [Vector2] to transform
     * @since 3.4.0
     */
    fun transformY(vector: Vector2) {
        vector.y = sint * vector.x + cost * vector.y + y
    }

    /**
     * Transforms the given [Vector2] and returns a new [Vector2] containing the result.
     * @param vector the [Vector2] to transform
     * @return [Vector2]
     */
    fun getTransformed(vector: Vector2): Vector2 {
        val tv = Vector2()
        val x: Double = vector.x
        val y: Double = vector.y
        tv.x = cost * x - sint * y + this.x
        tv.y = sint * x + cost * y + this.y
        return tv
    }

    /**
     * Transforms the given [Vector2] and returns the result in dest.
     * @param vector the [Vector2] to transform
     * @param destination the [Vector2] containing the result
     */
    fun getTransformed(vector: Vector2, destination: Vector2) {
        val x: Double = vector.x
        val y: Double = vector.y
        destination.x = cost * x - sint * y + this.x
        destination.y = sint * x + cost * y + this.y
    }

    /**
     * Transforms the given [Vector2] and places the result in the given [Vector2].
     * @param vector the [Vector2] to transform
     */
    fun transform(vector: Vector2) {
        val x: Double = vector.x
        val y: Double = vector.y
        vector.x = cost * x - sint * y + this.x
        vector.y = sint * x + cost * y + this.y
    }

    /**
     * Inverse transforms the given [Vector2] and returns a new [Vector2] containing the result.
     * @param vector the [Vector2] to transform
     * @return [Vector2]
     */
    fun getInverseTransformed(vector: Vector2): Vector2 {
        val tv = Vector2()
        val tx: Double = vector.x - x
        val ty: Double = vector.y - y
        tv.x = cost * tx + sint * ty
        tv.y = -sint * tx + cost * ty
        return tv
    }

    /**
     * Inverse transforms the given [Vector2] and returns the result in the destination [Vector2].
     * @param vector the [Vector2] to transform
     * @param destination the [Vector2] containing the result
     */
    fun getInverseTransformed(vector: Vector2, destination: Vector2) {
        val tx: Double = vector.x - x
        val ty: Double = vector.y - y
        destination.x = cost * tx + sint * ty
        destination.y = -sint * tx + cost * ty
    }

    /**
     * Inverse transforms the given [Vector2] and places the result in the given [Vector2].
     * @param vector the [Vector2] to transform
     */
    fun inverseTransform(vector: Vector2) {
        val x: Double = vector.x - x
        val y: Double = vector.y - y
        vector.x = cost * x + sint * y
        vector.y = -sint * x + cost * y
    }

    /**
     * Transforms the given [Vector2] only by the rotation and returns
     * a new [Vector2] containing the result.
     * @param vector the [Vector2] to transform
     * @return [Vector2]
     */
    fun getTransformedR(vector: Vector2): Vector2 {
        val v = Vector2()
        val x: Double = vector.x
        val y: Double = vector.y
        v.x = cost * x - sint * y
        v.y = sint * x + cost * y
        return v
    }

    /**
     * Transforms the given [Vector2] only by the rotation and returns the result in the
     * destination [Vector2].
     * @param vector the [Vector2] to transform
     * @param destination the [Vector2] containing the result
     * @since 3.1.5
     */
    fun getTransformedR(vector: Vector2, destination: Vector2) {
        val x: Double = vector.x
        val y: Double = vector.y
        destination.x = cost * x - sint * y
        destination.y = sint * x + cost * y
    }

    /**
     * Transforms the given [Vector2] only by the rotation and returns the
     * result in the given [Vector2].
     * @param vector the [Vector2] to transform
     */
    fun transformR(vector: Vector2) {
        val x: Double = vector.x
        val y: Double = vector.y
        vector.x = cost * x - sint * y
        vector.y = sint * x + cost * y
    }

    /**
     * Inverse transforms the given [Vector2] only by the rotation and returns
     * a new [Vector2] containing the result.
     * @param vector the [Vector2] to transform
     * @return [Vector2]
     */
    fun getInverseTransformedR(vector: Vector2): Vector2 {
        val v = Vector2()
        val x: Double = vector.x
        val y: Double = vector.y
        // since the transpose of a rotation matrix is the inverse
        v.x = cost * x + sint * y
        v.y = -sint * x + cost * y
        return v
    }

    /**
     * Transforms the given [Vector2] only by the rotation and returns the result in the
     * destination [Vector2].
     * @param vector the [Vector2] to transform
     * @param destination the [Vector2] containing the result
     * @since 3.1.5
     */
    fun getInverseTransformedR(vector: Vector2, destination: Vector2) {
        val x: Double = vector.x
        val y: Double = vector.y
        // since the transpose of a rotation matrix is the inverse
        destination.x = cost * x + sint * y
        destination.y = -sint * x + cost * y
    }

    /**
     * Transforms the given [Vector2] only by the rotation and returns the
     * result in the given [Vector2].
     * @param vector the [Vector2] to transform
     */
    fun inverseTransformR(vector: Vector2) {
        val x: Double = vector.x
        val y: Double = vector.y
        // since the transpose of a rotation matrix is the inverse
        vector.x = cost * x + sint * y
        vector.y = -sint * x + cost * y
    }

    /**
     * Returns the translation [Vector2].
     * @return [Vector2]
     */
    /**
     * Sets the translation.
     * @param translation the translation along both axes
     * @since 1.2.0
     */
    var translation: Vector2
        get() = Vector2(x, y)
        set(translation) {
            setTranslation(translation.x, translation.y)
        }

    /**
     * Sets the translation.
     * @param x the translation along the x axis
     * @param y the translation along the y axis
     * @since 1.2.0
     */
    fun setTranslation(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    /**
     * Returns a new [Transform] including only the
     * translation of this [Transform].
     * @return [Transform]
     */
    val translationTransform: Transform
        get() = Transform(1.0, 0.0, x, y)// Copied from Rotation class; See there for more info

    /**
     * Returns the rotation.
     * @return double angle in the range [-, ]
     */
    val rotationAngle: Double
        get() {
            // Copied from Rotation class; See there for more info
            val acos: Double = acos(cost)
            return if (sint >= 0) acos else -acos
        }

    /**
     * @return the [Rotation] object representing the rotation of this [Transform]
     * @since 3.4.0
     */
    val rotation: Rotation
        get() = Rotation.of(this)

    /**
     * Sets the rotation and returns the previous
     * rotation.
     * @param theta the angle in radians
     * @return double the old rotation in radians in the range [-, ]
     * @since 3.1.0
     */
    fun setRotation(theta: Double): Double {
        // get the current rotation
        val r = rotationAngle

        // get rid of the current rotation and rotate by the new theta
        cost = cos(theta)
        sint = sin(theta)

        // return the previous amount
        return r
    }

    /**
     * Sets the rotation and returns the previous
     * rotation.
     * @param rotation the [Rotation]
     * @return A new [Rotation] object representing the old rotation of this [Transform]
     * @since 3.4.0
     */
    fun setRotation(rotation: Rotation): Rotation {
        // get the current rotation
        val r = rotation

        // get rid of the current rotation and rotate by the new rotation
        cost = rotation.cost
        sint = rotation.sint

        // return the previous rotation object
        return r
    }

    /**
     * Returns a new [Transform] including only the
     * rotation of this [Transform].
     * @return [Transform]
     */
    val rotationTransform: Transform
        get() = Transform(cost, sint, 0.0, 0.0)

    /**
     * Returns the values stored in this transform.
     *
     *
     * The values are in the order of 00, 01, x, 10, 11, y.
     * @return double[]
     * @since 3.0.1
     */
    val values: DoubleArray
        get() = doubleArrayOf(
            cost, -sint, x,
            sint, cost, y
        )

    /**
     * Interpolates this transform linearly by alpha towards the given end transform.
     *
     *
     * Interpolating from one angle to another can have two results depending on the
     * direction of the rotation.  If a rotation was from 30 to 200 the rotation could
     * be 170 or -190.  This interpolation method will always choose the smallest
     * rotation (regardless of sign) as the rotation direction.
     * @param end the end transform
     * @param alpha the amount to interpolate
     * @since 1.2.0
     */
    fun lerp(end: Transform, alpha: Double) {
        // interpolate the position
        val x = x + alpha * (end.x - x)
        val y = y + alpha * (end.y - y)

        // compute the angle
        // get the start and end rotations
        // its key that these methods use atan2 because
        // it ensures that the angles are always within
        // the range -pi < theta < pi therefore no
        // normalization has to be done
        val rs = rotationAngle
        val re = end.rotationAngle
        // make sure we use the smallest rotation
        // as described in the comments above, there
        // are two possible rotations depending on the
        // direction, we always choose the smaller
        var diff = re - rs
        if (diff < -PI) diff += Geometry.TWO_PI
        if (diff > PI) diff -= Geometry.TWO_PI
        // interpolate
        // its ok if this method produces an angle
        // outside the range of -pi < theta < pi
        // since the rotate method uses sin and cos
        // which are not bounded
        val a = diff * alpha + rs

        // set this transform to the interpolated transform
        // the following performs the following calculations:
        // this.identity();
        // this.rotate(a);
        // this.translate(x, y);
        cost = cos(a)
        sint = sin(a)
        this.x = x
        this.y = y
    }

    /**
     * Interpolates linearly by alpha towards the given end transform placing
     * the result in the given transform.
     *
     *
     * Interpolating from one angle to another can have two results depending on the
     * direction of the rotation.  If a rotation was from 30 to 200 the rotation could
     * be 170 or -190.  This interpolation method will always choose the smallest
     * rotation (regardless of sign) as the rotation direction.
     * @param end the end transform
     * @param alpha the amount to interpolate
     * @param result the transform to place the result
     * @since 1.2.0
     */
    fun lerp(
        end: Transform,
        alpha: Double,
        result: Transform
    ) {
        // interpolate the position
        val x = x + alpha * (end.x - x)
        val y = y + alpha * (end.y - y)

        // compute the angle
        // get the start and end rotations
        // its key that these methods use atan2 because
        // it ensures that the angles are always within
        // the range -pi < theta < pi therefore no
        // normalization has to be done
        val rs = rotationAngle
        val re = end.rotationAngle
        // make sure we use the smallest rotation
        // as described in the comments above, there
        // are two possible rotations depending on the
        // direction, we always choose the smaller
        var diff = re - rs
        if (diff < -PI) diff += Geometry.TWO_PI
        if (diff > PI) diff -= Geometry.TWO_PI
        // interpolate
        // its ok if this method produces an angle
        // outside the range of -pi < theta < pi
        // since the rotate method uses sin and cos
        // which are not bounded
        val a = diff * alpha + rs

        // set the result transform to the interpolated transform
        // the following performs the following calculations:
        // result.identity();
        // result.rotate(a);
        // result.translate(x, y);
        result.cost = cos(a)
        result.sint = sin(a)
        result.x = x
        result.y = y
    }

    /**
     * Helper method for the lerp methods below.
     * Performs rotation but leaves translation intact.
     * @param theta the angle of rotation in radians
     * @since 3.4.0
     */
    private fun rotateOnly(theta: Double) {
        //perform rotation by theta but leave x and y intact
        val cos: Double = cos(theta)
        val sin: Double = sin(theta)
        val cost = cos * cost - sin * sint
        val sint = sin * this.cost + cos * sint
        this.cost = cost
        this.sint = sint
    }

    /**
     * Interpolates this transform linearly, by alpha, given the change in
     * position (p) and the change in angle (a) and places it into result.
     * @param dp the change in position
     * @param da the change in angle
     * @param alpha the amount to interpolate
     * @param result the transform to place the result
     * @since 3.1.5
     */
    fun lerp(dp: Vector2, da: Double, alpha: Double, result: Transform) {
        result.set(this)
        result.rotateOnly(da * alpha)
        result.translate(dp.x * alpha, dp.y * alpha)
    }

    /**
     * Interpolates this transform linearly, by alpha, given the change in
     * position (p) and the change in angle (a).
     * @param dp the change in position
     * @param da the change in angle
     * @param alpha the amount to interpolate
     * @since 3.1.5
     */
    fun lerp(dp: Vector2, da: Double, alpha: Double) {
        rotateOnly(da * alpha)
        this.translate(dp.x * alpha, dp.y * alpha)
    }

    /**
     * Interpolates this transform linearly, by alpha, given the change in
     * position (p) and the change in angle (a) and returns the result.
     * @param dp the change in position
     * @param da the change in angle
     * @param alpha the amount to interpolate
     * @return [Transform]
     * @since 3.1.5
     */
    fun lerped(dp: Vector2, da: Double, alpha: Double): Transform {
        val result = Transform(this)
        result.rotateOnly(da * alpha)
        result.translate(dp.x * alpha, dp.y * alpha)
        return result
    }

    /**
     * Interpolates linearly by alpha towards the given end transform returning
     * a new transform containing the result.
     *
     *
     * Interpolating from one angle to another can have two results depending on the
     * direction of the rotation.  If a rotation was from 30 to 200 the rotation could
     * be 170 or -190.  This interpolation method will always choose the smallest
     * rotation (regardless of sign) as the rotation direction.
     * @param end the end transform
     * @param alpha the amount to interpolate
     * @return [Transform] the resulting transform
     * @since 1.2.0
     */
    fun lerped(end: Transform, alpha: Double): Transform {
        // interpolate the position
        val x = x + alpha * (end.x - x)
        val y = y + alpha * (end.y - y)

        // compute the angle
        // get the start and end rotations
        // its key that these methods use atan2 because
        // it ensures that the angles are always within
        // the range -pi < theta < pi therefore no
        // normalization has to be done
        val rs = rotationAngle
        val re = end.rotationAngle
        // make sure we use the smallest rotation
        // as described in the comments above, there
        // are two possible rotations depending on the
        // direction, we always choose the smaller
        var diff = re - rs
        if (diff < -PI) diff += Geometry.TWO_PI
        if (diff > PI) diff -= Geometry.TWO_PI
        // interpolate
        // its ok if this method produces an angle
        // outside the range of -pi < theta < pi
        // since the rotate method uses sin and cos
        // which are not bounded
        val a = diff * alpha + rs

        // create the interpolated transform
        // the following performs the following calculations:
        // tx.rotate(a);
        // tx.translate(x, y);
        return Transform(cos(a), sin(a), x, y)
    }

    companion object {
        /**
         * NOTE: as of being deprecated this instance is no longer immutable.
         */
        @Deprecated("create your own instances of {@link Transform} instead; since 3.4.0")
        val IDENTITY = Transform()
    }
}