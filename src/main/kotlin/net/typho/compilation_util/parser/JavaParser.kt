package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlComment
import net.typho.compilation_util.lexer.JlIdentifier
import net.typho.compilation_util.lexer.JlKeyword
import net.typho.compilation_util.lexer.JlNumber
import net.typho.compilation_util.lexer.JlPunctuation
import net.typho.compilation_util.lexer.JlText
import net.typho.compilation_util.lexer.KeywordType
import net.typho.compilation_util.lexer.JlToken
import net.typho.compilation_util.lexer.nextUntilOrEndConsideringBrackets
import net.typho.compilation_util.lexer.readBrackets
import net.typho.parsing_util.ParsingIterator
import net.typho.parsing_util.error.DataReadException

@Suppress("UNCHECKED_CAST")
object JavaParser {
    @JvmStatic
    fun readArgumentListDeclaration(iterator: ParsingIterator<JlToken>, list: MutableList<JpArgument> = mutableListOf()): MutableList<JpArgument> {
        while (iterator.hasNext()) {
            val arg = iterator.nextUntilOrEndConsideringBrackets(',', "argument list")
            val name = arg.last()

            if (name !is JlIdentifier) {
                throw DataReadException(arg.toString())
            }

            val type = readType(ParsingIterator(arg, 0 until (arg.size - 1)), readAnnotations = true, readTypeArguments = true)
            list.add(JpArgument(type, name))
        }

        return list
    }

    @JvmStatic
    fun readType(iterator: ParsingIterator<JlToken>, readAnnotations: Boolean, readTypeArguments: Boolean): JpType {
        val annotations = mutableListOf<JpAnnotation>()
        val path = mutableListOf<JlToken>()
        val typeArguments = mutableListOf<JpTypeArgumentValue>()

        if (readAnnotations) {
            while (true) {
                val at = iterator.peek()

                if ((at as? JlPunctuation)?.char != '@') {
                    break
                }

                iterator.next()
                annotations.add(readAnnotation(iterator))
            }
        }

        while (true) {
            val token = iterator.peek()

            if (token is JlKeyword && token.type in KeywordType.primitives) {
                path.add(token)
                iterator.next()
                break
            }

            if (token !is JlIdentifier) {
                break
            }

            iterator.next()
            path.add(token)

            if ((iterator.peekOrNull() as? JlPunctuation)?.char != '.') {
                break
            }

            iterator.next()
        }

        if (readTypeArguments) {
            if ((iterator.peekOrNull() as? JlPunctuation)?.char == '<') {
                iterator.next()

                val iterator = ParsingIterator(iterator.readBrackets('<', '>', "type argument list"))

                while (iterator.hasNext()) {
                    val iterator = ParsingIterator(iterator.nextUntilOrEndConsideringBrackets(',', "type argument"))
                    var relativity: JlKeyword? = null

                    if ((iterator.peek() as? JlPunctuation)?.char == '?') {
                        iterator.next()
                        relativity = iterator.next() as JlKeyword
                    }

                    val type = readType(iterator, readAnnotations = true, readTypeArguments = true)

                    if (iterator.hasNext()) {
                        throw DataReadException()
                    }

                    typeArguments.add(JpTypeArgumentValue(type, relativity))

                    if (iterator.hasNext()) {
                        throw DataReadException()
                    }
                }
            }
        }

        return JpType(annotations, JpPath(path), typeArguments)
    }

