/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.builder

/**
 * The handler invoked when a (sub)command is executed. [args] are the already-parsed, typed
 * values in parameter order (cast at the call-site, exactly like nextgen). A handler may throw
 * [CommandException] to report a runtime error to the user without crashing.
 */
typealias CommandHandler = (command: BuiltCommand, args: Array<Any?>) -> Unit

/**
 * An immutable, fully-built command node — faithful port of nextgen's `Command`.
 *
 * A node has a [name], optional [aliases], an ordered list of [parameters], a list of
 * [subcommands], and an optional [handler]. A node is either a "leaf" (has a [handler] and usually
 * parameters) or a "group" (has subcommands and no handler of its own), exactly like nextgen — and
 * it may even be both (a group with a default handler).
 *
 * [parentName] is the dotted path of ancestors used to render usage (`.command sub <arg>`).
 */
class BuiltCommand(
    val name: String,
    val aliases: Array<String>,
    val parameters: List<Parameter<*>>,
    val subcommands: List<BuiltCommand>,
    val handler: CommandHandler?,
    val description: String?,
    var parent: BuiltCommand? = null
) {
    init {
        // Assign positional indices and back-link subcommands to this parent.
        parameters.forEachIndexed { i, p -> p.index = i }
        subcommands.forEach { it.parent = this }
    }

    fun getSubcommand(name: String): BuiltCommand? = subcommands.firstOrNull {
        it.name.equals(name, ignoreCase = true) || it.aliases.any { a -> a.equals(name, true) }
    }

    /** Names of this node and all its ancestors, root first (e.g. ["friend", "add"]). */
    private fun namePath(): List<String> {
        val path = ArrayList<String>()
        var node: BuiltCommand? = this
        while (node != null) {
            path.add(node.name)
            node = node.parent
        }
        return path.asReversed()
    }

    /**
     * Auto-generated usage line for this node, e.g. `friend add <name> [alias]`. Required
     * parameters are wrapped in `<>`, optional ones in `[]`, varargs get an ellipsis.
     */
    fun usage(): String {
        val sb = StringBuilder(namePath().joinToString(" "))
        for (parameter in parameters) {
            val inner = parameter.name + if (parameter.vararg) "..." else ""
            sb.append(' ')
            sb.append(if (parameter.required) "<$inner>" else "[$inner]")
        }
        if (subcommands.isNotEmpty()) {
            sb.append(' ')
            sb.append(subcommands.joinToString("/", prefix = "<", postfix = ">") { it.name })
        }
        return sb.toString()
    }

    /** Recursively collects usage lines for this node and every reachable subcommand. */
    fun allUsages(): List<String> {
        val lines = ArrayList<String>()
        if (handler != null || parameters.isNotEmpty()) {
            lines.add(usage())
        }
        for (sub in subcommands) {
            lines.addAll(sub.allUsages())
        }
        if (lines.isEmpty()) {
            lines.add(usage())
        }
        return lines
    }
}

/**
 * Fluent builder for a command tree — faithful port of nextgen's `CommandBuilder`.
 *
 * Example (port of FriendCommand):
 * ```
 * CommandBuilder.begin("friend").alias("friends")
 *     .subcommand(
 *         CommandBuilder.begin("add")
 *             .parameter(ParameterBuilder.begin<String>("name").required().build())
 *             .parameter(ParameterBuilder.begin<String>("alias").optional().build())
 *             .handler { _, args -> ... }
 *             .build()
 *     )
 *     .build()
 * ```
 *
 * Invariants enforced by [build] (matching nextgen):
 *  - at most one vararg parameter, and it must be last;
 *  - no required parameter may follow an optional one;
 *  - a node must have a handler OR at least one subcommand (otherwise it is unusable).
 *
 * @author ported from LiquidBounce nextgen (CCBlueX) for FDPClient 1.8.9
 */
class CommandBuilder private constructor(private val name: String) {

    private val aliases = ArrayList<String>()
    private val parameters = ArrayList<Parameter<*>>()
    private val subcommands = ArrayList<BuiltCommand>()
    private var handler: CommandHandler? = null
    private var description: String? = null

    fun alias(vararg alias: String): CommandBuilder {
        aliases.addAll(alias)
        return this
    }

    fun description(description: String): CommandBuilder {
        this.description = description
        return this
    }

    fun parameter(parameter: Parameter<*>): CommandBuilder {
        parameters.add(parameter)
        return this
    }

    fun subcommand(subcommand: BuiltCommand): CommandBuilder {
        subcommands.add(subcommand)
        return this
    }

    fun handler(handler: CommandHandler): CommandBuilder {
        this.handler = handler
        return this
    }

    fun build(): BuiltCommand {
        var seenOptional = false
        parameters.forEachIndexed { i, parameter ->
            if (parameter.vararg && i != parameters.lastIndex) {
                error("Vararg parameter '${parameter.name}' of command '$name' must be the last parameter")
            }
            if (parameter.required && seenOptional) {
                error("Required parameter '${parameter.name}' of command '$name' must not follow an optional parameter")
            }
            if (!parameter.required) {
                seenOptional = true
            }
        }

        if (handler == null && subcommands.isEmpty()) {
            error("Command '$name' must define a handler or at least one subcommand")
        }

        return BuiltCommand(
            name = name,
            aliases = aliases.toTypedArray(),
            parameters = parameters.toList(),
            subcommands = subcommands.toList(),
            handler = handler,
            description = description
        )
    }

    companion object {
        fun begin(name: String): CommandBuilder = CommandBuilder(name)
    }
}
