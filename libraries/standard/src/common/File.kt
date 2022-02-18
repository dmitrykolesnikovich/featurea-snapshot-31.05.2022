package featurea

expect fun existsFile(filePath: String): Boolean

expect fun System.existsFile(filePath: String): Boolean

expect fun System.findAbsolutePathOrNull(filePath: String): String?

expect suspend fun System.readTextOrNull(filePath: String, limit: Int = -1): String?

val String.parent: String
    get() {
        val lastIndex = lastIndexOf("/")
        return if (lastIndex == -1) {
            this
        } else {
            substring(0 until lastIndex)
        }
    }

val String.name: String
    get() {
        val lastIndex = lastIndexOf("/")
        return if (lastIndex == -1) {
            this
        } else {
            substring(lastIndex + 1)
        }
    }

fun String.withExtension(fileExtension: String) = if (endsWith(".${fileExtension}")) {
    this
} else {
    val dotIndex = lastIndexOf(".")
    if (dotIndex == -1) {
        "${this}.${fileExtension}"
    } else {
        "${pathWithoutExtension}.${fileExtension}"
    }
}

fun String.hasExtension(vararg extensions: String): Boolean {
    return hasExtension(extensions.asIterable())
}

fun String.hasExtension(extensions: Iterable<String>): Boolean {
    for (extension in extensions) {
        if (endsWith(".${extension}")) {
            return true
        }
    }
    return false
}

val String.pathWithoutExtension get() = substringBeforeLast(".")

val String.nameWithoutExtension get() = substringBeforeLast(".").substringAfterLast("/")

val String.extension: String get() = substringAfterLast(".").toLowerCase()

val String.normalizedPath: String get() = replace("\\", "/").replace(":", "")

fun String.truncatePath(maxCount: Int, truncateSymbol: String = ".../"): String {
    var result: String = ""
    var currentLength: Int = 0

    val tokens = normalizedPath.splitAndTrim("/")
    for (token in tokens.reversed()) {
        if (currentLength + token.length >= maxCount) break

        result = "$token/$result"
        currentLength += token.length
    }

    return "$truncateSymbol${result.removeSuffix("/")}"
}

/*
path=locationPath:idPath
path=/Users/dmitrykolesnikovich/workspace/Test.rml:/id1/id2/id3
locationPath=/Users/dmitrykolesnikovich/workspace/Test.rml
idPath=/id1/id2/id3
*/
fun String.toFilePath(): String {
    val tokens = splitAndTrim(":")
    val filePath = if (tokens.size == 1) {
        split(":")[0] // ~/workspace/project: -> ~/workspace/project
    } else if (tokens.size == 2) {
        if (tokens[0].length != 1) {
            tokens[0] // ~/workspace/project:featurea.project -> ~/workspace/project
        } else {
            this // D:/workspace/project -> D:/workspace/project
        }
    } else {
        tokens[0] + ":" + tokens[1] // D:/workspace/project:featurea.project -> D:/workspace/project
    }
    return filePath.removeSuffix("/") // ~/workspace/test.mdb/ -> ~/workspace/test.mdb
}

fun String.toIdPath(): String? {
    if (startsWith(":")) return removePrefix(":") // quickfix todo improve
    val tokens = splitAndTrim(":")
    if (tokens.size == 1) {
        return null // ~/workspace/project: -> null
    } else if (tokens.size == 2) {
        if (tokens[0].length != 1) {
            return tokens[1] // ~/workspace/project:featurea.project -> featurea.project
        } else {
            return null // D:/workspace/project -> null
        }
    } else {
        return tokens[2] // D:/workspace/project:featurea.project -> featurea.project
    }
}

fun String.toConfigPath(): String = when {
    this == "" -> "config"
    else -> "${toPath()}/config"
}

fun String.toPath() = replace(".", "/")

fun String.toPackage() = removePrefix("/").removeSuffix("/").replace("/", ".")

fun String.isValidFilePath() = !contains(",") // quickfix todo improve
