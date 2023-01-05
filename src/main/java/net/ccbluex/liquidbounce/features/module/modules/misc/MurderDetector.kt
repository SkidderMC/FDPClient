package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock

// Tested on blocksmc.com (1.8.9) - works fine
@ModuleInfo(name = "MurderDetector", category = ModuleCategory.MISC)
class MurderDetector : Module() {
    override fun onEnable() {
        detectedPlayers.clear()
        for (itemStack in mc.thePlayer.inventory.mainInventory) {
            if (itemStack.item != null && isWeapon(itemStack.item)) {
                mode = true
                return
            }
        }
        mode = false
    }

    companion object {
        var mc: Minecraft = Minecraft.getMinecraft()
        private var mode = false // Are you Killer?
        private var sendMessages = BoolValue("SendMessages", false)
        var detectedPlayers = ArrayList<EntityPlayer>()
        private var itemIds = intArrayOf(288, 396, 412, 398, 75, 50)
        private var itemTypes = arrayOf(
            Items.fishing_rod,
            Items.diamond_hoe,
            Items.golden_hoe,
            Items.iron_hoe,
            Items.stone_hoe,
            Items.wooden_hoe,
            Items.stone_sword,
            Items.diamond_sword,
            Items.golden_sword,
            ItemBlock.getItemFromBlock(Blocks.sponge),
            Items.iron_sword,
            Items.wooden_sword,
            Items.diamond_axe,
            Items.golden_axe,
            Items.iron_axe,
            Items.stone_axe,
            Items.diamond_pickaxe,
            Items.wooden_axe,
            Items.golden_pickaxe,
            Items.iron_pickaxe,
            Items.stone_pickaxe,
            Items.wooden_pickaxe,
            Items.stone_shovel,
            Items.diamond_shovel,
            Items.golden_shovel,
            Items.iron_shovel,
            Items.wooden_shovel
        )

        @EventTarget
        fun onUpdate(ignored: UpdateEvent?) {
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity is EntityPlayer) {
                    if (entity === mc.thePlayer) continue
                    if (detectedPlayers.contains(entity)) continue
                    if (entity.inventory.getCurrentItem() != null) {
                        if (isWeapon(entity.inventory.getCurrentItem().item)) {
                            displayChatMessage("§8[§dMurderDetector§8]§c " + entity.name + " is the murderer!")
                            LiquidBounce.hud.addNotification(
                                Notification(
                                    "§dMurderDetector",
                                    entity.name + " is the murderer!",
                                    NotifyType.WARNING,
                                    4000,
                                    500
                                )
                            )
                            if (sendMessages.get()) sendChatMessage(entity.name)
                            detectedPlayers.add(entity)
                        }
                    }
                }
            }
        }

        @EventTarget
        fun onWorldChange(ignored: WorldEvent?) {
            detectedPlayers.clear()
        }

        fun isWeapon(item: Item): Boolean {
            if (mode) {
                return item === Items.bow // Bow: HYP
            }
            for (id in itemIds) {
                val itemId = Item.getItemById(id)
                if (item === itemId) {
                    return true
                }
            }
            for (id in itemTypes) {
                if (item === id) {
                    return true
                }
            }
            return false
        }

        private fun sendChatMessage(target: String) {
            when ((Math.random() * 3).toInt()) {
                0 -> mc.thePlayer.sendChatMessage("$target is the killer!")
                1 -> mc.thePlayer.sendChatMessage("$target is the murderer!")
                2 -> mc.thePlayer.sendChatMessage("$target has tried to kill me")
            }
        }
    }
}