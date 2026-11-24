package net.typho.compilation_util.java.parser

open class JpNestedAnnotationValue(
    value: JpAnnotation
) : JpAnnotationValue<JpAnnotation>(value) {
    override fun fieldsToString(): String {
        return "@" + value.fieldsToString()
    }
}