/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager.friendsConfig
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.extensions.sendUseItem
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import org.lwjgl.input.Mouse

object MiddleClickAction : Module(
    "MiddleClickAction", Category.OTHER, Category.SubCategory.MISCELLANEOUS, subjective = true, gameDetecting = false
) {

    private val mode by choices("Mode", arrayOf("FriendClicker", "Pearl"), "FriendClicker")

    private val slotResetDelay by int("SlotResetDelay", 1, 0..10) { mode == "Pearl" }

    private var wasDown = false
    private var pearlPressed = false

    val onRender = handler<Render2DEvent> {
        if (mode != "FriendClicker") {
            wasDown = false
            return@handler
        }

        if (mc.currentScreen != null) {
            wasDown = false
            return@handler
        }

        if (!wasDown && Mouse.isButtonDown(2)) {
            val entity = mc.objectMouseOver?.entityHit

            if (entity is EntityPlayer) {
                val playerName = stripColor(entity.name)

                if (!friendsConfig.isFriend(playerName)) {
                    friendsConfig.addFriend(playerName)
                    saveConfig(friendsConfig)
                    chat("§a§l$playerName§c was added to your friends.")
                } else {
                    friendsConfig.removeFriend(playerName)
                    saveConfig(friendsConfig)
                    chat("§a§l$playerName§c was removed from your friends.")
                }
            } else {
                chat("§c§lError: §aYou need to select a player.")
            }
        }

        wasDown = Mouse.isButtonDown(2)
    }

    val onUpdate = handler<UpdateEvent> {
        if (mode != "Pearl") {
            pearlPressed = false
            return@handler
        }

        if (mc.currentScreen != null) {
            pearlPressed = false
            return@handler
        }

        val slot = InventoryUtils.findItem(36, 44, Items.ender_pearl)

        if (slot == null) {
            pearlPressed = false
            return@handler
        }

        val pickup = mc.gameSettings.keyBindPickBlock.isKeyDown

        if (pickup) {
            SilentHotbar.selectSlotSilently(this, slot, slotResetDelay)
            pearlPressed = true
        } else if (pearlPressed) {
            val stack = mc.thePlayer?.inventory?.getStackInSlot(slot)

            if (stack != null) {
                mc.thePlayer.sendUseItem(stack)
            }

            SilentHotbar.resetSlot(this)
            pearlPressed = false
        }
    }

    override fun onDisable() {
        SilentHotbar.resetSlot(this)
        wasDown = false
        pearlPressed = false
    }
}
