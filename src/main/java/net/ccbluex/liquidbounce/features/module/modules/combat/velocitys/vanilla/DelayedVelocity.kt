package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.BlinkUtils
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import java.util.concurrent.LinkedBlockingQueue
import java.util.*
import kotlin.concurrent.schedule

class DelayedVelocity : VelocityMode("Delayed") {
    
    private val delayValue = IntegerValue("Delayed-Delay", 300, 50, 1000)
    private val blinkValue = BoolValue("Delayed-Blink", true)
    private val blinkOutbound = BoolValue("Delayed-BlinkOutgoing", true).displayable { blinkValue.get() }
    private val delayC0F = BoolValue("Delayed-DelayTransaction", true).displayable { !blinkValue.get() }
    
    private var blink = false
    private var veloTick = 0
    
    private val delayTimer = MSTimer()
    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayClient>>()
    
  
    override fun onEnable() {
        packets.clear()
        blink = false
    }
    
    override fun onDisable() {
        clearPackets()
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId) {
            if (blinkValue.get()) {
                if (blinkOutbound.get()) {
                    BlinkUtils.setBlinkState(all = true)
                }
                blink = true
                delayTimer.reset()
            } else {
                event.cancelEvent()
                veloTick = mc.thePlayer.ticksExisted
                packets.add(packet as Packet<INetHandlerPlayClient>)
                queuePacket(delayValue.get().toLong())
            }
        }
        if (blink && blinkValue.get() && packet.javaClass.simpleName.startsWith("S", ignoreCase = true) && mc.thePlayer.ticksExisted > 10) {
            event.cancelEvent()
            packets.add(packet as Packet<INetHandlerPlayClient>)
        }
        
        if (!blinkValue.get() && delayC0F.get()) {
            if (packet is S32PacketConfirmTransaction && veloTick == mc.thePlayer.ticksExisted) {
                event.cancelEvent()
                packets.add(packet as Packet<INetHandlerPlayClient>)
                queuePacket(delayValue.get().toLong())
            }
        }
    }
    
    override fun onUpdate(event: UpdateEvent) {
        if (blink && blinkValue.get() && delayTimer.hasTimePassed(delayValue.get().toLong())) {
            clearPackets()
            blink = false
        }
    }

    private fun clearPackets() {
        if (blinkValue.get()) {
            while (!packets.isEmpty()) {
                PacketUtils.handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
            }
            if (blinkOutbound.get()) {
                BlinkUtils.setBlinkState(off = true, release = true)
            }
        }
    }
    
    private /*suspend*/ fun queuePacket(delayTime: Long) {
        Timer().schedule(delayTime) {
            PacketUtils.handlePacket(packets.poll() as Packet<INetHandlerPlayClient?> )
        }
    }
}
