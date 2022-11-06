package net.ccbluex.liquidbounce.features.module.modules.movement.flys.spartan

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class SpartanFastFly : FlyMode("SpartanFast") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 5f)

    override fun onEnable() {
        repeat(65) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.049, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
        }
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.1, mc.thePlayer.posZ, true))

        mc.thePlayer.motionX *= 0.1
        mc.thePlayer.motionZ *= 0.1
        mc.thePlayer.swingItem()
    }

    override fun onUpdate(event: UpdateEvent) {
        fly.antiDesync = true
        MovementUtils.resetMotion(true)
        if (mc.gameSettings.keyBindJump.isKeyDown) {
            mc.thePlayer.motionY += speedValue.get() * 0.5
        }
        if (mc.gameSettings.keyBindSneak.isKeyDown) {
            mc.thePlayer.motionY -= speedValue.get() * 0.5
        }

        MovementUtils.strafe(speedValue.get())
    }
}