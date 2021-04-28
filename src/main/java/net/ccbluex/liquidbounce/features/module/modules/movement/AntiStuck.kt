package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Alert
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook

@ModuleInfo(name = "AntiStuck", description = "Unstuck you when you stuck.", category = ModuleCategory.MOVEMENT)
class AntiStuck : Module() {
    private val flagValue=IntegerValue("Flag",5,1,10)

    private val timer=MSTimer()
    private var flagTime=0
    private var stuck=false

    private fun reset(){
        stuck=false
        flagTime=0
        timer.reset()
    }

    override fun onEnable() {
        reset()
    }

    @EventTarget
    fun onWorld(event: WorldEvent){
        reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(stuck){
            val freeze=LiquidBounce.moduleManager.getModule(Freeze::class.java) ?: return
            freeze.state=true

            if(timer.hasTimePassed(1500)){
                stuck=false
                flagTime=0
                freeze.state=false
                timer.reset()
            }
        }else{
            if(flagTime>flagValue.get()){
                timer.reset()
                flagTime=0
                stuck=true
                LiquidBounce.hud.addAlert(Alert("AntiStuck","Trying to unstuck you",NotifyType.OKAY,1500))
            }
            if(timer.hasTimePassed(1000)){
                flagTime=0
                timer.reset()
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        val packet=event.packet

        if(packet is S08PacketPlayerPosLook){
            flagTime++
        }
        if(stuck&&packet is C03PacketPlayer){
            event.cancelEvent()
        }
    }
}