package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.lexer.JlText

open class JpTextAnnotationValue(
    value: JlText
) : JpAnnotationValue<JlText>(value) {
    override fun fieldsToString(): String {
        return value.text.toString()
    }
}