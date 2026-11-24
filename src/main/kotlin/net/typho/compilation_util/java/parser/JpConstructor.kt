package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.lexer.JlToken

open class JpConstructor(
    override var access: Int,
    override val annotations: MutableList<JpAnnotation>,
    override val arguments: MutableList<JpArgument>,
    override val exceptions: MutableList<JpType>,
    override val body: MutableList<JlToken>
) : JpCallable(), JpNode.WithExceptions {
    override fun fieldsToString(): String {
        return arguments.joinToString { it.fieldsToString() }
    }
}