package net.typho.compilation_util.parser

abstract class JpAnnotationValue<V>(
    open val value: V
) : JpNode()