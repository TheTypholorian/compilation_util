package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.KeywordType
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

    @JvmStatic
    fun readArgumentListDeclaration(iterator: ParsingIterator<LexerToken>, list: MutableList<ParserNode.Argument> = mutableListOf()): MutableList<ParserNode.Argument> {
        while (iterator.hasNext()) {
            val arg = iterator.nextUntilOrEnd { index, value -> (value as? LexerToken.Punctuation)?.char == ',' }
            val name = arg.last() as LexerToken.Identifier
            list.add(ParserNode.Argument(ParserNode.Type(ParserNode.Path(arg.subList(0, arg.size - 1).toMutableList())), name))
        }

        return list
    }

    @JvmStatic
    fun readAttribute(iterator: ParsingIterator<LexerToken>): ParserNode.Attribute {
        var access: LexerToken.Keyword<KeywordType.Access>? = null
        val modifiers = mutableListOf<LexerToken.Keyword<KeywordType.Modifier>>()
        val annotations = mutableListOf<ParserNode.Annotation>()

        while (true) {
            val token = iterator.peek()

            if ((token as? LexerToken.Punctuation)?.char == '@') {
                iterator.next()
                annotations.add(readAnnotation(iterator))
            } else {
                break
            }
        }

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
            iterator.readBrackets('<', '>', "type argument list") // TODO
        }

        val type = iterator.next()

        if (type is LexerToken.Punctuation && type.char == '{') {
            return ParserNode.Initializer(modifiers, iterator.readBrackets('{', '}', "method body").toMutableList())
        } else if (type is LexerToken.Keyword<*> && type.type is KeywordType.ClassType) {
            val cls = ParserNode.Class(
                access,
                modifiers,
                annotations,
                type as LexerToken.Keyword<KeywordType.ClassType>,
                iterator.next() as LexerToken.Identifier,
                null,
                mutableListOf(),
                mutableListOf()
            )
            val typeArgumentsOpen = iterator.peek()

            if (typeArgumentsOpen is LexerToken.Punctuation && typeArgumentsOpen.char == '<') {
                iterator.next()
                iterator.readBrackets('<', '>', "type argument list") // TODO
            }

            val extends = iterator.peek()

            if (extends is LexerToken.Keyword<*> && extends.type == KeywordType.ClassParentType.EXTENDS) {
                iterator.next()
                cls.superClass = ParserNode.Type(ParserNode.Path(mutableListOf(iterator.next() as LexerToken.Identifier))) // TODO
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
                    cls.interfaces.add(ParserNode.Type(ParserNode.Path(mutableListOf(name)))) // TODO

                    val next = iterator.peek()

                    if (next is LexerToken.Punctuation) {
                        when (next.char) {
                            '<' -> {
                                iterator.next()
                                iterator.readBrackets('<', '>', "type argument list") // TODO
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

            val iterator = ParsingIterator(iterator.readBrackets('{', '}', "class body"))

            while (iterator.hasNext()) {
                cls.attributes.add(readAttribute(iterator))
            }

            return cls
        } else {
            val typeArgOpen = iterator.peek()

            if ((typeArgOpen is LexerToken.Punctuation) && typeArgOpen.char == '<') {
                iterator.next()
                iterator.readBrackets('<', '>', "type argument list") // TODO
            }

            val name = iterator.peek()

            return if (name is LexerToken.Punctuation && name.char == '(') {
                iterator.next()
                ParserNode.Constructor(
                    access,
                    modifiers,
                    annotations,
                    readArgumentListDeclaration(ParsingIterator(iterator.readBrackets('(', ')', "argument list"))),
                    when (val next = (iterator.peek() as LexerToken.Punctuation).char) {
                        ';' -> throw DataReadException("Constructor must have body")
                        '{' -> {
                            iterator.next()
                            iterator.readBrackets('{', '}', "method body").toMutableList()
                        }
                        else -> throw DataReadException(next.toString())
                    }
                )
            } else {
                iterator.next()
                name as LexerToken.Identifier
                val type = ParserNode.Type(ParserNode.Path(mutableListOf(type))) // TODO

                when (val token = (iterator.next() as LexerToken.Punctuation).char) {
                    '(' -> ParserNode.Method(
                        access,
                        modifiers,
                        annotations,
                        type,
                        readArgumentListDeclaration(ParsingIterator(iterator.readBrackets('(', ')', "argument list"))),
                        name,
                        when (val next = (iterator.peek() as LexerToken.Punctuation).char) {
                            ';' -> null
                            '{' -> {
                                iterator.next()
                                iterator.readBrackets('{', '}', "method body").toMutableList()
                            }
                            else -> throw DataReadException(next.toString())
                        }
                    )
                    '=' -> ParserNode.Field(
                        access,
                        modifiers,
                        annotations,
                        type,
                        name,
                        (iterator.nextUntil { index, value ->
                            (value as? LexerToken.Punctuation)?.char == ';'
                        } ?: throw DataReadException("Unterminated initializer")).toMutableList()
                    )
                    ';' -> ParserNode.Field(access, modifiers, annotations, type, name, null)
                    else -> throw DataReadException(token.toString())
                }
            }
        }
    }

    @JvmStatic
    fun readAnnotation(iterator: ParsingIterator<LexerToken>): ParserNode.Annotation {
        val type = ParserNode.Type(ParserNode.Path(mutableListOf(iterator.next()))) // TODO
        val anno = ParserNode.Annotation(type, mutableListOf())

        val open = iterator.peek()

        if (open is LexerToken.Punctuation && open.char == '(') {
            iterator.next()
            val body = ParsingIterator(iterator.readBrackets('(', ')', "annotation"))
            // TODO
        }

        return anno
    }

    @JvmStatic
    fun parse(tokens: List<LexerToken>): ParserNode.File {
        val iterator = ParsingIterator(tokens.filter { it !is LexerToken.Comment })

        var pkg: ParserNode.Path? = null
        val imports = mutableListOf<ParserNode.Path>()

        val token = iterator.peek()

        if (token is LexerToken.Keyword<*> && token.type == KeywordType.HeaderValue.PACKAGE) {
            iterator.next()

            val path = iterator.nextUntil { index, value ->
                (value as? LexerToken.Punctuation)?.char == ';'
            } ?: throw DataReadException()
            pkg = ParserNode.Path(path.toMutableList())
        }

        while (true) {
            val token = iterator.peek()

            if (token is LexerToken.Keyword<*> && token.type == KeywordType.HeaderValue.IMPORT) {
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