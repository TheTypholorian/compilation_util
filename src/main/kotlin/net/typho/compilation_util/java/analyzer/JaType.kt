package net.typho.compilation_util.java.analyzer

import net.typho.compilation_util.java.JPath

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
        fun array(
            path: JPath,
            //arrayAnnotations: MutableList<JaAnnotation>,
            //componentAnnotations: MutableList<JaAnnotation>,
            arguments: MutableList<JaTypeArgumentValue>
        ) = JaType(/* arrayAnnotations */ ARRAY, mutableListOf(JaTypeArgumentValue(JaType(/* componentAnnotations */ path, arguments), null)))
    }
}