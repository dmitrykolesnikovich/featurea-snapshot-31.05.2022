@file:JvmName("TargetOsUtils")

package featurea.utils

import featurea.System

actual val System.Companion.target get() = SystemTarget.ANDROID

actual val isPhoneOs: Boolean get() = TODO("Not implemented yet")

actual val isIphoneBrowser: Boolean = false