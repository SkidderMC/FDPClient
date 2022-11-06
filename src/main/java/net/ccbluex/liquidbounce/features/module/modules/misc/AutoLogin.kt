/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.TextValue
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S45PacketTitle
import java.util.*
import kotlin.concurrent.schedule

@ModuleInfo(name = "AutoLogin", category = ModuleCategory.MISC)
class AutoLogin : Module() {
    private val registerCommandValue = TextValue("Register", "/register %p %p")
    private val loginCommandValue = TextValue("Login", "/login %p")
    private val passwordValue = TextValue("Password", "password")
    private val delayValue = IntegerValue("Delay", 1500, 100, 5000)
    private val titleValue = BoolValue("Title", true)
    private val chatValue = BoolValue("Chat", true)

    private var logined = false

    override fun onEnable() {
        logined = false
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        logined = false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (logined) return
        val packet = event.packet

        if (titleValue.get() && packet is S45PacketTitle) {
            packet.message ?: return
            processMessage(packet.message.unformattedText)
        }

        if (chatValue.get() && packet is S02PacketChat) {
            processMessage(packet.chatComponent.unformattedText)
        }
    }

    private fun processMessage(msg: String) {
        if (registerCommandValue.get().isNotBlank()) {
            val regCommand = registerCommandValue.get().split(" ")[0]
            if (regCommand.isNotEmpty() && msg.contains(regCommand, ignoreCase = true)) {
                delayedMessage(registerCommandValue.get().replace("%p", passwordValue.get()))
            }
        }
        if (loginCommandValue.get().isNotBlank()) {
            val logCommand = loginCommandValue.get().split(" ")[0]
            if (logCommand.isNotEmpty() && msg.contains(logCommand, ignoreCase = true)) {
                delayedMessage(loginCommandValue.get().replace("%p", passwordValue.get()))
            }
        }
    }

    private fun delayedMessage(message: String) {
        logined = true
        Timer().schedule(delayValue.get().toLong()) {
            if (state && mc.thePlayer != null) {
                mc.thePlayer.sendChatMessage(message)
            }
        }
    }
}