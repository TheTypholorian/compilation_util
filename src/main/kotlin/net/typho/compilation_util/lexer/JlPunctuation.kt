package net.typho.compilation_util.lexer

import net.typho.parsing_util.StringView

class JlPunctuation(
    text: CharSequence,
    @JvmField
    val index: Int
) : JlToken(StringView(text, index..index + 1)) {
    override fun copyWithOffset(offset: Int, text: CharSequence) = JlPunctuation(
        text,
        index + offset
    )
    val char: Char
        get() = text.text[index]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JlPunctuation) return false

        if (index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        return index
    }

    override fun toString(): String {
        return "${javaClass.simpleName} at ${text.rangeAsLineColumn()}: $text"
    }
}