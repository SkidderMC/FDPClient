/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacketNoEvent
import net.ccbluex.liquidbounce.utils.timer.TimerUtil
import net.ccbluex.liquidbounce.value.FloatValue

import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook

@ModuleInfo(name = "AntiFall", category = ModuleCategory.PLAYER)
class AntiFall : Module() {
    var lastGroundPos = DoubleArray(3)
    @EventTarget
    fun onPacket(e: PacketEvent) {
        if (!packets.isEmpty() && mc.thePlayer.ticksExisted < 100) packets.clear()
        if (e.packet is C03PacketPlayer) {
            if (isInVoid) {
                e.cancelEvent()
                packets.add(e.packet)
                if (timer.delay(pullbackTime.get())) {
                    sendPacketNoEvent(
                        C04PacketPlayerPosition(
                            lastGroundPos[0], lastGroundPos[1] - 1,
                            lastGroundPos[2], true
                        )
                    )
                }
            } else {
                lastGroundPos[0] = mc.thePlayer.posX
                lastGroundPos[1] = mc.thePlayer.posY
                lastGroundPos[2] = mc.thePlayer.posZ
                if (!packets.isEmpty()) {
                    for (p in packets) sendPacketNoEvent(p)
                    packets.clear()
                }
                timer.reset()
            }
        }
    }

    @EventTarget
    fun onRevPacket(e: PacketEvent) {
        if (e.packet is S08PacketPlayerPosLook && packets.size > 1) {
            packets.clear()
        }
    }

    override val tag: String
        get() = pullbackTime.get().toString()

    companion object {
        var pullbackTime = FloatValue("Pullback Time", 1500f, 100f, 2000f)
        var timer: TimerUtil = TimerUtil()
        var packets = ArrayList<C03PacketPlayer>()
        val isInVoid: Boolean
            get() {
                for (i in 0..128) {
                    if (MovementUtils.isOnGround(i.toDouble())) {
                        return false
                    }
                }
                return true
            }
        val isPullbacking: Boolean
            get() = LiquidBounce.moduleManager[AntiFall::class.java]!!.state && !packets.isEmpty()
    }
}