package net.typho.compilation_util.java.index

import net.typho.compilation_util.java.JPath

interface MethodArgumentIndex {
    val type: JPath // TODO type params

    data class Impl(
        override val type: JPath
    ) : MethodArgumentIndex
}