package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.normal

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.C03PacketPlayer

class NoGround : CriticalMode("NoGround") {
    private val sNoGround = BoolValue("${valuePrefix}SmartNoGround", true)
    override fun onEnable() {
        mc.thePlayer.jump()
    }
    override fun onAttack(event: AttackEvent) {
        if (sNoGround.get()){
            critical.sendCriticalPacket(ground = false)
            critical.sendCriticalPacket(ground = false)
        }
    }

    override fun onPacket(event: PacketEvent) {
        if(event.packet is C03PacketPlayer) {
            if (!sNoGround.get()){
                event.packet.onGround = false
            }
        }
    }
}