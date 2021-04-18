package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.client.gui.inventory.GuiCrafting
import net.minecraft.network.play.client.C0EPacketClickWindow

@ModuleInfo(name = "AutoMakeBoat", description = "Automatically craft boats.", category = ModuleCategory.MISC)
class AutoMakeBoat : Module() {
    private var craftStat=0

    override fun onEnable() {
        craftStat=0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(mc.currentScreen is GuiCrafting){
            when(craftStat){
                0 -> {
                    for (i in 9 until 45) {
                        val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack

                        if (stack != null && stack.item.unlocalizedName.contains("plank",ignoreCase = true)){
                            break
                        }
                    }
                }
                5 -> {
                    craftStat=0
                    mc.thePlayer.closeScreen()
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(event.packet is C0EPacketClickWindow){
            val packet=event.packet
            chat("id=${packet.windowId}, mode=${packet.mode}, slot=${packet.slotId}, button=${packet.usedButton}")
        }
    }
}