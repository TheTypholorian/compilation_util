package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlIdentifier
import net.typho.compilation_util.lexer.JlKeyword

open class JpClass(
    override var access: JlKeyword?,
    override val modifiers: MutableList<JlKeyword>,
    override val annotations: MutableList<JpAnnotation>,
    open var classType: JlKeyword,
    open var name: JlIdentifier,
    override val typeArguments: MutableList<JpTypeArgument>,
    open var superClass: JpType?,
    open val interfaces: MutableList<JpType>,
    open val permits: MutableList<JpType>,
    open val attributes: MutableList<JpAttribute>
) : JpAttribute(), JpNode.WithAnnotations, JpNode.WithTypeArguments {
    override fun fieldsToString(): String {
        return "${classType.text} ${name.text} extends $superClass implements $interfaces"
    }

    override fun toString(): String {
        return if (attributes.isEmpty()) {
            super.toString()
        } else {
            super.toString() + "\n" + attributes.joinToString("\n").prependIndent("  ") + "\n"
        }
    }
}