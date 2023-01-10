package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.vanilla

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.minecraft.util.BlockPos

class DolphinJesus : JesusMode("Dolphin") {
    override fun onJesus(event: UpdateEvent, blockPos: BlockPos) {
        if (mc.thePlayer.isInWater) mc.thePlayer.motionY += 0.03999999910593033
    }
}