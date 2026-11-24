package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.JPath
import net.typho.compilation_util.java.lexer.JlIdentifier

open class JpAnnotation(
    open var type: JPath,
    open val values: MutableList<Pair<JlIdentifier?, JpAnnotationValue<*>>>
) : JpNode() {
    override fun fieldsToString(): String {
        return "$type(${values.joinToString { (key, value) ->
            if (key == null) {
                value.fieldsToString()
            } else {
                "${key.text} = ${value.fieldsToString()}"
            }
        }})"
    }
}