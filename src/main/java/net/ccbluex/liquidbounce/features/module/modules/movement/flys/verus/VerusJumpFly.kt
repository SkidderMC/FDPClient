package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.client.settings.GameSettings


class VerusJumpFly : FlyMode("VerusJump") {

    private val airStrafeValue = BoolValue("${valuePrefix}AirStrafe", true)

    override fun onUpdate(event: UpdateEvent) {
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.thePlayer.jump()
            if (mc.thePlayer.fallDistance > 1) {
                mc.thePlayer.motionY = -((mc.thePlayer.posY) - Math.floor(mc.thePlayer.posY))
            }

            if (mc.thePlayer.motionY == 0.0) {
                mc.thePlayer.jump()
                mc.thePlayer.onGround = true
                mc.thePlayer.fallDistance = 0f
            }

        } else if(airStrafeValue.get()) {
            MovementUtils.strafe()
        }
    }

}
