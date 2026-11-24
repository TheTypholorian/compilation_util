package net.typho.compilation_util.java

import net.typho.asm_util.AccessUtil.toAccessString

object ExtraOpcodes {
    const val ACC_SEALED = 0x40000
    const val ACC_NON_SEALED = 0x80000

    fun Int.toExtraAccessString(): String {
        return buildList {
            add(toAccessString())
            if (isSealed()) add("sealed")
            if (isNonSealed()) add("non-sealed")
        }.joinToString(separator = " ")
    }

    fun Int.isSealed() = (this and ACC_SEALED) != 0

    fun Int.isNonSealed() = (this and ACC_NON_SEALED) != 0
}