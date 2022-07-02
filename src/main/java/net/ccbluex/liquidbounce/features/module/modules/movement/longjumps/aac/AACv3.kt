package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minecraft.util.EnumFacing

class AACv3 : LongJumpMode("AACv3") {
    private var teleported = false
    override fun onEnable() {
        teleported = false
    }
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.fallDistance > 0.5f && !teleported) {
            val value = 3.0
            var x = 0.0
            var z = 0.0

            when(mc.thePlayer.horizontalFacing) {
                EnumFacing.NORTH -> z = -value
                EnumFacing.EAST -> x = +value
                EnumFacing.SOUTH -> z = +value
                EnumFacing.WEST -> x = -value
            }

            mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z)
            teleported = true
        }
    }
}