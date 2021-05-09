package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.item.ItemSword
import net.minecraft.network.play.server.S2FPacketSetSlot

@ModuleInfo(name = "KeepSword", description = "keep sword for fun and screenshot lol", category = ModuleCategory.MISC)
class KeepSword : Module() {
    private val replacedSlot=ArrayList<Int>()

    override fun onEnable() {
        replacedSlot.clear()
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(event.packet is S2FPacketSetSlot){
            val packet = event.packet
            val item=packet.func_149174_e()
            val windowId=packet.func_149175_c()
            if(windowId!=0) return
            val slot=packet.func_149173_d()
            val originalItem=(mc.thePlayer.inventoryContainer.getSlot(slot)?:return).stack?:return

            var isSword=item!=null
            if(isSword) isSword=(item.item is ItemSword)

            if(originalItem.item is ItemSword&&!isSword){
                if(replacedSlot.contains(slot)){
                    replacedSlot.remove(slot)
                }else{
                    replacedSlot.add(slot)
                    event.cancelEvent()
                }
            }
        }
    }
}