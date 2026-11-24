package net.typho.compilation_util.java.analyzer.lookup

import net.typho.compilation_util.java.analyzer.JaImport
import net.typho.compilation_util.java.JPath

class MultiClassLookup(
    @JvmField
    val lookups: List<ClassLookup>
) : ClassLookup {
    override fun resolve(path: JPath) = lookups.firstNotNullOfOrNull { it.resolve(path) }

    class Supplier(
        @JvmField
        val lookups: List<ClassLookup.Supplier>
    ) : ClassLookup.Supplier {
        override fun create(imports: List<JaImport>) = MultiClassLookup(lookups.map { it.create(imports) })
    }
}