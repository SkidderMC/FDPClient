/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.event.ClientChange
import net.ccbluex.liquidbounce.event.ClientChangeBus
import net.ccbluex.liquidbounce.features.command.builder.BuilderCommand
import net.ccbluex.liquidbounce.features.command.builder.CommandBuilder
import net.ccbluex.liquidbounce.features.command.builder.ParameterBuilder
import net.ccbluex.liquidbounce.utils.client.ClassUtils
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.chat
import java.util.IdentityHashMap

object CommandManager {
    val commands = mutableListOf<Command>()
    private val runtimes = IdentityHashMap<Command, BuilderCommand>()
    var latestAutoComplete = emptyArray<String>()

    var prefix = "."

    /**
     * Register all default commands
     */
    fun registerCommands() {
        ClassUtils.resolvePackage("${this.javaClass.`package`.name}.commands", Command::class.java)
            .forEach(this::registerCommand)
    }

    /**
     * Execute command by given [input]
     */
    fun executeCommands(input: String) {
        if (!input.startsWith(prefix)) {
            return
        }

        val args = input.removePrefix(prefix).split(' ').toTypedArray()

        for (command in commands) {
            if (args[0].equals(command.command, ignoreCase = true)) {
                CommandUtils.runSafe(command.command) { runtime(command).execute(args) }
                ClientChangeBus.publish(ClientChange.Command(command.command))
                return
            }

            for (alias in command.alias) {
                if (!args[0].equals(alias, ignoreCase = true))
                    continue

                CommandUtils.runSafe(command.command) { runtime(command).execute(args) }
                ClientChangeBus.publish(ClientChange.Command(command.command))
                return
            }
        }

        chat("§cCommand not found. Type ${prefix}help to view all commands.")
    }

    /**
     * Updates the [latestAutoComplete] array based on the provided [input].
     *
     * @param input text that should be used to check for auto completions.
     */
    fun autoComplete(input: String): Boolean {
        latestAutoComplete = try {
            getCompletions(input) ?: emptyArray()
        } catch (throwable: Throwable) {
            LOGGER.error("Auto-complete failed for input '$input'", throwable)
            emptyArray()
        }
        return input.startsWith(prefix) && latestAutoComplete.isNotEmpty()
    }

    /**
     * Returns the auto completions for [input].
     *
     * @param input text that should be used to check for auto completions.
     */
    private fun getCompletions(input: String): Array<String>? {
        if (!input.startsWith(prefix)) {
            return null
        }

        val rawInput = input.removePrefix(prefix)

        val args = rawInput.split(' ').toTypedArray()

        return if (args.size > 1) {
            val command = getCommand(args[0])
            val tabCompletions = try {
                command?.let { runtime(it).tabComplete(args.copyOfRange(1, args.size)) }
            } catch (throwable: Throwable) {
                LOGGER.error("Tab-complete failed for command '${args[0]}'", throwable)
                null
            }

            tabCompletions?.toTypedArray()
        } else {
            commands.mapNotNull { command ->
                val alias = when {
                    command.command.startsWith(rawInput, true) -> command.command
                    else -> command.alias.firstOrNull { alias -> alias.startsWith(rawInput, true) }
                } ?: return@mapNotNull null

                prefix + alias
            }.toTypedArray()
        }
    }

    /**
     * Get command instance by given [name]
     */
    fun getCommand(name: String) = commands.find {
        it.command.equals(name, ignoreCase = true) || it.alias.any { alias -> alias.equals(name, true) }
    }

    /**
     * Register [command] by just adding it to the commands registry
     */
    fun registerCommand(command: Command) {
        runtimes[command] = if (command is BuilderCommand) command else adaptLegacy(command)
        commands.add(command)
    }

    /**
     * Register [commandClass]
     */
    private fun registerCommand(commandClass: Class<out Command>) {
        try {
            val constructor = commandClass.getDeclaredConstructor()
            if (!constructor.isAccessible) {
                constructor.isAccessible = true
            }
            registerCommand(constructor.newInstance())
        } catch (e: Throwable) {
            LOGGER.info("Failed to load command: ${commandClass.name} (${e.javaClass.name}: ${e.message})")
        }
    }

    /**
     * Unregister [command] by just removing it from the commands registry
     */
    fun unregisterCommand(command: Command) {
        runtimes.remove(command)
        commands.removeIf { it == command }
    }

    private fun runtime(command: Command): BuilderCommand =
        runtimes[command] ?: error("Command '${command.command}' is not registered")

    /**
     * Wraps classic execute/tabComplete commands in the same typed command-tree dispatcher used by
     * native DSL commands. This removes the second runtime while preserving legacy implementations
     * as leaf handlers that can be migrated incrementally without changing user-visible syntax.
     */
    private fun adaptLegacy(legacy: Command): BuilderCommand {
        val arguments = ParameterBuilder.begin<String>("arguments")
            .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
            .autocompletedWith { _, args -> legacy.tabComplete(args.toTypedArray()) }
            .optional()
            .vararg()
            .build()

        val treeBuilder = CommandBuilder.begin(legacy.command)
        legacy.alias.forEach { treeBuilder.alias(it) }

        val tree = treeBuilder
            .parameter(arguments)
            .handler { _, values ->
                @Suppress("UNCHECKED_CAST")
                val rawArguments = values[0] as? List<String> ?: emptyList()
                legacy.execute((listOf(legacy.command) + rawArguments).toTypedArray())
            }
            .build()

        return object : BuilderCommand(tree) {}
    }
}
