/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.builder

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation

/**
 * Fluent builder for a single [Parameter] — faithful port of nextgen's `ParameterBuilder<T>`.
 *
 * Usage mirrors nextgen:
 * ```
 * ParameterBuilder.begin<Int>("amount")
 *     .verifiedBy(ParameterBuilder.INTEGER_VALIDATOR)
 *     .required()
 *     .build()
 * ```
 *
 * Defaults: a parameter is required and not a vararg until told otherwise, and has no verifier
 * (meaning it is treated as a raw String). [build] enforces nextgen's invariants.
 *
 * @author ported from LiquidBounce nextgen (CCBlueX) for FDPClient 1.8.9
 */
class ParameterBuilder<T> private constructor(private val name: String) {

    private var verifier: ((String) -> ParameterValidationResult<T>)? = null
    private var autocompleter: ((begin: String, args: List<String>) -> List<String>)? = null
    private var required = true
    private var vararg = false
    private var useMinecraftAutoCompletion = false

    /**
     * Sets the converter/validator used to turn a raw token into a typed value [T].
     * If unset, the parameter behaves as a raw [String] (only valid when [T] is String).
     */
    fun verifiedBy(verifier: (String) -> ParameterValidationResult<T>): ParameterBuilder<T> {
        this.verifier = verifier
        return this
    }

    /**
     * Provide static/dynamic auto-completions for this parameter. [begin] is the partially typed
     * token; [args] are all already-typed tokens of the command (after the command name) for
     * context-aware completion. Faithful to nextgen's `autocompletedWith`.
     */
    fun autocompletedWith(
        autocompleter: (begin: String, args: List<String>) -> List<String>
    ): ParameterBuilder<T> {
        this.autocompleter = autocompleter
        return this
    }

    /**
     * Convenience overload matching nextgen call-sites that ignore the full arg list.
     */
    fun autocompletedWith(autocompleter: (begin: String) -> List<String>): ParameterBuilder<T> {
        this.autocompleter = { begin, _ -> autocompleter(begin) }
        return this
    }

    /**
     * Marks this parameter to use Minecraft's native tab-completion (player names, etc.).
     * In 1.8.9 we don't hook the server completion packet from the builder, so this simply flags
     * the parameter; the dispatcher falls back to online player names which covers the common
     * nextgen use-case (target/player parameters).
     */
    fun useMinecraftAutoCompletion(): ParameterBuilder<T> {
        this.useMinecraftAutoCompletion = true
        return this
    }

    fun required(): ParameterBuilder<T> {
        this.required = true
        return this
    }

    fun optional(): ParameterBuilder<T> {
        this.required = false
        return this
    }

    /**
     * Marks this as a vararg parameter — it consumes all remaining tokens. Must be the last
     * parameter of the command (enforced by [CommandBuilder.build]).
     */
    fun vararg(): ParameterBuilder<T> {
        this.vararg = true
        return this
    }

    fun build(): Parameter<T> = Parameter(
        name = name,
        required = required,
        vararg = vararg,
        verifier = verifier,
        autocompleter = autocompleter,
        useMinecraftAutoCompletion = useMinecraftAutoCompletion
    )

