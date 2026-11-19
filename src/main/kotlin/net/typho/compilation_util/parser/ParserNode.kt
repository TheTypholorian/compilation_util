package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.LexerToken

abstract class ParserNode {
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
                    append(access?.type?.name ?: "package-private")
                    append(' ')
                }

                for (keyword in modifiers) {
                    append(keyword.type.name)
                    append(' ')
                }
            }

            append(this@ParserNode.javaClass.simpleName)
            append('(')
            append(fieldsToString())
            append(')')

            if (this@ParserNode is WithExceptions && exceptions.isNotEmpty()) {
                append(" throws ")
                exceptions.joinTo(this) { it.fieldsToString() }
            }
        }
    }

    interface WithAnnotations {
        val annotations: MutableList<Annotation>
    }

    interface WithTypeArguments {
        val typeArguments: MutableList<TypeArgument>
    }

    interface WithName {
        var name: LexerToken.Identifier
    }

    interface WithBody {
        val body: MutableList<LexerToken>?
    }

    interface WithType {
        var type: Type
    }

    interface WithExceptions {
        val exceptions: MutableList<Type>
    }

    abstract class WithAccess : ParserNode() {
        abstract var access: LexerToken.Keyword?
        abstract val modifiers: MutableList<LexerToken.Keyword>
    }

    abstract class Attribute : WithAccess()

    abstract class Callable : Attribute(), WithAnnotations, WithBody {
        abstract val arguments: MutableList<Argument>

        override fun toString(): String {
            val body = body

            return if (body.isNullOrEmpty()) {
                super.toString()
            } else {
                super.toString() + "\n" + body.joinToString(" ") { it.text }.prependIndent("  ")
            }
        }
    }

    open class Path(
        open val tokens: MutableList<LexerToken>
    ) : ParserNode() {
        val path: String
            get() = tokens.joinToString(separator = "") { it.text.toString() }

        override fun fieldsToString(): String {
            return path
        }
    }

    open class File(
        open var pkg: Path?,
        open val imports: MutableList<Path>,
        open val classes: MutableList<Class>
    ) : ParserNode() {
        override fun fieldsToString() = "package ${pkg?.path}, imports ${imports.map { it.path }}, $classes"
    }

    open class TypeArgumentValue(
        open var type: Type,
        open var relativity: LexerToken.Keyword?
    ) : ParserNode() {
        override fun fieldsToString(): String {
            return if (relativity == null) {
                type.fieldsToString()
            } else {
                "? ${relativity!!.text} ${type.fieldsToString()}"
            }
        }
    }

    open class Type(
        override val annotations: MutableList<Annotation>,
        open var path: Path,
        open val arguments: MutableList<TypeArgumentValue>
    ) : ParserNode(), WithAnnotations {
        override fun fieldsToString(): String {
            return if (arguments.isEmpty()) {
                path.fieldsToString()
            } else {
                "${path.fieldsToString()}<${arguments.joinToString { it.fieldsToString() }}>"
            }
        }

        override fun toString(): String {
            return fieldsToString()
        }
    }

    open class TypeArgument(
        override var name: LexerToken.Identifier,
        open val parents: MutableList<Type>
    ) : ParserNode(), WithName {
        override fun fieldsToString(): String {
            return if (parents.isEmpty()) {
                name.text.toString()
            } else {
                "${name.text} extends ${parents.joinToString { it.fieldsToString() }}"
            }
        }
    }

    open class Argument(
        override var type: Type,
        override var name: LexerToken.Identifier
    ) : ParserNode(), WithName, WithType {
        override fun fieldsToString(): String {
            return "$type ${name.text}"
        }
    }

    open class Annotation(
        override var type: Type,
        open val values: MutableList<AnnotationValue>
    ) : ParserNode(), WithType {
        override fun fieldsToString(): String {
            return "${type.fieldsToString()}(${values.joinToString() { it.fieldsToString() }})"
        }
    }

    open class AnnotationValue(
        open var key: LexerToken.Identifier?,
        override val body: MutableList<LexerToken>
    ) : ParserNode(), WithBody {
        override fun fieldsToString(): String {
            return if (key == null) {
                body.joinToString(separator = " ") { it.text }
            } else {
                "${key!!.text} = ${body.joinToString(separator = " ") { it.text }}"
            }
        }
    }

    open class Class(
        override var access: LexerToken.Keyword?,
        override val modifiers: MutableList<LexerToken.Keyword>,
        override val annotations: MutableList<Annotation>,
        open var classType: LexerToken.Keyword,
        override var name: LexerToken.Identifier,
        override val typeArguments: MutableList<TypeArgument>,
        open var superClass: Type?,
        open val interfaces: MutableList<Type>,
        open val permits: MutableList<Type>,
        open val attributes: MutableList<Attribute>
    ) : Attribute(), WithAnnotations, WithTypeArguments, WithName {
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

    open class Constructor(
        override var access: LexerToken.Keyword?,
        override val modifiers: MutableList<LexerToken.Keyword>,
        override val annotations: MutableList<Annotation>,
        override val arguments: MutableList<Argument>,
        override val exceptions: MutableList<Type>,
        override val body: MutableList<LexerToken>
    ) : Callable(), WithExceptions {
        override fun fieldsToString(): String {
            return arguments.joinToString { it.fieldsToString() }
        }
    }

    open class Initializer(
        override val modifiers: MutableList<LexerToken.Keyword>,
        override val body: MutableList<LexerToken>
    ) : Attribute(), WithBody {
        override var access: LexerToken.Keyword?
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

    open class Method(
        override var access: LexerToken.Keyword?,
        override val modifiers: MutableList<LexerToken.Keyword>,
        override val annotations: MutableList<Annotation>,
        override val typeArguments: MutableList<TypeArgument>,
        open var returnType: Type,
        override val arguments: MutableList<Argument>,
        override var name: LexerToken.Identifier,
        override val exceptions: MutableList<Type>,
        override val body: MutableList<LexerToken>?
    ) : Callable(), WithTypeArguments, WithName, WithExceptions {
        override fun fieldsToString(): String {
            return "$returnType ${name.text}(${arguments.joinToString { it.fieldsToString() }})"
        }
    }

    open class Field(
        override var access: LexerToken.Keyword?,
        override val modifiers: MutableList<LexerToken.Keyword>,
        override val annotations: MutableList<Annotation>,
        override var type: Type,
        override var name: LexerToken.Identifier,
        open val initializer: MutableList<LexerToken>?
    ) : Attribute(), WithAnnotations, WithName, WithType {
        override fun fieldsToString(): String {
            return "$type ${name.text}"
        }

        override fun toString(): String {
            val initializer = initializer

            return if (initializer.isNullOrEmpty()) {
                super.toString()
            } else {
                super.toString() + "\n" + initializer.joinToString("\n").prependIndent("  ")
            }
        }
    }
}