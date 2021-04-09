package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "AntiVoid", description = "anti void", category = ModuleCategory.PLAYER)
class AntiVoid : Module() {
    private val startFallDistValue=FloatValue("StartFallDistance",2F,0F,5F)
    private val maxFallDistValue=IntegerValue("MaxFallDistance",10,5,20)
    private val resetMotion=BoolValue("ResetMotion",false)
    private val autoScaffold=BoolValue("AutoScaffold",true)
    private val onlyOnce=BoolValue("OnlyOnce",true)

    private val packetCache=ArrayList<C03PacketPlayer>()
    private var blink=false
    private var canBlink=false

    private var posX=0.0
    private var posY=0.0
    private var posZ=0.0
    private var motionX=0.0
    private var motionY=0.0
    private var motionZ=0.0

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(!onlyOnce.get()){
            canBlink=true
        }

        if(!blink){
            if(canBlink && mc.thePlayer.motionY<0 && mc.thePlayer.fallDistance>startFallDistValue.get()){
                posX=mc.thePlayer.posX
                posY=mc.thePlayer.posY
                posZ=mc.thePlayer.posZ
                motionX=mc.thePlayer.motionX
                motionY=mc.thePlayer.motionY
                motionZ=mc.thePlayer.motionZ

                packetCache.clear()
                blink=true
            }

            if(mc.thePlayer.onGround){
                canBlink=true
            }
        }else{
            if(mc.thePlayer.fallDistance > maxFallDistValue.get()){
                mc.thePlayer.setPositionAndUpdate(posX,posY,posZ)
                if(resetMotion.get()){
                    mc.thePlayer.motionX=0.0
                    mc.thePlayer.motionY=0.0
                    mc.thePlayer.motionZ=0.0
                }else{
                    mc.thePlayer.motionX=motionX
                    mc.thePlayer.motionY=motionY
                    mc.thePlayer.motionZ=motionZ
                }

                if(autoScaffold.get()){
                    LiquidBounce.moduleManager.getModule(Scaffold::class.java)?.state=true
                }

                packetCache.clear()
                blink=false
                canBlink=false
            }else if(mc.thePlayer.onGround){
                blink=false

                for(packet in packetCache){
                    mc.netHandler.addToSendQueue(packet)
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(blink && (event.packet is C03PacketPlayer)){
            packetCache.add(event.packet)
            event.cancelEvent()
        }
    }
}