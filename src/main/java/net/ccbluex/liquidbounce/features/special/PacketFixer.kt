package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.login.client.C00PacketLoginStart
import net.minecraft.network.login.server.S00PacketDisconnect
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S09PacketHeldItemChange
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.play.server.S40PacketDisconnect
import net.minecraft.util.ChatComponentText
import net.minecraft.world.Explosion

class PacketFixer : Listenable,MinecraftInstance() {
    private var serversideSlot=0
    private val canBlockServer=System.getProperty("no-server-block")==null

    @EventTarget
    fun onPacket(event: PacketEvent){
        val packet=event.packet

        if(packet is C09PacketHeldItemChange){
            if(packet.slotId==serversideSlot)
                event.cancelEvent()

            serversideSlot=packet.slotId
        }else if(packet is S09PacketHeldItemChange){
            serversideSlot=packet.heldItemHotbarIndex
        }else if(packet is C02PacketUseEntity){
            if(mc.thePlayer.equals(packet.getEntityFromWorld(mc.theWorld)))
                event.cancelEvent()
        }else if(packet is C08PacketPlayerBlockPlacement){
            // c08 item override to solve issues in scaffold and some other modules, maybe bypass some anticheat in future
            packet.stack=mc.thePlayer.inventory.getStackInSlot(serversideSlot)
        }else if(packet is S27PacketExplosion){
            // this can patch some client crasher and make velocity works better
            event.cancelEvent()
            // particles
            val explosion = Explosion(mc.theWorld, null, packet.x, packet.y, packet.z, packet.strength, packet.affectedBlockPositions)
            explosion.doExplosionB(true)
            // convert it to velocity packet
            val velocityPacket=S12PacketEntityVelocity(mc.thePlayer.entityId,
                mc.thePlayer.motionX+packet.func_149149_c(),
                mc.thePlayer.motionY+packet.func_149144_d(),
                mc.thePlayer.motionZ+packet.func_149147_e())
            val packetEvent=PacketEvent(velocityPacket,PacketEvent.Type.RECEIVE)
            LiquidBounce.eventManager.callEvent(packetEvent)
            if(!packetEvent.isCancelled) {
                PacketUtils.handlePacket(velocityPacket)
            }
        }else if(canBlockServer && packet is C00PacketKeepAlive){
            LiquidBounce.blockedServers.forEach{
                ServerUtils.serverData?.serverIP?.contains(it, true)
                PacketUtils.handlePacket(S40PacketDisconnect(ChatComponentText("§cVOCÊ NÃO PODE HACKEAR NESTE SERVIDOR PORQUE ESTE SERVIDOR FOI BLOQUEADO.\n" +
                        "ADICIONE UM ARGUMENTO JVM \"-Dno-server-block\" PARA IGNORAR ESTE LIMITE!")))
                return
            }
        }
    }

    override fun handleEvents(): Boolean {
        return true
    }
}