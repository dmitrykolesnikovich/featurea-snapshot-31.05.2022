package featurea.utils

fun String.toUrlAddress(): String = split("?")[0]

fun String.toUrlParametersOrNull(): Map<String, String>? {
    val addressAndQuery: List<String> = split("?")
    if (addressAndQuery.size != 2) return null
    val query: String = addressAndQuery[1]
    val tokens = query.split("&")
    val result = mutableMapOf<String, String>()
    for (token in tokens) {
        val keyAndValue = token.split("=")
        if (keyAndValue.size == 1) {
            val key = keyAndValue[0]
            result[key] = ""
        } else {
            val key = keyAndValue[0]
            val value = keyAndValue[1]
            result[key] = value
        }
    }
    return result
}

fun Map<String, String>.toUrlParametersString(): String = entries.joinToString("&") { "${it.key}=${it.value}" }

fun joinUrlPath(firstToken: String, restTokens: List<String>): String {
    var result = firstToken
    restTokens.forEach { result = "$result/$it" }
    return result
}
