package net.ccbluex.liquidbounce.ui.elements

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.special.AntiForge
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.*
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.util.ResourceLocation
import java.awt.Color
import org.lwjgl.opengl.GL11.*

object ToolDropdown {

    private var fullHeight = 0F
    private var dropState = false

    private val expandIcon = ResourceLocation("fdpclient/imgs/icon/expand.png")

    @JvmStatic
    fun handleDraw(button: GuiButton) {
        val gray = Color(100, 100, 100).rgb
        val bWidth = button.getButtonWidth().toFloat()

        glPushMatrix()
        glTranslatef(button.xPosition.toFloat() + button.getButtonWidth().toFloat() - 10F, button.yPosition.toFloat() + 10F, 0F)
        if (button.isMouseOver())
            glTranslatef(0F, if (dropState) -1F else 1F, 0F)
        glPushMatrix()
        glRotatef(180F * (fullHeight / 100F), 0F, 0F, 1F)
        RenderUtils.drawImage(expandIcon, -4, -4, 8, 8)
        glPopMatrix()
        glPopMatrix()
        resetColor()

        if (!dropState && fullHeight == 0F) return
        fullHeight = AnimationUtils.animate(if (dropState) 100F else 0F, fullHeight, 0.01F * RenderUtils.deltaTime.toFloat())

        glPushMatrix()
        RenderUtils.makeScissorBox(button.xPosition.toFloat(), button.yPosition.toFloat() + 20F, button.xPosition.toFloat() + bWidth, button.yPosition.toFloat() + 20F + fullHeight)
        glEnable(GL_SCISSOR_TEST)
        glPushMatrix()
        glTranslatef(button.xPosition.toFloat(), button.yPosition.toFloat() + 20F - (100F - fullHeight), 0F)
        RenderUtils.newDrawRect(0F, 0F, bWidth, 100F, Color(24, 24, 24).rgb)
        Fonts.font35.drawString("AntiForge", 4F, 7F, -1)
        Fonts.font35.drawString("Block FML", 4F, 27F, if (AntiForge.enabled) -1 else gray)
        Fonts.font35.drawString("Block FML Proxy Packets", 4F, 47F, if (AntiForge.enabled) -1 else gray)
        Fonts.font35.drawString("Block Payload Packets", 4F, 67F, if (AntiForge.enabled) -1 else gray)
        drawToggleSwitch(bWidth - 24F, 5F, 20F, 10F, AntiForge.enabled)
        Fonts.font40.drawString("BungeeCord Spoof", 4F, 85F, -1)
        drawCheckbox(bWidth - 14F, 25F, 10F, AntiForge.blockFML)
        drawCheckbox(bWidth - 14F, 45F, 10F, AntiForge.blockProxyPacket)
        drawCheckbox(bWidth - 14F, 65F, 10F, AntiForge.blockPayloadPackets)
        drawToggleSwitch(bWidth - 20F, 86F, 16F, 8F, BungeeCordSpoof.enabled)
        glPopMatrix()
        glDisable(GL_SCISSOR_TEST)
        glPopMatrix()
    }

    @JvmStatic
    fun handleClick(mouseX: Int, mouseY: Int, button: GuiButton): Boolean {
        val bX = button.xPosition.toFloat()
        val bY = button.yPosition.toFloat()
        val bWidth = button.getButtonWidth().toFloat()
        if (dropState && isMouseOver(mouseX, mouseY, bX, bY + 20F, bWidth, fullHeight)) {
            when {
                isMouseOver(mouseX, mouseY, bX, bY + 20F, bWidth, 20F) -> AntiForge.enabled = !AntiForge.enabled
                isMouseOver(mouseX, mouseY, bX, bY + 40F, bWidth, 20F) -> AntiForge.blockFML = !AntiForge.blockFML
                isMouseOver(mouseX, mouseY, bX, bY + 60F, bWidth, 20F) -> AntiForge.blockProxyPacket = !AntiForge.blockProxyPacket
                isMouseOver(mouseX, mouseY, bX, bY + 80F, bWidth, 20F) -> AntiForge.blockPayloadPackets = !AntiForge.blockPayloadPackets
                isMouseOver(mouseX, mouseY, bX, bY + 100F, bWidth, 20F) -> BungeeCordSpoof.enabled = !BungeeCordSpoof.enabled
            }
            FDPClient.fileManager.saveConfig(FDPClient.fileManager.specialConfig)
            return true
        }
        return false
    }

    private fun isMouseOver(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, height: Float) = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height

    @JvmStatic
    fun toggleState() {
        dropState = !dropState
    }

    fun drawToggleSwitch(x: Float, y: Float, width: Float, height: Float, state: Boolean) {
        val borderColor = if (state) Color(0, 140, 255).rgb else Color(160, 160, 160).rgb
        val mainColor = if (state) borderColor else Color(24, 24, 24).rgb
        RenderUtils.originalRoundedRect(x - 0.5F, y - 0.5F, x + width + 0.5F, y + height + 0.5F, (height + 1F) / 2F, borderColor)
        RenderUtils.originalRoundedRect(x, y, x + width, y + height, height / 2F, mainColor)
        if (state)
            RenderUtils.drawFilledCircle(x + width - 2F - (height - 4F) / 2F, y + 2F + (height - 4F) / 2F, (height - 4F) / 2F, Color(24, 24, 24))
        else
            RenderUtils.drawFilledCircle(x + 2F + (height - 4F) / 2F, y + 2F + (height - 4F) / 2F, (height - 4F) / 2F, Color(160, 160, 160))
    }

    fun drawCheckbox(x: Float, y: Float, width: Float, state: Boolean) {
        val borderColor = if (state) Color(0, 140, 255).rgb else Color(160, 160, 160).rgb
        val mainColor = if (state) borderColor else Color(24, 24, 24).rgb
        RenderUtils.originalRoundedRect(x - 0.5F, y - 0.5F, x + width + 0.5F, y + width + 0.5F, 3F, borderColor)
        RenderUtils.originalRoundedRect(x, y, x + width, y + width, 3F, mainColor)
        if (state) {
            glColor4f(0.094F, 0.094F, 0.094F, 1F)
            RenderUtils.drawLine((x + width / 4F).toDouble(),
                (y + width / 2F).toDouble(), (x + width / 2.15F).toDouble(), (y + width / 4F * 3F).toDouble(), 2F)
            RenderUtils.drawLine((x + width / 2.15F).toDouble(),
                (y + width / 4F * 3F).toDouble(), (x + width / 3.95F * 3F).toDouble(), (y + width / 3F).toDouble(), 2F)
            resetColor()
            glColor4f(1F, 1F, 1F, 1F)
        }
    }

}