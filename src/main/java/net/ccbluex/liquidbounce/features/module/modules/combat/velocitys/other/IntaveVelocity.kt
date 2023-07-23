package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode

class IntaveVelocity : VelocityMode("Intave") {
    override fun onVelocity(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime == 9) {//0.00000012 reduce lmao
            mc.thePlayer.movementInput.jump = true
        }
    }
}
