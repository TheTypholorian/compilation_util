package net.typho.compilation_util.analyzer

import net.typho.parsing_util.error.DataReadException

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
    open class Path(
        open val path: String
    ) : AnalyzeNode() {
        constructor(tokens: List<CharSequence>) : this(if (tokens.isEmpty()) throw DataReadException("Empty path") else tokens.joinToString("."))

        operator fun plus(other: Path) = Path("$path.$other")

        operator fun plus(other: String) = Path("$path.$other")

        override fun toString(): String {
            return path
        }
    }
}