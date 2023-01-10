package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

class AAC3311Jesus : JesusMode("AAC3.3.11") {
    override fun onJesus(event: UpdateEvent, blockPos: BlockPos) {
        if (mc.thePlayer.isInWater) {
            mc.thePlayer.motionX *= 1.17
            mc.thePlayer.motionZ *= 1.17
            if (mc.thePlayer.isCollidedHorizontally) {
                mc.thePlayer.motionY = 0.24
            } else if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ)).block !== Blocks.air) {
                mc.thePlayer.motionY += 0.04
            }
        }
    }
}