/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.config.*

/** Validates invariants that are only observable after Minecraft-dependent module objects initialize. */
object ModuleRegistryValidator {

    const val EXPECTED_BUILT_IN_MODULES = 297

    fun validate(modules: Collection<Module>) {
        val errors = mutableListOf<String>()

        if (modules.size != EXPECTED_BUILT_IN_MODULES) {
            errors += "expected $EXPECTED_BUILT_IN_MODULES built-in modules, initialized ${modules.size}"
        }

        val identities = mutableMapOf<String, String>()
        for (module in modules) {
            if (!MODULE_NAME.matches(module.name)) {
                errors += "${module.javaClass.name} has invalid module name '${module.name}'"
            }

            if (module.subCategory != Category.SubCategory.GENERAL &&
                module.subCategory !in module.category.subCategories
            ) {
                errors += "${module.name} uses ${module.subCategory} outside ${module.category}"
            }

            for (identity in sequenceOf(module.name) + module.aliases.asSequence()) {
                val normalized = identity.lowercase()
                val previous = identities.putIfAbsent(normalized, module.name)
                if (previous != null && previous != module.name) {
                    errors += "module identity '$identity' is shared by $previous and ${module.name}"
                }
            }

            validateContainer(module, module.name, errors)
        }

        check(errors.isEmpty()) {
            errors.joinToString(separator = "\n - ", prefix = "Invalid built-in module registry:\n - ")
        }
    }

    private fun validateContainer(container: Configurable, path: String, errors: MutableList<String>) {
        val siblingNames = mutableSetOf<String>()
        for (value in container.values) {
            if (value.name.isBlank()) {
                errors += "blank value name at $path"
                continue
            }
            if (!siblingNames.add(value.name.lowercase())) {
                errors += "duplicate sibling value '${value.name}' at $path"
            }

            if (value is Configurable) {
                validateContainer(value, "$path.${value.name}", errors)
            } else if (container.findDeep(value.name) is Configurable) {
                errors += "leaf '${value.name}' at $path resolves to a group"
            } else {
                validateLeaf(value, "$path.${value.name}", errors)
            }
        }
    }

    private fun validateLeaf(value: Value<*>, path: String, errors: MutableList<String>) {
        val valid = validateScalar(value) ?: validateComposite(value) ?: true

        if (!valid) {
            errors += "$path has invalid default/current value '${value.get()}'"
        }
    }

    private fun validateScalar(value: Value<*>): Boolean? =
        when (value) {
            is IntValue -> value.get() in value.range
            is LongValue -> value.get() in value.range
            is FloatValue -> value.get().isFinite() && value.get() in value.range
            is DoubleValue -> value.get().isFinite() && value.get() in value.range
            is BlockValue -> value.get() in value.range
            is ListValue -> value.get() in value
            else -> null
        }

    private fun validateComposite(value: Value<*>): Boolean? =
        when (value) {
            is IntRangeValue -> isValid(value)
            is LongRangeValue -> isValid(value)
            is FloatRangeValue -> isValid(value)
            is DoubleRangeValue -> isValid(value)
            is Vec2Value -> value.get().size == 2 && value.get().all(Double::isFinite)
            is Vec3Value -> value.get().size == 3 && value.get().all(Double::isFinite)
            is CurveValue -> value.get().size >= 2 &&
                value.get().all { it.isFinite() && it in 0.0..1.0 }
            else -> null
        }

    private fun isValid(value: IntRangeValue) = value.get().first <= value.get().last &&
        value.get().first in value.range && value.get().last in value.range

    private fun isValid(value: LongRangeValue) = value.get().first <= value.get().last &&
        value.get().first in value.range && value.get().last in value.range

    private fun isValid(value: FloatRangeValue) = value.get().start.isFinite() &&
        value.get().endInclusive.isFinite() && value.get().start <= value.get().endInclusive &&
        value.get().start in value.range && value.get().endInclusive in value.range

    private fun isValid(value: DoubleRangeValue) = value.get().start.isFinite() &&
        value.get().endInclusive.isFinite() && value.get().start <= value.get().endInclusive &&
        value.get().start in value.range && value.get().endInclusive in value.range

    private val MODULE_NAME = Regex("[A-Za-z][A-Za-z0-9]*")
}
