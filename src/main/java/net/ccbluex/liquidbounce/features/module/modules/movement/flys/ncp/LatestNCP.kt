package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import jdk.nashorn.internal.ir.Block
import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

class LatestNCP : FlyMode("LatestNCP") {
    private val speedValue = FloatValue("Speed", 15f, 10f, 20f)
    private val timerValue = FloatValue("Timer", 0.8F , 0.5f , 1.0f)
    private var jumped = false

    override fun onEnable() {
        if(!mc.thePlayer.onGround) {
            fly.state = false
            //alert
        } else {
            mc.thePlayer.jump()
            mc.thePlayer.motionY = 0.2
            jumped = true
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.timer.timerSpeed = timerValue.get()
        if(jumped) {
            jumped = false
            mc.thePlayer.motionY = -0.0784
        }
        MovementUtils.strafe(speedValue.get())
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
    }

}
