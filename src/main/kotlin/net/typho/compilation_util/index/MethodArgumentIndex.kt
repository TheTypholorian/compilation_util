package net.typho.compilation_util.index

import net.typho.compilation_util.analyzer.JaPath

interface MethodArgumentIndex {
    val type: JaPath // TODO type params

    data class Impl(
        override val type: JaPath
    ) : MethodArgumentIndex
}