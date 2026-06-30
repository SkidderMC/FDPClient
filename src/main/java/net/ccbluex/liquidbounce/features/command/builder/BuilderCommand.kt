/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.builder

import net.ccbluex.liquidbounce.FDPClient.commandManager
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER

/**
 * Adapter that exposes a builder [BuiltCommand] tree as a classic FDPClient [Command].
 *
 * [CommandManager] routes every command through this tree runtime. Classic commands are wrapped as
 * leaf handlers, while migrated commands expose typed parameters and subcommands directly.
 *
 * Subclass it with a no-arg constructor so the classpath scanner in [CommandManager] can
 * instantiate it, OR register an instance manually via `commandManager.registerCommand(...)`.
 *
 * Faithful to the original design semantics: typed parameters, subcommands, auto usage, and tab-completion all
 * derived from the same [BuiltCommand]. Crash-safe: every failure path becomes a §c chat message.
 *
 */
abstract class BuilderCommand(private val built: BuiltCommand) :
    Command(built.name, *built.aliases) {

    /**
     * [args] is the raw token array including the command name at index 0, as delivered by
     * [CommandManager.executeCommands].
     */
    override fun execute(args: Array<String>) {
        // Drop the command name; tokens[] are everything the user typed after it.
        val tokens = args.copyOfRange(1, args.size).toList()

        try {
            dispatch(built, tokens)
        } catch (e: CommandException) {
            chat("§c${e.message}")
            e.usageInfo?.forEach { chatSyntax(it) }
        } catch (e: Throwable) {
            // Last-resort guard: a buggy handler must never crash the client/chat input.
            LOGGER.error("Builder command '${built.name}' threw", e)
            chat("§cAn internal error occurred while running this command.")
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        // CommandManager already stripped the command name; args are the post-name tokens.
        return try {
            complete(built, args.toList())
        } catch (e: Throwable) {
            emptyList()
        }
    }

    /**
     * Resolves [tokens] against [node], descending into subcommands, then parses parameters and
     * invokes the handler. Throws [CommandException] with an auto-generated usage on any problem.
     */
    private fun dispatch(node: BuiltCommand, tokens: List<String>) {
        // Prefer subcommand resolution when the next token names one —,
        // where subcommand names take precedence over being treated as a positional argument.
        if (tokens.isNotEmpty() && node.subcommands.isNotEmpty()) {
            val sub = node.getSubcommand(tokens[0])
            if (sub != null) {
                dispatch(sub, tokens.drop(1))
                return
            }
        }

        val handler = node.handler
        if (handler == null) {
            // Group node with no default handler: the user must pick a subcommand.
            if (tokens.isEmpty()) {
                throw CommandException(
                    "Missing subcommand for '${node.name}'.",
                    node.subcommands.map { it.usage() }
                )
            }
            throw CommandException(
                "Unknown subcommand '${tokens[0]}'.",
                node.subcommands.map { it.usage() }
            )
        }

        val parsed = parseParameters(node, tokens)
        handler(node, parsed)
    }

    /**
     * Parses [tokens] into typed values according to [node]'s parameter list. Enforces required
     * parameters, runs verifiers, and joins varargs.
     */
    private fun parseParameters(node: BuiltCommand, tokens: List<String>): Array<Any?> {
        val parameters = node.parameters
        val result = arrayOfNulls<Any?>(parameters.size)

        var tokenIndex = 0
        for (parameter in parameters) {
            if (parameter.vararg) {
                val remaining = tokens.drop(tokenIndex)
                if (remaining.isEmpty()) {
                    if (parameter.required) {
                        throw CommandException(
                            "Missing required argument <${parameter.name}>.",
                            listOf(node.usage())
                        )
                    }
                    result[parameter.index] = emptyList<Any?>()
                } else {
                    result[parameter.index] = remaining.map { verify(node, parameter, it) }
                }
                tokenIndex = tokens.size
                continue
            }

            if (tokenIndex >= tokens.size) {
                if (parameter.required) {
                    throw CommandException(
                        "Missing required argument <${parameter.name}>.",
                        listOf(node.usage())
                    )
                }
                result[parameter.index] = null
                continue
            }

            result[parameter.index] = verify(node, parameter, tokens[tokenIndex])
            tokenIndex++
        }

        // Too many tokens and no vararg to absorb them.
        if (tokenIndex < tokens.size && parameters.none { it.vararg }) {
            throw CommandException(
                "Too many arguments.",
                listOf(node.usage())
            )
        }

        return result
    }

    private fun verify(node: BuiltCommand, parameter: Parameter<*>, token: String): Any? {
        val verifier = parameter.verifier ?: return token
        val validation = verifier(token)
        if (validation.isError) {
            throw CommandException(
                "Invalid value for <${parameter.name}>: ${validation.errorMessage}",
                listOf(node.usage())
            )
        }
        return validation.value
    }

    companion object {
        /**
         * Tab-completion derived entirely from the built tree. Mirrors how the dispatcher resolves
         * tokens: descend through fully-typed subcommand names, then complete either the next
         * subcommand name or the parameter currently being typed.
         */
        fun complete(root: BuiltCommand, args: List<String>): List<String> {
            if (args.isEmpty()) return emptyList()

            var node = root
            var index = 0

            // Walk through complete subcommand tokens (every token except the last, while a node
            // still has subcommands and the token names one of them).
            while (index < args.lastIndex && node.subcommands.isNotEmpty()) {
                val sub = node.getSubcommand(args[index]) ?: break
                node = sub
                index++
            }

            val begin = args.last()
            val consumed = args.subList(index, args.lastIndex)
            val completions = ArrayList<String>()

            // If the current node still resolves the current token as a subcommand selector,
            // suggest subcommand names (only at the position where a subcommand is expected).
            if (node.subcommands.isNotEmpty() && consumed.isEmpty()) {
                node.subcommands.forEach { sub ->
                    if (sub.name.startsWith(begin, true)) completions.add(sub.name)
                    sub.aliases.forEach { a -> if (a.startsWith(begin, true)) completions.add(a) }
                }
            }

            // Suggest completions for the parameter at the current position.
            val paramPos = consumed.size
            val parameter = node.parameters.getOrNull(paramPos)
                ?: node.parameters.lastOrNull()?.takeIf { it.vararg }
            if (parameter != null) {
                completions.addAll(completeParameter(parameter, begin, args.subList(index, args.size)))
            }

            return completions.distinct()
        }

        private fun completeParameter(
            parameter: Parameter<*>,
            begin: String,
            args: List<String>
        ): List<String> {
            parameter.autocompleter?.let { ac ->
                return ac(begin, args).filter { it.startsWith(begin, true) }
            }
            if (parameter.useMinecraftAutoCompletion) {
                val world = net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc.theWorld
                    ?: return emptyList()
                return world.playerEntities
                    .mapNotNull { it.name }
                    .filter { it.startsWith(begin, true) }
            }
            return emptyList()
        }
    }

    /** Convenience: prints all auto-generated usage lines (used by builder-based help). */
    protected fun printUsages() {
        chat("§3Syntax:")
        built.allUsages().forEach { chat("§8> §7${commandManager.prefix}$it") }
    }
}
