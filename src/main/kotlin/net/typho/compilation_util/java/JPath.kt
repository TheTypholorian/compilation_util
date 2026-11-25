package net.typho.compilation_util.java

import net.typho.compilation_util.java.analyzer.lookup.ClassLookup
import net.typho.parsing_util.error.DataReadException
import org.objectweb.asm.Type

@JvmName("plusNullable")
operator fun JPath?.plus(other: JPath?): JPath? {
    return if (this == null) {
        other
    } else if (other == null) {
        this
    } else {
        plus(other)
    }
}

@JvmName("plusNullable")
operator fun JPath?.plus(other: String?): JPath? {
    return if (this == null) {
        other?.let { JPath(it) }
    } else if (other == null) {
        this
    } else {
        plus(other)
    }
}

operator fun JPath?.plus(other: JPath): JPath {
    return this?.plus(other) ?: other
}

operator fun JPath?.plus(other: String): JPath {
    return this?.plus(other) ?: JPath(other)
}

/**
 * Classes represented as their [Class.getSimpleName].
 * Arrays will not be stored in this class, they will be stored as a [net.typho.compilation_util.java.analyzer.JaType], as `$ARRAY<[component type]>`
 */
data class JPath(
    @JvmField
    val path: String
) {
    constructor(tokens: List<CharSequence>) : this(if (tokens.isEmpty()) throw DataReadException("Empty path") else tokens.joinToString("."))

    constructor(type: Type) : this(type.className.replace('/', '.'))

    init {
        if (path.endsWith("[]")) {
            throw IllegalArgumentException("Created an array JPath, use JaType.arrayOf() instead")
        }
    }

    fun isSubclassOf(parent: JPath, lookup: ClassLookup): Boolean? {
        var child = lookup.resolve(this) ?: return null
        val parent = lookup.resolve(parent) ?: return null

        TODO()
    }

    operator fun plus(other: JPath) = JPath("$path.$other")

    operator fun plus(other: String) = if (other.isEmpty()) this else JPath("$path.$other")

    override fun toString(): String {
        return path
    }
}