/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.grim

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.EnumFacing

class Grim117 : VelocityMode("Grim1.17") {

    private var velocityInput = false
    override fun onPacket(event: PacketEvent) {
        mc.netHandler.addToSendQueue(
            C03PacketPlayer.C06PacketPlayerPosLook(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                mc.thePlayer.rotationYaw,
                mc.thePlayer.rotationPitch,
                mc.thePlayer.onGround
            )
        )
        mc.netHandler.addToSendQueue(
            C07PacketPlayerDigging(
                C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,mc.thePlayer.position,
                EnumFacing.DOWN)
        )
        velocityInput = false
    }

}