package net.typho.compilation_util.java.analyzer

import net.typho.compilation_util.java.ExtraOpcodes.toExtraAccessString
import net.typho.compilation_util.java.JPath
import net.typho.compilation_util.java.analyzer.lookup.ClassLookup
import net.typho.compilation_util.java.parser.JpAnnotationValue
import net.typho.compilation_util.java.parser.JpClass
import net.typho.compilation_util.java.parser.JpFile
import net.typho.compilation_util.java.plus
import net.typho.parsing_util.ParsingIterator
import net.typho.parsing_util.error.DataReadException

object JavaAnalyzer {
    @JvmStatic
    fun analyze(file: JpFile, lookup: ClassLookup.Supplier, defaultImports: List<JaImport> = listOf(JaImport(JPath("java.lang"), true))) {
        val imports = file.imports.map { analyzeImport(it) }

        for (cls in file.classes) {
            analyzeClass(file.pkg, imports, cls, lookup.create(defaultImports + imports))
        }
    }

    @JvmStatic
    fun analyzeClass(pkg: JPath?, imports: List<JaImport>, cls: JpClass, lookup: ClassLookup) {
        val name = pkg + JPath(cls.name.text.toString())

        println("Class '$name'")

        for (path in imports) {
            println("Import '$path'")
        }

        println("Access '${cls.access.toExtraAccessString()}'")

        for (anno in cls.annotations) {
            val type = lookup.resolve(anno.type)!!
            println("Annotation with type '${type.name}' access '${type.access.toExtraAccessString()}'")

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
    fun analyzeImport(path: JPath): JaImport {
        val folders = mutableListOf<CharSequence>()
        val iterator = ParsingIterator(path.path.split('.'))
        var isPackage = false

        while (iterator.hasNext()) {
            when (val token = iterator.next()) {
                "*" if !iterator.hasNext() -> {
                    isPackage = true
                    break
                }
                else -> folders.add(token) // TODO primitives
            }
        }

        return JaImport(JPath(folders), isPackage) // TODO error is thrown in here
    }
}