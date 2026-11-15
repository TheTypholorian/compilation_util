package net.typho.compilation_util.lexer

import net.typho.parsing_util.StringView

sealed class LexerToken(
    @JvmField
    val text: StringView
) {
    abstract fun copyWithOffset(offset: Int, text: CharSequence = this.text.text): LexerToken

    override fun toString(): String {
        return "${javaClass.simpleName} at ${text.rangeAsLineColumn()}: $text"
    }

    class Identifier(
        range: StringView
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int, text: CharSequence) = Identifier(range = this.text.offset(offset, text))
    }

    class Text(
        range: StringView,
        @JvmField
        val contentsRange: StringView,
        @JvmField
        val quoteType: String
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int, text: CharSequence) = Text(
            this.text.offset(offset, text),
            contentsRange.offset(offset),
            quoteType
        )
    }

    class Comment(
        range: StringView,
        @JvmField
        val contentsRange: StringView,
        @JvmField
        val inline: Boolean
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int, text: CharSequence) = Comment(
            this.text.offset(offset, text),
            contentsRange.offset(offset),
            inline
        )
    }

    class Number(
        range: StringView
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int, text: CharSequence) = Number(
            this.text.offset(offset, text)
        )
    }

    class Keyword(
        range: StringView,
        @JvmField
        val type: net.typho.compilation_util.lexer.Keyword
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int, text: CharSequence) = Keyword(
            this.text.offset(offset, text),
            type
        )
    }

    class Punctuation(
        text: CharSequence,
        @JvmField
        val index: Int
    ) : LexerToken(StringView(text, index..index + 1)) {
        override fun copyWithOffset(offset: Int, text: CharSequence) = Punctuation(
            text,
            index + offset
        )
        val char: Char
            get() = text.text[index]

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
            return "${javaClass.simpleName} at ${text.rangeAsLineColumn()}: $text"
        }
    }

    class Error(
        range: StringView,
        @JvmField
        val message: String
    ) : LexerToken(range) {
        override fun copyWithOffset(offset: Int, text: CharSequence) = Error(
            this.text.offset(offset, text),
            message
        )

        override fun toString(): String {
            return "${javaClass.simpleName} at ${text.rangeAsLineColumn()} (text $text): $message"
        }
    }
}