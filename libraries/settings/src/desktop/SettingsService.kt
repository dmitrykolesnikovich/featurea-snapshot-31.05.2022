package featurea.settings

import featurea.desktop.Preferences
import featurea.runtime.Component
import featurea.runtime.Module

private val preferences: Preferences = Preferences("SettingsService")

actual class SettingsService actual constructor(override val module: Module) : Component {
    actual operator fun set(key: String, value: String) = preferences.edit { preferences[key] = value }
    actual operator fun set(key: String, value: Boolean) = preferences.edit { preferences[key] = value.toString() }
    actual operator fun get(key: String, default: String): String = preferences[key, default]
    actual fun getInt(key: String, default: Int): Int = preferences[key, default]
    actual operator fun get(key: String, default: Boolean): Boolean = preferences[key, default]
    actual fun containsKey(key: String): Boolean = preferences.containsKey(key)
}
