package net.typho.compilation_util.java.lexer

import net.typho.parsing_util.StringView

class JlNumber(
    range: StringView
) : JlToken(range) {
    override fun copyWithOffset(offset: Int, text: CharSequence) = JlNumber(
        this.text.offset(offset, text)
    )
}