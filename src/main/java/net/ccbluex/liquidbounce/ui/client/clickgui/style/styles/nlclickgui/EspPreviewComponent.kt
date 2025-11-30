/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.config.BoolValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.resetColor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.scissor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.EaseInOutQuad
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.client.gui.inventory.GuiInventory.drawEntityOnScreen
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs

class EspPreviewComponent(private val gui: NeverloseGui) : MinecraftInstance {

    private var posX = gui.x
    private var posY = gui.y
    private var dragX = 0
    private var dragY = 0
    private var dragging = false
    private var adsorb = true
    private var managingElements = false

    private val openAnimation: Animation = EaseInOutQuad(250, 1.0, Direction.BACKWARDS)
    private val espValues: List<Value<*>> = ModuleManager["ESP"]?.values ?: emptyList()

    fun draw(mouseX: Int, mouseY: Int) {
        if (dragging) {
            posX = mouseX + dragX
            posY = mouseY + dragY
        }

        if (adsorb && !dragging) {
            posX = gui.x
            posY = gui.y
        }

        adsorb = abs(posX - gui.x) <= 30 && abs(posY - gui.y) <= gui.h
        openAnimation.direction = if (managingElements) Direction.FORWARDS else Direction.BACKWARDS

        val previewX = posX + gui.w + 12
        val previewY = posY + 10
        val previewWidth = 200f
        val previewHeight = gui.h - 20f

        val backgroundColor = if (gui.light) Color(243, 246, 249, 230) else Color(9, 13, 19, 210)
        val outlineColor = if (gui.light) Color(200, 208, 216, 180) else Color(40, 50, 64, 180)
        val iconColor = NeverloseGui.neverlosecolor
        val textColor = if (gui.light) Color(34, 34, 34) else Color(230, 230, 230)

        RoundedUtil.drawRoundOutline(
            previewX.toFloat(),
            previewY.toFloat(),
            previewWidth,
            previewHeight,
            2f,
            0.1f,
            backgroundColor,
            outlineColor
        )

        Fonts.NlIcon.nlfont_20.nlfont_20.drawString(
            "b",
            (previewX + 6).toFloat(),
            (previewY + 6).toFloat(),
            iconColor.rgb
        )
        val title = "Interactive ESP Preview"
        Fonts.Nl_18.drawString(
            title,
            previewX + previewWidth - Fonts.Nl_18.stringWidth(title) - 6,
            (previewY + 7).toFloat(),
            textColor.rgb
        )
        resetColor()

        GlStateManager.pushMatrix()
        drawEntityOnScreen(
            (previewX + previewWidth / 2).toInt(),
            (previewY + 200 + 75 * (1 - openAnimation.getOutput())).toInt(),
            80,
            0f,
            0f,
            mc.thePlayer
        )
        GlStateManager.popMatrix()

        drawElementsManager(mouseX, mouseY, previewX, previewY, previewWidth, previewHeight, backgroundColor, outlineColor, textColor)
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val previewX = posX + gui.w + 12
        val previewY = posY + 10
        val previewWidth = 200f
        val previewHeight = gui.h - 20f

        if (isHovering(previewX.toFloat(), previewY.toFloat(), previewWidth, previewHeight, mouseX, mouseY) && mouseButton == 0 && !managingElements) {
            dragging = true
            dragX = posX - mouseX
            dragY = posY - mouseY
        }

        val manageButtonX = previewX + 70f
        val manageButtonY = previewY + previewHeight - 25f

        if (isHovering(manageButtonX, manageButtonY, 85f, 12f, mouseX, mouseY) && mouseButton == 0) {
            managingElements = true
        }

        val closeX = previewX + previewWidth - 14f
        val closeY = previewY + previewHeight - (170f * openAnimation.getOutput()).toFloat()
        if (managingElements && isHovering(closeX, closeY, 10f, 10f, mouseX, mouseY) && mouseButton == 0) {
            managingElements = false
        }

        if (managingElements) {
            handleElementClick(mouseX, mouseY, previewX, previewY, previewWidth, previewHeight)
        }
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0) {
            dragging = false
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {}

    private fun drawElementsManager(
        mouseX: Int,
        mouseY: Int,
        previewX: Int,
        previewY: Int,
        previewWidth: Float,
        previewHeight: Float,
        backgroundColor: Color,
        outlineColor: Color,
        textColor: Color,
    ) {
        val manageButtonX = previewX + 70f
        val manageButtonY = previewY + previewHeight - 25f
        val hoveringManage = isHovering(manageButtonX, manageButtonY, 85f, 12f, mouseX, mouseY)

        RoundedUtil.drawRoundOutline(
            manageButtonX,
            manageButtonY,
            85f,
            12f,
            2f,
            0.1f,
            backgroundColor,
            if (hoveringManage || managingElements) NeverloseGui.neverlosecolor else outlineColor
        )
        Fonts.Nl_16.drawCenteredString(
            if (managingElements) "Managing Elements" else "Manage Elements",
            manageButtonX + 42.5f,
            manageButtonY + 2f,
            textColor.rgb
        )

        val progress = openAnimation.getOutput().toFloat()
        if (progress <= 0.05f) {
            return
        }

        val panelHeight = 180f * progress
        val panelY = previewY + previewHeight - panelHeight

        GL11.glPushMatrix()
        scissor(previewX.toDouble(), panelY.toDouble(), previewWidth.toDouble(), panelHeight.toDouble())
        GL11.glEnable(GL11.GL_SCISSOR_TEST)

        RoundedUtil.drawRoundOutline(
            previewX.toFloat(),
            panelY,
            previewWidth,
            panelHeight,
            4f,
            0.1f,
            backgroundColor,
            outlineColor
        )

        Fonts.Nl_16.drawString("Drag & Drop Elements", previewX + 8f, panelY + 8f, textColor.rgb)
        Fonts.Nl_16_ICON.drawString(
            "m",
            previewX + previewWidth - 14f,
            panelY + 6f,
            if (isHovering(previewX + previewWidth - 14f, panelY + 2f, 12f, 12f, mouseX, mouseY)) NeverloseGui.neverlosecolor.rgb else textColor.rgb
        )

        drawValueButtons(mouseX, mouseY, previewX, panelY, previewWidth, panelHeight, textColor, outlineColor, backgroundColor)

        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        GL11.glPopMatrix()
    }

    private fun drawValueButtons(
        mouseX: Int,
        mouseY: Int,
        previewX: Int,
        panelY: Float,
        previewWidth: Float,
        panelHeight: Float,
        textColor: Color,
        outlineColor: Color,
        backgroundColor: Color,
    ) {
        var xOffset = 0f
        var yOffset = 26f

        for (value in espValues) {
            if (value !is BoolValue || value.hidden || value.excluded || !value.isSupported()) continue

            val label = value.name
            val width = Fonts.Nl_16.stringWidth(label) + 6f
            if (xOffset + width > previewWidth - 16f) {
                xOffset = 0f
                yOffset += 14f
            }

            val buttonX = previewX + 8f + xOffset
            val buttonY = panelY + yOffset
            val enabled = value.get()
            val buttonBackground = if (enabled) NeverloseGui.neverlosecolor else backgroundColor
            val buttonOutline = if (enabled) NeverloseGui.neverlosecolor else outlineColor

            RoundedUtil.drawRoundOutline(buttonX, buttonY, width, 12f, 2f, 0.1f, buttonBackground, buttonOutline)
            Fonts.Nl_16.drawCenteredString(label, buttonX + width / 2f, buttonY + 2f, textColor.rgb)

            xOffset += width + 4f
        }
    }

    private fun handleElementClick(mouseX: Int, mouseY: Int, previewX: Int, previewY: Int, previewWidth: Float, previewHeight: Float) {
        val progress = openAnimation.getOutput().toFloat()
        if (progress <= 0.05f) return

        var xOffset = 0f
        var yOffset = 26f
        for (value in espValues) {
            if (value !is BoolValue || value.hidden || value.excluded || !value.isSupported()) continue

            val label = value.name
            val width = Fonts.Nl_16.stringWidth(label) + 6f
            if (xOffset + width > previewWidth - 16f) {
                xOffset = 0f
                yOffset += 14f
            }

            val buttonX = previewX + 8f + xOffset
            val buttonY = previewY + previewHeight - 180f * progress + yOffset

            if (isHovering(buttonX, buttonY, width, 12f, mouseX, mouseY)) {
                value.set(!value.get())
            }

            xOffset += width + 4f
        }
    }

    private fun isHovering(x: Float, y: Float, w: Float, h: Float, mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h
    }
}