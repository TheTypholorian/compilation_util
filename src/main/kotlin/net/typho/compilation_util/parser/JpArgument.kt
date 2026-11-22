package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlIdentifier

open class JpArgument(
    open var type: JpType,
    open var name: JlIdentifier
) : JpNode() {
    override fun fieldsToString(): String {
        return "$type ${name.text}"
    }
}