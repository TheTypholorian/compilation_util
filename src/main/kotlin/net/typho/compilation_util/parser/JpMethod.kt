package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlIdentifier
import net.typho.compilation_util.lexer.JlKeyword
import net.typho.compilation_util.lexer.JlToken

open class JpMethod(
    override var access: JlKeyword?,
    override val modifiers: MutableList<JlKeyword>,
    override val annotations: MutableList<JpAnnotation>,
    override val typeArguments: MutableList<JpTypeArgument>,
    open var returnType: JpType,
    override val arguments: MutableList<JpArgument>,
    open var name: JlIdentifier,
    override val exceptions: MutableList<JpType>,
    override val body: MutableList<JlToken>?
) : JpCallable(), JpNode.WithExceptions, JpNode.WithTypeArguments {
    override fun fieldsToString(): String {
        return "$returnType ${name.text}(${arguments.joinToString { it.fieldsToString() }})"
    }
}