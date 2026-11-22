package net.typho.compilation_util.parser

import net.typho.compilation_util.lexer.JlKeyword

abstract class JpAttribute : JpNode() {
    abstract var access: JlKeyword?
    abstract val modifiers: MutableList<JlKeyword>
}