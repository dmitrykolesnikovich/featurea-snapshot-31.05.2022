package featurea

// quickfix todo replace to Standard Library
class Properties : Iterable<Map.Entry<String, Any?>> {

    val map = linkedMapOf<String, Any?>() // it is not private because `get` operator is inline todo make private

    constructor() {
        // no op
    }

    constructor(original: Map<String, Any?>) {
        putAll(original)
    }

    constructor(vararg original: Pair<String, Any?>) {
        putAll(*original)
    }

    fun putAll(original: Map<String, Any?>) {
        original.toMap(map)
    }

    fun putAll(vararg original: Pair<String, Any?>) {
        original.toMap(map)
    }

    override fun iterator(): Iterator<Map.Entry<String, Any?>> {
        return map.iterator()
    }

    inline operator fun <reified T : Any?> get(key: String): T? {
        val value = map[key] ?: return null
        return when (T::class) {
            Boolean::class -> value.toString().toBoolean() as T
            Byte::class -> value.toString().toByte() as T
            Int::class -> value.toString().toInt() as T
            Long::class -> value.toString().toLong() as T
            Float::class -> value.toString().toFloat() as T
            Double::class -> value.toString().toDouble() as T
            String::class -> value as T
            List::class -> {
                when (value) {
                    is List<*> -> value as T
                    is String -> value.split(", ") as T
                    else -> error("key: $key, value: $value")
                }
            }
            else -> error("key: $key, value: $value")
        }
    }

    operator fun set(key: String, value: Any?) {
        map[key] = value
    }


    fun remove(key: String): Any? {
        return map.remove(key)
    }

}
