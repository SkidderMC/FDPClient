/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.injection.forge.mixins.gui.MixinGuiContainerAccessor
import net.minecraft.client.gui.inventory.GuiContainer
import org.lwjgl.input.Mouse

/**
 * Hold a mouse button and drag across slots in any container screen to shift-move each stack you
 * pass over, instead of clicking them one by one. It only fires while a button is held with an empty
 * cursor, so it never interferes with vanilla drag-distribution and only acts once per slot.
 */
object ItemScroller : Module("ItemScroller", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val button by choices("Button", arrayOf("Left", "Right", "Both"), "Both")

    private var lastSlot = -1

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        val screen = mc.currentScreen as? GuiContainer ?: run {
            lastSlot = -1
            return@handler
        }

        val held = when (button.lowercase()) {
            "left" -> Mouse.isButtonDown(0)
            "right" -> Mouse.isButtonDown(1)
            else -> Mouse.isButtonDown(0) || Mouse.isButtonDown(1)
        }

        if (!held) {
            lastSlot = -1
            return@handler
        }

        // Leave vanilla drag-distribution alone whenever a stack is on the cursor.
        if (player.inventory.itemStack != null) {
            return@handler
        }

        val slot = (screen as MixinGuiContainerAccessor).hoveredSlot ?: return@handler
        val slotId = slot.slotNumber

        if (slotId == lastSlot) {
            return@handler
        }
        lastSlot = slotId

        if (!slot.hasStack) {
            return@handler
        }

        // Quick-move (shift-click) transfers the stack to the other inventory.
        mc.playerController.windowClick(player.openContainer.windowId, slotId, 0, 1, player)
    }
}
