package featurea

actual val System.Companion.target get() = SystemTarget.IOS

actual val isPhoneOs: Boolean get() = TODO("Not implemented yet")

actual val isIphoneBrowser: Boolean = false
