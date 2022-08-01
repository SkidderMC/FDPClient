/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.world.WorldSettings
import net.minecraft.init.Items
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "KeyPearl", category = ModuleCategory.PLAYER, keyBind = Keyboard.KEY_GRAVE, canEnable = false)
class KeyPearl : Module() {
    override fun onEnable() {
        if (mc.currentScreen != null || mc.playerController.currentGameType == WorldSettings.GameType.SPECTATOR 
		|| mc.playerController.currentGameType == WorldSettings.GameType.CREATIVE) return

    	val pearlInHotbar = InventoryUtils.findItem(36, 45, Items.ender_pearl)

        if (pearlInHotbar != -1) {
        	mc.netHandler.addToSendQueue(C09PacketHeldItemChange(pearlInHotbar - 36))
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        }
    }
}
