/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.grim

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation

class GrimDamage : VelocityMode("GrimDamage") {

    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime == 9) {
            val target = FDPClient.combatManager.getNearByEntity(3f)
            repeat(12) {
                mc.thePlayer.sendQueue.addToSendQueue(
                    C02PacketUseEntity(
                        target,
                        C02PacketUseEntity.Action.ATTACK
                    )
                )
                mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
            }
            mc.thePlayer.motionX *= 0.077760000
            mc.thePlayer.motionZ *= 0.077760000
        }
    }

}