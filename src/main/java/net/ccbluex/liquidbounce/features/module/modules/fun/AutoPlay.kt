package net.ccbluex.liquidbounce.features.module.modules.`fun`

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.notify.Notification
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import java.util.*

@ModuleInfo(name = "AutoPlay", description = "Auto join another game(only redesky).", category = ModuleCategory.FUN)
class AutoPlay : Module(){
    private var clickState=0
    private val silentValue=BoolValue("Silent",true)
    private val delayValue=IntegerValue("JoinDelay",3,0,7)

    override fun onEnable() {
        clickState=0
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S2FPacketSetSlot) {
            val item=packet.func_149174_e() ?: return
            val windowId=packet.func_149175_c()
            val slot=packet.func_149173_d()
            val name=item.unlocalizedName
            //do check
            if(clickState==0 && windowId==0 && slot==42 && name.contains("paper",ignoreCase = true) && item.displayName.contains("Jogar novamente",ignoreCase = true)){
                LiquidBounce.hud.addNotification(Notification("Sending you to next game in ${delayValue.get()}s...",NotifyType.INFO))
                clickState=1
                Timer().schedule(object :TimerTask(){
                    override fun run() {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(6))
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(item))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        clickState=2
                    }
                },delayValue.get()*1000L)
            }else if(clickState==2 && windowId!=0 && slot==11 && name.contains("enderPearl",ignoreCase = true)){
                Timer().schedule(object :TimerTask() {
                    override fun run() {
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
        clickState=0
    }
}