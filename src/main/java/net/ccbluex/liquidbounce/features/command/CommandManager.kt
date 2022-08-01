/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.ClientUtils

class CommandManager {
    val commands = HashMap<String, Command>()
    var latestAutoComplete: Array<String> = emptyArray()

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
        val command = commands[args[0].substring(1).lowercase()]

        if (command != null) {
            command.execute(args)
        } else {
            ClientUtils.displayChatMessage("§cCommand not found. Type ${prefix}help to view all commands.")
        }
    }

    /**
     * Updates the [latestAutoComplete] array based on the provided [input].
     *
     * @param input text that should be used to check for auto completions.
     * @author NurMarvin
     */
    fun autoComplete(input: String): Boolean {
        this.latestAutoComplete = this.getCompletions(input) ?: emptyArray()
        return input.startsWith(this.prefix) && this.latestAutoComplete.isNotEmpty()
    }

    /**
     * Returns the auto completions for [input].
     *
     * @param input text that should be used to check for auto completions.
     * @author NurMarvin
     */
    private fun getCompletions(input: String): Array<String>? {
        if (input.isNotEmpty() && input.toCharArray()[0] == this.prefix) {
            val args = input.split(" ")

            return if (args.size > 1) {
                val command = getCommand(args[0].substring(1))
                val tabCompletions = command?.tabComplete(args.drop(1).toTypedArray())

                tabCompletions?.toTypedArray()
            } else {
                commands.map { ".${it.key}" }.filter { it.lowercase().startsWith(args[0].lowercase()) }.toTypedArray()
            }
        }
        return null
    }

    /**
     * Get command instance by given [name]
     */
    fun getCommand(name: String): Command? {
        return commands[name.lowercase()]
    }

    /**
     * Register [command] by just adding it to the commands registry
     */
    fun registerCommand(command: Command) {
        commands[command.command.lowercase()] = command
        command.alias.forEach {
            commands[it.lowercase()] = command
        }
    }

    /**
     * Register [commandClass]
     */
    private fun registerCommand(commandClass: Class<out Command>) {
        try {
            registerCommand(commandClass.newInstance())
        } catch (e: Throwable) {
            ClientUtils.logError("Failed to load command: ${commandClass.name} (${e.javaClass.name}: ${e.message})")
        }
    }

    /**
     * Unregister [command] by just removing it from the commands registry
     */
    fun unregisterCommand(command: Command) {
        commands.toList().forEach {
            if (it.second == command) {
                commands.remove(it.first)
            }
        }
    }
}
