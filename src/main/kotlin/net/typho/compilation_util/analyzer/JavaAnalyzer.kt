package net.typho.compilation_util.analyzer

import net.typho.asm_util.AccessUtil.toAccessString
import net.typho.compilation_util.analyzer.lookup.ClassLookup
import net.typho.compilation_util.lexer.Keyword
import net.typho.compilation_util.lexer.LexerToken
import net.typho.compilation_util.parser.ParserNode
import net.typho.parsing_util.ParsingIterator
import net.typho.parsing_util.error.DataReadException
import org.objectweb.asm.Opcodes

object JavaAnalyzer {
    @JvmStatic
    fun analyze(file: ParserNode.File, lookup: ClassLookup.Supplier, defaultImports: List<AnalyzeNode.Import> = listOf(AnalyzeNode.Import(AnalyzeNode.Path("java.lang"), true))) {
        val pkg = file.pkg?.let { analyzePath(it) }
        val imports = file.imports.map { AnalyzeNode.Import(analyzePath(it)) }

        for (cls in file.classes) {
            analyzeClass(pkg, imports, cls, lookup.create(defaultImports + imports))
        }
    }

    @JvmStatic
    fun analyzeClass(pkg: AnalyzeNode.Path?, imports: List<AnalyzeNode.Import>, cls: ParserNode.Class, lookup: ClassLookup) {
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
                Keyword.STATIC -> TODO("static classes")
                Keyword.FINAL -> Opcodes.ACC_FINAL
                Keyword.ABSTRACT -> Opcodes.ACC_ABSTRACT
                else -> throw DataReadException(modifier.toString())
            }
        }

        println("Access '${classAccess.toAccessString()}'")

        for (anno in cls.annotations) {
            val type = lookup.resolve(analyzePath(anno.type))!!
            println("- Annotation with type '${type.name}' access '${type.access.toAccessString()}'")

            fun readAnnoValue(key: String, value: List<LexerToken>) {
                val method = type.methods.first { it.name == key && it.arguments.isEmpty() }
                val type = method.returnType
            }

            if (anno.values.size == 1) {
                val value = anno.values.single()
                readAnnoValue(value.key?.text?.toString() ?: "value", value.body)
            } else {
                anno.values.forEach { value -> readAnnoValue((value.key ?: throw DataReadException()).text.toString(), value.body) }
            }
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