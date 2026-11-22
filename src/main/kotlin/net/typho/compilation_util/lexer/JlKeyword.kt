package net.typho.compilation_util.lexer

import net.typho.parsing_util.StringView

class JlKeyword(
    range: StringView,
    @JvmField
    val type: KeywordType
) : JlToken(range) {
    override fun copyWithOffset(offset: Int, text: CharSequence) = JlKeyword(
        this.text.offset(offset, text),
        type
    )
}