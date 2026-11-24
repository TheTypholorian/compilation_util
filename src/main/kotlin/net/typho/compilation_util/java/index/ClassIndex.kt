package net.typho.compilation_util.java.index

import net.typho.compilation_util.java.JPath
import org.objectweb.asm.tree.ClassNode

interface ClassIndex {
    val name: JPath
    val access: Int
    val methods: List<MethodIndex>

    class OfNode(
        override val name: JPath,
        @JvmField
        val node: ClassNode
    ): ClassIndex {
        override val access: Int
            get() = node.access
        override val methods = node.methods.map { MethodIndex.OfNode(it) }
    }
}