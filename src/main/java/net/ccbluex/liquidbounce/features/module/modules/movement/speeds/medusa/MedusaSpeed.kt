package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.medusa

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class MedusaSpeed : SpeedMode("MedusaFloat") {
    private var ticks = 0
    private var launchY = 0.0
    override fun onEnable() {
        ticks = 0
        launchY = mc.thePlayer.posY
    }

    override fun onUpdate() {
        if(mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(0.0, -0.4875, 0.0)).isEmpty()) {
            launchY = mc.thePlayer.posY
            return
        }
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
        if (mc.thePlayer.onGround) {
            mc.thePlayer.onGround = false
            mc.thePlayer.jump()
            MovementUtils.strafe(0.12f)
        } else {
            ticks++
            mc.thePlayer.setPosition(mc.thePlayer.posX, launchY + 0.001, mc.thePlayer.posZ)
            mc.thePlayer.motionY = 0.0
            if(ticks % 2 > 0) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, launchY + 0.00101, mc.thePlayer.posZ)
            }
            MovementUtils.strafe(0.3f)
        }
    }
}
