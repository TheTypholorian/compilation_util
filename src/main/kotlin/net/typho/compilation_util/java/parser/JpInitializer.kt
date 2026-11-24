package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.lexer.JlToken

open class JpInitializer(
    override var access: Int,
    open val body: MutableList<JlToken>
) : JpAttribute() {
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