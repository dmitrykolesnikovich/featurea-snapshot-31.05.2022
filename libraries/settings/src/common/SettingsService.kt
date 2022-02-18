package featurea.settings

import featurea.runtime.Component
import featurea.runtime.Module

expect class SettingsService(module: Module) : Component {
    operator fun set(key: String, value: String)
    operator fun set(key: String, value: Boolean)
    operator fun get(key: String, default: String): String
    fun getInt(key: String, default: Int): Int
    operator fun get(key: String, default: Boolean): Boolean
    fun containsKey(key: String): Boolean
}
