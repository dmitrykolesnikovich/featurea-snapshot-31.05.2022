package featurea.settings

import featurea.runtime.Component
import featurea.runtime.Module

actual class SettingsService actual constructor(override val module: Module) : Component {
    actual operator fun set(key: String, value: String) {}
    actual operator fun set(key: String, value: Boolean) {}
    actual operator fun get(key: String, default: String): String = default
    actual fun getInt(key: String, default: Int): Int = -1
    actual operator fun get(key: String, default: Boolean): Boolean = false
    actual fun containsKey(key: String): Boolean = false
}
