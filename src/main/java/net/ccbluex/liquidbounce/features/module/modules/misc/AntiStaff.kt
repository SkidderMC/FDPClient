package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S1DPacketEntityEffect
import java.util.regex.Pattern

import kotlin.concurrent.thread

@ModuleInfo(name = "AntiStaff", category = ModuleCategory.MISC)
class AntiStaff : Module() {

    private var obStaffs = "none"
    private var detected = false
    override fun onEnable() {
        Thread {
            try {
                obStaffs = HttpUtils.get("https://gitee.com/insaneNMSL/bmcstaff/raw/master/stafflist")
                println("[Staff list] " + obStaffs)
                LiquidBounce.hud.addNotification(Notification("%notify.module.title%","BlocksMC Staff list ready" , NotifyType.INFO))
            } catch (e: Exception) {
                LiquidBounce.hud.addNotification(Notification("%notify.module.title%","Cant load BlocksMC Staff list" , NotifyType.ERROR))
                e.printStackTrace()
            }
            detected = false
        }.start()
    }
    @EventTarget
    fun onWorld(e: WorldEvent) {
        detected = false
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if (mc.theWorld == null || mc.thePlayer == null) return

        val packet = event.packet // smart convert
        if (packet is S1DPacketEntityEffect) {
            val entity = mc.theWorld.getEntityByID(packet.entityId)
            if (entity != null && (obStaffs.contains(entity.name) || obStaffs.contains(entity.displayName.unformattedText))) {
                if (!detected) {
                    LiquidBounce.hud.addNotification(Notification(name, "Detected BlocksMC staff members with invis. You should quit ASAP.", NotifyType.WARNING, 8000))
                    mc.thePlayer.sendChatMessage("/leave")
                    detected = true
                }
            }
        }
        if (packet is S14PacketEntity) {
            val entity = packet.getEntity(mc.theWorld)

            if (entity != null && (obStaffs.contains(entity.name) || obStaffs.contains(entity.displayName.unformattedText))) {
                if (!detected) {
                    LiquidBounce.hud.addNotification(Notification(name, "Detected BlocksMC staff members. You should quit ASAP.", NotifyType.WARNING,8000))
                    mc.thePlayer.sendChatMessage("/leave")
                    detected = true
                }
            }
        }
    }
}