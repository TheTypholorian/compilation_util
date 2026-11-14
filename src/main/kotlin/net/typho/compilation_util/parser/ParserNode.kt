package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.KeywordType
import net.typho.compilation_util.lexer.LexerToken

sealed class ParserNode {
    abstract fun fieldsToString(): String

    override fun toString(): String {
        return buildString {
            if (this@ParserNode is WithAccess) {
                if (this@ParserNode !is Initializer) {
                    append(access?.type?.id ?: "package-private")
                    append(' ')
                }

                for (keyword in modifiers) {
                    append(keyword.type.id)
                    append(' ')
                }
            }

            append(this@ParserNode.javaClass.simpleName)
            append('(')
            append(fieldsToString())
            append(')')

            if (this@ParserNode is Container && children.isNotEmpty()) {
                append('\n')
                append(children.joinToString("\n").prependIndent("  "))
            }
        }
    }

    sealed interface Container {
        val children: MutableList<out ParserNode>
    }

    sealed class WithAccess : ParserNode() {
        abstract var access: LexerToken.Keyword<KeywordType.Access>?
        abstract val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>
    }

    sealed class Attribute : WithAccess(), Container

    class File(
        @JvmField
        val classes: MutableList<Class>
    ) : ParserNode() {
        override fun fieldsToString() = classes.toString()
    }

    class Type(
        // TODO annotation
        @JvmField
        var token: LexerToken
        // TODO type arguments
    ) : ParserNode() {
        override fun fieldsToString(): String {
            return token.text.toString()
        }

        override fun toString(): String {
            return fieldsToString()
        }
    }

    class Argument(
        @JvmField
        var type: Type,
        @JvmField
        var name: LexerToken.Identifier
    ) : ParserNode() {
        override fun fieldsToString(): String {
            return "$type ${name.text}"
        }
    }

    class Number(
        @JvmField
        var number: LexerToken.Number
    ) : ParserNode() {
        override fun fieldsToString(): String {
            return number.text.toString()
        }
    }

    class Text(
        @JvmField
        var text: LexerToken.Text
    ) : ParserNode() {
        override fun fieldsToString(): String {
            return text.text.toString()
        }
    }

    class Class(
        override var access: LexerToken.Keyword<KeywordType.Access>?,
        override val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>,
        // TODO type arguments
        @JvmField
        var classType: LexerToken.Keyword<KeywordType.ClassType>,
        @JvmField
        var name: LexerToken.Identifier,
        @JvmField
        var superClass: Type?,
        @JvmField
        val interfaces: MutableList<Type>,
        override val children: MutableList<Attribute>
    ) : Attribute(), Container {
        override fun fieldsToString(): String {
            return "${classType.text} ${name.text} extends $superClass implements $interfaces"
        }
    }

    sealed class MethodLike : Attribute(), Container {
        abstract val arguments: MutableList<Argument>
    }

    class Constructor(
        override var access: LexerToken.Keyword<KeywordType.Access>?,
        override val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>,
        override val arguments: MutableList<Argument>,
        override val children: MutableList<ParserNode>
    ) : MethodLike() {
        override fun fieldsToString(): String {
            return arguments.joinToString()
        }
    }

    class Initializer(
        override val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>,
        override val children: MutableList<ParserNode>
    ) : Attribute(), Container {
        override var access: LexerToken.Keyword<KeywordType.Access>?
            get() = null
            set(value) {
                throw UnsupportedOperationException("Initializers do not have access")
            }

        override fun fieldsToString(): String {
            return ""
        }
    }

    class Method(
        override var access: LexerToken.Keyword<KeywordType.Access>?,
        override val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>,
        @JvmField
        var returnType: Type,
        override val arguments: MutableList<Argument>,
        @JvmField
        val name: LexerToken.Identifier,
        @JvmField
        var hasBody: Boolean,
        override val children: MutableList<ParserNode>
    ) : MethodLike() {
        override fun fieldsToString(): String {
            return "$returnType ${name.text}(${arguments.joinToString()})"
        }
    }

    class Field(
        override var access: LexerToken.Keyword<KeywordType.Access>?,
        override val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>,
        @JvmField
        var type: Type,
        @JvmField
        val name: LexerToken.Identifier,
        @JvmField
        var isInitialized: Boolean,
        override val children: MutableList<ParserNode>
    ) : Attribute(), Container {
        override fun fieldsToString(): String {
            return "$type ${name.text}"
        }
    }
}