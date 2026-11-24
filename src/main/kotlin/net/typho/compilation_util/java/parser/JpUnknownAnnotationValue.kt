package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.lexer.JlToken

open class JpUnknownAnnotationValue(
    value: List<JlToken>
) : JpAnnotationValue<List<JlToken>>(value) {
    override fun fieldsToString(): String {
        return value.joinToString(" ") { it.text.toString() }
    }
}