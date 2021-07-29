package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.AutoDisable
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import java.util.*

@ModuleInfo(name = "AutoPlay", category = ModuleCategory.MISC)
class AutoPlay : Module(){
    private var clickState=0
    private val silentValue= BoolValue("Silent",true)
    private val delayValue= IntegerValue("JoinDelay",3,0,7)

    private var clicking=false

    override fun onEnable() {
        clickState=0
        clicking=false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(clicking&&(packet is C0EPacketClickWindow||packet is C07PacketPlayerDigging)){
            event.cancelEvent()
            return
        }
        if (packet is S2FPacketSetSlot) {
            val item=packet.func_149174_e() ?: return
            val windowId=packet.func_149175_c()
            val slot=packet.func_149173_d()
            val itemName=item.unlocalizedName
            //do check
            if(clickState==0 && windowId==0 && slot==42 && itemName.contains("paper",ignoreCase = true) && item.displayName.contains("Jogar novamente",ignoreCase = true)){
                AutoDisable.handleGameEnd()
                if(state){
                    clickState=1
                    clicking=true
                    Timer().schedule(object :TimerTask(){
                        override fun run() {
                            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(6))
                            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(item))
                            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                            clickState=2
                        }
                    },delayValue.get()*1000L)
                    LiquidBounce.hud.addNotification(Notification(name,"Sending you to next game in ${delayValue.get()}s...", NotifyType.INFO,time=delayValue.get()*1000-500))
                }
            }else if(clickState==2 && windowId!=0 && slot==11 && itemName.contains("enderPearl",ignoreCase = true)){
                Timer().schedule(object :TimerTask() {
                    override fun run() {
                        clicking=false
                        clickState=0
                        mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slot, 0, 0, item, 1919))
                    }
                },500L)
            }
        }
        if(silentValue.get() && clickState==2 && packet is S2DPacketOpenWindow){
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent){
        clicking=false
        clickState=0
    }

    override fun handleEvents() = true
}