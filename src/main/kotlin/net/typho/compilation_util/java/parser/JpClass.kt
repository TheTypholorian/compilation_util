package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.lexer.JlIdentifier

open class JpClass(
    override var access: Int,
    override val annotations: MutableList<JpAnnotation>,
    open var name: JlIdentifier,
    override val typeArguments: MutableList<JpTypeArgument>,
    open var superClass: JpType?,
    open val interfaces: MutableList<JpType>,
    open val permits: MutableList<JpType>,
    open val attributes: MutableList<JpAttribute>
) : JpAttribute(), JpNode.WithAnnotations, JpNode.WithTypeArguments {
    override fun fieldsToString(): String {
        return "${name.text} extends $superClass implements $interfaces permits $permits"
    }

    override fun toString(): String {
        return if (attributes.isEmpty()) {
            super.toString()
        } else {
            super.toString() + "\n" + attributes.joinToString("\n").prependIndent("  ") + "\n"
        }
    }
}