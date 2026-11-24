package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.JPath

open class JpFile(
    open var pkg: JPath?,
    open val imports: MutableList<JPath>,
    open val classes: MutableList<JpClass>
) : JpNode() {
    override fun fieldsToString() = "package ${pkg?.path}, imports ${imports.map { it.path }}, $classes"
}