package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.JPath

open class JpType(
    override val annotations: MutableList<JpAnnotation>,
    open var path: JPath,
    open val arguments: MutableList<JpTypeArgumentValue>
) : JpNode(), JpNode.WithAnnotations {
    override fun fieldsToString(): String {
        return if (arguments.isEmpty()) {
            path.toString()
        } else {
            "$path<${arguments.joinToString { it.fieldsToString() }}>"
        }
    }

    override fun toString(): String {
        return fieldsToString()
    }
}