package net.ccbluex.liquidbounce.features.module.modules.movement.flys.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer

class AAC1910Fly : FlyMode("AAC1.9.10") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 0.3f, 0.2f, 1.7f)

    private var aacJump = 0.0
    
    override fun onEnable() {
        aacJump = -3.8
    }

    override fun onUpdate(event: UpdateEvent) {
        if (mc.gameSettings.keyBindJump.isKeyDown) aacJump += 0.2

        if (mc.gameSettings.keyBindSneak.isKeyDown) aacJump -= 0.2

        if (fly.launchY + aacJump > mc.thePlayer.posY) {
            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
            mc.thePlayer.motionY = 0.8
            MovementUtils.strafe(speedValue.get())
        }

        MovementUtils.strafe()
    }
}
