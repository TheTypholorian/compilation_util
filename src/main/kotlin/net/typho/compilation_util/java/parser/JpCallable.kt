package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.lexer.JlToken

abstract class JpCallable : JpAttribute(), JpNode.WithAnnotations {
    abstract val body: MutableList<JlToken>?
    abstract val arguments: MutableList<JpArgument>

    override fun toString(): String {
        val body = body

        return if (body.isNullOrEmpty()) {
            super.toString()
        } else {
            super.toString() + "\n" + body.joinToString(" ") { it.text }.prependIndent("  ")
        }
    }
}