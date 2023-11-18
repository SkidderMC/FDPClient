package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.minecraft.util.EnumFacing

class AACv3Longjump : LongJumpMode("AACv3") {
    private val tpdistance = FloatValue("TpDistance", 3f, 1f, 6f)
    
    private var teleported = false
    override fun onEnable() {
        teleported = false
    }
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.fallDistance > 0.5f && !teleported) {
            val value = tpdistance.get().toDouble()
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
    override fun onAttemptJump() {
        mc.thePlayer.jump()
        teleported = false
    }
    override fun onAttemptDisable() {
        longjump.state = false
    }
}
