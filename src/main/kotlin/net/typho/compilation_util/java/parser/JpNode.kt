package net.typho.compilation_util.java.parser

import net.typho.asm_util.AccessUtil.toAccessString
import net.typho.compilation_util.java.ExtraOpcodes.toExtraAccessString

abstract class JpNode {
    abstract fun fieldsToString(): String

    override fun toString(): String {
        return buildString {
            if (this@JpNode is WithAnnotations) {
                for (annotation in annotations) {
                    append('@')
                    append(annotation.fieldsToString())
                    append(' ')
                }
            }

            if (this@JpNode is JpAttribute) {
                append(access.toExtraAccessString())

                if (access != 0) {
                    append(' ')
                }
            }

            append(this@JpNode.javaClass.simpleName)
            append('(')
            append(fieldsToString())
            append(')')

            if (this@JpNode is WithExceptions && exceptions.isNotEmpty()) {
                append(" throws ")
                exceptions.joinTo(this) { it.fieldsToString() }
            }
        }
    }

    interface WithAnnotations {
        val annotations: MutableList<JpAnnotation>
    }

    interface WithTypeArguments {
        val typeArguments: MutableList<JpTypeArgument>
    }

    interface WithExceptions {
        val exceptions: MutableList<JpType>
    }
}