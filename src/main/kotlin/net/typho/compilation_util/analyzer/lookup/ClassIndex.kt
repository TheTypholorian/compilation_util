package net.typho.compilation_util.analyzer.lookup

import net.typho.compilation_util.analyzer.AnalyzeNode
import org.objectweb.asm.tree.ClassNode

interface ClassIndex {
    val name: AnalyzeNode.Path

    class OfNode(
        override val name: AnalyzeNode.Path,
        @JvmField
        val node: ClassNode
    ): ClassIndex
}