@file:OptIn(ExperimentalUnsignedTypes::class)

package featurea.ios

import kotlinx.cinterop.*
import platform.glescommon.GLboolean

// https://stackoverflow.com/a/53242002/909169
fun Int.toCOpaque(): CPointer<*>? = toLong().toCPointer<COpaque>()

inline fun <reified T : CVariable> MemScope.kValueOf(cValue: CValue<T>): T = TODO()

inline fun <reified T : CVariable> kValueOf(cValue: CValue<T>): T = memScoped { kValueOf(cValue) }

inline val <reified T : CVariable> CValue<T>.value: T
    get() {
        val cValue: CValue<T> = this
        return memScoped { cValue.getPointer(memScope).pointed }
    }

fun Boolean.toGLboolean(): GLboolean = toByte().toUByte()
