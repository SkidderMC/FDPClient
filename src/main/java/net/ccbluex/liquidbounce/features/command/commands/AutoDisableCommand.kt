package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.AutoDisableType

class AutoDisableCommand : Command("autodisable", arrayOf("ad")) {
    private val modes=ArrayList<String>()

    init {
        for(type in AutoDisableType.values()){
            modes.add(type.toString().toLowerCase())
        }
    }

    override fun execute(args: Array<String>) {
        if (args.size > 2) {
            val module = LiquidBounce.moduleManager.getModule(args[1])

            if (module == null) {
                chat("Module '${args[1]}' not found.")
                return
            }

            when(args[2].toUpperCase()){
                "RESPAWN","FLAG" -> {
                    module.autoDisable = AutoDisableType.valueOf(args[2].toUpperCase())
                }
                else -> module.autoDisable = AutoDisableType.NONE
            }

            chat("Set module §l${module.name}§r AutoDisable state to §l${module.autoDisable}§r.")

            return
        }

        chatSyntax("toggle <module> [on/off]")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> LiquidBounce.moduleManager.modules
                .map { it.name }
                .filter { it.startsWith(args[0], true) }
                .toList()

            2 -> modes.filter { it.startsWith(args[0], true) }

            else -> emptyList()
        }
    }
}