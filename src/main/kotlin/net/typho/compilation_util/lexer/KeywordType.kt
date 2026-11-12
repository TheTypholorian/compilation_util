package net.typho.compilation_util.lexer

class KeywordType private constructor(
    @JvmField
    val name: String,
    private val id: Int
) {
    companion object {
        private val allKeywords = mutableMapOf<String, KeywordType>()

        @JvmStatic
        fun get(name: String) = allKeywords.computeIfAbsent(name) { KeywordType(name, allKeywords.size) }

        @JvmField
        val ABSTRACT = get("abstract")
        @JvmField
        val ASSERT = get("assert")
        @JvmField
        val BOOLEAN = get("boolean")
        @JvmField
        val BREAK = get("break")
        @JvmField
        val BYTE = get("byte")
        @JvmField
        val CASE = get("case")
        @JvmField
        val CATCH = get("catch")
        @JvmField
        val CHAR = get("char")
        @JvmField
        val CLASS = get("class")
        @JvmField
        val CONST = get("const")
        @JvmField
        val CONTINUE = get("continue")
        @JvmField
        val DEFAULT = get("default")
        @JvmField
        val DO = get("do")
        @JvmField
        val DOUBLE = get("double")
        @JvmField
        val ELSE = get("else")
        @JvmField
        val ENUM = get("enum")
        @JvmField
        val EXTENDS = get("extends")
        @JvmField
        val FINAL = get("final")
        @JvmField
        val FINALLY = get("finally")
        @JvmField
        val FLOAT = get("float")
        @JvmField
        val FOR = get("for")
        @JvmField
        val GOTO = get("goto")
        @JvmField
        val IF = get("if")
        @JvmField
        val IMPLEMENTS = get("implements")
        @JvmField
        val IMPORT = get("import")
        @JvmField
        val INSTANCEOF = get("instanceof")
        @JvmField
        val INT = get("int")
        @JvmField
        val INTERFACE = get("interface")
        @JvmField
        val LONG = get("long")
        @JvmField
        val NATIVE = get("native")
        @JvmField
        val NEW = get("new")
        @JvmField
        val PACKAGE = get("package")
        @JvmField
        val PRIVATE = get("private")
        @JvmField
        val PROTECTED = get("protected")
        @JvmField
        val PUBLIC = get("public")
        @JvmField
        val RECORD = get("record")
        @JvmField
        val RETURN = get("return")
        @JvmField
        val SHORT = get("short")
        @JvmField
        val STATIC = get("static")
        @JvmField
        val STRICTFP = get("strictfp")
        @JvmField
        val SUPER = get("super")
        @JvmField
        val SWITCH = get("switch")
        @JvmField
        val SYNCHRONIZED = get("synchronized")
        @JvmField
        val THIS = get("this")
        @JvmField
        val THROW = get("throw")
        @JvmField
        val THROWS = get("throws")
        @JvmField
        val TRANSIENT = get("transient")
        @JvmField
        val TRY = get("try")
        @JvmField
        val VOID = get("void")
        @JvmField
        val VOLATILE = get("volatile")
        @JvmField
        val WHILE = get("while")

        @JvmField
        val ACCESS_KEYWORDS = listOf(PUBLIC, PROTECTED, PRIVATE)
        @JvmField
        val TYPE_KEYWORDS = listOf(BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE, VOID)
        @JvmField
        val CLASS_TYPE_KEYWORDS = listOf(CLASS, INTERFACE, ENUM, RECORD)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeywordType) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString() = name
}