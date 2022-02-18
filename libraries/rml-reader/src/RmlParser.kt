package featurea.rml.reader

import featurea.consumeString
import featurea.content.ResourceTag
import featurea.content.UNDEFINED_RESOURCE_PATH

class RmlParserException(filePath: String) : RuntimeException(filePath)

object RmlParser {

    fun parseRmlSource(rmlSource: String, filePath: String = UNDEFINED_RESOURCE_PATH): ResourceTag {
        var rootRmlTag: ResourceTag? = null
        var currentRmlTag: ResourceTag? = null
        var attributeKey: String? = null
        val state = RmlParserState()
        val charArray = rmlSource.toCharArray()
        for (char in charArray) {
            /*print("$char")*/
            if (char == '<' && state.isDefault()) {
                state.toTagOpen()
            } else if (state.isTagOpen && (char == ' ' || char == '>')) {
                val tagName = state.toTagName(char)
                val parentRmlTag = currentRmlTag
                if (parentRmlTag != null) {
                    currentRmlTag = ResourceTag(tagName, parent = parentRmlTag)
                    parentRmlTag.children.add(currentRmlTag)
                } else {
                    currentRmlTag = ResourceTag(tagName, filePath = filePath)
                    rootRmlTag = currentRmlTag
                }
            } else if (char == '"' && state.isAttributeKey) {
                attributeKey = state.toAttributeValue()
            } else if (char == '"' && state.isAttributeValue) {
                if (currentRmlTag == null || attributeKey == null) {
                    throw RmlParserException(filePath)
                }
                val attributeValue = state.toAttributeKey()
                currentRmlTag.attributes[attributeKey] = attributeValue
                if (attributeKey == "id") currentRmlTag.parent!!.properties[attributeValue] = currentRmlTag
                attributeKey = null
            } else if (char == '/' && !state.isAttributeValue) {
                if (currentRmlTag == null) {
                    throw RmlParserException(filePath)
                }
                state.toDefault()
                currentRmlTag = currentRmlTag.parent
            } else if (char == '>' && !state.isAttributeValue) {
                state.toDefault()
            } else {
                state.appendChar(char)
            }
        }
        check(rootRmlTag != null) { filePath ?: "" }
        return rootRmlTag
    }

}

private class RmlParserState {

    var isTagOpen: Boolean = false
    var isAttributeKey: Boolean = false
    var isAttributeValue: Boolean = false
    var useBackSlashChar: Boolean = false
    private val currentToken = StringBuilder()

    fun toTagOpen(): String {
        isTagOpen = true
        isAttributeKey = false
        isAttributeValue = false
        return currentToken.consumeString()
    }

    fun toTagName(char: Char): String {
        return when (char) {
            '>' -> toDefault()
            ' ' -> toAttributeKey()
            else -> error("char: $char")
        }
    }

    fun toAttributeKey(): String {
        isTagOpen = false
        isAttributeKey = true
        isAttributeValue = false
        return currentToken.consumeString()
    }

    fun toAttributeValue(): String {
        isTagOpen = false
        isAttributeKey = false
        isAttributeValue = true
        return currentToken.consumeString()
    }

    fun toDefault(): String {
        isTagOpen = false
        isAttributeKey = false
        isAttributeValue = false
        return currentToken.consumeString()
    }

    fun isDefault(): Boolean {
        return !isTagOpen && !isAttributeKey && !isAttributeValue
    }

    fun appendChar(char: Char) {
        // update
        val shouldAppendChar = !useBackSlashChar
        useBackSlashChar = isAttributeValue && char == '\\'

        // filter
        if (currentToken.isEmpty() && char.isWhitespace() && !isAttributeValue) return // 1. trim everything except attribute
        if (shouldAppendChar && useBackSlashChar) return                               // 2. skip first back slash
        if (isTagOpen && char == ' ') return                                           // 3. skip tag open delimiter
        if (isAttributeKey && char == '=') return                                      // 4. skip attribute key delimiter
        if (isAttributeValue && char == '"') return                                    // 5. skip attribute value delimiter todo delete

        // append
        if (shouldAppendChar) {
            currentToken.append(char)
        } else {
            when (char) {
                'n' -> currentToken.appendLine()
                else -> currentToken.append('\\').append(char)
            }
        }
        useBackSlashChar = false
    }

}
