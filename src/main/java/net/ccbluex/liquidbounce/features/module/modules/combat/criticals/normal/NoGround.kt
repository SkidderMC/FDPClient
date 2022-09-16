package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.normal

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.C03PacketPlayer

class NoGround : CriticalMode("NoGround") {
    private val sNoGround = BoolValue("${valuePrefix}SmartNoGround", true)
    private val newPacket = BoolValue("${valuePrefix}SmartNoGroundNewPacket", true).displayable {sNoGround.get()}
    private val packetAmount = IntegerValue("${valuePrefix}SmartNoGroundPacketAmount", 2, 1, 5).displayable {sNoGround.get() && newPacket.get()}
    
    private var shouldEdit = false
    override fun onEnable() {
        mc.thePlayer.jump()
    }
    override fun onAttack(event: AttackEvent) {
        if (sNoGround.get()){
            if (newPacket.get()) {
                repeat(packetAmount.get()) {
                    critical.sendCriticalPacket(ground = false)
                }
            } else {
                shouldEdit = true
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        if(event.packet is C03PacketPlayer) {
            if (sNoGround.get()){
                if (!newPacket.get() && shouldEdit) {
                    event.packet.onGround = false
                    shouldEdit = false
                }
            } else {
                event.packet.onGround = false
            }
        }
    }
}
