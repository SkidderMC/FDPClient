/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.FDPClient.commandManager
import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.features.command.builder.BuilderCommand
import net.ccbluex.liquidbounce.features.command.builder.CommandBuilder
import net.ccbluex.liquidbounce.features.command.builder.ParameterBuilder
import net.ccbluex.liquidbounce.utils.client.chat

object ToggleCommand : BuilderCommand(
    CommandBuilder.begin("toggle")
        .alias("t")
        .description("Toggle a module on or off.")
        .parameter(
            ParameterBuilder.begin<String>("module")
                .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                .autocompletedWith { begin ->
                    moduleManager
                        .map { it.name }
                        .filter { it.startsWith(begin, true) }
                }
                .required()
                .build()
        )
        .parameter(
            ParameterBuilder.begin<String>("on/off")
                .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                .autocompletedWith { begin -> listOf("on", "off").filter { it.startsWith(begin, true) } }
                .optional()
                .build()
        )
        .handler { _, args ->
            val moduleName = args[0] as String
            val module = moduleManager[moduleName]

            if (module == null) {
                chat("§3Module '$moduleName' not found.")
                return@handler
            }

            val newState = args[1] as String?

            if (newState != null) {
                if (newState.lowercase() == "on" || newState.lowercase() == "off") {
                    module.state = newState.lowercase() == "on"
                } else {
                    chat("§3Syntax: §7${commandManager.prefix}toggle <module> [on/off]")
                }
            } else {
                module.toggle()
            }

            chat("§3${if (module.state) "Enabled" else "Disabled"} module §8${module.getName()}§3.")
        }
        .build()
)
