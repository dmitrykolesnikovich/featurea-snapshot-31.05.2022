package featurea.ktor

import featurea.splitAndTrim
import io.ktor.http.*

inline fun <reified T> Parameters.findRequired(key: String): T {
    val value: String = getAll(key)!!.firstOrNull()!!
    val type = T::class
    if (type is List<*>) return value.splitAndTrim(",") as T
    return when (type) {
        Double::class -> value.toDouble()
        Int::class -> value.toInt()
        Long::class -> value.toLong()
        Float::class -> value.toFloat()
        String::class -> value
        Boolean::class -> value.toBoolean()
        else -> error("key: $key, value: $value, type: $type")
    } as T
}
