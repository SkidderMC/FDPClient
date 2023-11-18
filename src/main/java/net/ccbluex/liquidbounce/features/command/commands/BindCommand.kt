/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import org.lwjgl.input.Keyboard

class BindCommand : Command("bind", emptyArray()) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            // Get module by name
            val module = FDPClient.moduleManager.getModule(args[1])

            if (module == null) {
                alert("Module §l" + args[1] + "§r not found.")
                return
            }

            if (args.size > 2) {
                // Find key by name and change
                val key = Keyboard.getKeyIndex(args[2].uppercase())
                module.keyBind = key

                // Response to user
                alert("Bound module §l${module.name}§r to key §a§l${Keyboard.getKeyName(key)}§3.")
                FDPClient.hud.addNotification(
                    Notification("KeyBind", "Bound ${module.name} to ${Keyboard.getKeyName(key)}.", NotifyType.INFO)
                )
                playEdit()
            } else {
                FDPClient.moduleManager.pendingBindModule = module
                alert("Press any key to bind module ${module.name}")
            }
            return
        }

        chatSyntax(arrayOf("<module> <key>", "<module> none"))
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        val moduleName = args[0]

        return when (args.size) {
            1 -> FDPClient.moduleManager.modules
                    .map { it.name }
                    .filter { it.startsWith(moduleName, true) }
                    .toList()
            else -> emptyList()
        }
    }
}
