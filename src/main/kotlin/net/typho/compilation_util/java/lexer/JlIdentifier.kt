package net.typho.compilation_util.java.lexer

import net.typho.parsing_util.StringView

class JlIdentifier(
    range: StringView
) : JlToken(range) {
    override fun copyWithOffset(offset: Int, text: CharSequence) = JlIdentifier(range = this.text.offset(offset, text))
}