package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.matrix

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.BlockLiquid
import net.minecraft.util.BlockPos

class MatrixJesus : JesusMode("Matrix") {
    override fun onJesus(event: UpdateEvent, blockPos: BlockPos) {
        if (mc.thePlayer.isInWater) {
            mc.gameSettings.keyBindJump.pressed = false
            if (mc.thePlayer.isCollidedHorizontally) {
                mc.thePlayer.motionY = 0.09
                return
            }
            val block = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1, mc.thePlayer.posZ))
            val blockUp = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1.1, mc.thePlayer.posZ))
            if (blockUp is BlockLiquid) {
                mc.thePlayer.motionY = 0.1
            } else if (block is BlockLiquid) {
                mc.thePlayer.motionY = 0.0
            }
            mc.thePlayer.motionX *= 1.15
            mc.thePlayer.motionZ *= 1.15
        }
    }
}