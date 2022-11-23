/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper

class AACPortSpeed : SpeedMode("AACPort") {
    private val length = FloatValue("${valuePrefix}Length", 1F, 1F, 20F)

    override fun onUpdate() {
        if (!MovementUtils.isMoving()) return

        val f = mc.thePlayer.rotationYaw * 0.017453292f
        var d = 0.2
        while (d <= length.get()) {
            val x = mc.thePlayer.posX - MathHelper.sin(f) * d
            val z = mc.thePlayer.posZ + MathHelper.cos(f) * d

            if (mc.thePlayer.posY < mc.thePlayer.posY.toInt() + 0.5 && getBlock(BlockPos(x, mc.thePlayer.posY, z)) !is BlockAir) {
                break
            }

            mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(x, mc.thePlayer.posY, z, true))
            d += 0.2
        }
    }
}