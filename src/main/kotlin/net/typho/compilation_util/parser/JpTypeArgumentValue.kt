package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlKeyword

open class JpTypeArgumentValue(
    open var type: JpType,
    open var relativity: JlKeyword?
) : JpNode() {
    override fun fieldsToString(): String {
        return if (relativity == null) {
            type.fieldsToString()
        } else {
            "? ${relativity!!.text} ${type.fieldsToString()}"
        }
    }
}