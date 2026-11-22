package net.typho.compilation_util.index

import net.typho.compilation_util.analyzer.JaPath
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode

interface MethodIndex {
    val name: String
    val access: Int
    val arguments: List<MethodArgumentIndex>
    val returnType: JaPath

    class OfNode(
        @JvmField
        val node: MethodNode
    ): MethodIndex {
        override val name: String
            get() = node.name
        override val access: Int
            get() = node.access
        override val arguments = Type.getArgumentTypes(node.desc).map { MethodArgumentIndex.Impl(JaPath(it)) }
        override val returnType = JaPath(Type.getReturnType(node.desc))
    }
}