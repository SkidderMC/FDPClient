/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.misc

import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.event.WorldEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.IntegerValue
import net.skiddermc.fdpclient.value.TextValue
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S45PacketTitle
import java.util.*
import kotlin.concurrent.schedule

@ModuleInfo(name = "AutoLogin", category = ModuleCategory.MISC)
class AutoLogin : Module() {
    private val registerCommand = TextValue("Register", "/register %p %p")
    private val loginCommand = TextValue("Login", "/login %p")
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
        if (registerCommand.get().isNotBlank()) {
            val regCommand = registerCommand.get().split(" ")[0]
            if (regCommand.isNotEmpty() && msg.contains(regCommand, ignoreCase = true)) {
                delayedMessage(registerCommand.get().replace("%p", passwordValue.get()))
            }
        }
        if (loginCommand.get().isNotBlank()) {
            val logCommand = loginCommand.get().split(" ")[0]
            if (logCommand.isNotEmpty() && msg.contains(logCommand, ignoreCase = true)) {
                delayedMessage(loginCommand.get().replace("%p", passwordValue.get()))
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