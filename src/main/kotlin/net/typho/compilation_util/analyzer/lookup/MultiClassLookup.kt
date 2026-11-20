package net.typho.compilation_util.analyzer.lookup

import net.typho.compilation_util.analyzer.AnalyzeNode

class MultiClassLookup(
    @JvmField
    val lookups: List<ClassLookup>
) : ClassLookup {
    override fun resolve(path: AnalyzeNode.Path) = lookups.firstNotNullOfOrNull { it.resolve(path) }

    class Supplier(
        @JvmField
        val lookups: List<ClassLookup.Supplier>
    ) : ClassLookup.Supplier {
        override fun create(imports: List<AnalyzeNode.Import>) = MultiClassLookup(lookups.map { it.create(imports) })
    }
}