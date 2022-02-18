package featurea.settings

import featurea.runtime.Component
import featurea.runtime.Module
import platform.Foundation.NSUserDefaults

actual class SettingsService actual constructor(override val module: Module) : Component {
    private val prefs = NSUserDefaults.standardUserDefaults
    actual operator fun set(key: String, value: String): Unit = prefs.setObject(value, key)
    actual operator fun set(key: String, value: Boolean): Unit = prefs.setBool(value, key)
    actual operator fun get(key: String, default: String): String = prefs.objectForKey(key) as String? ?: default
    actual fun getInt(key: String, default: Int): Int = prefs.objectForKey(key) as Int? ?: default
    actual operator fun get(key: String, default: Boolean): Boolean = prefs.objectForKey(key) as Boolean? ?: default
    actual fun containsKey(key: String): Boolean = prefs.objectForKey(key) != null
}
