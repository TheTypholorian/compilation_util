package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.KeywordType
import net.typho.compilation_util.lexer.LexerToken
import net.typho.parsing_util.ParsingIterator
import net.typho.parsing_util.error.DataReadException

object JavaParser {
    private inline fun <V> ParsingIterator<V>.forkAndMergeIf(func: ParsingIterator<V>.() -> Boolean) {
        val fork = fork()

        if (func(fork)) {
            fork.merge()
        }
    }

    @JvmStatic
    fun readArgumentList(iterator: ParsingIterator<LexerToken>) {
        while (iterator.hasNext()) {
            // TODO nesting
            val arg = iterator.nextUntilOrEnd { index, value -> (value as? LexerToken.Punctuation)?.char == ',' }
            println("\t\t- Argument ${arg.map { it.text }}")
        }
    }

    @JvmStatic
    fun readArgumentListDeclaration(iterator: ParsingIterator<LexerToken>) {
        while (iterator.hasNext()) {
            val arg = iterator.nextUntilOrEnd { index, value -> (value as? LexerToken.Punctuation)?.char == ',' }
            println("\t- Argument ${arg.map { it.text }}")
        }
    }

    @JvmStatic
    fun readMethodBody(iterator: ParsingIterator<LexerToken>) {
        var last: LexerToken? = null

        while (iterator.hasNext()) {
            when (val token = iterator.next()) {
                is LexerToken.Identifier -> {
                    println("\t\t- Identifier '${token.text}'")

                    if (last is LexerToken.Identifier) {
                        throw DataReadException()
                    }

                    last = token
                }
                is LexerToken.Punctuation -> when (token.char) {
                    '.' -> {
                        println("\t\t- Operation with target '${last!!.text}'")
                        last = token
                    }
                    '(' -> {
                        println("\t\t- Method call on '${last!!.text}'")

                        var depth = 1
                        val body = iterator.nextUntil { index, value ->
                            if (value is LexerToken.Punctuation) {
                                when (value.char) {
                                    '(' -> depth++
                                    ')' -> depth--
                                }
                            }

                            depth == 0
                        } ?: throw DataReadException("Unterminated argument list")
                        readArgumentList(ParsingIterator(body))
                    }
                    ';' -> {
                        println()
                        last = null
                    }
                    else -> throw DataReadException(token.char.toString())
                }
                is LexerToken.Number -> println("\t\t- Number constant $token")
                is LexerToken.Text -> println("\t\t- String constant $token")
                is LexerToken.Keyword -> throw DataReadException(token.type.toString())
                is LexerToken.Error -> throw DataReadException(token.message)
                else -> {}
            }
        }
    }

    @JvmStatic
    fun readClassElement(iterator: ParsingIterator<LexerToken>) {
        while (true) {
            val token = iterator.peek()

            if (!(token is LexerToken.Punctuation && token.char == '@')) {
                break
            }

            iterator.next()
            readAnnotation(iterator)
        }

        while (true) {
            val token = iterator.peek()

            if (token !is LexerToken.Keyword) {
                break
            }

            when (token.type) {
                in KeywordType.ACCESS_KEYWORDS -> println("\t- Attribute access: ${token.type}")
                KeywordType.STATIC -> println("\t- Attribute is static")
                KeywordType.FINAL -> println("\t- Attribute is final")
                KeywordType.VOLATILE -> println("\t- Attribute is volatile")
                KeywordType.TRANSIENT -> println("\t- Attribute is transient")
                else -> break
            }

            iterator.next()
        }

        val type = iterator.next()
        println("\t- Attribute type: '${type.text}'")

        val typeArgOpen = iterator.peek()

        if ((typeArgOpen is LexerToken.Punctuation) && typeArgOpen.char == '<') {
            iterator.next()
            var depth = 1
            val body = iterator.nextUntil { index, value ->
                if (value is LexerToken.Punctuation) {
                    when (value.char) {
                        '<' -> depth++
                        '>' -> depth--
                    }
                }

                depth == 0
            } ?: throw DataReadException("Unterminated type argument list")
            readArgumentList(ParsingIterator(body))
        }

        val name = (iterator.next() as LexerToken.Identifier).text
        println("\t- Attribute name: $name")

        when (val type = (iterator.next() as LexerToken.Punctuation).char) {
            '(' -> {
                println("\t- Attribute is a method")

                var depth = 1
                val body = iterator.nextUntil { index, value ->
                    if (value is LexerToken.Punctuation) {
                        when (value.char) {
                            '(' -> depth++
                            ')' -> depth--
                        }
                    }

                    depth == 0
                } ?: throw DataReadException("Unterminated argument list")
                readArgumentListDeclaration(ParsingIterator(body))

                when (val next = (iterator.peek() as LexerToken.Punctuation).char) {
                    ';' -> println("\t- Method has no body")
                    '{' -> {
                        iterator.next()
                        var depth = 1
                        val body = iterator.nextUntil { index, value ->
                            if (value is LexerToken.Punctuation) {
                                when (value.char) {
                                    '{' -> depth++
                                    '}' -> depth--
                                }
                            }

                            depth == 0
                        } ?: throw DataReadException("Unterminated method body")
                        readMethodBody(ParsingIterator(body))
                    }
                    else -> throw DataReadException(next.toString())
                }

                // TODO read body
            }
            '=' -> {
                println("\t- Attribute is an initialized field")
                val init = iterator.nextUntil { index, value -> (value as? LexerToken.Punctuation)?.char == ';' } ?: throw DataReadException("Unterminated initializer")
                println("\t- Attribute initializer ${init.map { it.text }}")
            }
            ';' -> {
                println("\t- Attribute is an uninitialized field")
            }
            else -> throw DataReadException(type.toString())
        }
    }

