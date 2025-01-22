/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.utils.attack.CPSCounter

/**
 * Panel Element - Abstract class
 * @author opZywl
 */
abstract class PanelElement(
    val parent: Panel,
    var x: Int,
    var y: Int,
    var width: Int,
    var height: Int
) {

    /**
     * Check if the mouse is hovering over the element.
     */
    fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        return CPSCounter.isHovering(mouseX, mouseY, x, y, x + width, y + height)
    }

    /**
     * Draw the panel element on the screen.
     */
    abstract fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float)

    /**
     * Handle mouse click events.
     */
    abstract fun mouseClicked(mouseX: Int, mouseY: Int, button: Int)

    /**
     * Handle mouse release events.
     */
    abstract fun mouseReleased(mouseX: Int, mouseY: Int, state: Int)

    /**
     * Handle keyboard input events.
     */
    abstract fun keyTyped(character: Char, code: Int)
}