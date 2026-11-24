package net.typho.compilation_util.java.lexer

import net.typho.parsing_util.StringView
import net.typho.parsing_util.error.DataReadException

sealed class JlToken(
    @JvmField
    val text: StringView
) {
    abstract fun copyWithOffset(offset: Int, text: CharSequence = this.text.text): JlToken

    override fun toString(): String {
        return "${javaClass.simpleName}@[${text.rangeAsLineColumn()}]: $text"
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : JlToken> cast(): T {
        if (this !is T) {
            throw DataReadException("Cannot cast $this to ${T::class.simpleName}")
        }

        return this
    }
}