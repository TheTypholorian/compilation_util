package net.typho.compilation_util.index

import net.typho.compilation_util.analyzer.JaPath
import org.objectweb.asm.tree.ClassNode

interface ClassIndex {
    val name: JaPath
    val access: Int
    val methods: List<MethodIndex>

    class OfNode(
        override val name: JaPath,
        @JvmField
        val node: ClassNode
    ): ClassIndex {
        override val access: Int
            get() = node.access
        override val methods = node.methods.map { MethodIndex.OfNode(it) }
    }
}