    companion object {
        fun <T> begin(name: String): ParameterBuilder<T> = ParameterBuilder(name)

        /**
         * Validator that accepts any string (identity). Faithful to nextgen's `STRING_VALIDATOR`.
         */
        val STRING_VALIDATOR: (String) -> ParameterValidationResult<String> = { value ->
            ParameterValidationResult.ok(value)
        }

        /**
         * Parses a base-10 integer. Faithful to nextgen's `INTEGER_VALIDATOR`.
         */
        val INTEGER_VALIDATOR: (String) -> ParameterValidationResult<Int> = { value ->
            val parsed = value.toIntOrNull()
            if (parsed == null) {
                ParameterValidationResult.error("'$value' is not a valid integer")
            } else {
                ParameterValidationResult.ok(parsed)
            }
        }

        /**
         * Parses a long. Faithful to nextgen's `LONG_VALIDATOR`.
         */
        val LONG_VALIDATOR: (String) -> ParameterValidationResult<Long> = { value ->
            val parsed = value.toLongOrNull()
            if (parsed == null) {
                ParameterValidationResult.error("'$value' is not a valid number")
            } else {
                ParameterValidationResult.ok(parsed)
            }
        }

        /**
         * Parses a float. Faithful to nextgen's `FLOAT_VALIDATOR`.
         */
        val FLOAT_VALIDATOR: (String) -> ParameterValidationResult<Float> = { value ->
            val parsed = value.toFloatOrNull()
            if (parsed == null || !parsed.isFinite()) {
                ParameterValidationResult.error("'$value' is not a valid decimal number")
            } else {
                ParameterValidationResult.ok(parsed)
            }
        }

        /**
         * Parses a double. Faithful to nextgen's `DOUBLE_VALIDATOR`.
         */
        val DOUBLE_VALIDATOR: (String) -> ParameterValidationResult<Double> = { value ->
            val parsed = value.toDoubleOrNull()
            if (parsed == null || !parsed.isFinite()) {
                ParameterValidationResult.error("'$value' is not a valid decimal number")
            } else {
                ParameterValidationResult.ok(parsed)
            }
        }

        /**
         * Parses a boolean from common truthy/falsy spellings. Faithful to nextgen's
         * `BOOLEAN_VALIDATOR`, extended with the on/off spelling 1.8.9 commands commonly use.
         */
        val BOOLEAN_VALIDATOR: (String) -> ParameterValidationResult<Boolean> = { value ->
            when (value.lowercase()) {
                "true", "on", "yes", "1", "enable", "enabled" -> ParameterValidationResult.ok(true)
                "false", "off", "no", "0", "disable", "disabled" ->
                    ParameterValidationResult.ok(false)
                else -> ParameterValidationResult.error("'$value' is not a valid boolean (on/off)")
            }
        }

        /**
         * Resolves a Minecraft [Block] by its registry name (e.g. `stone`, `minecraft:stone`).
         * 1.8.9 adaptation of nextgen's `BLOCK_VALIDATOR` (which used the 1.21 block registry).
         */
        val BLOCK_VALIDATOR: (String) -> ParameterValidationResult<Block> = { value ->
            // Block.getBlockFromName accepts both `stone` and `minecraft:stone` and returns null
            // for unknown names in 1.8.9 (no air-vs-unknown ambiguity, unlike getObject()).
            val block = Block.getBlockFromName(value)
            if (block == null) {
                ParameterValidationResult.error("'$value' is not a valid block")
            } else {
                ParameterValidationResult.ok(block)
            }
        }

        /**
         * Resolves a Minecraft [Item] by its registry name. 1.8.9 adaptation of nextgen's item
         * parameter support.
         */
        val ITEM_VALIDATOR: (String) -> ParameterValidationResult<Item> = { value ->
            val location = if (value.contains(':')) ResourceLocation(value)
            else ResourceLocation("minecraft", value)
            val item = Item.itemRegistry.getObject(location)
            if (item == null) {
                ParameterValidationResult.error("'$value' is not a valid item")
            } else {
                ParameterValidationResult.ok(item)
            }
        }

        /**
         * Creates an enum validator that accepts any of the [values] by case-insensitive name.
         * Faithful to nextgen's enum parameter support.
         */
        inline fun <reified E : Enum<E>> enumValidator(): (String) -> ParameterValidationResult<E> {
            val values = enumValues<E>()
            return { value ->
                val match = values.firstOrNull { it.name.equals(value, ignoreCase = true) }
                if (match == null) {
                    ParameterValidationResult.error(
                        "'$value' must be one of: ${values.joinToString(", ") { it.name.lowercase() }}"
                    )
                } else {
                    ParameterValidationResult.ok(match)
                }
            }
        }
    }
}
