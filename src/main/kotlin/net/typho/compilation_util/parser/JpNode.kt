package net.typho.compilation_util.parser

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
                if (this@JpNode !is JpInitializer) {
                    append(access?.type?.name ?: "package-private")
                    append(' ')
                }

                for (keyword in modifiers) {
                    append(keyword.type.name)
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