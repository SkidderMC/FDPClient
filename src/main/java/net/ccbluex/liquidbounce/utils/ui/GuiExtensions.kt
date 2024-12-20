/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.ui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen

abstract class AbstractScreen : GuiScreen() {
    protected operator fun <T : GuiButton> T.unaryPlus(): T {
        buttonList.add(this)
        return this
    }
}