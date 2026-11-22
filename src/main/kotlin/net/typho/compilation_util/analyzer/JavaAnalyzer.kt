package net.typho.compilation_util.analyzer

import net.typho.asm_util.AccessUtil.toAccessString
import net.typho.compilation_util.analyzer.lookup.ClassLookup
import net.typho.compilation_util.lexer.JlIdentifier
import net.typho.compilation_util.lexer.JlPunctuation
import net.typho.compilation_util.lexer.KeywordType
import net.typho.compilation_util.parser.JpAnnotationValue
import net.typho.compilation_util.parser.JpClass
import net.typho.compilation_util.parser.JpFile
import net.typho.compilation_util.parser.JpPath
import net.typho.parsing_util.ParsingIterator
import net.typho.parsing_util.error.DataReadException
import org.objectweb.asm.Opcodes

object JavaAnalyzer {
    @JvmStatic
    fun analyze(file: JpFile, lookup: ClassLookup.Supplier, defaultImports: List<JaImport> = listOf(JaImport(JaPath("java.lang"), true))) {
        val pkg = file.pkg?.let { analyzePath(it) }
        val imports = file.imports.map { analyzeImport(it) }

        for (cls in file.classes) {
            analyzeClass(pkg, imports, cls, lookup.create(defaultImports + imports))
        }
    }

    @JvmStatic
    fun analyzeClass(pkg: JaPath?, imports: List<JaImport>, cls: JpClass, lookup: ClassLookup) {
        val name = pkg + JaPath(cls.name.text.toString())

        println("Class '$name'")

        for (path in imports) {
            println("Import '$path'")
        }

        var classAccess = when (cls.classType.type) {
            KeywordType.CLASS -> 0
            KeywordType.ENUM -> Opcodes.ACC_ENUM
            KeywordType.INTERFACE -> Opcodes.ACC_INTERFACE
            KeywordType.RECORD -> Opcodes.ACC_RECORD
            else -> throw DataReadException(cls.classType.toString())
        }

        cls.access?.let { access ->
            classAccess = classAccess or when (access.type) {
                KeywordType.PUBLIC -> Opcodes.ACC_PUBLIC
                KeywordType.PRIVATE -> Opcodes.ACC_PRIVATE
                KeywordType.PROTECTED -> Opcodes.ACC_PROTECTED
                else -> throw DataReadException(access.toString())
            }
        }

        for (modifier in cls.modifiers) {
            classAccess = classAccess or when (modifier.type) {
                KeywordType.STATIC -> TODO("static classes")
                KeywordType.FINAL -> Opcodes.ACC_FINAL
                KeywordType.ABSTRACT -> Opcodes.ACC_ABSTRACT
                else -> throw DataReadException(modifier.toString())
            }
        }

        println("Access '${classAccess.toAccessString()}'")

        for (anno in cls.annotations) {
            val type = lookup.resolve(analyzePath(anno.type))!!
            println("Annotation with type '${type.name}' access '${type.access.toAccessString()}'")

            fun readAnnoValue(key: String, value: JpAnnotationValue<*>) {
                val method = type.methods.first { it.name == key && it.arguments.isEmpty() }
                println("Expected '${method.returnType}' got '$value'")
            }

            if (anno.values.size == 1) {
                val (key, body) = anno.values.single()
                readAnnoValue(key?.text?.toString() ?: "value", body)
            } else {
                anno.values.forEach { (key, body) -> readAnnoValue((key ?: throw DataReadException()).text.toString(), body) }
            }
        }
    }

    @JvmStatic
    fun analyzeImport(path: JpPath): JaImport {
        val folders = mutableListOf<CharSequence>()
        val iterator = ParsingIterator(path.tokens)
        var isPackage = false

        while (iterator.hasNext()) {
            val token = iterator.next()

            if (token is JlIdentifier) {
                folders.add(token.text)
            } else if (token is JlPunctuation && token.char == '*' && !iterator.hasNext()) {
                isPackage = true
                break
            } else {
                throw DataReadException("Invalid token '$token' in path")
            }

            if (iterator.hasNext()) {
                val dot = iterator.next()

                if ((dot as? JlPunctuation)?.char != '.') {
                    throw DataReadException("Invalid token '$token' in path")
                }
            }
        }

        return JaImport(JaPath(folders), isPackage) // TODO error is thrown in here
    }

    @JvmStatic
    fun analyzePath(path: JpPath): JaPath {
        val folders = mutableListOf<CharSequence>()
        val iterator = ParsingIterator(path.tokens)

        while (iterator.hasNext()) {
            val token = iterator.next()

            if (token is JlIdentifier) {
                folders.add(token.text)
            } else if (token is JlPunctuation && token.char == '*' && !iterator.hasNext()) {
                folders.add("*")
                break
            } else {
                throw DataReadException("Invalid token '$token' in path")
            }

            if (iterator.hasNext()) {
                val dot = iterator.next()

                if ((dot as? JlPunctuation)?.char != '.') {
                    throw DataReadException("Invalid token '$token' in path")
                }
            }
        }

        return JaPath(folders) // TODO error is thrown in here
    }
}