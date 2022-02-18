package featurea.math

import kotlin.math.sqrt

typealias Point = Vector2

data class Vector2(var x: Float = 0f, var y: Float = 0f) {

    val isEmpty: Boolean get() = x == 0f && y == 0f
    val isNotEmpty: Boolean get() = !isEmpty

    constructor(original: Vector2) : this(original.x, original.y)

    constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())

    infix fun assign(original: Point): Vector2 {
        return assign(original.x, original.y)
    }

    fun assign(x: Float, y: Float): Vector2 {
        this.x = x
        this.y = y
        return this
    }

    fun invert() {
        x *= -1
        y *= -1
    }

    operator fun times(scalar: Float): Vector2 = Vector2(x * scalar, y * scalar)

    fun div(w: Float, h: Float): Vector2 {
        x /= w
        y /= h
        return this
    }

    fun move(dx: Float, dy: Float): Vector2 {
        this.x += dx
        this.y += dy
        return this
    }

    fun move(step: Vector2): Vector2 {
        return move(step.x, step.y)
    }

    fun scale(x: Float, y: Float, ox: Float, oy: Float, ratio: Float) {
        var dx = x - ox
        var dy = y - oy
        dx *= ratio
        dy *= ratio
        this.x = ox + dx
        this.y = oy + dy
    }

    fun assignSum(point1: Vector2, point2: Vector2) {
        this.x = point1.x + point2.x
        this.y = point1.y + point2.y
    }

    fun assignDiff(point1: Vector2, point2: Vector2) {
        this.x = point1.x - point2.x
        this.y = point1.y - point2.y
    }

    fun assignRevert(original: Vector2) {
        this.x = -original.x
        this.y = -original.y
    }

    fun revert(): Vector2 = Vector2(-x, -y)

    operator fun plusAssign(other: Vector2) {
        x += other.x
        y += other.y
    }

    fun clear() {
        this.x = 0f
        this.y = 0f
    }

    fun normalize(): Vector2 {
        val len2: Float = len2()
        if (len2 != 0f && len2 != 1f) {
            val sqrt: Float = sqrt(len2)
            x /= sqrt
            y /= sqrt
        }
        return this
    }

    fun len2(): Float = x * x + y * y

    @OptIn(ExperimentalPrecision::class)
    fun toPixelPerfect(result: Vector2.Result): Vector2.Result {
        return result.apply(x.toPixelPerfectFloat(), y.toPixelPerfectFloat())
    }

    inner class Result {

        operator fun component1() = x
        operator fun component2() = y

        fun apply(x: Float, y: Float): Result = apply {
            this@Vector2.x = x
            this@Vector2.y = y
        }

        operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)

    }

    companion object {

        fun rotate(result: Result, x: Float, y: Float, ox: Float, oy: Float, angle: Angle): Result {
            val cos = angle.cos
            val sin = angle.sin * -1
            result.apply(
                ox + ((x - ox) * cos - (y - oy) * sin).toFloat(),
                oy + ((x - ox) * sin + (y - oy) * cos).toFloat()
            )
            return result
        }

        fun sum(p1: Vector2, p2: Vector2, result: Vector2): Vector2 {
            result.assign(p1.x + p2.x, p1.y + p2.y)
            return result
        }

    }

}

fun String.toPoint(): Vector2 = Vector2().assign(this)

fun Vector2.assign(value: String): Vector2 = apply {
    val (x, y) = value.split(",").map { it.trim().toFloat() }
    this.x = x
    this.y = y

}

@ExperimentalPrecision
fun Vector2.toPixelPerfectFloat(result: Vector2): Vector2 {
    val xp: Float = x.toPixelPerfectFloat()
    val yp: Float = y.toPixelPerfectFloat()
    result.assign(xp, yp)
    return result
}
