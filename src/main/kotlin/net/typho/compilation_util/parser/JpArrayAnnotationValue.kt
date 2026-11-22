package net.typho.compilation_util.parser

open class JpArrayAnnotationValue(
    value: /* TODO */ List<JpAnnotationValue<*>>
) : JpAnnotationValue<List<JpAnnotationValue<*>>>(value) {
    override fun fieldsToString(): String {
        return value.joinToString { it.fieldsToString() }
    }
}