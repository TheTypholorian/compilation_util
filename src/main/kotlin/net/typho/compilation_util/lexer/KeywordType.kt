package net.typho.compilation_util.lexer

interface KeywordType {
    companion object {
        @JvmField
        val all: Map<String, KeywordType> = (Access.entries + Modifier.entries + Primitive.entries + SelfReference.entries + HeaderValue.entries + Operation.entries + ClassType.entries + ClassModifier.entries).associateBy { it.id }
    }

    val id: String

    enum class Access : KeywordType {
        PUBLIC,
        PROTECTED,
        PRIVATE;

        override val id = name.lowercase()
    }

    enum class Modifier : KeywordType {
        ABSTRACT,
        CONST,
        FINAL,
        NATIVE,
        STATIC,
        STRICTFP,
        SYNCHRONIZED,
        THROWS,
        TRANSIENT,
        VOLATILE;

        override val id = name.lowercase()
    }

    enum class Primitive : KeywordType {
        BYTE,
        SHORT,
        CHAR,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        VOID;

        override val id = name.lowercase()
    }

    enum class SelfReference : KeywordType {
        THIS,
        SUPER;

        override val id = name.lowercase()
    }

    enum class HeaderValue : KeywordType {
        PACKAGE,
        IMPORT;

        override val id = name.lowercase()
    }

    enum class Operation : KeywordType {
        ASSERT,
        BREAK,
        CASE,
        CATCH,
        CONTINUE,
        DEFAULT,
        DO,
        ELSE,
        FINALLY,
        FOR,
        GOTO,
        IF,
        INSTANCEOF,
        NEW,
        RETURN,
        SWITCH,
        THROW,
        TRY,
        WHILE;

        override val id = name.lowercase()
    }

    enum class ClassType : KeywordType {
        CLASS,
        ENUM,
        INTERFACE,
        RECORD;

        override val id = name.lowercase()
    }

    enum class ClassModifier : KeywordType {
        EXTENDS,
        IMPLEMENTS;

        override val id = name.lowercase()
    }
}