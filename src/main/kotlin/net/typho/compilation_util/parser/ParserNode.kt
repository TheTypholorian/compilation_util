package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.KeywordType

sealed class ParserNode {
    abstract fun fieldsToString(): String

    override fun toString(): String {
        var text = ""

        if (this is WithAccess) {
            text += "$access "
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
        abstract val access: KeywordType
        abstract val modifiers: List<KeywordType>
    }

    class Class(
        override val access: KeywordType,
        override val modifiers: List<KeywordType>,
        @JvmField
        val classType: KeywordType,
        override val children: List<WithAccess>
    ) : WithAccess(), Container {
    }
}