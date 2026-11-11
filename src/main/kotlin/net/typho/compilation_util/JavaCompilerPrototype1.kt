package net.typho.compilation_util

import net.typho.parsing_util.ParsingIterator

object JavaCompilerPrototype1 {
    private fun ParsingIterator.forkLine(): ParsingIterator.Child {
        var doubleQuotes = 0
        var singleQuotes = 0
        val endIndex = fork().skipUntilOrEnd { index, char ->
            if (char == '"') {
                if (singleQuotes % 2 == 0) {
                    doubleQuotes++
                }
            } else if (char == '\'') {
                if (doubleQuotes % 2 == 0) {
                    singleQuotes++
                }
            }

            (doubleQuotes % 2 == 0) && (singleQuotes % 2 == 0) && (char == '\n' || char == ';')
        }
        return fork(range = index until index + endIndex, index = index)
    }

    private fun ParsingIterator.readWord(): String {
        skipUntil { index, char -> !(char.isWhitespace() || char == ';') }
        return takeUntilOrEnd { index, char -> char.isWhitespace() || char == ';' }
    }

    private fun ParsingIterator.readFileAttribute(name: String): List<String> {
        val result = mutableListOf<String>()

        while (hasNext()) {
            val fork = forkLine()
            val word = fork.readWord()

            if (word.isBlank()) {
                fork.merge().next()
            } else if (word == name) {
                val tokens = mutableListOf<String>()

                while (fork.hasNext()) {
                    fork.skipUntil { index, char -> !char.isWhitespace() } ?: break
                    tokens.add(fork.nextUntilOrEnd { index, char -> char.isWhitespace() })
                }

                result.add(tokens.joinToString(separator = ""))
                fork.merge()
            } else {
                break
            }
        }

        return result
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val multiline = "\"\"\"This is multiline text\nline 2;\n\nline 4!\n\"this is me trying to break the text\"\"\"\""
        val input = ParsingIterator(
            """
            package net.typho.compilation_util;
            
            import java.lang.System;
            
            public class Test {
            	public static void main(String[] args) {
                    System.out.println("Hello world");
                    System.out.println($multiline);
                }
            }
            """
        )
        val pkg = input.readFileAttribute("package").ifEmpty { listOf(null) }.single()
        val imports = input.readFileAttribute("import")

        println("Package $pkg")
        println("Imports $imports")
    }
}