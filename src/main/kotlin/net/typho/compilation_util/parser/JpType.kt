package net.typho.compilation_util.parser

open class JpType(
    override val annotations: MutableList<JpAnnotation>,
    open var path: JpPath,
    open val arguments: MutableList<JpTypeArgumentValue>
) : JpNode(), JpNode.WithAnnotations {
    override fun fieldsToString(): String {
        return if (arguments.isEmpty()) {
            path.fieldsToString()
        } else {
            "${path.fieldsToString()}<${arguments.joinToString { it.fieldsToString() }}>"
        }
    }

    override fun toString(): String {
        return fieldsToString()
    }
}