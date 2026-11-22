package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlIdentifier
import net.typho.compilation_util.lexer.JlKeyword
import net.typho.compilation_util.lexer.JlToken

open class JpField(
    override var access: JlKeyword?,
    override val modifiers: MutableList<JlKeyword>,
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