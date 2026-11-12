package net.typho.compilation_util.lexer

fun String.range(range: IntRange) = StringRange(this, range)

class StringRange(
    @JvmField
    val text: String,
    @JvmField
	val range: IntRange
) : CharSequence {
    override val length = range.last - range.first

    override fun get(index: Int): Char {
        if (index !in indices) {
            throw IndexOutOfBoundsException(index.toString())
        }

        return this@StringRange.text[range.first + index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): StringRange {
        if (startIndex !in indices) {
            throw IndexOutOfBoundsException(startIndex.toString())
        }

        if (endIndex !in indices) {
            throw IndexOutOfBoundsException(endIndex.toString())
        }

        return StringRange(this@StringRange.text, (range.first + startIndex)..(range.first + endIndex))
    }

    fun offset(offset: Int) = StringRange(this@StringRange.text, (range.first + offset)..(range.last + offset))

    override fun toString(): String {
        return text.substring(range.first, range.last)
    }
}