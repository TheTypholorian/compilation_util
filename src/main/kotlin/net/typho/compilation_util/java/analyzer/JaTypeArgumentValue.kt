package net.typho.compilation_util.java.analyzer

import net.typho.compilation_util.java.TypeArgumentRelativity

data class JaTypeArgumentValue(
    @JvmField
    val type: JaType,
    @JvmField
    val relativity: TypeArgumentRelativity?
) : JaNode() {
    override fun toString(): String {
        return relativity?.let { relativity ->
            "? ${relativity.name.lowercase()} $type"
        } ?: type.toString()
    }
}