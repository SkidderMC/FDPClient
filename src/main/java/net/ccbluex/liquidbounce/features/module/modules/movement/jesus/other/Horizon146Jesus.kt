package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.util.BlockPos

class Horizon146Jesus : JesusMode("Horizon1.4.6") {
    override fun onJesus(event: UpdateEvent, blockPos: BlockPos) {
        mc.gameSettings.keyBindJump.pressed = mc.thePlayer.isInWater
        if (mc.thePlayer.isInWater) {
            MovementUtils.strafe()
            if (MovementUtils.isMoving() && !mc.thePlayer.onGround) {
                mc.thePlayer.motionY += 0.13
            }
        }
    }
}