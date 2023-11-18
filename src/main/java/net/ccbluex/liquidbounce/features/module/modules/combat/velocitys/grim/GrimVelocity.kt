package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.grim

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S32PacketConfirmTransaction

class GrimVelocity : VelocityMode("Grim") {
    var cancelPacket = 6
    var resetPersec = 8
    var grimTCancel = 0
    var updates = 0

    override fun onEnable() {
        grimTCancel = 0
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId) {
            event.cancelEvent()
            grimTCancel = cancelPacket
        }
        if (packet is S32PacketConfirmTransaction && grimTCancel > 0) {
            event.cancelEvent()
            grimTCancel--
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        updates++

        if (resetPersec > 0) {
            if (updates >= 0 || updates >= resetPersec) {
                updates = 0
                if (grimTCancel > 0){
                    grimTCancel--
                }
            }
        }
    }
}
