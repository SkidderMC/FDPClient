/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C09PacketHeldItemChange

class OldMineplexSpeed : SpeedMode("OldMineplex") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 0.60f, 0.48f, 2f)
    private var lastJump = false
    private var lastSpeed = 0.1f
    override fun onEnable() {
        lastJump = false
        lastSpeed = MovementUtils.getSpeed()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
    }

    override fun onUpdate() {
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
            mc.thePlayer.jump()
            MovementUtils.strafe(-0.025f)
            lastJump = true
            return
        } else if (MovementUtils.isMoving() && lastJump) {
            lastJump = false
            MovementUtils.strafe(speedValue.get())
            lastSpeed = speedValue.get()
            return
        }
        lastSpeed = (lastSpeed * 0.75f + MovementUtils.getSpeed() * 0.25f).toFloat()
        MovementUtils.strafe(lastSpeed)
    }
}
