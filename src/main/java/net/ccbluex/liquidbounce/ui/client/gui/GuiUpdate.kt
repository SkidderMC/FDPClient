/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.FDPClient.IN_DEV
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.handler.api.ClientUpdate
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

class GuiUpdate : GuiScreen() {

    override fun initGui() {
        val j = height / 4 + 48

        buttonList.run {
            add(GuiButton(1, width / 2 + 2, j + 24 * 2, 98, 20, "Ignore"))
            add(GuiButton(2, width / 2 - 100, j + 24 * 2, 98, 20, "Go to download page"))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        if (!IN_DEV) {
            Fonts.font35.drawCenteredStringWithShadow("${ClientUpdate.newestVersion?.lbVersion} got released!", width / 2f, height / 8f + 80, 0xffffff)
        } else {
            Fonts.font35.drawCenteredStringWithShadow("New build available!", width / 2f, height / 8f + 80, 0xffffff)
        }

        Fonts.font35.drawCenteredStringWithShadow("Press \"Download\" to visit our website or dismiss this message by pressing \"OK\".", width / 2f, height / 8f + 80 + Fonts.font35.fontHeight, 0xffffff)

        super.drawScreen(mouseX, mouseY, partialTicks)

        // Title
        glScalef(2F, 2F, 2F)
        Fonts.font35.drawCenteredStringWithShadow("New update available!", width / 4f, height / 16f + 20, Color(255, 0, 0).rgb)

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            1 -> mc.displayGuiScreen(GuiMainMenu())
            2 -> MiscUtils.showURL("https://fdpinfo.github.io/download")
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode)
            return

        super.keyTyped(typedChar, keyCode)
    }
}