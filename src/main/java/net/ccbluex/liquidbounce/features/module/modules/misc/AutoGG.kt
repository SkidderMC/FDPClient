/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.TextValue
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.play.server.S02PacketChat

@ModuleInfo(name = "AutoGG", category = ModuleCategory.MISC)
object AutoGG : Module() {

    private val winCheckValue = TextValue("WinCheck", "Winner")
    private val delayValue = IntegerValue("Delay", 1000, 1000, 5000)
    private val ggMessageValue = TextValue("GGMessage", "GG!")

    private var winning = false
    private val timer = MSTimer()
    override fun onEnable() {
        timer.reset()
        winning = false
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        winning = false
        timer.reset()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S02PacketChat) {
            val message = packet.chatComponent.unformattedText

            if(message.contains(winCheckValue.get()) && !message.contains(":")) {
                winning = true
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(winning) {
            if(timer.hasTimePassed(delayValue.get().toLong())){
                FDPClient.hud.addNotification(Notification("AutoGG", "Sent", NotifyType.SUCCESS, 1000, 500))
                mc.thePlayer.sendChatMessage(ggMessageValue.get())
                timer.reset()
                winning = false
            }
        } else {
            timer.reset()
        }
    }
}