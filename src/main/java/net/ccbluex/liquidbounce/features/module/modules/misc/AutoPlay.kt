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
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import java.util.*

@ModuleInfo(name = "AutoPlay", category = ModuleCategory.MISC)
class AutoPlay : Module(){
    private var clickState=0
    private val modeValue=ListValue("Server", arrayOf("RedeSky", "BlocksMC", "Minemora"), "RedeSky")
    private val silentValue=BoolValue("Silent",true)
    private val delayValue=IntegerValue("JoinDelay",3,0,7)

    private var clicking=false

    override fun onEnable() {
        clickState=0
        clicking=false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if(modeValue.get().equals("RedeSky",true)){
            if(clicking&&(packet is C0EPacketClickWindow||packet is C07PacketPlayerDigging)){
                event.cancelEvent()
                return
            }
            if(silentValue.get() && clickState==2 && packet is S2DPacketOpenWindow){
                event.cancelEvent()
            }
        }

        if (packet is S2FPacketSetSlot) {
            val item=packet.func_149174_e() ?: return
            val windowId=packet.func_149175_c()
            val slot=packet.func_149173_d()
            val itemName=item.unlocalizedName
            val displayName=item.displayName

            if(modeValue.get().equals("RedeSky",true)){
                if(clickState==0 && windowId==0 && slot==42 && itemName.contains("paper",ignoreCase = true) && displayName.contains("Jogar novamente",ignoreCase = true)){
                    clickState=1
                    clicking=true
                    queueAutoPlay {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(6))
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(item))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        clickState=2
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

            if(modeValue.get().equals("BlocksMC",true)){
                if(clickState==0 && windowId==0 && slot==43 && itemName.contains("paper",ignoreCase = true) && displayName.contains("Play Again",ignoreCase = true)){
                    queueAutoPlay {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(7))
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(item))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
                    clickState=1
                }
            }
        }
        if(packet is S02PacketChat) {
            val text=packet.chatComponent.unformattedText
            // Minemora check
            if(text.contains("Has click en alguna de las siguientes opciones",true)) {
                queueAutoPlay {
                    mc.thePlayer.sendChatMessage("/join")
                }
            }
        }
    }

    private fun queueAutoPlay(runnable: () -> Unit){
        AutoDisable.handleGameEnd()
        if(this.state){
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    runnable()
                }
            },delayValue.get().toLong()*1000)
            LiquidBounce.hud.addNotification(Notification(this.name,"Sending you to next game in ${delayValue.get()}s...", NotifyType.INFO,delayValue.get()*1000))
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent){
        clicking=false
        clickState=0
    }

    override fun handleEvents() = true
}
