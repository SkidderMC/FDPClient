package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class VerusFloatSpeed : SpeedMode("VerusFloat") {

    private var ticks = 0
    private var verusBypass = false
    private var isFloating = false
    
    override fun onEnable() {
        verusBypass = false
    }

    override fun onPreMotion() {
        ticks++
        if (!mc.gameSettings.keyBindJump.isKeyDown) {
            if (mc.thePlayer.onGround) {
                ticks = 0
                MovementUtils.strafe(0.44f)
                mc.thePlayer.motionY = 0.42
                mc.timer.timerSpeed = 2.1f
                isFloating = true
            } else if (isFloating) {
                if (ticks >= 10) {
                    verusBypass = true
                    MovementUtils.strafe(0.2865f)
                    isFloating = false
                }

                if (verusBypass) {
                    if (ticks <= 1) {
                        MovementUtils.strafe(0.45f)
                    }

                    if (ticks >= 2) {
                        MovementUtils.strafe(0.69f - (ticks - 2) * 0.019f)
                    }
                }

                mc.thePlayer.motionY = 0.0
                mc.timer.timerSpeed = 0.9f

                mc.thePlayer.onGround = true
            }
        }
    }
} 
