package net.typho.compilation_util.analyzer

import net.typho.parsing_util.error.DataReadException
import org.objectweb.asm.Type

@JvmName("plusNullable")
operator fun AnalyzeNode.Path?.plus(other: AnalyzeNode.Path?): AnalyzeNode.Path? {
    return if (this == null) {
        other
    } else if (other == null) {
        this
    } else {
        plus(other)
    }
}

@JvmName("plusNullable")
operator fun AnalyzeNode.Path?.plus(other: String?): AnalyzeNode.Path? {
    return if (this == null) {
        other?.let { AnalyzeNode.Path(it) }
    } else if (other == null) {
        this
    } else {
        plus(other)
    }
}

operator fun AnalyzeNode.Path?.plus(other: AnalyzeNode.Path): AnalyzeNode.Path {
    return this?.plus(other) ?: other
}

operator fun AnalyzeNode.Path?.plus(other: String): AnalyzeNode.Path {
    return this?.plus(other) ?: AnalyzeNode.Path(other)
}

abstract class AnalyzeNode {
    data class Path(
        @JvmField
        val path: String
    ) : AnalyzeNode() {
        constructor(tokens: List<CharSequence>) : this(if (tokens.isEmpty()) throw DataReadException("Empty path") else tokens.joinToString("."))

        constructor(type: Type) : this(type.className.replace('/', '.'))

        operator fun plus(other: Path) = Path("$path.$other")

        operator fun plus(other: String) = if (other.isEmpty()) this else Path("$path.$other")

        override fun toString(): String {
            return path
        }
    }

    data class Import(
        @JvmField
        val path: Path,
        @JvmField
        val isPackage: Boolean
    ) : AnalyzeNode() {
        constructor(path: Path) : this(
            if (path.path.endsWith(".*")) Path(path.path.removeSuffix(".*")) else path,
            path.path.endsWith(".*")
        )

        override fun toString(): String {
            return if (isPackage) "$path.*" else path.toString()
        }
    }
}