package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

class AAC421Jesus : JesusMode("AAC4.2.1") {
    override fun onJesus(event: UpdateEvent, blockPos: BlockPos) {
        if (!mc.thePlayer.onGround && BlockUtils.getBlock(blockPos) === Blocks.water || mc.thePlayer.isInWater) {
            mc.thePlayer.motionY *= 0.0
            mc.thePlayer.jumpMovementFactor = 0.08f
            if (mc.thePlayer.fallDistance > 0) {
                return
            } else if (mc.thePlayer.isInWater) {
                mc.gameSettings.keyBindJump.pressed = true
            }
        }
    }

}