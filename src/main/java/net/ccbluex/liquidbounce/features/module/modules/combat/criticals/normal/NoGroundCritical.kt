package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.normal

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.network.play.client.C03PacketPlayer

class NoGroundCritical : CriticalMode("NoGround") {
    private val autoJumpValue = BoolValue("${valuePrefix}AutoJump", false)
    private val smartValue = BoolValue("${valuePrefix}Smart", true)
    private val morePacketValue = BoolValue("${valuePrefix}MorePacket", false)
    private val packetAmount = IntegerValue("${valuePrefix}PacketAmount", 2, 1, 5).displayable {morePacketValue.get()}
    
    private var shouldEdit = false
    override fun onEnable() {
        if (autoJumpValue.get()) {
            mc.thePlayer.jump()
        }
    }
    override fun onAttack(event: AttackEvent) {
        if (morePacketValue.get()) {
            repeat(packetAmount.get()) {
                shouldEdit = true
                critical.sendCriticalPacket(ground = false)
                shouldEdit = true //make sure to modify one more C03 after attack.
            }
        } else {
            shouldEdit = true
        }
    }

    override fun onPacket(event: PacketEvent) {
        if(event.packet is C03PacketPlayer) {
            if (smartValue.get()){
                if (shouldEdit) {
                    event.packet.onGround = false
                    shouldEdit = false
                }
            } else {
                event.packet.onGround = false
            }
        }
    }
}
