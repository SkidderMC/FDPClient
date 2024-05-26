/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import com.google.gson.Gson
import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.PacketEvent
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.network.play.server.S02PacketChat
import java.io.File

@ModuleInfo(name = "ChatResponder", category = ModuleCategory.OTHER)
object ChatResponder : Module() {

    private val gson = Gson()
    private val configFile = File(FDPClient.fileManager.dir, "chat_responses.json")
    private val responses = mutableMapOf<String, String>()

    init {
        loadResponses()
        registerCommands()
    }

    private fun loadResponses() {
        if (configFile.exists()) {
            val json = configFile.readText()
            responses.putAll(gson.fromJson(json, responses.javaClass))
        }
    }

    private fun saveResponses() {
        val json = gson.toJson(responses)
        configFile.writeText(json)
    }

    private fun registerCommands() {
        val commandManager = FDPClient.commandManager

        commandManager.registerCommand(object : Command("ChatResponder", arrayOf("cr")) {
            override fun execute(args: Array<String>) {
                when (args.getOrNull(1)) {
                    "add" -> addResponse(args.getOrNull(2), args.drop(3).joinToString(" "))
                    "list" -> listResponses()
                    "delete" -> deleteResponse(args.getOrNull(2))
                    else -> chatSyntax(arrayOf("add <TriggerMessage> <ResponseMessage>", "list", "delete <TriggerMessage>"))
                }
            }

            override fun tabComplete(args: Array<String>): List<String> {
                return when (args.size) {
                    2 -> listOf("add", "list", "delete")
                    3 -> when (args[1]) {
                        "add", "delete" -> responses.keys.toList()
                        else -> emptyList()
                    }
                    else -> emptyList()
                }
            }
        })
    }

    private fun addResponse(trigger: String?, response: String?) {
        if (trigger == null || response == null) {
            chat("Usage: .ChatResponder add <TriggerMessage> <ResponseMessage>")
            return
        }
        responses[trigger] = response
        saveResponses()
        chat("Response added for trigger '$trigger'")
    }

    private fun listResponses() {
        if (responses.isEmpty()) {
            chat("No responses found.")
            return
        }
        chat("Responses:")
        responses.forEach { (trigger, response) ->
            chat("$trigger -> $response")
        }
    }

    private fun deleteResponse(trigger: String?) {
        if (trigger == null) {
            chat("Usage: .ChatResponder delete <TriggerMessage>")
            return
        }
        if (responses.remove(trigger) != null) {
            saveResponses()
            chat("Response deleted for trigger '$trigger'")
        } else {
            chat("No response found for trigger '$trigger'")
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is S02PacketChat) {
            val chatMessage = (event.packet as S02PacketChat).chatComponent.unformattedText
            responses.forEach { (trigger, response) ->
                if (chatMessage.contains(trigger)) {
                    mc.thePlayer.sendChatMessage(response)
                    return
                }
            }
        }
    }

    override fun onDisable() {
        saveResponses()
    }
}
