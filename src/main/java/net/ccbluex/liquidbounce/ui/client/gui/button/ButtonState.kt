/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.button

import net.minecraft.client.Minecraft

open class ButtonState(
    var text: String,
    var x: Int,
    var y: Int,
    var width: Int = 132,
    var height: Int = 12
) {
    var hoverFade: Int = 0
        protected set

    open fun updateHover(mouseX: Int, mouseY: Int): Boolean {
        val hovered = mouseX in x until (x + width) && mouseY in y until (y + height)

        hoverFade = when {
            hovered && hoverFade < 40 -> hoverFade + 10
            !hovered && hoverFade > 0 -> hoverFade - 10
            else -> hoverFade
        }

        return hovered
    }

    open fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {

    }
}