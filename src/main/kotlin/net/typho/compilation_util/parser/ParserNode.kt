package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.KeywordType
import net.typho.compilation_util.lexer.LexerToken

sealed class ParserNode {
    abstract fun fieldsToString(): String

    override fun toString(): String {
        return buildString {
            if (this@ParserNode is WithAnnotations) {
                for (annotation in annotations) {
                    append('@')
                    append(annotation.fieldsToString())
                    append(' ')
                }
            }

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
        }
    }

    sealed interface WithAnnotations {
        val annotations: MutableList<Annotation>
    }

    sealed class WithAccess : ParserNode() {
        abstract var access: LexerToken.Keyword<KeywordType.Access>?
        abstract val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>
    }

    sealed class Attribute : WithAccess()

    class Path(
        @JvmField
        val tokens: MutableList<LexerToken>
    ) : ParserNode() {
        val path: String
            get() = tokens.joinToString(separator = "") { it.text.toString() }

        override fun fieldsToString(): String {
            return path
        }
    }

    class File(
        @JvmField
        var pkg: Path?,
        @JvmField
        val imports: MutableList<Path>,
        @JvmField
        val classes: MutableList<Class>
    ) : ParserNode() {
        override fun fieldsToString() = "package ${pkg?.path}, imports ${imports.map { it.path }}, $classes"
    }

    class Type(
        // TODO annotations
        @JvmField
        var path: Path
        // TODO type arguments
    ) : ParserNode() {
        override fun fieldsToString(): String {
            return path.fieldsToString()
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

    class Annotation(
        @JvmField
        var type: Type,
        @JvmField
        val values: MutableList<AnnotationValue>
    ) : ParserNode() {
        override fun fieldsToString(): String {
            return "${type.fieldsToString()}(${values.joinToString()})"
        }
    }

    class AnnotationValue(
        @JvmField
        var key: LexerToken.Identifier,
        @JvmField
        var value: LexerToken
    ) : ParserNode() {
        override fun fieldsToString(): String {
            return "${key.text} = ${value.text}"
        }
    }

    class Class(
        override var access: LexerToken.Keyword<KeywordType.Access>?,
        override val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>,
        override val annotations: MutableList<Annotation>,
        // TODO type arguments
        @JvmField
        var classType: LexerToken.Keyword<KeywordType.ClassType>,
        @JvmField
        var name: LexerToken.Identifier,
        @JvmField
        var superClass: Type?,
        @JvmField
        val interfaces: MutableList<Type>,
        @JvmField
        val attributes: MutableList<Attribute>
    ) : Attribute(), WithAnnotations {
        override fun fieldsToString(): String {
            return "${classType.text} ${name.text} extends $superClass implements $interfaces"
        }

        override fun toString(): String {
            return if (attributes.isEmpty()) {
                super.toString()
            } else {
                super.toString() + "\n" + attributes.joinToString("\n").prependIndent("  ") + "\n"
            }
        }
    }

    sealed class Callable : Attribute(), WithAnnotations {
        abstract val arguments: MutableList<Argument>
        abstract val body: MutableList<LexerToken>?

        override fun toString(): String {
            return if (body.isNullOrEmpty()) {
                super.toString()
            } else {
                super.toString() + "\n" + body!!.joinToString(" ") { it.text }.prependIndent("  ")
            }
        }
    }

    class Constructor(
        override var access: LexerToken.Keyword<KeywordType.Access>?,
        override val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>,
        override val annotations: MutableList<Annotation>,
        override val arguments: MutableList<Argument>,
        override val body: MutableList<LexerToken>
    ) : Callable() {
        override fun fieldsToString(): String {
            return arguments.joinToString { it.fieldsToString() }
        }
    }

    class Initializer(
        override val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>,
        @JvmField
        val body: MutableList<LexerToken>
    ) : Attribute() {
        override var access: LexerToken.Keyword<KeywordType.Access>?
            get() = null
            set(value) {
                throw UnsupportedOperationException("Initializers do not have access")
            }

        override fun fieldsToString(): String {
            return ""
        }

        override fun toString(): String {
            return if (body.isEmpty()) {
                super.toString()
            } else {
                super.toString() + "\n" + body.joinToString(" ") { it.text }.prependIndent("  ")
            }
        }
    }

    class Method(
        override var access: LexerToken.Keyword<KeywordType.Access>?,
        override val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>,
        override val annotations: MutableList<Annotation>,
        @JvmField
        var returnType: Type,
        override val arguments: MutableList<Argument>,
        @JvmField
        val name: LexerToken.Identifier,
        override val body: MutableList<LexerToken>?
    ) : Callable() {
        override fun fieldsToString(): String {
            return "$returnType ${name.text}(${arguments.joinToString { it.fieldsToString() }})"
        }
    }

    class Field(
        override var access: LexerToken.Keyword<KeywordType.Access>?,
        override val modifiers: MutableList<LexerToken.Keyword<KeywordType.Modifier>>,
        override val annotations: MutableList<Annotation>,
        @JvmField
        var type: Type,
        @JvmField
        val name: LexerToken.Identifier,
        @JvmField
        val initializer: MutableList<LexerToken>?
    ) : Attribute(), WithAnnotations {
        override fun fieldsToString(): String {
            return "$type ${name.text}"
        }

        override fun toString(): String {
            return if (initializer.isNullOrEmpty()) {
                super.toString()
            } else {
                super.toString() + "\n" + initializer.joinToString("\n").prependIndent("  ")
            }
        }
    }
}