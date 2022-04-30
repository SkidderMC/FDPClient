package net.ccbluex.liquidbounce.launch.data

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.launch.EnumLaunchFilter
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import java.awt.Color

class GuiLaunchOptionSelectMenu : GuiScreen() {
    override fun initGui() {
        //this.buttonList.add(GuiButton(0, this.width / 2 - 50, height / 2 - 20, 100, 20, "Legacy UI"))
        //this.buttonList.add(GuiButton(1, this.width / 2 - 50, height / 2 + 10, 100, 20, "Fancy UI"))

        LiquidBounce.launchFilters.addAll(when (0) {
            0 -> arrayListOf(EnumLaunchFilter.LEGACY_UI)
            1 -> arrayListOf(EnumLaunchFilter.FANCY_UI)
            else -> emptyList()
        })

        LiquidBounce.startClient()

        if(mc.currentScreen is GuiLaunchOptionSelectMenu)
            mc.displayGuiScreen(LiquidBounce.mainMenu)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, pTicks: Float) {
        drawDefaultBackground()

        drawCenteredString(mc.fontRendererObj, "FDPClient Loading...", width / 2, height / 2 - 40, Color.WHITE.rgb)

        //mc.fontRendererObj.drawString(LiquidBounce.CLIENT_NAME, 3F, (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
        //mc.fontRendererObj.drawString(LiquidBounce.CLIENT_VERSION, (this.width - mc.fontRendererObj.getStringWidth(LiquidBounce.CLIENT_VERSION) - 3).toFloat(), (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)

        super.drawScreen(mouseX, mouseY, pTicks)
    }

    override fun actionPerformed(button: GuiButton) {
    }

    override fun keyTyped(p_keyTyped_1_: Char, p_keyTyped_2_: Int) { }
}