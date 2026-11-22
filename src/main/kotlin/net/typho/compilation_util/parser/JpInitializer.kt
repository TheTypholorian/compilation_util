package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlKeyword
import net.typho.compilation_util.lexer.JlToken

open class JpInitializer(
    override val modifiers: MutableList<JlKeyword>,
    open val body: MutableList<JlToken>
) : JpAttribute() {
    override var access: JlKeyword?
        get() = null
        set(value) {
            throw UnsupportedOperationException("Initializers do not have access")
        }

    override fun fieldsToString(): String {
        return ""
    }

    override fun toString(): String {
        return if (body.isEmpty()) {
            super.toString()
        } else {
            super.toString() + "\n" + body.joinToString(" ") { it.text }.prependIndent("  ")
        }
    }
}