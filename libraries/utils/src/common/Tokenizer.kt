package featurea

import featurea.utils.Stack

private typealias Token = StringBuilder

private typealias Tokens = ArrayList<String>

fun String.splitWithWrappers(delimiter: Char): List<String> {
    return Tokenizer.tokenize(this, delimiter)
}

object Tokenizer {

    private val openWrappers = listOf('(', '"', '\'')
    private val closeWrappers = listOf(')', '"', '\'')

    fun tokenize(string: String, delimiter: Char): List<String> {
        val tokens = Tokens()
        val token = Token()
        val wrappers = Stack<Char>()
        for (char in string) {
            nextChar(tokens, char, delimiter, wrappers, token)
        }
        nextChar(tokens, delimiter, delimiter, wrappers, token)
        return tokens
    }

    /*internals*/

    private fun isWrappersEmptyAfterNextChar(wrappers: Stack<Char>, nextChar: Char): Boolean {
        if (!openWrappers.contains(nextChar) && !closeWrappers.contains(nextChar)) {
            // no op
        } else if (wrappers.isEmpty()) {
            if (openWrappers.contains(nextChar)) {
                wrappers.push(nextChar)
            } else {
                throw IllegalArgumentException("Close wrapper character has no open one")
            }
        } else {
            val wrapper = wrappers.last()
            if (wrapper == '"' && nextChar == '"') {
                wrappers.pop()
            } else if (wrapper == '\'' && nextChar == '\'') {
                wrappers.pop()
            } else if (wrapper == '(' && nextChar == ')') {
                wrappers.pop()
            } else if (wrapper == '(') {
                wrappers.push(nextChar)
            }
        }
        return wrappers.isEmpty()
    }

    private fun nextToken(char: Char, delimiter: Char, wrappers: Stack<Char>, token: Token): Boolean {
        if (isWrappersEmptyAfterNextChar(wrappers, char) && char == delimiter) {
            return true
        } else {
            token.append(char)
            return false
        }
    }

    private fun nextChar(tokens: Tokens, char: Char, delimiter: Char, wrappers: Stack<Char>, token: Token) {
        if (nextToken(char, delimiter, wrappers, token)) {
            if (token.isNotEmpty()) {
                tokens.add(token.toString().trim())
                token.clear()
            }
        }
    }

}
