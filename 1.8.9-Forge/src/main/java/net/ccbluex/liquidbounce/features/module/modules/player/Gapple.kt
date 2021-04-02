package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.init.Items
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange

@ModuleInfo(name = "Gapple", description = "Eat Gapples.", category = ModuleCategory.PLAYER)
class Gapple : Module() {
    val modeValue = ListValue("Mode", arrayOf("Auto", "Once"), "Once")

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        when(modeValue.get().toLowerCase()){
            "once" -> {
                doEat()
                state = false
            }
            "auto" -> {

            }
        }
    }

    private fun doEat(){
        val gappleInHotbar = InventoryUtils.findItem(36, 45, Items.golden_apple)
        if(gappleInHotbar != -1){
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(gappleInHotbar))
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            repeat(35) {
                mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
            }
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
            ClientUtils.displayChatMessage("§8[§9§l${LiquidBounce.CLIENT_NAME}§8] §3Gapple eaten")
        }
    }
}