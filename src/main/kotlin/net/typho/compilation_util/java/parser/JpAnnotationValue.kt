package net.typho.compilation_util.java.parser

abstract class JpAnnotationValue<V>(
    open val value: V
) : JpNode()