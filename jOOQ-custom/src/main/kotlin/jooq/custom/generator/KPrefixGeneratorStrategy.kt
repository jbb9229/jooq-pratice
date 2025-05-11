package jooq.custom.generator

import org.jooq.codegen.DefaultGeneratorStrategy
import org.jooq.codegen.GeneratorStrategy.Mode
import org.jooq.meta.Definition

class KPrefixGeneratorStrategy: DefaultGeneratorStrategy() {
    override fun getJavaClassName(
        definition: Definition?,
        mode: Mode?
    ): String? {
        if (mode == Mode.DEFAULT) {
            return "K${super.getJavaClassName(definition, mode)}"
        }

        return super.getJavaClassName(definition, mode)
    }
}