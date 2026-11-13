package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.KeywordType
import net.typho.compilation_util.lexer.LexerToken
import net.typho.parsing_util.ParsingIterator
import net.typho.parsing_util.error.DataReadException

object JavaParser {
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
                    '=' -> {
                        println("\t\t- Set on '${last!!.text}'")

                        val init = iterator.nextUntil { index, value -> (value as? LexerToken.Punctuation)?.char == ';' } ?: throw DataReadException("Unterminated initializer")
                        println("\t\t- Value ${init.map { it.text }}")
                    }
                    ';' -> {
                        println()
                        last = null
                    }
                    '+' -> {
                        if ((iterator.peek() as? LexerToken.Punctuation)?.char == '+') {
                            iterator.next()
                            println("\t\t- ++ on $last")
                        }
                    }
                    '-' -> {
                        if ((iterator.peek() as? LexerToken.Punctuation)?.char == '-') {
                            iterator.next()
                            println("\t\t- -- on $last")
                        }
                    }
                    '<', '>' -> {
                        println("\t\t- ${token.char} on $last")
                    }
                    else -> throw DataReadException(token.char.toString())
                }
                is LexerToken.Number -> println("\t\t- Number constant $token")
                is LexerToken.Text -> println("\t\t- String constant $token")
                is LexerToken.Keyword -> when (token.type) {
                    is KeywordType.SelfReference -> {
                        println("\t\t- ${token.type}")
                        last = token
                    }
                    KeywordType.Operation.RETURN -> {
                        println("\t\t- Return")
                        last = token
                    }
                    is KeywordType.Primitive -> {
                        println("\t\t- Type ${token.type}")
                        last = token
                    }
                    else -> throw DataReadException(token.type.toString())
                }
                is LexerToken.Error -> throw DataReadException(token.message)
                else -> {}
            }
        }
    }

    @JvmStatic
    fun readAttribute(iterator: ParsingIterator<LexerToken>) {
        while (true) {
            val token = iterator.peek()

            if ((token as? LexerToken.Punctuation)?.char == '@') {
                iterator.next()
                readAnnotation(iterator)
            } else {
                break
            }
        }

        while (true) {
            val token = iterator.peek()

            if (token !is LexerToken.Keyword) {
                break
            }

            when (token.type) {
                is KeywordType.Access -> println("\t- Attribute access: ${token.type}")
                is KeywordType.Modifier -> println("\t- Attribute is ${token.type}")
                else -> break
            }

            iterator.next()
        }

        val typeArgumentsOpen = iterator.peek()

        // TODO this should only happen for methods, not fields nor classes
        if ((typeArgumentsOpen as? LexerToken.Punctuation)?.char == '<') {
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

        val type = iterator.next()

        if (type is LexerToken.Punctuation && type.char == '{') {
            println("\t- Initializer block")
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
        } else if (type is LexerToken.Keyword && type.type is KeywordType.ClassType) {
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

            if (extends is LexerToken.Keyword && extends.type == KeywordType.ClassModifier.EXTENDS) {
                iterator.next()
                println("- Extends ${(iterator.next() as LexerToken.Identifier).text}")
            }

            val implements = iterator.peek()

            if (implements is LexerToken.Keyword && implements.type == KeywordType.ClassModifier.IMPLEMENTS) {
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
                readAttribute(iterator)
                println()
            }
        } else {
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

            val name = iterator.peek()

            if (name is LexerToken.Punctuation && name.char == '(') {
                println("\t- Attribute is a constructor")
            } else {
                println("\t- Attribute name: ${(name as LexerToken.Identifier).text}")
                iterator.next()
            }

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
    fun parse(tokens: List<LexerToken>) {
        val iterator = ParsingIterator(tokens.filter { it !is LexerToken.Comment })

        // TODO package/imports

        val token = iterator.peek()

        if (token is LexerToken.Keyword && token.type == KeywordType.HeaderValue.PACKAGE) {
            iterator.next()

            val pkg = iterator.nextUntil { index, value -> (value as? LexerToken.Punctuation)?.char == ';' } ?: throw DataReadException()
            println("Package ${pkg.map { it.text }}")
        }

        while (true) {
            val token = iterator.peek()

            if (token is LexerToken.Keyword && token.type == KeywordType.HeaderValue.IMPORT) {
                iterator.next()

                val pkg = iterator.nextUntil { index, value -> (value as? LexerToken.Punctuation)?.char == ';' } ?: throw DataReadException()
                println("Import ${pkg.map { it.text }}")
            } else {
                break
            }
        }

        readAttribute(iterator)

        while (iterator.hasNext()) {
            println("Leftover: ${iterator.next()}")
        }
    }
}