package net.typho.compilation_util

import net.typho.parsing_util.ParsingIterator
import kotlin.io.path.Path
import kotlin.io.path.readText

object JavaCompilerPrototype2 {
    sealed interface Token {
        val range: IntRange

        fun toString(text: String): String {
            return text.substring(range.first, range.last)
        }
    }

    data class Identifier(
        override val range: IntRange
    ) : Token

    data class Text(
        override val range: IntRange,
        @JvmField
        val contentsRange: IntRange,
        @JvmField
        val multiline: Boolean = false
    ) : Token

    data class Comment(
        override val range: IntRange,
        @JvmField
        val contentsRange: IntRange,
        @JvmField
        val inline: Boolean = false
    ) : Token

    data class Number(
        override val range: IntRange
    ) : Token

    enum class Keyword {
        ABSTRACT,
        ASSERT,
        BOOLEAN,
        BREAK,
        BYTE,
        CASE,
        CATCH,
        CHAR,
        CLASS,
        CONST,
        CONTINUE,
        DEFAULT,
        DO,
        DOUBLE,
        ELSE,
        ENUM,
        EXTENDS,
        FINAL,
        FINALLY,
        FLOAT,
        FOR,
        GOTO,
        IF,
        IMPLEMENTS,
        IMPORT,
        INSTANCEOF,
        INT,
        INTERFACE,
        LONG,
        NATIVE,
        NEW,
        PACKAGE,
        PRIVATE,
        PROTECTED,
        PUBLIC,
        RETURN,
        SHORT,
        STATIC,
        STRICTFP,
        SUPER,
        SWITCH,
        SYNCHRONIZED,
        THIS,
        THROW,
        THROWS,
        TRANSIENT,
        TRY,
        VOID,
        VOLATILE,
        WHILE,
    }

    data class KeywordToken(
        override val range: IntRange,
        @JvmField
        val keyword: Keyword
    ) : Token

    data class Punctuation(
        @JvmField
        val index: Int
    ) : Token {
        override val range: IntRange
            get() = index..index + 1
    }

    private fun Char.isSpecial() = !isWhitespace() && !isLetterOrDigit() && this != '"'

    @JvmStatic
    fun process(original: String): List<Token> {
        val input = ParsingIterator(original)
        val keywords = Keyword.entries.map { it.name.lowercase() }
        val tokens = mutableListOf<Token>()

        while (input.hasNext()) {
            input.skipUntil { index, char -> !char.isWhitespace() } ?: break

            if (input.peek().isSpecial()) { // Slashes are a special character
                if (input.currentMatches("//")) {
                    val startIndex1 = input.index
                    input.index += 2
                    val startIndex2 = input.index

                    input.skipUntil { index, char -> char == '\n' }

                    val endIndex = input.index

                    tokens.add(Comment(startIndex1..endIndex, startIndex2..endIndex))
                } else if (input.currentMatches("/*")) {
                    val startIndex1 = input.index
                    input.index += 2
                    val startIndex2 = input.index

                    input.skipUntil { index, char -> input.currentMatches("*/") }

                    val endIndex2 = input.index
                    input.index += 2
                    val endIndex1 = input.index

                    tokens.add(Comment(startIndex1..endIndex1, startIndex2..endIndex2, true))
                }

                tokens.add(Punctuation(input.index))
                input.next()

                continue
            }

            if (input.peek().isDigit()) {
                val startIndex = input.index
                input.skipUntilOrEnd { index, char ->
                    val char = char.lowercaseChar() // grab everything that might be a number and deal with it later
                    !(char.isDigit() || char == 'x' || char == 'p' || char == 'L' || char == '.' || char == '-' || char == '+' || char == '_' || char in 'a'..'f')
                }
                val endIndex = input.index
                tokens.add(Number(startIndex..endIndex))

                continue
            }

            val fork = input.fork()
            val wordStart = fork.index
            val word = fork.takeUntilOrEnd { index, char -> char.isWhitespace() || char.isSpecial() }
            val wordEnd = fork.index

            if (word.isBlank()) {
                continue
            }

            if (word in keywords) {
                tokens.add(KeywordToken(wordStart..wordEnd, Keyword.valueOf(word.uppercase())))
                fork.merge()
            } else if (word[0] == '"') {
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
                    } ?: TODO()

                    val endIndex2 = input.index
                    input.index += 3
                    val endIndex1 = input.index

                    tokens.add(Text(startIndex1..endIndex1, startIndex2..endIndex2, true))
                } else {
                    val startIndex1 = input.index++
                    val startIndex2 = input.index

                    var escaped = false
                    input.skipUntil { index, char ->
                        if (char == '\n') {
                            TODO("End of line before closing quote $quote")
                        } else if (char == '\\') {
                            escaped = true
                            false
                        } else if (escaped) {
                            escaped = false
                            false
                        } else {
                            char == quote
                        }
                    } ?: TODO()

                    val endIndex2 = input.index++
                    val endIndex1 = input.index

                    tokens.add(Text(startIndex1..endIndex1, startIndex2..endIndex2))
                }
            } else {
                tokens.add(Identifier(wordStart..wordEnd))
                fork.merge()
            }
        }

        return tokens
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val original = Path("tests/JavaTest.java").readText()
        val tokens = process(original)

        tokens.forEach { println(it.toString(original)) }

        // ensure that all non-whitespace chars have a token
        var test = original

        tokens.forEach { token ->
            test = test.replaceRange(token.range, " ".repeat(token.range.last - token.range.first + 1))
        }

        if (test.isNotBlank()) {
            error("")
        }
    }
}