package net.typho.compilation_util.lexer

private fun IntRange.offset(offset: Int) = (first + offset)..(last + offset)

sealed interface LexerToken {
    val range: IntRange

    fun copyWithOffset(offset: Int): LexerToken

    fun toString(text: String): String {
        return text.substring(range.first, range.last)
    }

    data class Identifier(
        override val range: IntRange
    ) : LexerToken {
        override fun copyWithOffset(offset: Int) = copy(range = range.offset(offset))
    }

    data class Text(
        override val range: IntRange,
        @JvmField
        val contentsRange: IntRange,
        @JvmField
        val quoteType: String
    ) : LexerToken {
        override fun copyWithOffset(offset: Int) = copy(
            range = range.offset(offset),
            contentsRange = contentsRange.offset(offset)
        )
    }

    data class Comment(
        override val range: IntRange,
        @JvmField
        val contentsRange: IntRange,
        @JvmField
        val inline: Boolean = false
    ) : LexerToken {
        override fun copyWithOffset(offset: Int) = copy(
            range = range.offset(offset),
            contentsRange = contentsRange.offset(offset)
        )
    }

    data class Number(
        override val range: IntRange
    ) : LexerToken {
        override fun copyWithOffset(offset: Int) = copy(range = range.offset(offset))
    }

    data class Keyword(
        override val range: IntRange
    ) : LexerToken {
        override fun copyWithOffset(offset: Int) = copy(range = range.offset(offset))
    }

    data class Punctuation(
        @JvmField
        val index: Int
    ) : LexerToken {
        override val range: IntRange
            get() = index..index + 1

        override fun copyWithOffset(offset: Int) = copy(index = index + offset)
    }

    data class Error(
        override val range: IntRange,
        @JvmField
        val message: String
    ) : LexerToken {
        override fun copyWithOffset(offset: Int) = copy(range = range.offset(offset))
    }
}