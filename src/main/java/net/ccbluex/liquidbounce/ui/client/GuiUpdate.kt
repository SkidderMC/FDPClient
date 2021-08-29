package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color

class GuiUpdate : GuiScreen() {

    override fun initGui() {
        val j = height / 4 + 48

        buttonList.add(GuiButton(1, this.width / 2 + 2, j + 24 * 2, 98, 20, "%ui.update.dismiss%"))
        buttonList.add(GuiButton(2, this.width / 2 - 100, j + 24 * 2, 98, 20, "%ui.update.download%"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if(LiquidBounce.displayedUpdateScreen){
            mc.displayGuiScreen(GuiMainMenu())
        }

        drawBackground(0)

        drawCenteredString(Fonts.font35, LanguageManager.getAndFormat("ui.update.released",LiquidBounce.latestVersion), width / 2, height / 8 + 80, 0xffffff)
//        drawCenteredString(Fonts.font35, LiquidBounce.updateMessage, width / 2, height / 8 + 80 + Fonts.font35.FONT_HEIGHT, 0xffffff)

        super.drawScreen(mouseX, mouseY, partialTicks)

        // Title
        GL11.glScalef(2F, 2F, 2F)
        drawCenteredString(Fonts.font40, "%ui.update.available%", width / 2 / 2, height / 8 / 2 + 20, Color(255, 0, 0).rgb)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            1 -> closeGui()
            2 -> MiscUtils.showURL("https://${LiquidBounce.CLIENT_WEBSITE}")
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode)
            return

        super.keyTyped(typedChar, keyCode)
    }

    private fun closeGui(){
        mc.displayGuiScreen(GuiMainMenu())
        LiquidBounce.displayedUpdateScreen=true
    }
}
