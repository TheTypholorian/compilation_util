package net.typho.compilation_util.java.analyzer

import net.typho.compilation_util.java.JPath

/**
 * Package imports (ex. `java.lang.*`) will be stored as `JaImport("java.lang", true)`
 */
data class JaImport(
    @JvmField
    val path: JPath,
    @JvmField
    val isPackage: Boolean
) : JaNode() {
    override fun toString(): String {
        return if (isPackage) "$path.*" else path.toString()
    }
}