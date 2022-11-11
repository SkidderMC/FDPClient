package net.ccbluex.liquidbounce.ui.client.gui.mainmenu

import net.ccbluex.liquidbounce.LiquidBounce
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import java.awt.Color

class GuiMainSelectMenu : GuiScreen() {
    override fun initGui() {
        this.buttonList.add(GuiButton(0, this.width / 2 - 50, height / 2 - 20, 100, 20, "modern (v5)"))
        this.buttonList.add(GuiButton(1, this.width / 2 - 50, height / 2 + 10, 100, 20, "simple (v3)"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, pTicks: Float) {
        drawDefaultBackground()

        drawCenteredString(mc.fontRendererObj, "Select your main menu", width / 2, height / 2 - 40, Color.WHITE.rgb)

        mc.fontRendererObj.drawString(LiquidBounce.CLIENT_NAME, 3F, (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
        mc.fontRendererObj.drawString(LiquidBounce.CLIENT_VERSION, (this.width - mc.fontRendererObj.getStringWidth(LiquidBounce.CLIENT_VERSION) - 3).toFloat(), (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)

        super.drawScreen(mouseX, mouseY, pTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(ModernGuiMainMenu())
            1 -> mc.displayGuiScreen(ClassicGuiMainMenu())
        }
    }

    override fun keyTyped(p_keyTyped_1_: Char, p_keyTyped_2_: Int) { }
}