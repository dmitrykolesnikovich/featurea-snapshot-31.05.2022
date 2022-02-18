package featurea.settings

import androidx.core.content.edit
import featurea.android.MainActivityProxy
import featurea.android.get
import featurea.android.sharedPreferences
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

actual class SettingsService actual constructor(override val module: Module) : Component {

    private val sharedPreferences = import(MainActivityProxy).sharedPreferences

    actual operator fun set(key: String, value: String) {
        sharedPreferences.edit {
            putString(key, value)
        }
    }

    actual operator fun set(key: String, value: Boolean) {
        sharedPreferences.edit {
            putBoolean(key, value)
        }
    }

    actual operator fun get(key: String, default: String): String {
        return sharedPreferences[key] ?: default
    }

    actual fun getInt(key: String, default: Int): Int {
        return sharedPreferences.getInt(key, default)
    }

    actual operator fun get(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    actual fun containsKey(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

}
