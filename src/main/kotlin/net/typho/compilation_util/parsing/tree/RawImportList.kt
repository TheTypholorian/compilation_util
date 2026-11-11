package net.typho.compilation_util.parsing.tree

class RawImportList : RawNode() {
    @JvmField
    val directImports = mutableListOf<RawImport>()
    @JvmField
    val wildcardImports = mutableListOf<RawImport>()

    fun getDirect(name: String): List<RawImport> {
        return directImports.filter { it.path.last() == name }
    }
}