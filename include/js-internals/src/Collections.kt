package featurea.js

fun <K, V> dynamicMapOf(vararg pairs: Pair<K, V>): dynamic {
    val arguments: dynamic = js("({})")
    for ((key, value) in pairs) {
        arguments[key] = value
    }
    return arguments
}
