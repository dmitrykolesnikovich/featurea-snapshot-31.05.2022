package featurea

import kotlinx.browser.window

actual val System.Companion.target get() = SystemTarget.JS

actual val isPhoneOs: Boolean = window.navigator.userAgent.toLowerCase().let {
    (it.contains("mobi") && !it.contains("tab")) && (it.contains("ios") || it.contains("android"))
}

actual val isIphoneBrowser: Boolean = window.navigator.userAgent.toLowerCase().contains("iphone")
