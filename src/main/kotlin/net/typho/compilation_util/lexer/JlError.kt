package net.typho.compilation_util.lexer

import net.typho.parsing_util.StringView

class JlError(
    range: StringView,
    @JvmField
    val message: String
) : JlToken(range) {
    override fun copyWithOffset(offset: Int, text: CharSequence) = JlError(
        this.text.offset(offset, text),
        message
    )

    override fun toString(): String {
        return "${javaClass.simpleName} at ${text.rangeAsLineColumn()} (text $text): $message"
    }
}