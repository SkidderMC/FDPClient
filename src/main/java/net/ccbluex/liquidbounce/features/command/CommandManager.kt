/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.utils.client.ClassUtils
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.chat

object CommandManager {
    val commands = mutableListOf<Command>()
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
                command.execute(args)
                return
            }

            for (alias in command.alias) {
                if (!args[0].equals(alias, ignoreCase = true))
                    continue

                command.execute(args)
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
        latestAutoComplete = getCompletions(input) ?: emptyArray()
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
            val tabCompletions = command?.tabComplete(args.copyOfRange(1, args.size))

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
        commands.removeIf { it == command }
    }
}