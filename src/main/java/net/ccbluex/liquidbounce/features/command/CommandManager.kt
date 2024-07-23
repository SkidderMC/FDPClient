/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage

object CommandManager {
    val commands = mutableListOf<Command>()
    var latestAutoComplete = emptyArray<String>()

    var prefix = '.'

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
        val args = input.split(" ").toTypedArray()

        for (command in commands) {
            if (args[0].equals(prefix.toString() + command.command, ignoreCase = true)) {
                command.execute(args)
                return
            }

            for (alias in command.alias) {
                if (!args[0].equals(prefix.toString() + alias, ignoreCase = true)) continue

                command.execute(args)
                return
            }
        }

        displayChatMessage("§cCommand not found. Type ${prefix}help to view all commands.")
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
        if (input.isNotEmpty() && input[0] == prefix) {
            val args = input.split(" ")

            return if (args.size > 1) {
                val command = getCommand(args[0].substring(1))
                command?.tabComplete(args.drop(1).toTypedArray())?.toTypedArray()
            } else {
                val rawInput = input.substring(1)
                commands
                    .filter {
                        it.command.startsWith(rawInput, true) ||
                                it.alias.any { alias -> alias.startsWith(rawInput, true) }
                    }
                    .map {
                        val alias = if (it.command.startsWith(rawInput, true)) it.command
                        else it.alias.first { alias -> alias.startsWith(rawInput, true) }
                        prefix + alias
                    }
                    .toTypedArray()
            }
        }
        return null
    }

    /**
     * Get command instance by given [name]
     */
    fun getCommand(name: String) =
        commands.find {
            it.command.equals(name, ignoreCase = true) ||
                    it.alias.any { alias -> alias.equals(name, true) }
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