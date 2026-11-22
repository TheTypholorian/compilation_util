package net.typho.compilation_util.lexer

class KeywordType private constructor(
    @JvmField
    val name: String
) {
    companion object {
        private val all = mutableMapOf<String, KeywordType>()
        @JvmField
        val access = mutableSetOf<KeywordType>()
        @JvmField
        val modifiers = mutableSetOf<KeywordType>()
        @JvmField
        val primitives = mutableSetOf<KeywordType>()
        @JvmField
        val classTypes = mutableSetOf<KeywordType>()

        @JvmStatic
        operator fun get(name: String) = all[name]

        @JvmStatic
        fun create(name: String) = all.computeIfAbsent(name) { KeywordType(name) }

        @JvmField
        val ABSTRACT = create("abstract").also { modifiers.add(it) }
        @JvmField
        val ASSERT = create("assert")
        @JvmField
        val BOOLEAN = create("boolean").also { primitives.add(it) }
        @JvmField
        val BREAK = create("break")
        @JvmField
        val BYTE = create("byte").also { primitives.add(it) }
        @JvmField
        val CASE = create("case")
        @JvmField
        val CATCH = create("catch")
        @JvmField
        val CHAR = create("char").also { primitives.add(it) }
        @JvmField
        val CLASS = create("class").also { classTypes.add(it) }
        @JvmField
        val CONST = create("const").also { modifiers.add(it) }
        @JvmField
        val CONTINUE = create("continue")
        @JvmField
        val DEFAULT = create("default").also { modifiers.add(it) }
        @JvmField
        val DO = create("do")
        @JvmField
        val DOUBLE = create("double").also { primitives.add(it) }
        @JvmField
        val ELSE = create("else")
        @JvmField
        val ENUM = create("enum").also { classTypes.add(it) }
        @JvmField
        val EXTENDS = create("extends")
        @JvmField
        val FINAL = create("final").also { modifiers.add(it) }
        @JvmField
        val FINALLY = create("finally")
        @JvmField
        val FLOAT = create("float").also { primitives.add(it) }
        @JvmField
        val FOR = create("for")
        @JvmField
        val GOTO = create("goto")
        @JvmField
        val IF = create("if")
        @JvmField
        val IMPLEMENTS = create("implements")
        @JvmField
        val IMPORT = create("import")
        @JvmField
        val INSTANCEOF = create("instanceof")
        @JvmField
        val INT = create("int").also { primitives.add(it) }
        @JvmField
        val INTERFACE = create("interface").also { classTypes.add(it) }
        @JvmField
        val LONG = create("long").also { primitives.add(it) }
        @JvmField
        val NATIVE = create("native").also { modifiers.add(it) }
        @JvmField
        val NEW = create("new")
        @JvmField
        val NON_SEALED = create("non-sealed").also { modifiers.add(it) }
        @JvmField
        val PACKAGE = create("package")
        @JvmField
        val PERMITS = create("permits")
        @JvmField
        val PRIVATE = create("private").also { access.add(it) }
        @JvmField
        val PROTECTED = create("protected").also { access.add(it) }
        @JvmField
        val PUBLIC = create("public").also { access.add(it) }
        @JvmField
        val RECORD = create("record").also { classTypes.add(it) }
        @JvmField
        val RETURN = create("return")
        @JvmField
        val SEALED = create("sealed").also { modifiers.add(it) }
        @JvmField
        val SHORT = create("short").also { primitives.add(it) }
        @JvmField
        val STATIC = create("static").also { modifiers.add(it) }
        @JvmField
        val STRICTFP = create("strictfp").also { modifiers.add(it) }
        @JvmField
        val SUPER = create("super")
        @JvmField
        val SWITCH = create("switch")
        @JvmField
        val SYNCHRONIZED = create("synchronized").also { modifiers.add(it) }
        @JvmField
        val THIS = create("this")
        @JvmField
        val THROW = create("throw")
        @JvmField
        val THROWS = create("throws").also { modifiers.add(it) }
        @JvmField
        val TRANSIENT = create("transient").also { modifiers.add(it) }
        @JvmField
        val TRY = create("try")
        @JvmField
        val VOID = create("void").also { primitives.add(it) }
        @JvmField
        val VOLATILE = create("volatile").also { modifiers.add(it) }
        @JvmField
        val WHILE = create("while")
    }
}