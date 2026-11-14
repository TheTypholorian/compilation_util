package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.KeywordType
import net.typho.compilation_util.lexer.LexerToken
import net.typho.parsing_util.ParsingIterator
import net.typho.parsing_util.error.DataReadException

@Suppress("UNCHECKED_CAST")
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
    fun readArgumentListDeclaration(iterator: ParsingIterator<LexerToken>, list: MutableList<ParserNode.Argument>) {
        while (iterator.hasNext()) {
            val arg = iterator.nextUntilOrEnd { index, value -> (value as? LexerToken.Punctuation)?.char == ',' }
            println("\t- Argument ${arg.map { it.text }}")
        }
    }

    @JvmStatic
    fun readMethodBody(iterator: ParsingIterator<LexerToken>, method: ParserNode.MethodLike) {
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
                is LexerToken.Keyword<*> -> when (token.type) {
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
    fun readAttribute(iterator: ParsingIterator<LexerToken>): ParserNode.Attribute {
        while (true) {
            val token = iterator.peek()

            if ((token as? LexerToken.Punctuation)?.char == '@') {
                iterator.next()
                readAnnotation(iterator) // TODO
            } else {
                break
            }
        }

        var access: LexerToken.Keyword<KeywordType.Access>? = null
        val modifiers = mutableListOf<LexerToken.Keyword<KeywordType.Modifier>>()

        while (true) {
            val token = iterator.peek()

            if (token !is LexerToken.Keyword<*>) {
                break
            }

            when (token.type) {
                is KeywordType.Access -> access = token as LexerToken.Keyword<KeywordType.Access>
                is KeywordType.Modifier -> modifiers.add(token as LexerToken.Keyword<KeywordType.Modifier>)
                else -> break
            }

            iterator.next()
        }

        val typeArgumentsOpen = iterator.peek()

        // TODO type parameters are only here for methods, not fields nor classes
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
            // TODO
            //readMethodBody(ParsingIterator(body))
            return ParserNode.Initializer(modifiers, mutableListOf())
        } else if (type is LexerToken.Keyword<*> && type.type is KeywordType.ClassType) {
            val cls = ParserNode.Class(
                access,
                modifiers,
                type as LexerToken.Keyword<KeywordType.ClassType>,
                iterator.next() as LexerToken.Identifier,
                null,
                mutableListOf(),
                mutableListOf()
            )
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
                readArgumentList(ParsingIterator(body)) // TODO
            }

            val extends = iterator.peek()

            if (extends is LexerToken.Keyword<*> && extends.type == KeywordType.ClassParentType.EXTENDS) {
                iterator.next()
                cls.superClass = ParserNode.Type(iterator.next() as LexerToken.Identifier)
            }

            val implements = iterator.peek()

            if (implements is LexerToken.Keyword<*> && implements.type == KeywordType.ClassParentType.IMPLEMENTS) {
                iterator.next()

                while (true) {
                    val name = iterator.peek()

                    if (name !is LexerToken.Identifier) {
                        break
                    }

                    iterator.next()
                    cls.interfaces.add(ParserNode.Type(name))

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
                                // TODO
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
                cls.children.add(readAttribute(iterator))
            }

            return cls
        } else {
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
                //readArgumentList(ParsingIterator(body)) // TODO
            }

            val name = iterator.peek()

            fun readMethod(method: ParserNode.MethodLike, hasReadParentheses: Boolean): Boolean {
                var depth = if (hasReadParentheses) 1 else 0
                val body = iterator.nextUntil { index, value ->
                    if (value is LexerToken.Punctuation) {
                        when (value.char) {
                            '(' -> depth++
                            ')' -> depth--
                        }
                    }

                    depth == 0
                } ?: throw DataReadException("Unterminated argument list")
                readArgumentListDeclaration(ParsingIterator(body), method.arguments)

                return when (val next = (iterator.peek() as LexerToken.Punctuation).char) {
                    ';' -> false
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
                        readMethodBody(ParsingIterator(body), method)
                        true
                    }
                    else -> throw DataReadException(next.toString())
                }
            }

            return if (name is LexerToken.Punctuation && name.char == '(') {
                val constructor = ParserNode.Constructor(access, modifiers, mutableListOf(), mutableListOf())

                if (!readMethod(constructor, false)) {
                    throw DataReadException("Constructor with no body")
                }

                constructor
            } else {
                iterator.next()
                name as LexerToken.Identifier

                when (val token = (iterator.next() as LexerToken.Punctuation).char) {
                    '(' -> {
                        val method = ParserNode.Method(access, modifiers, ParserNode.Type(type), mutableListOf(), name, true, mutableListOf())
                        method.hasBody = readMethod(method, true)
                        method
                    }
                    '=' -> {
                        val field = ParserNode.Field(access, modifiers, ParserNode.Type(type), name, true, mutableListOf())
                        val init = iterator.nextUntil { index, value -> (value as? LexerToken.Punctuation)?.char == ';' } ?: throw DataReadException("Unterminated initializer")
                        // TODO
                        field
                    }
                    ';' -> ParserNode.Field(access, modifiers, ParserNode.Type(type), name, false, mutableListOf())
                    else -> throw DataReadException(token.toString())
                }
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
    fun parse(tokens: List<LexerToken>): ParserNode.File {
        val iterator = ParsingIterator(tokens.filter { it !is LexerToken.Comment })

        // TODO package/imports

        val token = iterator.peek()

        if (token is LexerToken.Keyword<*> && token.type == KeywordType.HeaderValue.PACKAGE) {
            iterator.next()

            val pkg = iterator.nextUntil { index, value -> (value as? LexerToken.Punctuation)?.char == ';' } ?: throw DataReadException()
            println("Package ${pkg.map { it.text }}")
        }

        while (true) {
            val token = iterator.peek()

            if (token is LexerToken.Keyword<*> && token.type == KeywordType.HeaderValue.IMPORT) {
                iterator.next()

                val pkg = iterator.nextUntil { index, value -> (value as? LexerToken.Punctuation)?.char == ';' } ?: throw DataReadException()
                println("Import ${pkg.map { it.text }}")
            } else {
                break
            }
        }

        val file = ParserNode.File(mutableListOf())

        while (iterator.hasNext()) {
            file.classes.add(readAttribute(iterator) as? ParserNode.Class ?: TODO())
        }

        return file
    }
}