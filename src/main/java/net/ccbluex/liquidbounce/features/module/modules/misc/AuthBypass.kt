package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.features.IntegerValue
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import java.util.*

@ModuleInfo(name = "AuthBypass", description = "Bypass auth when join server(only redesky).", category = ModuleCategory.MISC)
class AuthBypass : Module(){
    //redesky add a authbypass check :(
    private val delayValue= IntegerValue("Delay",1500,100,5000)
    private var windowId=0

    @EventTarget
    fun onPacket(event: PacketEvent){
        val packet=event.packet
        if(packet is S2FPacketSetSlot){
            if(packet.func_149175_c()==0){
                return
            }
            val item=packet.func_149174_e()
            if(item!=null&&item.displayName.contains("aqui",ignoreCase = true)){
                Timer().schedule(object :TimerTask(){
                    override fun run() {
                        mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId,packet.func_149173_d(),0,0,item,1919))
                        LiquidBounce.hud.addNotification(Notification(name,"Authenticate bypassed.", NotifyType.INFO))
                    }
                },delayValue.get().toLong())
            }
        }
        //silent auth xd
        if(packet is S2DPacketOpenWindow){
            if(packet.slotCount==27 && packet.guiId.contains("container",ignoreCase = true)
                && packet.windowTitle.unformattedText.contains("Clique no bloco verde",ignoreCase = true)){
                windowId=packet.windowId
                event.cancelEvent()
            }
        }
    }
}