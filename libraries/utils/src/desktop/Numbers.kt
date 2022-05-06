@file:JvmName("Numbers")

package featurea.utils

import featurea.jvm.toDoubleString as toDoubleStringJvm

actual fun Double.toDoubleString(): String = toDoubleStringJvm()
