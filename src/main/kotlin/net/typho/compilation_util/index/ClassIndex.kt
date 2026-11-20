package net.typho.compilation_util.index

import net.typho.compilation_util.analyzer.AnalyzeNode
import org.objectweb.asm.tree.ClassNode

interface ClassIndex {
    val name: AnalyzeNode.Path
    val access: Int
    val methods: List<MethodIndex>

    class OfNode(
        override val name: AnalyzeNode.Path,
        @JvmField
        val node: ClassNode
    ): ClassIndex {
        override val access: Int
            get() = node.access
        override val methods = node.methods.map { MethodIndex.OfNode(it) }
    }
}