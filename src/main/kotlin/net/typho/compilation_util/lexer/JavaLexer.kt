package net.typho.compilation_util.lexer

import net.typho.parsing_util.ParsingIterator
import javax.swing.event.DocumentEvent
import kotlin.math.max
import kotlin.math.min

open class JavaLexer {
    companion object {
        @JvmStatic
        fun Char.isJavaPunctuation() = !isWhitespace() && !isLetterOrDigit() && this != '"'

        private fun IntRange.intersects(other: IntRange) = first <= other.last && other.first <= last
    }

    @JvmField
    val keywords = mutableSetOf(
        "abstract",
        "assert",
        "boolean",
        "break",
        "byte",
        "case",
        "catch",
        "char",
        "class",
        "const",
        "continue",
        "default",
        "do",
        "double",
        "else",
        "enum",
        "extends",
        "final",
        "finally",
        "float",
        "for",
        "goto",
        "if",
        "implements",
        "import",
        "instanceof",
        "int",
        "interface",
        "long",
        "native",
        "new",
        "package",
        "private",
        "protected",
        "public",
        "return",
        "short",
        "static",
        "strictfp",
        "super",
        "switch",
        "synchronized",
        "this",
        "throw",
        "throws",
        "transient",
        "try",
        "void",
        "volatile",
        "while",
    )

    /**
     * No tokens will ever overlap, but they will not be snug.
     */
    @JvmField
    val tokens = mutableListOf<LexerToken>() // TODO turn into inline linked list

    data class UpdateResult(
        @JvmField
        val range: IntRange,
        @JvmField
        val newTokensInRange: List<LexerToken>
    )

    fun update(change: DocumentEvent): UpdateResult? {
        val offset = when (change.type) {
            DocumentEvent.EventType.INSERT -> change.length
            DocumentEvent.EventType.REMOVE -> -change.length
            else -> return null
        }

        val changeRange = change.offset..(change.offset + change.length)

        val firstTokenIndex = tokens.indexOfFirst { it.range.last >= changeRange.first }
        val lastTokenIndex = tokens.indexOfLast { it.range.first <= changeRange.last }

        if (firstTokenIndex == -1 || lastTokenIndex == -1) {
            return null
        }

        for (i in (lastTokenIndex + 1) until tokens.size) {
            tokens[i] = tokens[i].copyWithOffset(offset)
        }

        val subList = tokens.subList(firstTokenIndex, lastTokenIndex + 1)

        val range = if (offset > 0) {
            if (subList.isEmpty()) {
                changeRange
            } else {
                min(subList.first().range.first, changeRange.first)..max(subList.last().range.last + offset, changeRange.last)
            }
        } else {
            if (subList.isEmpty()) {
                return null
            }

            min(subList.first().range.first, changeRange.first)..min(subList.last().range.last, changeRange.last + offset)
        }
        subList.clear()
        process(change.document.getText(range.first, range.last - range.first), subList)

        subList.replaceAll { it.copyWithOffset(range.first) }

        return UpdateResult(range, subList)
    }

    @JvmOverloads
    fun process(input: String, output: MutableList<LexerToken> = this.tokens.also { it.clear() }): List<LexerToken> {
        val input = ParsingIterator(input)

        while (input.hasNext()) {
            input.skipUntil { index, char -> !char.isWhitespace() } ?: break

            if (input.peek().isJavaPunctuation()) { // Slashes are a special character
                if (input.currentMatches("//")) {
                    val startIndex1 = input.index
                    input.index += 2
                    val startIndex2 = input.index

                    input.skipUntil { index, char -> char == '\n' }

                    val endIndex = input.index

                    output.add(LexerToken.Comment(startIndex1..endIndex, startIndex2..endIndex))
                } else if (input.currentMatches("/*")) {
                    val startIndex1 = input.index
                    input.index += 2
                    val startIndex2 = input.index

                    input.skipUntil { index, char -> input.currentMatches("*/") }

                    val endIndex2 = input.index
                    input.index += 2
                    val endIndex1 = input.index

                    output.add(LexerToken.Comment(startIndex1..endIndex1, startIndex2..endIndex2, true))
                } else {
                    output.add(LexerToken.Punctuation(input.index))
                    input.next()
                }

                continue
            }

            if (input.peek().isDigit()) {
                val startIndex = input.index
                input.skipUntilOrEnd { index, char ->
                    val char = char.lowercaseChar() // grab everything that might be a number and deal with it later
                    !(char.isDigit() || char == 'x' || char == 'p' || char == 'L' || char == '.' || char == '-' || char == '+' || char == '_' || char in 'a'..'f')
                }
                val endIndex = input.index
                output.add(LexerToken.Number(startIndex..endIndex))

                continue
            }

            val fork = input.fork()
            val wordStart = fork.index
            val word = fork.takeUntilOrEnd { index, char -> char.isWhitespace() || char.isJavaPunctuation() }
            val wordEnd = fork.index

            if (word.isBlank()) {
                continue
            }

            when {
                word in keywords -> {
                    output.add(LexerToken.Keyword(wordStart..wordEnd))
                    fork.merge()
                }
                word[0] == '"' || word[0] == '\'' -> {
                    val quote = word[0]

                    if (input.currentMatches("\"\"\"")) {
                        val startIndex1 = input.index
                        input.index += 3
                        val startIndex2 = input.index

                        var escaped = false
                        input.skipUntil { index, char ->
                            if (char == '\\') {
                                escaped = true
                                false
                            } else if (escaped) {
                                escaped = false
                                false
                            } else {
                                input.currentMatches("\"\"\"")
                            }
                        } ?: output.add(LexerToken.Error(startIndex1..input.index, "Unterminated string")).also { break }

                        val endIndex2 = input.index
                        input.index += 3
                        val endIndex1 = input.index

                        output.add(LexerToken.Text(startIndex1..endIndex1, startIndex2..endIndex2, "\"\"\""))
                    } else {
                        val startIndex1 = input.index++
                        val startIndex2 = input.index

                        var escaped = false
                        input.skipUntil { index, char ->
                            if (char == '\n') {
                                output.add(LexerToken.Error(startIndex1..input.index, "Unterminated string"))
                                false
                            } else if (char == '\\') {
                                escaped = true
                                false
                            } else if (escaped) {
                                escaped = false
                                false
                            } else {
                                char == quote
                            }
                        } ?: output.add(LexerToken.Error(startIndex1..input.index, "Unterminated string")).also { break }

                        val endIndex2 = input.index++
                        val endIndex1 = input.index

                        output.add(LexerToken.Text(startIndex1..endIndex1, startIndex2..endIndex2, quote.toString()))
                    }
                }
                else -> {
                    output.add(LexerToken.Identifier(wordStart..wordEnd))
                    fork.merge()
                }
            }
        }

        return output
    }
}