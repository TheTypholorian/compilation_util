package net.typho.compilation_util.parsing.tree

data class RawImport(
    @JvmField
    val path: List<String>
)