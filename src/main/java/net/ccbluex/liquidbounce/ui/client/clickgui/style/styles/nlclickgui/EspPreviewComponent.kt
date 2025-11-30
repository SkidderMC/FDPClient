/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.config.BoolValue
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
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
import org.lwjgl.input.Mouse
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
    private var managingElements = true
    private var selectedModule: Module? = null
    private var modelYaw = 0f
    private var modelPitch = 0f

    private val openAnimation: Animation = EaseInOutQuad(250, 1.0, Direction.BACKWARDS)

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
        val previewY = posY + 12
        val previewWidth = 230f
        val previewHeight = gui.h - 24f
        val playerAreaHeight = 205f

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
        drawPreviewPlayer(mouseX, mouseY, previewX, previewY, previewWidth, playerAreaHeight)

        drawElementsManager(
            mouseX,
            mouseY,
            previewX,
            previewY,
            previewWidth,
            previewHeight,
            playerAreaHeight,
            backgroundColor,
            outlineColor,
            textColor
        )
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val previewX = posX + gui.w + 12
        val previewY = posY + 12
        val previewWidth = 230f
        val previewHeight = gui.h - 24f

        if (isHovering(previewX.toFloat(), previewY.toFloat(), previewWidth, previewHeight, mouseX, mouseY) && mouseButton == 0 && !managingElements) {
            dragging = true
            dragX = posX - mouseX
            dragY = posY - mouseY
        }

        val manageButtonX = previewX + 20f
        val manageButtonY = previewY + previewHeight - 26f

        if (isHovering(manageButtonX, manageButtonY, 190f, 16f, mouseX, mouseY) && mouseButton == 0 && activeVisualModules().isNotEmpty()) {
            managingElements = !managingElements
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
        playerAreaHeight: Float,
        backgroundColor: Color,
        outlineColor: Color,
        textColor: Color,
    ) {
        val visuals = activeVisualModules()
        if (selectedModule !in visuals) {
            selectedModule = visuals.firstOrNull()
        }

        val manageButtonX = previewX + 20f
        val manageButtonY = previewY + previewHeight - 26f
        val manageButtonWidth = 190f
        val manageButtonHeight = 16f
        val hoveringManage = isHovering(manageButtonX, manageButtonY, manageButtonWidth, manageButtonHeight, mouseX, mouseY)

        val manageBg = if (visuals.isEmpty()) Color(backgroundColor.red, backgroundColor.green, backgroundColor.blue, 90) else backgroundColor
        val manageOutline = when {
            visuals.isEmpty() -> Color(outlineColor.red, outlineColor.green, outlineColor.blue, 120)
            hoveringManage || managingElements -> NeverloseGui.neverlosecolor
            else -> outlineColor
        }

        RoundedUtil.drawRoundOutline(
            manageButtonX,
            manageButtonY,
            manageButtonWidth,
            manageButtonHeight,
            3f,
            0.1f,
            manageBg,
            manageOutline
        )
        val manageLabel = when {
            visuals.isEmpty() -> "No visual modules active"
            managingElements -> "Close visual manager"
            else -> "Manage active visuals"
        }
        val manageLabelY = manageButtonY + (manageButtonHeight - Fonts.Nl_16.height) / 2f
        Fonts.Nl_16.drawCenteredString(manageLabel, manageButtonX + manageButtonWidth / 2f, manageLabelY, textColor.rgb)

        val progress = openAnimation.getOutput().toFloat()
        if (progress <= 0.05f || visuals.isEmpty()) {
            return
        }

        val panelMaxHeight = (previewHeight - playerAreaHeight - manageButtonHeight - 30f).coerceAtLeast(80f)
        val panelHeight = panelMaxHeight * progress
        val panelY = previewY + playerAreaHeight + 14f

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

        drawManagerHeader(mouseX, mouseY, previewX, previewWidth, panelY, textColor)

        val moduleButtons = moduleButtons(previewX, panelY, previewWidth)
        drawModuleSelector(moduleButtons, mouseX, mouseY, textColor, outlineColor, backgroundColor)

        val valueButtons = valueButtons(previewX, panelY, previewWidth, moduleButtons, panelHeight)
        drawValueButtons(valueButtons, textColor, outlineColor, backgroundColor)

        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        GL11.glPopMatrix()
    }

    private fun drawPreviewPlayer(
        mouseX: Int,
        mouseY: Int,
        previewX: Int,
        previewY: Int,
        previewWidth: Float,
        playerAreaHeight: Float
    ) {
        val playerAreaY = previewY + 10
        val playerAreaX = previewX + 8
        val playerAreaWidth = previewWidth - 16f

        RoundedUtil.drawRound(playerAreaX.toFloat(), playerAreaY.toFloat(), playerAreaWidth, playerAreaHeight - 8f, 3f, Color(0, 0, 0, 35))

        val hoveringPlayer = isHovering(playerAreaX.toFloat(), playerAreaY.toFloat(), playerAreaWidth, playerAreaHeight - 8f, mouseX, mouseY)
        if (hoveringPlayer && (Mouse.isButtonDown(0) || Mouse.isButtonDown(1))) {
            modelYaw += Mouse.getDX() * 0.8f
            modelPitch = (modelPitch - Mouse.getDY() * 0.6f).coerceIn(-60f, 60f)
        }

        val entityX = (previewX + previewWidth / 2).toInt()
        val entityY = (playerAreaY + playerAreaHeight - 18f).toInt()
        GlStateManager.pushMatrix()
        drawEntityOnScreen(
            entityX,
            entityY,
            70,
            modelYaw,
            modelPitch,
            mc.thePlayer
        )
        GlStateManager.popMatrix()
    }

    private fun drawManagerHeader(mouseX: Int, mouseY: Int, previewX: Int, previewWidth: Float, panelY: Float, textColor: Color) {
        Fonts.Nl_16.drawString("Visual modules", previewX + 10f, panelY + 8f, textColor.rgb)
        val closeIconX = previewX + previewWidth - 16f
        val closeIconY = panelY + 5f
        val hoveringClose = isHovering(closeIconX, closeIconY, 12f, 12f, mouseX, mouseY)
        Fonts.Nl_16_ICON.drawString(
            "m",
            closeIconX,
            closeIconY,
            if (hoveringClose) NeverloseGui.neverlosecolor.rgb else textColor.rgb
        )
        if (hoveringClose && Mouse.isButtonDown(0)) {
            managingElements = false
        }
    }

    private fun drawModuleSelector(
        moduleButtons: List<ButtonArea<Module>>,
        mouseX: Int,
        mouseY: Int,
        textColor: Color,
        outlineColor: Color,
        backgroundColor: Color,
    ) {
        moduleButtons.forEach { button ->
            val selected = button.target == selectedModule
            val buttonBackground = when {
                selected -> NeverloseGui.neverlosecolor
                button.target.state -> Color(44, 120, 168, 110)
                else -> backgroundColor
            }
            val buttonOutline = if (selected || button.target.state) NeverloseGui.neverlosecolor else outlineColor

            RoundedUtil.drawRoundOutline(button.x, button.y, button.w, button.h, 2f, 0.1f, buttonBackground, buttonOutline)
            val textY = button.y + (button.h - Fonts.Nl_16.height) / 2f
            Fonts.Nl_16.drawCenteredString(button.target.name, button.x + button.w / 2f, textY, textColor.rgb)

            if (isHovering(button.x, button.y, button.w, button.h, mouseX, mouseY) && Mouse.isButtonDown(0)) {
                selectedModule = button.target
            }
        }
    }

    private fun drawValueButtons(
        valueButtons: List<ButtonArea<BoolValue>>,
        textColor: Color,
        outlineColor: Color,
        backgroundColor: Color,
    ) {
        valueButtons.forEach { button ->
            val enabled = button.target.get()
            val buttonBackground = if (enabled) NeverloseGui.neverlosecolor else backgroundColor
            val buttonOutline = if (enabled) NeverloseGui.neverlosecolor else outlineColor

            RoundedUtil.drawRoundOutline(button.x, button.y, button.w, button.h, 2f, 0.1f, buttonBackground, buttonOutline)
            val textY = button.y + (button.h - Fonts.Nl_16.height) / 2f
            Fonts.Nl_16.drawCenteredString(button.target.name, button.x + button.w / 2f, textY, textColor.rgb)
        }
    }

    private fun handleElementClick(
        mouseX: Int,
        mouseY: Int,
        previewX: Int,
        previewY: Int,
        previewWidth: Float,
        previewHeight: Float
    ) {
        val progress = openAnimation.getOutput().toFloat()
        if (progress <= 0.05f) return
        val playerAreaHeight = 205f
        val manageButtonHeight = 16f
        val panelY = previewY + playerAreaHeight + 14f
        val panelHeight = (previewHeight - playerAreaHeight - manageButtonHeight - 30f).coerceAtLeast(80f) * progress
        val moduleButtons = moduleButtons(previewX, panelY, previewWidth)
        moduleButtons.firstOrNull { isHovering(it.x, it.y, it.w, it.h, mouseX, mouseY) }?.let {
            selectedModule = it.target
            return
        }

        val valueButtons = valueButtons(previewX, panelY, previewWidth, moduleButtons, panelHeight)
        valueButtons.firstOrNull { isHovering(it.x, it.y, it.w, it.h, mouseX, mouseY) }?.let {
            it.target.set(!it.target.get())
            return
        }
    }

    private fun activeVisualModules(): List<Module> =
        ModuleManager[Category.VISUAL].filter { it.state }

    private fun moduleButtons(previewX: Int, panelY: Float, previewWidth: Float): List<ButtonArea<Module>> {
        val buttons = mutableListOf<ButtonArea<Module>>()
        var xOffset = 0f
        var yOffset = 26f
        for (module in activeVisualModules()) {
            val labelWidth = Fonts.Nl_16.stringWidth(module.name) + 10f
            if (xOffset + labelWidth > previewWidth - 16f) {
                xOffset = 0f
                yOffset += 16f
            }
            buttons += ButtonArea(module, previewX + 8f + xOffset, panelY + yOffset, labelWidth, 14f)
            xOffset += labelWidth + 4f
        }
        return buttons
    }

    private fun valueButtons(
        previewX: Int,
        panelY: Float,
        previewWidth: Float,
        moduleButtons: List<ButtonArea<Module>>,
        panelHeight: Float
    ): List<ButtonArea<BoolValue>> {
        val buttons = mutableListOf<ButtonArea<BoolValue>>()
        val values = selectedModule?.values.orEmpty().filterIsInstance<BoolValue>()
        var xOffset = 0f
        val startY = (moduleButtons.maxOfOrNull { it.y + it.h } ?: panelY + 26f) + 10f
        var yOffset = startY - panelY
        for (value in values) {
            val labelWidth = Fonts.Nl_16.stringWidth(value.name) + 6f
            if (xOffset + labelWidth > previewWidth - 16f) {
                xOffset = 0f
                yOffset += 14f
            }
            if (panelY + yOffset + 12f <= panelY + panelHeight - 8f) {
                buttons += ButtonArea(value, previewX + 8f + xOffset, panelY + yOffset, labelWidth, 12f)
            }
            xOffset += labelWidth + 5f
        }
        return buttons
    }

    private data class ButtonArea<T>(val target: T, val x: Float, val y: Float, val w: Float, val h: Float)

    private fun isHovering(x: Float, y: Float, w: Float, h: Float, mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h
    }
}