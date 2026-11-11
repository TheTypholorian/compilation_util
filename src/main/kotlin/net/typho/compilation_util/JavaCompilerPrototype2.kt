package net.typho.compilation_util

import net.typho.parsing_util.ParsingIterator

object JavaCompilerPrototype2 {
    private sealed interface Token

    private data class Identifier(
        @JvmField
        val text: String
    ) : Token

    private data class Text(
        @JvmField
        val text: String,
        @JvmField
        val multiline: Boolean = false
    ) : Token

    private data class Number(
        @JvmField
        val number: kotlin.Number
    ) : Token

    private enum class Keyword : Token {
        PACKAGE, IMPORT, PUBLIC, CLASS, STATIC, VOID; // TODO
    }

    private enum class Special(
        @JvmField
        val char: Char
    ) : Token {
        LBRACKET('{'),
        RBRACKET('}'),
        LBRACE('['),
        RBRACE(']'),
        LPARENTHESES('('),
        RPARENTHESES(')'),
        LANGLE('<'),
        RANGLE('>'),
        DOT('.'),
        SEMICOLON(';')
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val multiline = "\"\"\"This is multiline text\nline 2;\n\nline 4!\n\"this is me trying to break the text\"\"\"\""
        val original = """
            package net.typho.compilation_util;
            
            import java.lang.System;
            
            public class Test {
            	public static void main(String[] args) {
                    System.out.println("Hello world");
                    System.out.println($multiline);
                    System.out.println(123);
                }
            }
            """
        val input = ParsingIterator(original)
        val keywords = Keyword.entries.map { it.name.lowercase() }
        val special = Special.entries.map { it.char }
        val quotes = setOf('"', '\'')
        val tokens = mutableListOf<Token>()

        main@
        while (input.hasNext()) {
            input.skipUntil { index, char -> !char.isWhitespace() } ?: break

            while (input.peek() in special) {
                val char = input.next()
                tokens.add(Special.entries.first { it.char == char })

                if (!input.hasNext()) {
                    break@main
                }
            }

            val fork = input.fork()
            val word = fork.takeUntilOrEnd { index, char -> (char.isWhitespace() || char in special) }

            if (word.isBlank()) {
                continue
            }

            if (word in keywords) {
                tokens.add(Keyword.valueOf(word.uppercase()))
                fork.merge()
            } else if (word[0] in quotes) {
                val quote = word[0]

                if (quote == '"' && input.currentMatches("\"\"\"")) {
                    input.next()
                    input.next()
                    input.next()
                    tokens.add(Text(input.takeUntil { index, char -> input.currentMatches("\"\"\"") } ?: TODO(), true))
                    input.next()
                    input.next()
                    input.next()
                } else {
                    input.next()
                    tokens.add(Text(input.takeUntil { index, char ->
                        if (char == '\n') {
                            TODO("End of line before closing quote $quote")
                        } else {
                            char == quote
                        }
                    } ?: TODO())) // TODO escaping
                    input.next()
                }
            } else if (word.all { it.isDigit() }) {
                tokens.add(Number(word.toInt()))
                fork.merge()
            } else {
                tokens.add(Identifier(word))
                fork.merge()
            }
        }

        tokens.forEach { println(it) }

        val result = buildString {
            var last: Token? = null

            tokens.forEach { token ->
                if ((token is Identifier || token is Keyword) && (last is Identifier || last is Keyword)) {
                    append(' ')
                }

                when (token) {
                    is Identifier -> append(token.text)
                    is Keyword -> append(token.name.lowercase())
                    is Special -> append(token.char)
                    is Text -> {
                        val quotes = if (token.multiline) "\"\"\"" else "\""
                        append(quotes)
                        append(token.text)
                        append(quotes)
                    }
                    is Number -> append(token.number)
                }

                last = token
            }
        }
        println(original)
        println(result)
    }
}