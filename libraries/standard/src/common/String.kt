package featurea

import kotlin.text.startsWith as startsWithSingle

typealias StringBlock = (String) -> Unit

const val emptyString: String = ""

fun String?.equals(vararg strings: String): Boolean {
    if (this == null) return false
    return strings.any { it == this }
}

fun String.ensureLength(length: Int, trailingChar: String): String {
    val delta: Int = length - this.length
    if (delta <= 0) return this
    val prefix: String = trailingChar.repeat(delta)
    return "$prefix$this"
}

fun List<String>.joinToStringWithQuotes(): String {
    return joinToString(" ") { "\"$it\"" }
}

fun String.replaceVariables(vararg tokens: String): String {
    var result: String = this
    for (index in tokens.indices step 2) {
        val variable: String = tokens[index]
        val replacement: String = tokens[index + 1]
        result = result.replaceVariable(variable, replacement)
    }
    return result
}

fun String.replaceVariable(variable: String, replacement: String?): String {
    if (replacement == null) return this
    return replace("\\$\\{${variable}\\}".toRegex(), replacement) // IntelliJ consider '\\' redundant but it's not
}

fun String.firstLine(): String {
    val index = indexOf("\n")
    if (index == -1) return this
    else return substring(0, index)
}

fun String.isWrapped(vararg wrappers: String): Boolean {
    for (wrapper in wrappers) {
        if (startsWith(wrapper) && endsWith(wrapper)) {
            return true
        }
    }
    return false
}

fun String.startsWith(vararg prefixes: String): Boolean = startsWith(prefixes.asIterable())

fun String.startsWith(prefixes: Iterable<String>): Boolean {
    prefixes.forEach { if (startsWithSingle(it, true)) return true }
    return false
}

fun String.endsWith(vararg suffixes: String): Boolean = endsWith(suffixes.asIterable())

fun String.endsWith(suffixes: Iterable<String>): Boolean {
    suffixes.forEach { if (endsWith(it, true)) return true }
    return false
}

fun StringBuilder.consumeString(): String {
    val string = toString()
    clear()
    return string
}

fun String.ensureSuffix(suffix: String): String {
    if (!endsWith(suffix)) return plus(suffix)
    return this
}

fun String.appendSuffix(suffix: CharSequence): String {
    return plus(suffix)
}

fun String.replaceSuffix(oldSuffix: String, newSuffix: String): String {
    // quickfix todo revert somehow
    // >>
    /*check(endsWith(oldSuffix))*/
    if (!endsWith(oldSuffix)) return this
    // <<

    val withoutOldSuffix = substring(0, length - oldSuffix.length)
    val result = "${withoutOldSuffix}${newSuffix}"
    return result
}

fun String.substrings(vararg ranges: IntRange): Array<String> = Array(ranges.size) { substring(ranges[it]) }

fun String.toUpperCaseFirst(count: Int = 1): String {
    val firstToken = substring(0, count)
    val secondToken = substring(count)
    val result = firstToken.toUpperCase() + secondToken
    return result
}

// quickfix todo improve
fun String.splitLines(count: Int = 1): List<String> {
    val lineSeparator = if (contains("\r")) "\r\n" else "\n"
    val delimiter = StringBuilder().apply {
        repeat(count) { append(lineSeparator) }
    }.toString()
    return trim().split(delimiter)
}

/*
given: this="a/b/c/d/e", delimiter="/", maxTokenCount=4
result: "a", "b", "c", "d/e"
1. before first delimiter:   "a"
2. after first delimiter:    "b", "c"
3. on break:                 "d/e"
*/
fun String.splitAndTrim(delimiter: String, limit: Int = 0): MutableList<String> {
    if (limit < 0) error("limit: $limit")
    if (isBlank()) return mutableListOf()
    if (limit == 1) return arrayListOf(this)

    val shouldBreak = limit != 0
    /*result*/
    val trimmed = trim()
    val result = mutableListOf<String>()
    val firstIndexOfDelimiterPattern = trimmed.indexOf(delimiter)
    // 0. delimiter does not exist
    if (firstIndexOfDelimiterPattern == -1) {
        return arrayListOf(this)
    }
    // 1. before first delimiter
    if (firstIndexOfDelimiterPattern != 0) {
        result.add(trimmed.substring(0 until firstIndexOfDelimiterPattern))
    }
    val regex = delimiter.delimiterToRegex()
    for (matchResult in regex.findAll(trimmed)) {
        if (shouldBreak && result.size >= limit - 1) {
            // 3. on break
            val lastToken = trimmed.substring(matchResult.range.first).removePrefix(delimiter).trim()
            if (lastToken.isNotEmpty()) {
                result.add(lastToken)
            }
            break
        } else {
            // 2. after first delimiter
            val nextToken = matchResult.value.removePrefix(delimiter).trim()
            if (nextToken.isNotEmpty()) {
                result.add(nextToken)
            }
        }
    }
    return result
}

/*internals*/

private val String.isRegexSymbol get() = ".?+*^$|".contains(this)

private fun String.delimiterToRegex(): Regex {
    val symbol = if (this == "..") {
        "\\.\\." // quickfix todo improve
    } else if (isRegexSymbol) {
        "\\$this"
    } else {
        this
    }
    return "$symbol[^$symbol]*".toRegex()
}
