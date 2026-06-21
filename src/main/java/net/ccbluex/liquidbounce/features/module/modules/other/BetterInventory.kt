/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.injection.forge.mixins.gui.MixinGuiContainerAccessor
import net.minecraft.client.gui.inventory.GuiContainer
import org.lwjgl.input.Mouse
import java.awt.Color

object BetterInventory : Module("BetterInventory", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    val highlightClicked by boolean("HighlightClicked", true)
    val highlightMode by choices("HighlightMode", arrayOf("Border", "Fill"), "Border") { highlightClicked }
    val highlightColor by color("HighlightColor", Color(0, 255, 0)) { highlightClicked }
    val borderWidth by float("BorderWidth", 2F, 1F..5F) { highlightClicked && highlightMode == "Border" }

    var clickedSlot = -1
        private set

    private var leftHeld = false
    private var rightHeld = false

    val onUpdate = handler<UpdateEvent> {
        val screen = mc.currentScreen as? GuiContainer ?: run {
            clickedSlot = -1
            leftHeld = false
            rightHeld = false
            return@handler
        }

        val leftDown = Mouse.isButtonDown(0)
        val rightDown = Mouse.isButtonDown(1)

        if ((leftDown && !leftHeld) || (rightDown && !rightHeld)) {
            val slot = (screen as MixinGuiContainerAccessor).hoveredSlot
            if (slot != null) {
                clickedSlot = slot.slotNumber
            }
        }

        leftHeld = leftDown
        rightHeld = rightDown
    }
}
