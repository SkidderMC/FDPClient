/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.builder

@DslMarker
annotation class CommandTreeDsl

class CommandArguments internal constructor(
    val command: BuiltCommand,
    val values: Array<Any?>
) {
    operator fun <T> get(parameter: Parameter<T>): T = parameter.cast(values)
    fun <T> optional(parameter: Parameter<T>): T? = parameter.castOrNull(values)
    fun <T> vararg(parameter: Parameter<T>): List<T> = parameter.castVararg(values)
}

@CommandTreeDsl
class CommandNodeDsl internal constructor(name: String) {
    private val builder = CommandBuilder.begin(name)

    fun aliases(vararg aliases: String) {
        builder.alias(*aliases)
    }

    fun description(text: String) {
        builder.description(text)
    }

    fun <T> parameter(name: String, configure: ParameterBuilder<T>.() -> Unit = {}): Parameter<T> {
        val parameter = ParameterBuilder.begin<T>(name).apply(configure).build()
        builder.parameter(parameter)
        return parameter
    }

    fun subcommand(name: String, configure: CommandNodeDsl.() -> Unit) {
        builder.subcommand(buildCommand(name, configure))
    }

    fun subcommand(command: BuiltCommand) {
        builder.subcommand(command)
    }

    fun executes(handler: CommandArguments.() -> Unit) {
        builder.handler { command, values -> CommandArguments(command, values).handler() }
    }

    internal fun build(): BuiltCommand = builder.build()
}

fun buildCommand(name: String, configure: CommandNodeDsl.() -> Unit): BuiltCommand =
    CommandNodeDsl(name).apply(configure).build()
