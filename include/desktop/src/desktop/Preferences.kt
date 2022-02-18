package featurea.desktop

import featurea.jvm.createNewFileAndDirs
import featurea.jvm.writeText
import featurea.parseProperties
import featurea.jvm.userHomePath
import java.io.File
import kotlin.reflect.KProperty

class Preferences : Iterable<Map.Entry<String, String>> {

    lateinit var name: String
        private set
    private lateinit var map: MutableMap<String, String>
    private lateinit var filePath: String
    val properties: MutableMap<String, String> get() = map

    fun initName(name: String) {
        this.name = name
        this.filePath = "$userHomePath/.featurea/preferences/${name}.properties"
        val file = filePath.createNewFileAndDirs()
        map = parseProperties(file.readText())
    }

    fun save(vararg properties: Pair<String, String>) {
        for ((key, value) in properties) {
            map[key] = value
        }
        map.writeText(File(filePath))
    }

    operator fun get(key: String): String? = map[key]
    operator fun get(key: String, defaultValue: String): String = map[key] ?: defaultValue
    operator fun get(key: String, defaultValue: Int): Int = map[key]?.toInt() ?: defaultValue
    operator fun get(key: String, defaultValue: Boolean): Boolean = map[key]?.toBoolean() ?: defaultValue
    operator fun get(key: String, defaultValue: Double): Double = map[key]?.toDouble() ?: defaultValue
    operator fun get(key: String, defaultValue: Float): Float = map[key]?.toFloat() ?: defaultValue

    operator fun set(key: String, value: String) {
        map[key] = value
    }

    override fun iterator(): Iterator<MutableMap.MutableEntry<String, String>> = map.iterator()

    fun edit(action: Preferences.() -> Unit) {
        action()
        map.writeText(File(filePath))
    }

    fun containsKey(key: String): Boolean = map.containsKey(key)

}

fun Preferences(name: String): Preferences = PreferencesCache.getPreferences(name)

class PreferencesDelegate(val key: String) {

    operator fun getValue(preferences: Preferences, property: KProperty<*>): String {
        return preferences[key, ""]
    }

    operator fun setValue(preferences: Preferences, property: KProperty<*>, value: String) {
        preferences[key] = value
    }

}
