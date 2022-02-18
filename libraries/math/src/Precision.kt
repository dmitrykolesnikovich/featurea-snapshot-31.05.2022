package featurea.math

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.math.pow

@Suppress("DEPRECATION")
@Experimental(level = Experimental.Level.ERROR)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Target(CLASS, FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalPrecision

/*
GLSL language for OpenGL ES introduces concept of precision

https://asawicki.info/news_1596_watch_out_for_reduced_precision_normalizelength_in_opengl_es.html

highp basically means normal, single-precision, 32-bit float (IEEE 754), as we know it from CPU programming.
mediump is said to have have range of at least -2^14 ... 2^14 and relative precision 2^-10, so it can be, for example, implemented using a 16-bit, half-precision float.
lowp is said to have range at least -2 ... 2 and absolute precision 2^-8, so basically it can be stored as a 10-bit, fixed-point number.

*/

val epsilonMediumPrecision: Float = 2f.pow(-10)
val Float.prevIntMediumPrecision: Int get() = (this - epsilonMediumPrecision).toInt()
val Float.nextIntMediumPrecision: Int get() = prevIntMediumPrecision + 1

@ExperimentalPrecision
fun Float.toPixelPerfectFloat(): Float = prevIntMediumPrecision.toFloat() + 0.5f

enum class Precision {
    MEDIUM,
}
