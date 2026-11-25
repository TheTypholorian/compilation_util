package net.typho.compilation_util.java.analyzer

import net.typho.compilation_util.java.JPath
import net.typho.compilation_util.java.analyzer.lookup.ClassLookup
import org.objectweb.asm.Type

data class JaType(
    //@JvmField
    //val annotations: MutableList<JaAnnotation>,
    @JvmField
    val path: JPath,
    @JvmField
    val arguments: MutableList<JaTypeArgumentValue>
) : JaNode() {
    companion object {
        @JvmField
        val ARRAY = JPath($$"$ARRAY")

        @JvmStatic
        fun of(type: Type, arguments: MutableList<JaTypeArgumentValue>): JaType = if (type.sort == Type.ARRAY) {
            var component = of(type.elementType, arguments)

            repeat(type.dimensions) {
                component = arrayOf(component)
            }

            component
        } else {
            JaType(JPath(type /* TODO primitives? kotlin? */), arguments)
        }

        @JvmStatic
        fun arrayOf(
            component: JaType,
            //arrayAnnotations: MutableList<JaAnnotation>
        ) = JaType(/* arrayAnnotations */ ARRAY, mutableListOf(JaTypeArgumentValue(component, null)))
    }

    fun canBeUsedAs(other: JaType, lookup: ClassLookup): Boolean {
        if (this == other) {
            return true
        }

        if (!path.isSubclassOf(other.path, lookup)!!) {
            return false
        }

        TODO()
    }

    override fun toString(): String {
        return if (path == ARRAY && arguments.size == 1) {
            arguments[0].type.toString() + "[]"
        } else if (arguments.isEmpty()) {
            path.toString()
        } else {
            "$path<${arguments.joinToString()}>"
        }
    }
}