    @JvmStatic
    fun readAnnotation(iterator: ParsingIterator<LexerToken>) {
        val name = iterator.next() as LexerToken.Identifier
        println("- Annotation '${name.text}'")

        val open = iterator.peek()

        if (open is LexerToken.Punctuation && open.char == '(') {
            iterator.next()
            var depth = 1
            val body = iterator.nextUntil { index, value ->
                if (value is LexerToken.Punctuation) {
                    when (value.char) {
                        '(' -> depth++
                        ')' -> depth--
                    }
                }

                depth == 0
            } ?: throw DataReadException("Unterminated annotation")
            val iterator = ParsingIterator(body)

            readArgumentList(iterator)
        }
    }

    @JvmStatic
    fun readClass(iterator: ParsingIterator<LexerToken>) {
        while (true) {
            val token = iterator.peek()

            if (token !is LexerToken.Punctuation) {
                break
            }

            when (token.char) {
                '@' -> {
                    iterator.next()
                    readAnnotation(iterator)
                }
                else -> throw DataReadException(token.char.toString())
            }
        }

        iterator.forkAndMergeIf {
            when (val token = next()) {
                is LexerToken.Keyword -> if (token.type in KeywordType.ACCESS_KEYWORDS) {
                    println("Class access: ${token.type}")
                    true
                } else {
                    false
                }
                else -> throw DataReadException(token.toString())
            }
        }

        iterator.forkAndMergeIf {
            val token = next() as LexerToken.Keyword

            if (token.type in KeywordType.CLASS_TYPE_KEYWORDS) {
                println("Class type: ${token.type}")
                true
            } else {
                false
            }
        }

        println("Class name: ${(iterator.next() as LexerToken.Identifier).text}")

        val typeArgumentsOpen = iterator.peek()

        if (typeArgumentsOpen is LexerToken.Punctuation && typeArgumentsOpen.char == '<') {
            iterator.next()
            var depth = 1
            val body = iterator.nextUntil { index, value ->
                if (value is LexerToken.Punctuation) {
                    when (value.char) {
                        '<' -> depth++
                        '>' -> depth--
                    }
                }

                depth == 0
            } ?: throw DataReadException("Unterminated type argument list")
            readArgumentList(ParsingIterator(body))
        }

        val extends = iterator.peek()

        if (extends is LexerToken.Keyword && extends.type == KeywordType.EXTENDS) {
            iterator.next()
            println("- Extends ${(iterator.next() as LexerToken.Identifier).text}")
        }

        val implements = iterator.peek()

        if (implements is LexerToken.Keyword && implements.type == KeywordType.IMPLEMENTS) {
            iterator.next()

            while (true) {
                val name = iterator.peek()

                if (name !is LexerToken.Identifier) {
                    break
                }

                iterator.next()
                println("- Implements ${name.text}")

                val next = iterator.peek()

                if (next is LexerToken.Punctuation) {
                    when (next.char) {
                        '<' -> {
                            iterator.next()
                            var depth = 1
                            val body = iterator.nextUntil { index, value ->
                                if (value is LexerToken.Punctuation) {
                                    when (value.char) {
                                        '<' -> depth++
                                        '>' -> depth--
                                    }
                                }

                                depth == 0
                            } ?: throw DataReadException("Unterminated type argument")
                            println("\t- Type arguments ${body.map { it.text }}")
                        }
                        ',' -> iterator.next()
                        '{' -> break
                        else -> throw DataReadException(next.char.toString())
                    }
                } else {
                    throw DataReadException(next.toString())
                }
            }
        }

        val bodyOpen = iterator.peek()

        if (bodyOpen is LexerToken.Punctuation && bodyOpen.char == '{') {
            iterator.next()
        } else {
            throw DataReadException(bodyOpen.toString())
        }

        var depth = 1
        val body = iterator.nextUntil { index, value ->
            if (value is LexerToken.Punctuation) {
                when (value.char) {
                    '{' -> depth++
                    '}' -> depth--
                }
            }

            depth == 0
        } ?: throw DataReadException("Unterminated class body")
        val iterator = ParsingIterator(body)

        while (iterator.hasNext()) {
            readClassElement(iterator)
            println()
        }
    }

    @JvmStatic
    fun parse(tokens: List<LexerToken>) {
        val iterator = ParsingIterator(tokens.filter { it !is LexerToken.Comment })

        // TODO package/imports

        val token = iterator.peek()

        if (token is LexerToken.Keyword && token.type == KeywordType.PACKAGE) {
            iterator.next()

            val pkg = iterator.nextUntil { index, value -> (value as? LexerToken.Punctuation)?.char == ';' } ?: throw DataReadException()
            println("Package ${pkg.map { it.text }}")
        }

        while (true) {
            val token = iterator.peek()

            if (token is LexerToken.Keyword && token.type == KeywordType.IMPORT) {
                iterator.next()

                val pkg = iterator.nextUntil { index, value -> (value as? LexerToken.Punctuation)?.char == ';' } ?: throw DataReadException()
                println("Import ${pkg.map { it.text }}")
            } else {
                break
            }
        }

        readClass(iterator)

        while (iterator.hasNext()) {
            println("Leftover: ${iterator.next()}")
        }
    }
}