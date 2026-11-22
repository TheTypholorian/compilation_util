package net.typho.compilation_util.lexer

import net.typho.parsing_util.StringView

class JlComment(
    range: StringView,
    @JvmField
    val contentsRange: StringView,
    @JvmField
    val inline: Boolean
) : JlToken(range) {
    override fun copyWithOffset(offset: Int, text: CharSequence) = JlComment(
        this.text.offset(offset, text),
        contentsRange.offset(offset),
        inline
    )
}