package net.typho.compilation_util.lexer

import net.typho.parsing_util.StringView

sealed class JlToken(
    @JvmField
    val text: StringView
) {
    abstract fun copyWithOffset(offset: Int, text: CharSequence = this.text.text): JlToken

    override fun toString(): String {
        return "${javaClass.simpleName}@[${text.rangeAsLineColumn()}]: $text"
    }
}