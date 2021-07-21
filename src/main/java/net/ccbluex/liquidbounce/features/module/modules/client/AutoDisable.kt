package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S2FPacketSetSlot

@ModuleInfo(name = "AutoDisable", description = "Auto disable modules.", category = ModuleCategory.CLIENT, defaultOn = true)
object AutoDisable : Module() {
    @EventTarget
    fun onWorld(event: WorldEvent){
        LiquidBounce.moduleManager.modules
            .filter { it.state&&it.autoDisable==EnumAutoDisableType.RESPAWN }
            .forEach { module ->
                module.state=false
                LiquidBounce.hud.addNotification(Notification(this.name,"Disabled ${module.name} due to respawn.",NotifyType.WARNING,2000))
            }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(event.packet is S08PacketPlayerPosLook){
            LiquidBounce.moduleManager.modules
                .filter { it.state&&it.autoDisable==EnumAutoDisableType.FLAG }
                .forEach { module ->
                    module.state=false
                    LiquidBounce.hud.addNotification(Notification(this.name,"Disabled ${module.name} due to flag.",NotifyType.WARNING,2000))
                }
        }
    }

    fun handleGameEnd(){
        LiquidBounce.moduleManager.modules
            .filter { it.state&&it.autoDisable==EnumAutoDisableType.GAME_END }
            .forEach { module ->
                module.state=false
                LiquidBounce.hud.addNotification(Notification(this.name,"Disabled ${module.name} due to game end.",NotifyType.WARNING,2000))
            }
    }
}