package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue

class NCPYPort2Speed : SpeedMode("NCPYPort2") {
    var ticks = 0
    var jumps = 0

    private val jumpBoost = FloatValue("JumpLaunchSpeed", 0.98f, 0f, 2f)
    private val speedMult = FloatValue("BoostSpeed", 1.02f, 0f, 2f)
    private val launchTimer = FloatValue("LaunchTimer", 0.97f,0.05f,2f)
    private val normTimer = FloatValue("Timer", 1.2f,0.05f,2f)


    override fun onEnable() {
        super.onEnable()
        mc.thePlayer.speedInAir = 0.0213f
    }

    override fun onDisable() {
        mc.thePlayer.speedInAir = 0.02f
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }

    override fun onUpdate() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                if (jumps <= 4) {
                    mc.timer.timerSpeed = launchTimer.get()
                    mc.thePlayer.jump()
                    mc.thePlayer.motionX *= jumpBoost.get().toDouble()
                    mc.thePlayer.motionZ *= jumpBoost.get().toDouble()
                    ticks = 1
                } else if (jumps >= 5) {
                    jumps = 0
                }
                jumps ++
                MovementUtils.strafe()
            } else {
                mc.timer.timerSpeed = normTimer.get()
                if (ticks <= 4) {
                    if(ticks % 2 == 0) {
                        mc.thePlayer.motionY = -0.17
                    } else {
                        mc.thePlayer.motionY = -0.1
                    }
                }

                ticks++
                mc.thePlayer.motionX = mc.thePlayer.motionX * speedMult.get().toDouble()
                mc.thePlayer.motionZ = mc.thePlayer.motionZ * speedMult.get().toDouble()
                MovementUtils.strafe()
            }

        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
