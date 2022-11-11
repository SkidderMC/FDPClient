/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.mainmenu

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.GuiBackground
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.minecraft.client.gui.*
import net.minecraft.client.resources.I18n
import net.minecraftforge.fml.client.GuiModList
import java.awt.Color

class ClassicGuiMainMenu : GuiScreen(), GuiYesNoCallback {
    override fun initGui() {
        val defaultHeight = (this.height / 3.5).toInt()

        this.buttonList.add(GuiButton(1, this.width / 2 - 50, defaultHeight, 100, 20, I18n.format("menu.singleplayer")))
        this.buttonList.add(GuiButton(2, this.width / 2 - 50, defaultHeight + 24, 100, 20, I18n.format("menu.multiplayer")))
        this.buttonList.add(GuiButton(100, this.width / 2 - 50, defaultHeight + 24 * 2, 100, 20, "%ui.altmanager%"))
        this.buttonList.add(GuiButton(103, this.width / 2 - 50, defaultHeight + 24 * 3, 100, 20, "%ui.mods%"))
        this.buttonList.add(GuiButton(102, this.width / 2 - 50, defaultHeight + 24 * 4, 100, 20, "%ui.background%"))
        this.buttonList.add(GuiButton(0, this.width / 2 - 50, defaultHeight + 24 * 5, 100, 20, I18n.format("menu.options")))
        this.buttonList.add(GuiButton(4, this.width / 2 - 50, defaultHeight + 24 * 6, 100, 20, I18n.format("menu.quit")))

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        val bHeight = (this.height / 3.5).toInt()

        Gui.drawRect(width / 2 - 60, bHeight - 30, width / 2 + 60, bHeight + 174, Integer.MIN_VALUE)

        mc.fontRendererObj.drawCenteredString(LiquidBounce.CLIENT_NAME, (width / 2).toFloat(), (bHeight - 20).toFloat(), Color.WHITE.rgb, false)
        mc.fontRendererObj.drawString(LiquidBounce.CLIENT_VERSION, 3F, (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
        "§cWebsite: §fhttps://${LiquidBounce.CLIENT_WEBSITE}/".also { str ->
            mc.fontRendererObj.drawString(str, (this.width - mc.fontRendererObj.getStringWidth(str) - 3).toFloat(), (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(GuiAltManager(this))
            102 -> mc.displayGuiScreen(GuiBackground(this))
            103 -> mc.displayGuiScreen(GuiModList(this))
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}