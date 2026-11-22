package net.typho.compilation_util.analyzer.lookup

import net.typho.compilation_util.analyzer.JaImport
import net.typho.compilation_util.analyzer.JaPath

class MultiClassLookup(
    @JvmField
    val lookups: List<ClassLookup>
) : ClassLookup {
    override fun resolve(path: JaPath) = lookups.firstNotNullOfOrNull { it.resolve(path) }

    class Supplier(
        @JvmField
        val lookups: List<ClassLookup.Supplier>
    ) : ClassLookup.Supplier {
        override fun create(imports: List<JaImport>) = MultiClassLookup(lookups.map { it.create(imports) })
    }
}