package featurea.utils

fun <K, V> effectiveMapOf(): EffectiveHashMap<K, V> = EffectiveHashMap()

data class EffectiveHashMapEntry<K, V>(val key: K, var mutableValue: V)

// https://stackoverflow.com/a/7941876/909169
class EffectiveHashMap<K, V> {

    val entries = mutableListOf<EffectiveHashMapEntry<K, V>>()
    val keys: Collection<K> get() = entries.map { it.key }
    val values: Collection<V> get() = entries.map { it.mutableValue }

    operator fun get(key: K): V? {
        val index: Int = indexOfKey(key)
        if (index == -1) return null
        return entries[index].mutableValue
    }

    operator fun set(key: K, value: V) {
        val index = indexOfKey(key)
        if (index == -1) {
            entries.add(EffectiveHashMapEntry(key, value))
        } else {
            entries[index].mutableValue = value
        }
    }

    inline fun getOrPut(key: K, defaultValue: () -> V): V {
        // 1. existing
        val existingValue: V? = get(key)
        if (existingValue != null) {
            return existingValue
        }

        // 2. newly created
        val answer: V = defaultValue()
        set(key, answer)
        return answer
    }

    fun filter(predicate: (entry: EffectiveHashMapEntry<K, V>) -> Boolean): EffectiveHashMap<K, V> {
        val result = EffectiveHashMap<K, V>()
        for (entry in entries) {
            if (predicate(entry)) {
                result.entries.add(entry)
            }
        }
        return result
    }

    /*internals*/

    private fun indexOfKey(key: K): Int {
        var counter: Int = -1
        for ((existingKey, _) in entries) {
            if (existingKey == key) {
                counter++
                return counter
            }
        }
        return counter
    }

}
