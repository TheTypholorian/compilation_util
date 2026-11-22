package net.typho.compilation_util.analyzer

data class JaImport(
    @JvmField
    val path: JaPath,
    @JvmField
    val isPackage: Boolean
) : JaNode() {
    override fun toString(): String {
        return if (isPackage) "$path.*" else path.toString()
    }
}