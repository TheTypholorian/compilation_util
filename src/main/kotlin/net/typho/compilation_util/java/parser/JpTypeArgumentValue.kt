package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.TypeArgumentRelativity

open class JpTypeArgumentValue(
    open var type: JpType,
    open var relativity: TypeArgumentRelativity?
) : JpNode() {
    override fun fieldsToString(): String {
        return relativity?.let { relativity ->
            "? ${relativity.name.lowercase()} ${type.fieldsToString()}"
        } ?: type.fieldsToString()
    }

}