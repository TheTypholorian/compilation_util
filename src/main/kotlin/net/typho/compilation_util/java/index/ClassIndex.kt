package net.typho.compilation_util.java.index

import net.typho.compilation_util.java.JPath
import org.objectweb.asm.tree.ClassNode

interface ClassIndex {
    val name: JPath
    val access: Int
    val methods: List<MethodIndex>
    val superClass: JPath?
    val interfaces: List<JPath>

    class OfNode(
        override val name: JPath,
        @JvmField
        val node: ClassNode
    ): ClassIndex {
        override val access: Int
            get() = node.access
        override val methods = node.methods.map { MethodIndex.OfNode(it) }
        override val superClass = node.superName?.let { JPath(it.replace('/', '.')) }
        override val interfaces = node.interfaces?.map { JPath(it.replace('/', '.')) } ?: listOf()
    }
}