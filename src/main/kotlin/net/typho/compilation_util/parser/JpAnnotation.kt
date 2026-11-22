package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlIdentifier

open class JpAnnotation(
    open var type: JpPath,
    open val values: MutableList<Pair<JlIdentifier?, JpAnnotationValue<*>>>
) : JpNode() {
    override fun fieldsToString(): String {
        return "${type.fieldsToString()}(${values.joinToString { (key, value) ->
            if (key == null) {
                value.fieldsToString()
            } else {
                "${key.text} = ${value.fieldsToString()}"
            }
        }})"
    }
}