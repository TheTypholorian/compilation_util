package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlIdentifier

open class JpTypeArgument(
    open var name: JlIdentifier,
    open val parents: MutableList<JpType>
) : JpNode() {
    override fun fieldsToString(): String {
        return if (parents.isEmpty()) {
            name.text.toString()
        } else {
            "${name.text} extends ${parents.joinToString { it.fieldsToString() }}"
        }
    }
}