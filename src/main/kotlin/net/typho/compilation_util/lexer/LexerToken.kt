package net.typho.compilation_util.lexer

sealed class LexerToken(
    @JvmField
    val text: StringRange
) {
    abstract fun copyWithOffset(offset: Int): LexerToken

    override fun toString(): String {
        return "${javaClass.simpleName} at ${this@LexerToken.text.range}: $text"
    }

    class Identifier(
        range: StringRange
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int) = Identifier(range = text.offset(offset))
    }

    class Text(
        range: StringRange,
        @JvmField
        val contentsRange: StringRange,
        @JvmField
        val quoteType: String
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int) = Text(
            text.offset(offset),
            contentsRange.offset(offset),
            quoteType
        )
    }

    class Comment(
        range: StringRange,
        @JvmField
        val contentsRange: StringRange,
        @JvmField
        val inline: Boolean
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int) = Comment(
            text.offset(offset),
            contentsRange.offset(offset),
            inline
        )
    }

    class Number(
        range: StringRange
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int) = Number(
            text.offset(offset)
        )
    }

    class Keyword(
        range: StringRange
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int) = Keyword(
            text.offset(offset)
        )
    }

    class Punctuation(
        text: String,
        @JvmField
        val index: Int
    ) : LexerToken(StringRange(text, index..index + 1)) {
        override fun copyWithOffset(offset: Int) = Punctuation(
            text.text,
            index + offset
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Punctuation) return false

            if (index != other.index) return false

            return true
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "${javaClass.simpleName} at $index: $text"
        }
    }

    class Error(
        range: StringRange,
        @JvmField
        val message: String
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int) = Error(
            text.offset(offset),
            message
        )

        override fun toString(): String {
            return "${javaClass.simpleName} at ${this@Error.text.range} (text $text): $message"
        }
    }
}