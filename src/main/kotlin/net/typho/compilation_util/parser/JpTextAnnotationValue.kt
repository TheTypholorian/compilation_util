package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlText

open class JpTextAnnotationValue(
    value: JlText
) : JpAnnotationValue<JlText>(value) {
    override fun fieldsToString(): String {
        return value.text.toString()
    }
}