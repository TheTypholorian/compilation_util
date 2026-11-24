package net.typho.compilation_util.java.lexer

import net.typho.parsing_util.StringView

class JlText(
    range: StringView,
    @JvmField
    val contentsRange: StringView,
    @JvmField
    val quoteType: String
) : JlToken(range) {
    override fun copyWithOffset(offset: Int, text: CharSequence) = JlText(
        this.text.offset(offset, text),
        contentsRange.offset(offset),
        quoteType
    )
}