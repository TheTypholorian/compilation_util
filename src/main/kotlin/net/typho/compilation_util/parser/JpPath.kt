package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlToken

open class JpPath(
    open val tokens: MutableList<JlToken>
) : JpNode() {
    val path: String
        get() = tokens.joinToString(separator = "") { it.text.toString() }

    override fun fieldsToString(): String {
        return path
    }
}