package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.Keyword
import net.typho.compilation_util.lexer.LexerToken
import net.typho.parsing_util.ParsingIterator
import net.typho.parsing_util.error.DataReadException

@Suppress("UNCHECKED_CAST")
object JavaParser {
    private fun <V> ParsingIterator<V>.readBrackets(open: Char, close: Char, name: String): List<V> {
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

    private fun <V> ParsingIterator<V>.nextUntilOrEndConsideringBrackets(char: Char, name: String): List<V> {
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

    @JvmStatic
    fun readArgumentListDeclaration(iterator: ParsingIterator<LexerToken>, list: MutableList<ParserNode.Argument> = mutableListOf()): MutableList<ParserNode.Argument> {
        while (iterator.hasNext()) {
            val arg = iterator.nextUntilOrEndConsideringBrackets(',', "argument list")
            val name = arg.last()

            if (name !is LexerToken.Identifier) {
                throw DataReadException(arg.toString())
            }

            val type = readType(ParsingIterator(arg, 0 until (arg.size - 1)), readAnnotations = true, readTypeArguments = true)
            list.add(ParserNode.Argument(type, name))
        }

        return list
    }

    @JvmStatic
    fun readType(iterator: ParsingIterator<LexerToken>, readAnnotations: Boolean, readTypeArguments: Boolean): ParserNode.Type {
        val annotations = mutableListOf<ParserNode.Annotation>()
        val path = mutableListOf<LexerToken>()
        val typeArguments = mutableListOf<ParserNode.TypeArgumentValue>()

        if (readAnnotations) {
            while (true) {
                val at = iterator.peek()

                if ((at as? LexerToken.Punctuation)?.char != '@') {
                    break
                }

                iterator.next()
                annotations.add(readAnnotation(iterator))
            }
        }

        while (true) {
            val token = iterator.peek()

            if (token is LexerToken.Keyword && token.type in Keyword.primitives) {
                path.add(token)
                iterator.next()
                break
            }

            if (token !is LexerToken.Identifier) {
                break
            }

            iterator.next()
            path.add(token)

            if ((iterator.peekOrNull() as? LexerToken.Punctuation)?.char != '.') {
                break
            }

            iterator.next()
        }

        if (readTypeArguments) {
            if ((iterator.peekOrNull() as? LexerToken.Punctuation)?.char == '<') {
                iterator.next()

                val iterator = ParsingIterator(iterator.readBrackets('<', '>', "type argument list"))

                while (iterator.hasNext()) {
                    val iterator = ParsingIterator(iterator.nextUntilOrEndConsideringBrackets(',', "type argument"))
                    var relativity: LexerToken.Keyword? = null

                    if ((iterator.peek() as? LexerToken.Punctuation)?.char == '?') {
                        iterator.next()
                        relativity = iterator.next() as LexerToken.Keyword
                    }

                    val type = readType(iterator, readAnnotations = true, readTypeArguments = true)

                    if (iterator.hasNext()) {
                        throw DataReadException()
                    }

                    typeArguments.add(ParserNode.TypeArgumentValue(type, relativity))

                    if (iterator.hasNext()) {
                        throw DataReadException()
                    }
                }
            }
        }

        return ParserNode.Type(annotations, ParserNode.Path(path), typeArguments)
    }

    @JvmStatic
    fun readTypeArgumentListDeclaration(iterator: ParsingIterator<LexerToken>, list: MutableList<ParserNode.TypeArgument> = mutableListOf()): MutableList<ParserNode.TypeArgument> {
        while (iterator.hasNext()) {
            val iterator = ParsingIterator(iterator.nextUntilOrEndConsideringBrackets(',', "type argument"))
            val argument = ParserNode.TypeArgument(iterator.next() as LexerToken.Identifier, mutableListOf())

            if (iterator.hasNext()) {
                when (val token = iterator.next()) {
                    is LexerToken.Keyword -> when (token.type) {
                        Keyword.EXTENDS -> {
                            while (iterator.hasNext()) {
                                argument.parents.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                                if (iterator.hasNext()) {
                                    val token = iterator.next()

                                    if ((token as? LexerToken.Punctuation)?.char != '&') {
                                        throw DataReadException(token.toString())
                                    }

                                    if (!iterator.hasNext()) {
                                        throw DataReadException()
                                    }
                                }
                            }
                        }
                        else -> throw DataReadException(token.type.toString())
                    }
                    else -> throw DataReadException(token.toString())
                }
            }

            list.add(argument)
        }

        return list
    }

    @JvmStatic
    fun readAttribute(iterator: ParsingIterator<LexerToken>): ParserNode.Attribute {
        var access: LexerToken.Keyword? = null
        val modifiers = mutableListOf<LexerToken.Keyword>()
        val annotations = mutableListOf<ParserNode.Annotation>()

        while (true) {
            val at = iterator.peek()

            if ((at as? LexerToken.Punctuation)?.char == '@') {
                iterator.next()
                annotations.add(readAnnotation(iterator))
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
                in Keyword.access -> access = token
                in Keyword.modifiers -> {
                    if (modifiers.any { it.type == token.type }) {
                        throw DataReadException("Duplicate modifier ${token.type}")
                    } else {
                        modifiers.add(token)
                    }
                }
                else -> break
            }

            iterator.next()
        }

        val typeArguments = mutableListOf<ParserNode.TypeArgument>()
        val typeArgumentsOpen = iterator.peek()
        var earlyTypeArguments = false

        if ((typeArgumentsOpen as? LexerToken.Punctuation)?.char == '<') {
            iterator.next()
            readTypeArgumentListDeclaration(ParsingIterator(iterator.readBrackets('<', '>', "type argument list")), typeArguments)
            earlyTypeArguments = true
        }

        val attributeType = iterator.peek()

        if (attributeType is LexerToken.Punctuation && attributeType.char == '{') {
            if (typeArguments.isNotEmpty()) {
                throw DataReadException("Class initializers cannot have type arguments")
            }

            iterator.next()

            return ParserNode.Initializer(modifiers, iterator.readBrackets('{', '}', "method body").toMutableList())
        } else if (attributeType is LexerToken.Keyword && attributeType.type in Keyword.classTypes) {
            if (earlyTypeArguments) {
                throw DataReadException("Type arguments for classes must come after the class name, not before")
            }

            iterator.next()

            val cls = ParserNode.Class(
                access,
                modifiers,
                annotations,
                attributeType,
                iterator.next() as LexerToken.Identifier,
                typeArguments,
                null,
                mutableListOf(),
                mutableListOf(),
                mutableListOf()
            )
            val typeArgumentsOpen = iterator.peek()

            if (typeArgumentsOpen is LexerToken.Punctuation && typeArgumentsOpen.char == '<') {
                iterator.next()
                readTypeArgumentListDeclaration(ParsingIterator(iterator.readBrackets('<', '>', "type argument list")), typeArguments)
            }

            val extends = iterator.peek()

            if (extends is LexerToken.Keyword && extends.type == Keyword.EXTENDS) {
                iterator.next()
                cls.superClass = readType(iterator, readAnnotations = true, readTypeArguments = true)
            }

            val implements = iterator.peek()

            if (implements is LexerToken.Keyword && implements.type == Keyword.IMPLEMENTS) {
                iterator.next()

                while (true) {
                    cls.interfaces.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                    val next = iterator.peek()

                    if (next is LexerToken.Punctuation) {
                        when (next.char) {
                            ',' -> iterator.next()
                            '{' -> break
                            else -> throw DataReadException(next.char.toString())
                        }
                    } else {
                        throw DataReadException(next.toString())
                    }
                }
            }

            val permits = iterator.peek()

            if (permits is LexerToken.Keyword && permits.type == Keyword.PERMITS) {
                iterator.next()

                while (true) {
                    cls.permits.add(readType(iterator, readAnnotations = true, readTypeArguments = false))

                    val next = iterator.peek()

                    if (next is LexerToken.Punctuation) {
                        when (next.char) {
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

            val iterator = ParsingIterator(iterator.readBrackets('{', '}', "class body"))

            while (iterator.hasNext()) {
                cls.attributes.add(readAttribute(iterator))
            }

            return cls
        } else {
            if ((attributeType as? LexerToken.Punctuation)?.char == '<') {
                if (earlyTypeArguments) {
                    throw DataReadException("Two definitions for type arguments")
                }

                iterator.next()
                readTypeArgumentListDeclaration(ParsingIterator(iterator.readBrackets('<', '>', "type argument list")), typeArguments)
            }

            val type = readType(iterator, readAnnotations = true, readTypeArguments = true)
            val name = iterator.peek()

            return if (name is LexerToken.Punctuation && name.char == '(') {
                if (typeArguments.isNotEmpty()) {
                    throw DataReadException("Constructors do not have type arguments")
                }

                iterator.next()

                val arguments = readArgumentListDeclaration(ParsingIterator(iterator.readBrackets('(', ')', "argument list")))

                val exceptions = mutableListOf<ParserNode.Type>()
                val token = iterator.peek()

                if (token is LexerToken.Keyword && token.type == Keyword.THROWS) {
                    iterator.next()

                    while (true) {
                        exceptions.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                        val next = iterator.peek()

                        if (next is LexerToken.Punctuation) {
                            when (next.char) {
                                ',' -> iterator.next()
                                else -> break
                            }
                        } else {
                            throw DataReadException(next.toString())
                        }
                    }
                }

                val body = when (val next = (iterator.peek() as LexerToken.Punctuation).char) {
                    ';' -> throw DataReadException("Constructor must have body")
                    '{' -> {
                        iterator.next()
                        iterator.readBrackets('{', '}', "method body").toMutableList()
                    }
                    else -> throw DataReadException(next.toString())
                }

                ParserNode.Constructor(access, modifiers, annotations, arguments, exceptions, body)
            } else {
                iterator.next()
                name as? LexerToken.Identifier ?: throw DataReadException(name.toString())

                when (val token = (iterator.next() as LexerToken.Punctuation).char) {
                    '(' -> {
                        val arguments = readArgumentListDeclaration(ParsingIterator(iterator.readBrackets('(', ')', "argument list")))

                        val exceptions = mutableListOf<ParserNode.Type>()
                        val token = iterator.peek()

                        if (token is LexerToken.Keyword && token.type == Keyword.THROWS) {
                            iterator.next()

                            while (true) {
                                exceptions.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                                val next = iterator.peek()

                                if (next is LexerToken.Punctuation) {
                                    when (next.char) {
                                        ',' -> iterator.next()
                                        else -> break
                                    }
                                } else {
                                    throw DataReadException(next.toString())
                                }
                            }
                        }

                        val body = when (val next = (iterator.next() as LexerToken.Punctuation).char) {
                            ';' -> null
                            '{' -> iterator.readBrackets('{', '}', "method body").toMutableList()
                            else -> throw DataReadException(next.toString())
                        }

                        ParserNode.Method(access, modifiers, annotations, typeArguments, type, arguments, name, exceptions, body)
                    }
                    '=' -> {
                        if (typeArguments.isNotEmpty()) {
                            throw DataReadException("Fields do not have type arguments")
                        }

                        val initializer = (iterator.nextUntil { index, value ->
                            (value as? LexerToken.Punctuation)?.char == ';'
                        } ?: throw DataReadException("Unterminated initializer")).toMutableList()

                        ParserNode.Field(access, modifiers, annotations, type, name, initializer)
                    }
                    ';' -> {
                        if (typeArguments.isNotEmpty()) {
                            throw DataReadException("Fields do not have type arguments")
                        }

                        ParserNode.Field(access, modifiers, annotations, type, name, null)
                    }
                    else -> throw DataReadException(token.toString())
                }
            }
        }
    }

    @JvmStatic
    fun readAnnotation(iterator: ParsingIterator<LexerToken>): ParserNode.Annotation {
        val type = readType(iterator, readAnnotations = false, readTypeArguments = false)
        val anno = ParserNode.Annotation(type.path, mutableListOf())

        val open = iterator.peek()

        if (open is LexerToken.Punctuation && open.char == '(') {
            iterator.next()
            var key: LexerToken.Identifier? = null
            var value = iterator.readBrackets('(', ')', "annotation value")

            if (value.size >= 2 && value.first() is LexerToken.Identifier && (value[1] as? LexerToken.Punctuation)?.char == '=') {
                key = value.first() as LexerToken.Identifier
                value = value.subList(2, value.size)
            }

            anno.values.add(ParserNode.AnnotationValue(key, value.toMutableList()))
        }

        return anno
    }

    @JvmStatic
    fun parse(tokens: List<LexerToken>): ParserNode.File {
        val iterator = ParsingIterator(tokens.filter { it !is LexerToken.Comment })

        var pkg: ParserNode.Path? = null
        val imports = mutableListOf<ParserNode.Path>()

        val token = iterator.peek()

        if (token is LexerToken.Keyword && token.type == Keyword.PACKAGE) {
            iterator.next()

            val path = iterator.nextUntil { index, value ->
                (value as? LexerToken.Punctuation)?.char == ';'
            } ?: throw DataReadException()
            pkg = ParserNode.Path(path.toMutableList())
        }

        while (true) {
            val token = iterator.peek()

            if (token is LexerToken.Keyword && token.type == Keyword.IMPORT) {
                iterator.next()

                val path = iterator.nextUntil { index, value ->
                    (value as? LexerToken.Punctuation)?.char == ';'
                } ?: throw DataReadException()
                imports.add(ParserNode.Path(path.toMutableList()))
            } else {
                break
            }
        }

        val file = ParserNode.File(pkg, imports, mutableListOf())

        while (iterator.hasNext()) {
            file.classes.add(readAttribute(iterator) as? ParserNode.Class ?: TODO())
        }

        return file
    }
}