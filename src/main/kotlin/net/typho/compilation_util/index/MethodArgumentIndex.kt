package net.typho.compilation_util.index

import net.typho.compilation_util.analyzer.AnalyzeNode

interface MethodArgumentIndex {
    val type: AnalyzeNode.Path // TODO type params

    data class Impl(
        override val type: AnalyzeNode.Path
    ) : MethodArgumentIndex
}