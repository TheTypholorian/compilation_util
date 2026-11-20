package net.typho.compilation_util.analyzer.lookup

import net.typho.compilation_util.analyzer.AnalyzeNode
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.nio.file.Path
import java.util.jar.JarFile
import kotlin.io.path.extension

/**
 * Interface to resolve other classes (and their attributes) from imports and a class name.
 *
 * [ClassLookup.getForPath] (passing in a source directory or a jar) is sufficient for almost every use case.
 */
interface ClassLookup {
    fun resolve(path: AnalyzeNode.Path): ClassIndex?

    fun interface Supplier {
        fun create(imports: List<AnalyzeNode.Path>): ClassLookup
    }

    open class Jar(
        @JvmField
        val file: File
    ) : Supplier {
        protected val jar by lazy { JarFile(file) }
        /**
         * Map of absolute names to their [ClassIndex].
         */
        @JvmField
        protected val files = mutableMapOf<AnalyzeNode.Path, ClassIndex?>()

        protected fun getFile(path: AnalyzeNode.Path): ClassIndex? {
            if (files.containsKey(path)) {
                return files[path]
            }

            val entry = jar.getJarEntry("$path.class") ?: return null.also { files[path] = null }
            jar.getInputStream(entry).use { input ->
                val reader = ClassReader(input)
                val node = ClassNode()
                reader.accept(node, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
                return ClassIndex.OfNode(path, node).also { files[path] = it }
            }
        }

        override fun create(imports: List<AnalyzeNode.Path>): ClassLookup = Lookup(imports)

        protected open inner class Lookup(
            @JvmField
            protected val imports: List<AnalyzeNode.Path>
        ) : ClassLookup {
            @JvmField
            protected val cache = mutableMapOf<AnalyzeNode.Path, ClassIndex?>()

            override fun resolve(path: AnalyzeNode.Path): ClassIndex? {
                if (cache.containsKey(path)) {
                    return cache[path]
                }

                val fileName = path.path.replace('.', '/')

                for ((import) in imports) {
                    if (import.last() != '*') {
                        TODO()
                    }
                }

                for ((import) in imports) {
                    if (import.last() == '*') {
                        val import = AnalyzeNode.Path(import.removeSuffix("*") /* TODO */ + fileName)
                        getFile(import)?.let {
                            cache[path] = it
                            return it
                        }
                    }
                }

                cache[path] = null
                return null
            }
        }
    }

    companion object {
        @JvmField
        val byExtension = mutableMapOf<String, (Path) -> Supplier>(
            "jar" to { Jar(it.toFile()) }
        )

        @JvmStatic
        fun getForPath(file: Path): Supplier? = byExtension[file.extension]?.invoke(file)
    }
}