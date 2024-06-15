/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.normal

import me.zywl.fdpclient.event.AttackEvent
import me.zywl.fdpclient.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.IntegerValue
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
