package net.typho.compilation_util.lexer

import net.typho.parsing_util.ParsingIterator
import net.typho.parsing_util.error.DataReadException

fun ParsingIterator<LexerToken>.readBrackets(open: Char, close: Char, name: String): List<LexerToken> {
    var depth = 1
    return nextUntil { index, value ->
        if (value is LexerToken.Punctuation) {
            when (value.char) {
                open -> depth++
                close -> depth--
            }
        }

        depth == 0
    } ?: throw DataReadException("Unterminated $name")
}

fun ParsingIterator<LexerToken>.nextUntilOrEndConsideringBrackets(char: Char, name: String): List<LexerToken> {
    var depth = 0
    return nextUntilOrEnd { index, value ->
        if (value is LexerToken.Punctuation) {
            when (value.char) {
                '{', '[', '(', '<' -> depth++
                '}', ']', ')', '>' -> depth--
                char -> if (depth == 0) {
                    return@nextUntilOrEnd true
                }
            }

            if (depth < 0) {
                throw DataReadException("Unterminated $name")
            }
        }

        false
    }
}