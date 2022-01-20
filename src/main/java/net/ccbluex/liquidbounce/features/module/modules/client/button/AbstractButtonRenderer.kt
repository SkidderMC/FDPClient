package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import java.awt.Color

abstract class AbstractButtonRenderer(protected val button: GuiButton) {
    abstract fun render(mouseX: Int, mouseY: Int, mc: Minecraft)

    open fun drawButtonText(mc: Minecraft) {
        mc.fontRendererObj.drawString(
            button.displayString,
            button.xPosition + button.width / 2f - mc.fontRendererObj.getStringWidth(button.displayString) / 2f,
            button.yPosition + button.height / 2f - mc.fontRendererObj.FONT_HEIGHT / 2f + 1,
            if (button.enabled) Color.WHITE.rgb else Color.GRAY.rgb,
            false
        )
    }
}