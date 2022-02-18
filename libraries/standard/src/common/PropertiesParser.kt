package featurea

fun parseProperties(source: String, delimiter: String = "="): MutableMap<String, String> {
    val result = mutableMapOf<String, String>()
    var lineCounter = 1
    val lines = source.lines()
    var key = ""
    var value = ""
    for (line in lines) {
        if (line.isBlank() || line.startsWith("#")) continue
        try {
            val indexOfDelimiter = line.indexOf(delimiter)
            if (indexOfDelimiter == -1) {
                if (key.isEmpty()) {
                    error("line: $line")
                } else {
                    value += line.trim()
                }
            } else {
                val (lineKey, lineValue) = Pair(line.substring(0, indexOfDelimiter), line.substring(indexOfDelimiter + 1))
                key = lineKey.trim()
                value += lineValue.trim()
            }
            if (value.endsWith("\\")) {
                value = value.substring(0, value.length - 1)
            } else {
                result[key] = value
                key = ""
                value = ""
            }
        } catch (e: IndexOutOfBoundsException) {
            println("$lineCounter: $line")
            println(e.message)
        }
        lineCounter++
    }
    return result
}
