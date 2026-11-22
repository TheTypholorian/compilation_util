package net.typho.compilation_util.analyzer

import net.typho.parsing_util.error.DataReadException
import org.objectweb.asm.Type

@JvmName("plusNullable")
operator fun JaPath?.plus(other: JaPath?): JaPath? {
    return if (this == null) {
        other
    } else if (other == null) {
        this
    } else {
        plus(other)
    }
}

@JvmName("plusNullable")
operator fun JaPath?.plus(other: String?): JaPath? {
    return if (this == null) {
        other?.let { JaPath(it) }
    } else if (other == null) {
        this
    } else {
        plus(other)
    }
}

operator fun JaPath?.plus(other: JaPath): JaPath {
    return this?.plus(other) ?: other
}

operator fun JaPath?.plus(other: String): JaPath {
    return this?.plus(other) ?: JaPath(other)
}

data class JaPath(
    @JvmField
    val path: String
) : JaNode() {
    constructor(tokens: List<CharSequence>) : this(if (tokens.isEmpty()) throw DataReadException("Empty path") else tokens.joinToString("."))

    constructor(type: Type) : this(type.className.replace('/', '.'))

    operator fun plus(other: JaPath) = JaPath("$path.$other")

    operator fun plus(other: String) = if (other.isEmpty()) this else JaPath("$path.$other")

    override fun toString(): String {
        return path
    }
}