    @JvmStatic
    fun readTypeArgumentListDeclaration(iterator: ParsingIterator<JlToken>, list: MutableList<JpTypeArgument> = mutableListOf()): MutableList<JpTypeArgument> {
        while (iterator.hasNext()) {
            val iterator = ParsingIterator(iterator.nextUntilOrEndConsideringBrackets(',', "type argument"))
            val argument = JpTypeArgument(iterator.next() as JlIdentifier, mutableListOf())

            if (iterator.hasNext()) {
                when (val token = iterator.next()) {
                    is JlKeyword -> when (token.type) {
                        KeywordType.EXTENDS -> {
                            while (iterator.hasNext()) {
                                argument.parents.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                                if (iterator.hasNext()) {
                                    val token = iterator.next()

                                    if ((token as? JlPunctuation)?.char != '&') {
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
    fun readAttribute(iterator: ParsingIterator<JlToken>): JpAttribute {
        var access: JlKeyword? = null
        val modifiers = mutableListOf<JlKeyword>()
        val annotations = mutableListOf<JpAnnotation>()

        while (true) {
            val at = iterator.peek()

            if ((at as? JlPunctuation)?.char == '@') {
                iterator.next()
                annotations.add(readAnnotation(iterator))
            } else {
                break
            }
        }

        while (true) {
            val token = iterator.peek()

            if (token !is JlKeyword) {
                break
            }

            when (token.type) {
                in KeywordType.access -> access = token
                in KeywordType.modifiers -> {
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

        val typeArguments = mutableListOf<JpTypeArgument>()
        val typeArgumentsOpen = iterator.peek()
        var earlyTypeArguments = false

        if ((typeArgumentsOpen as? JlPunctuation)?.char == '<') {
            iterator.next()
            readTypeArgumentListDeclaration(ParsingIterator(iterator.readBrackets('<', '>', "type argument list")), typeArguments)
            earlyTypeArguments = true
        }

        val attributeType = iterator.peek()

        if (attributeType is JlPunctuation && attributeType.char == '{') {
            if (typeArguments.isNotEmpty()) {
                throw DataReadException("Class initializers cannot have type arguments")
            }

            iterator.next()

            return JpInitializer(modifiers, iterator.readBrackets('{', '}', "method body").toMutableList())
        } else if (attributeType is JlKeyword && attributeType.type in KeywordType.classTypes) {
            if (earlyTypeArguments) {
                throw DataReadException("Type arguments for classes must come after the class name, not before")
            }

            iterator.next()

            val cls = JpClass(
                access,
                modifiers,
                annotations,
                attributeType,
                iterator.next() as JlIdentifier,
                typeArguments,
                null,
                mutableListOf(),
                mutableListOf(),
                mutableListOf()
            )
            val typeArgumentsOpen = iterator.peek()

            if (typeArgumentsOpen is JlPunctuation && typeArgumentsOpen.char == '<') {
                iterator.next()
                readTypeArgumentListDeclaration(ParsingIterator(iterator.readBrackets('<', '>', "type argument list")), typeArguments)
            }

            val extends = iterator.peek()

            if (extends is JlKeyword && extends.type == KeywordType.EXTENDS) {
                iterator.next()
                cls.superClass = readType(iterator, readAnnotations = true, readTypeArguments = true)
            }

            val implements = iterator.peek()

            if (implements is JlKeyword && implements.type == KeywordType.IMPLEMENTS) {
                iterator.next()

                while (true) {
                    cls.interfaces.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                    val next = iterator.peek()

                    if (next is JlPunctuation) {
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

            if (permits is JlKeyword && permits.type == KeywordType.PERMITS) {
                iterator.next()

                while (true) {
                    cls.permits.add(readType(iterator, readAnnotations = true, readTypeArguments = false))

                    val next = iterator.peek()

                    if (next is JlPunctuation) {
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

            if (bodyOpen is JlPunctuation && bodyOpen.char == '{') {
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
            if ((attributeType as? JlPunctuation)?.char == '<') {
                if (earlyTypeArguments) {
                    throw DataReadException("Two definitions for type arguments")
                }

                iterator.next()
                readTypeArgumentListDeclaration(ParsingIterator(iterator.readBrackets('<', '>', "type argument list")), typeArguments)
            }

            val type = readType(iterator, readAnnotations = true, readTypeArguments = true)
            val name = iterator.peek()

            return if (name is JlPunctuation && name.char == '(') {
                if (typeArguments.isNotEmpty()) {
                    throw DataReadException("Constructors do not have type arguments")
                }

                iterator.next()

                val arguments = readArgumentListDeclaration(ParsingIterator(iterator.readBrackets('(', ')', "argument list")))

                val exceptions = mutableListOf<JpType>()
                val token = iterator.peek()

                if (token is JlKeyword && token.type == KeywordType.THROWS) {
                    iterator.next()

                    while (true) {
                        exceptions.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                        val next = iterator.peek()

                        if (next is JlPunctuation) {
                            when (next.char) {
                                ',' -> iterator.next()
                                else -> break
                            }
                        } else {
                            throw DataReadException(next.toString())
                        }
                    }
                }

                val body = when (val next = (iterator.peek() as JlPunctuation).char) {
                    ';' -> throw DataReadException("Constructor must have body")
                    '{' -> {
                        iterator.next()
                        iterator.readBrackets('{', '}', "method body").toMutableList()
                    }
                    else -> throw DataReadException(next.toString())
                }

                JpConstructor(access, modifiers, annotations, arguments, exceptions, body)
            } else {
                iterator.next()
                name as? JlIdentifier ?: throw DataReadException(name.toString())

                when (val token = (iterator.next() as JlPunctuation).char) {
                    '(' -> {
                        val arguments = readArgumentListDeclaration(ParsingIterator(iterator.readBrackets('(', ')', "argument list")))

                        val exceptions = mutableListOf<JpType>()
                        val token = iterator.peek()

                        if (token is JlKeyword && token.type == KeywordType.THROWS) {
                            iterator.next()

                            while (true) {
                                exceptions.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                                val next = iterator.peek()

                                if (next is JlPunctuation) {
                                    when (next.char) {
                                        ',' -> iterator.next()
                                        else -> break
                                    }
                                } else {
                                    throw DataReadException(next.toString())
                                }
                            }
                        }

                        val body = when (val next = (iterator.next() as JlPunctuation).char) {
                            ';' -> null
                            '{' -> iterator.readBrackets('{', '}', "method body").toMutableList()
                            else -> throw DataReadException(next.toString())
                        }

                        JpMethod(access, modifiers, annotations, typeArguments, type, arguments, name, exceptions, body)
                    }
                    '=' -> {
                        if (typeArguments.isNotEmpty()) {
                            throw DataReadException("Fields do not have type arguments")
                        }

                        val initializer = (iterator.nextUntil { index, value ->
                            (value as? JlPunctuation)?.char == ';'
                        } ?: throw DataReadException("Unterminated initializer")).toMutableList()

                        JpField(access, modifiers, annotations, type, name, initializer)
                    }
                    ';' -> {
                        if (typeArguments.isNotEmpty()) {
                            throw DataReadException("Fields do not have type arguments")
                        }

                        JpField(access, modifiers, annotations, type, name, null)
                    }
                    else -> throw DataReadException(token.toString())
                }
            }
        }
    }

    @JvmStatic
    fun readAnnotationValue(value: List<JlToken>): JpAnnotationValue<*> {
        val iterator = ParsingIterator(value)
        val value = when (val type = iterator.next()) {
            is JlNumber -> JpNumberAnnotationValue(type)
            is JlText -> JpTextAnnotationValue(type)
            is JlPunctuation if type.char == '@' -> JpNestedAnnotationValue(readAnnotation(iterator))
            is JlPunctuation if type.char == '{' -> {
                val body = ParsingIterator(iterator.readBrackets('{', '}', "annotation array contents"))
                val array = mutableListOf<JpAnnotationValue<*>>()

                while (body.hasNext()) {
                    array.add(readAnnotationValue(body.nextUntilOrEndConsideringBrackets(',', "annotation array value")))
                }

                JpArrayAnnotationValue(array)
            }
            else -> JpUnknownAnnotationValue(value)
        }

        if (iterator.hasNext()) {
            throw DataReadException()
        }

        return value
    }

    @JvmStatic
    fun readAnnotation(iterator: ParsingIterator<JlToken>): JpAnnotation {
        val type = readType(iterator, readAnnotations = false, readTypeArguments = false)
        val anno = JpAnnotation(type.path, mutableListOf())

        val open = iterator.peek()

        if (open is JlPunctuation && open.char == '(') {
            iterator.next()
            var key: JlIdentifier? = null
            var value = iterator.readBrackets('(', ')', "annotation value")

            if (value.size >= 2 && value.first() is JlIdentifier && (value[1] as? JlPunctuation)?.char == '=') {
                key = value.first() as JlIdentifier
                value = value.subList(2, value.size)
            }

            anno.values.add(key to readAnnotationValue(value))
        }

        return anno
    }

    @JvmStatic
    fun parse(tokens: List<JlToken>): JpFile {
        val iterator = ParsingIterator(tokens.filter { it !is JlComment })

        var pkg: JpPath? = null
        val imports = mutableListOf<JpPath>()

        val token = iterator.peek()

        if (token is JlKeyword && token.type == KeywordType.PACKAGE) {
            iterator.next()

            val path = iterator.nextUntil { index, value ->
                (value as? JlPunctuation)?.char == ';'
            } ?: throw DataReadException()
            pkg = JpPath(path.toMutableList())
        }

        while (true) {
            val token = iterator.peek()

            if (token is JlKeyword && token.type == KeywordType.IMPORT) {
                iterator.next()

                val path = iterator.nextUntil { index, value ->
                    (value as? JlPunctuation)?.char == ';'
                } ?: throw DataReadException()
                imports.add(JpPath(path.toMutableList()))
            } else {
                break
            }
        }

        val file = JpFile(pkg, imports, mutableListOf())

        while (iterator.hasNext()) {
            file.classes.add(readAttribute(iterator) as? JpClass ?: TODO())
        }

        return file
    }
}