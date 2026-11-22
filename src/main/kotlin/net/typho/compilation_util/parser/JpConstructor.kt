package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlKeyword
import net.typho.compilation_util.lexer.JlToken

open class JpConstructor(
    override var access: JlKeyword?,
    override val modifiers: MutableList<JlKeyword>,
    override val annotations: MutableList<JpAnnotation>,
    override val arguments: MutableList<JpArgument>,
    override val exceptions: MutableList<JpType>,
    override val body: MutableList<JlToken>
) : JpCallable(), JpNode.WithExceptions {
    override fun fieldsToString(): String {
        return arguments.joinToString { it.fieldsToString() }
    }
}