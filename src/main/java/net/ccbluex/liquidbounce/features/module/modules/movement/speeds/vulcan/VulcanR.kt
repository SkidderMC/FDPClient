package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook


class VulcanR : SpeedMode("VulcanR") {
    private var offGroundTicks = 0
    override fun onEnable() {
        offGroundTicks = 0
    }

    fun onMotion() {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0
        } else {
            offGroundTicks += 1
        }
        if (mc.thePlayer.onGround && mc.thePlayer.motionY > -.2) {
            mc.netHandler.addToSendQueue(
                C04PacketPlayerPosition(
                    (mc.thePlayer.posX + mc.thePlayer.lastTickPosX) / 2,
                    (mc.thePlayer.posY + mc.thePlayer.lastTickPosY) / 2 - 0.0784000015258789,
                    (mc.thePlayer.posZ + mc.thePlayer.lastTickPosZ) / 2,
                    false
                )
            )
            mc.netHandler.addToSendQueue(
                C06PacketPlayerPosLook(
                    (mc.thePlayer.posX + mc.thePlayer.lastTickPosX) / 2,
                    (mc.thePlayer.posY + mc.thePlayer.lastTickPosY) / 2,
                    (mc.thePlayer.posZ + mc.thePlayer.lastTickPosZ) / 2,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch,
                    true
                )
            )
            mc.netHandler.addToSendQueue(
                C04PacketPlayerPosition(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    false
                )
            )
            mc.netHandler.addToSendQueue(
                C04PacketPlayerPosition(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY - 0.0784000015258789,
                    mc.thePlayer.posZ,
                    false
                )
            )
            mc.netHandler.addToSendQueue(
                C06PacketPlayerPosLook(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch,
                    true
                )
            )
            strafe((MovementUtils.getBaseMoveSpeed() * 1.25 * 2) as Float)
        } else if (offGroundTicks == 1) {
            strafe((MovementUtils.getBaseMoveSpeed() * 0.91f) as Float)
        }
    }
}