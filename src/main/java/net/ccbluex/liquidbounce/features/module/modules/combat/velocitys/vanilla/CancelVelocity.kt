package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode

class CancelVelocity : VelocityMode("Cancel") {
    override fun onVelocityPacket(event: PacketEvent) {
        event.cancelEvent()
    }
}