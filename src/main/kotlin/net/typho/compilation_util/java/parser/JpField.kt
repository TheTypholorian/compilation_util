package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.lexer.JlIdentifier
import net.typho.compilation_util.java.lexer.JlToken

open class JpField(
    override var access: Int,
    override val annotations: MutableList<JpAnnotation>,
    open var type: JpType,
    open var name: JlIdentifier,
    open val initializer: MutableList<JlToken>?
) : JpAttribute(), JpNode.WithAnnotations {
    override fun fieldsToString(): String {
        return "$type ${name.text}"
    }

    override fun toString(): String {
        val initializer = initializer

        return if (initializer.isNullOrEmpty()) {
            super.toString()
        } else {
            super.toString() + "\n" + initializer.joinToString("\n").prependIndent("  ")
        }
    }
}