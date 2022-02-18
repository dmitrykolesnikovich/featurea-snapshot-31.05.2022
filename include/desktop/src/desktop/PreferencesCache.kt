package featurea.desktop

object PreferencesCache {

    private val map = mutableMapOf<String, Preferences>()

    fun getPreferences(name: String): Preferences {
        val preferences = map[name]
        if (preferences != null) {
            return preferences
        } else {
            val newPreferences = Preferences().apply { initName(name) }
            map[name] = newPreferences
            return newPreferences
        }
    }

}
