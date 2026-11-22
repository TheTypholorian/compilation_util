package net.typho.compilation_util.parser

open class JpFile(
    open var pkg: JpPath?,
    open val imports: MutableList<JpPath>,
    open val classes: MutableList<JpClass>
) : JpNode() {
    override fun fieldsToString() = "package ${pkg?.path}, imports ${imports.map { it.path }}, $classes"
}