/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.impl

abstract class Component {

    abstract fun initGui()

    abstract fun keyTyped(typedChar: Char, keyCode: Int)

    abstract fun drawScreen(mouseX: Int, mouseY: Int)

    abstract fun mouseClicked(mouseX: Int, mouseY: Int, button: Int)

    abstract fun mouseReleased(mouseX: Int, mouseY: Int, state: Int)

}