/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.builder

/**
 * A typed, named command parameter — implementation of the original `Parameter<T>`.
 *
 * A parameter knows:
 *  - its [name] (used in auto-generated usage like `<name>` / `[name]`)
 *  - whether it is [required]
 *  - whether it is a [vararg] (consumes all remaining tokens, must be the last parameter)
 *  - how to [verifier] convert a raw string token into a value of type [T]
 *  - how to [autocompleter] produce completions for a partially typed token
 *
 * The generic type is erased at runtime; values come out of [ParameterValidationResult] and the
 * command handler reads them positionally/by index from the parsed argument list.
 *
 */
class Parameter<T>(
    val name: String,
    val required: Boolean,
    val vararg: Boolean,
    val verifier: ((String) -> ParameterValidationResult<T>)?,
    val autocompleter: ((begin: String, args: List<String>) -> List<String>)?,
    val useMinecraftAutoCompletion: Boolean,
    /** Index assigned by the owning Command once the parameter list is finalized. */
    var index: Int = -1
) {
    @Suppress("UNCHECKED_CAST")
    fun cast(arguments: Array<Any?>): T {
        check(index in arguments.indices) { "Parameter '$name' does not belong to this argument list" }
        return arguments[index] as? T
            ?: error("Required parameter '$name' has no value or has an incompatible value")
    }

    @Suppress("UNCHECKED_CAST")
    fun castOrNull(arguments: Array<Any?>): T? {
        check(index in arguments.indices) { "Parameter '$name' does not belong to this argument list" }
        return arguments[index] as? T
    }

    @Suppress("UNCHECKED_CAST")
    fun castVararg(arguments: Array<Any?>): List<T> {
        check(vararg) { "Parameter '$name' is not a vararg parameter" }
        check(index in arguments.indices) { "Parameter '$name' does not belong to this argument list" }
        val values = arguments[index] as? List<*>
            ?: error("Vararg parameter '$name' has an incompatible value")
        return values.map { it as T }
    }
}

/**
 * Result of converting/validating a single raw token into a typed value.
 *
 * Matches the original `ParameterValidationResult`: either [ok] with a value, or an error with a
 * message. Use the factory methods [ok] / [error].
 */
class ParameterValidationResult<T> private constructor(
    val value: T?,
    val errorMessage: String?
) {
    val isError: Boolean
        get() = errorMessage != null

    companion object {
        fun <T> ok(value: T) = ParameterValidationResult(value, null)
        fun <T> error(message: String) = ParameterValidationResult<T>(null, message)
    }
}
