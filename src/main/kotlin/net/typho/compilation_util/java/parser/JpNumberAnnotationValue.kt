package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.lexer.JlNumber

open class JpNumberAnnotationValue(
    value: JlNumber
) : JpAnnotationValue<JlNumber>(value) {
    override fun fieldsToString(): String {
        return value.text.toString()
    }
}