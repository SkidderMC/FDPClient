package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.other

import me.zywl.fdpclient.event.MoveEvent
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.util.BlockPos

class TwillightJesus : JesusMode("Twillight") {
    override fun onJesus(event: UpdateEvent, blockPos: BlockPos) {
        if (mc.thePlayer.isInWater) {
            mc.thePlayer.motionX *= 1.04
            mc.thePlayer.motionZ *= 1.04
            MovementUtils.strafe()
        }
    }

    override fun onMove(event: MoveEvent) {
        if (!mc.thePlayer.isInWater) return

        event.y = 0.01
        mc.thePlayer.motionY = 0.01
    }
}