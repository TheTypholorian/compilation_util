package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.KeywordType
import net.typho.compilation_util.lexer.LexerToken

sealed class ParserNode {
    abstract fun fieldsToString(): String

    override fun toString(): String {
        var text = ""

        if (this is WithAccess) {
            text += "${access ?: "PACKAGE-PRIVATE"} "
        }

        text += "${javaClass.simpleName}(${fieldsToString()})"

        if (this is Container) {
            text += "\n"
            text += children.joinToString("\n").prependIndent("  ")
        }

        return text
    }

    sealed interface Container {
        val children: List<ParserNode>
    }

    sealed class WithAccess : ParserNode() {
        abstract val access: LexerToken.Keyword<KeywordType.Access>?
        abstract val modifiers: List<LexerToken.Keyword<*>>
    }

    class Class(
        override val access: LexerToken.Keyword<KeywordType.Access>?,
        override val modifiers: List<LexerToken.Keyword<*>>,
        // TODO type arguments
        @JvmField
        val classType: LexerToken.Keyword<*>,
        @JvmField
        val superClass: LexerToken.Identifier?,
        @JvmField
        val interfaces: List<LexerToken.Identifier>,
        override val children: List<WithAccess>
    ) : WithAccess(), Container {
        override fun fieldsToString(): String {
            return "modifiers=${modifiers.map { it.text }}, type=${classType.text}, superClass=${superClass?.text}, interfaces=${interfaces.map { it.text }}"
        }
    }

    class Method(
        override val access: LexerToken.Keyword<KeywordType.Access>?,
        override val modifiers: List<LexerToken.Keyword<*>>,
        override val children: List<ParserNode>
    ) : WithAccess(), Container {
        override fun fieldsToString(): String {
        }
    }
}