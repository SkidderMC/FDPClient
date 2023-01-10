package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

class AACJesus : JesusMode("AAC") {
    override fun onJesus(event: UpdateEvent, blockPos: BlockPos) {
        if (!mc.thePlayer.onGround && BlockUtils.getBlock(blockPos) === Blocks.water || mc.thePlayer.isInWater) {
            if (!mc.thePlayer.isSprinting) {
                mc.thePlayer.motionX *= 0.99999
                mc.thePlayer.motionY *= 0.0
                mc.thePlayer.motionZ *= 0.99999
                if (mc.thePlayer.isCollidedHorizontally) {
                    mc.thePlayer.motionY = ((mc.thePlayer.posY - (mc.thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                }
            } else {
                mc.thePlayer.motionX *= 0.99999
                mc.thePlayer.motionY *= 0.0
                mc.thePlayer.motionZ *= 0.99999
                if (mc.thePlayer.isCollidedHorizontally) {
                    mc.thePlayer.motionY = ((mc.thePlayer.posY - (mc.thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                }
            }
            if (mc.thePlayer.fallDistance >= 4) {
                mc.thePlayer.motionY = -0.004
            } else if (mc.thePlayer.isInWater) mc.thePlayer.motionY = 0.09
        }
        if (mc.thePlayer.hurtTime != 0) {
            mc.thePlayer.onGround = false
        }
    }
}