package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlToken

open class JpUnknownAnnotationValue(
    value: List<JlToken>
) : JpAnnotationValue<List<JlToken>>(value) {
    override fun fieldsToString(): String {
        return value.joinToString(" ") { it.text.toString() }
    }
}