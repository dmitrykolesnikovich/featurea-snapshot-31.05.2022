@file:JvmName("Numbers")

package featurea

import featurea.jvm.toDoubleString as toDoubleStringJvm

actual fun Double.toDoubleString(): String = toDoubleStringJvm()
