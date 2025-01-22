/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui

abstract class GuiPanel {
    var rectWidth: Float = 0f
    var rectHeight: Float = 0f

    abstract fun initGui()

    abstract fun keyTyped(typedChar: Char, keyCode: Int)

    abstract fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float, alpha: Int)

    abstract fun mouseClicked(mouseX: Int, mouseY: Int, button: Int)

    abstract fun mouseReleased(mouseX: Int, mouseY: Int, button: Int)
}