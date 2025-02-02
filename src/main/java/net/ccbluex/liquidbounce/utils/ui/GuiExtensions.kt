/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.ui

import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField

abstract class AbstractScreen : GuiScreen() {

    protected val textFields = arrayListOf<GuiTextField>()

    protected operator fun <T : GuiTextField> T.unaryPlus(): T {
        textFields.add(this)
        return this
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        textFields.forEach {
            it.mouseClicked(mouseX, mouseY, mouseButton)
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    protected operator fun <T : GuiButton> T.unaryPlus(): T {
        buttonList.add(this)
        return this
    }

    protected inline fun textField(
        id: Int,
        fontRenderer: FontRenderer,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        block: GuiTextField.() -> Unit = {}
    ) = +GuiTextField(id, fontRenderer, x, y, width, height).apply(block)

}