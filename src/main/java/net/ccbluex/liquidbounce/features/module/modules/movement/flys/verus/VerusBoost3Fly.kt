package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C0BPacketEntityAction

class VerusBoost3Fly : FlyMode("VerusBoost3") {
    private val reDamage = BoolValue("${valuePrefix}ReDamage", true)
    private val speedValue = FloatValue("${valuePrefix}Speed", 1.5f, 0f, 3f)

    private var ticks = 0

    override fun onEnable() {
        ticks = 1
    }

    override fun onUpdate(event: UpdateEvent) {

        if (ticks == 1) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.42, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            mc.timer.timerSpeed = 0.15f
            mc.thePlayer.jump()
            mc.thePlayer.onGround = true
        } else if (ticks == 2) {
            mc.timer.timerSpeed = 1f
        }

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
        }

        if (mc.thePlayer.fallDistance > 1) {
            mc.thePlayer.motionY = -((mc.thePlayer.posY) - Math.floor(mc.thePlayer.posY))
        }

        if (mc.thePlayer.motionY == 0.0) {
            mc.thePlayer.jump()

            mc.thePlayer.onGround = true
            mc.thePlayer.fallDistance = 0f
        }

        if (ticks < 25) {
            MovementUtils.strafe(speedValue.get())
        } else {
            if (ticks == 25){
                MovementUtils.strafe(0.48f)
            }
            if (reDamage.get()) {
                ticks = 1
            }
            MovementUtils.strafe()
        }

        ticks ++
    }

}
