@file:JvmName("TargetOsUtils")

package featurea.utils

import java.lang.System as JvmSystem
import featurea.System

actual val System.Companion.target get() = SystemTarget.DESKTOP
actual val isPhoneOs: Boolean get() = TODO("Not implemented yet")
actual val isIphoneBrowser: Boolean = false

