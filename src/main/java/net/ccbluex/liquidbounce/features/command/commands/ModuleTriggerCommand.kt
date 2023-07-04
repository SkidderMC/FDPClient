package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.EnumTriggerType
import net.ccbluex.liquidbounce.utils.misc.StringUtils

class ModuleTriggerCommand : Command("moduletrigger", arrayOf("trigger")) {
    private val modes = EnumTriggerType.values().map { it.name.lowercase() }.toTypedArray()

    override fun execute(args: Array<String>) {
        if (args.size > 2) {
            val module = FDPClient.moduleManager.getModule(args[1])

            if (module == null) {
                alert("Module '${args[1]}' not found.")
                return
            }

            module.triggerType = try {
                EnumTriggerType.valueOf(args[2].uppercase())
            } catch (e: IllegalArgumentException) {
                EnumTriggerType.TOGGLE
            }
            playEdit()

            alert("Set module §l${module.name}§r trigger type to §l${module.triggerType}§r.")

            return
        }

        chatSyntax("moduletrigger <module> [${StringUtils.toCompleteString(modes,0,",")}]")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> FDPClient.moduleManager.modules
                .map { it.name }
                .filter { it.startsWith(args[0], true) }
                .toList()

            2 -> modes.filter { it.startsWith(args[1], true) }

            else -> emptyList()
        }
    }
}