@file:JvmName("TargetOsUtils")

package featurea

import java.lang.System as JvmSystem

actual val System.Companion.target get() = SystemTarget.DESKTOP
actual val isPhoneOs: Boolean get() = TODO("Not implemented yet")
actual val isIphoneBrowser: Boolean = false

