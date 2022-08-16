package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.utils.timer.MSTimer

class VerusHop3 : SpeedMode("VerusHop3") {

    private val timerBoost = BoolValue("TimerBoost",true)

    private var jumps = 0
    private var lastY = 0.0
  
    private val reSprint = MSTimer()

    override fun onPreMotion() {

        if (MovementUtils.isMoving()) {
            if (timerBoost.get() && (jumps >= 1)) {
                mc.timer.timerSpeed = if (mc.thePlayer.motionY < 0) { 0.88f } else { 1.25f }
            }

            when {
                mc.thePlayer.onGround -> {
                    MovementUtils.strafe(0.55f)
                    mc.thePlayer.motionY = (0.42f).toDouble()

                    if (mc.thePlayer.posY == lastY) {
                        jumps++
                    } else {
                        jumps = 0
                    }

                    lastY = mc.thePlayer.posY
                }
                else -> {
                    MovementUtils.strafe(0.349f) C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING
                }
            }
            
            if (reSprint.hasTimePassed(200)) {
                PacketUtils.sendPacketNoEvent(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
                PacketUtils.sendPacketNoEvent(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
            }
                
        }
    }
}
