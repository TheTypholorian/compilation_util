package net.typho.compilation_util.java.index

import net.typho.compilation_util.java.analyzer.JaType

interface MethodArgumentIndex {
    val type: JaType

    data class Impl(
        override val type: JaType
    ) : MethodArgumentIndex
}