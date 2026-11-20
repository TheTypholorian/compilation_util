package net.typho.compilation_util.analyzer

import net.typho.compilation_util.lexer.Keyword
import net.typho.compilation_util.lexer.LexerToken
import net.typho.compilation_util.parser.ParserNode
import net.typho.parsing_util.ParsingIterator
import net.typho.parsing_util.error.DataReadException
import org.objectweb.asm.Opcodes

object JavaAnalyzer {
    @JvmStatic
    fun analyze(file: ParserNode.File) {
        val pkg = file.pkg?.let { analyzePath(it) }
        val imports = file.imports.map { analyzePath(it) }

        for (cls in file.classes) {
            analyzeClass(pkg, imports, cls)
        }
    }

    @JvmStatic
    fun analyzeClass(pkg: AnalyzeNode.Path?, imports: List<AnalyzeNode.Path>, cls: ParserNode.Class) {
        val name = pkg + AnalyzeNode.Path(cls.name.text.toString())

        println("Class '$name'")

        for (path in imports) {
            println("Import '$path'")
        }

        var classAccess = when (cls.classType.type) {
            Keyword.CLASS -> 0
            Keyword.ENUM -> Opcodes.ACC_ENUM
            Keyword.INTERFACE -> Opcodes.ACC_INTERFACE
            Keyword.RECORD -> Opcodes.ACC_RECORD
            else -> throw DataReadException(cls.classType.toString())
        }

        cls.access?.let { access ->
            classAccess = classAccess or when (access.type) {
                Keyword.PUBLIC -> Opcodes.ACC_PUBLIC
                Keyword.PRIVATE -> Opcodes.ACC_PRIVATE
                Keyword.PROTECTED -> Opcodes.ACC_PROTECTED
                else -> throw DataReadException(access.toString())
            }
        }

        for (modifier in cls.modifiers) {
            classAccess = classAccess or when (modifier.type) {
                Keyword.STATIC -> TODO()
                Keyword.FINAL -> Opcodes.ACC_FINAL
                Keyword.ABSTRACT -> Opcodes.ACC_ABSTRACT
                else -> throw DataReadException(modifier.toString())
            }
        }

        println("Access '${classAccess.toString(2)}'")

        for (anno in cls.annotations) {
            val type = analyzePath()
        }
    }

    @JvmStatic
    fun analyzePath(path: ParserNode.Path): AnalyzeNode.Path {
        val folders = mutableListOf<CharSequence>()
        val iterator = ParsingIterator(path.tokens)

        while (iterator.hasNext()) {
            val token = iterator.next()

            if (token is LexerToken.Identifier) {
                folders.add(token.text)
            } else if (token is LexerToken.Punctuation && token.char == '*' && !iterator.hasNext()) {
                folders.add("*")
                break
            } else {
                throw DataReadException("Invalid token '$token' in path")
            }

            if (iterator.hasNext()) {
                val dot = iterator.next()

                if ((dot as? LexerToken.Punctuation)?.char != '.') {
                    throw DataReadException("Invalid token '$token' in path")
                }
            }
        }

        return AnalyzeNode.Path(folders) // TODO error is thrown in here
    }
}