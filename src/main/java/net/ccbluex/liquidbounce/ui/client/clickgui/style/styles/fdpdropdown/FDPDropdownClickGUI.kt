/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.SideGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.impl.SettingComponents
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.EaseBackIn
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.Main
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import java.awt.Color

/**
 * ClickGUI FDP
 */
class FDPDropdownClickGUI : GuiScreen() {

    private val sideGui = SideGui()

    private lateinit var openingAnimation: Animation
    private lateinit var fadeAnimation: EaseBackIn
    private lateinit var configHover: DecelerateAnimation

    private val hudIcon = ResourceLocation("${CLIENT_NAME.lowercase()}/custom_hud_icon.png")

    private var categoryPanels: MutableList<DropdownCategory>? = null
    private var isInitialized = false

    override fun initGui() {
        try {
            if (categoryPanels == null || Main.reloadModules || !isInitialized) {
                categoryPanels = mutableListOf<DropdownCategory>().apply {
                    Category.entries.forEach { category ->
                        add(DropdownCategory(category))
                    }
                }
                Main.reloadModules = false
                isInitialized = true
            }

            sideGui.initGui()

            fadeAnimation = EaseBackIn(400, 1.0, 2.0f)
            openingAnimation = EaseBackIn(400, 0.4, 2.0f)
            configHover = DecelerateAnimation(250, 1.0)

            categoryPanels?.forEach { panel ->
                panel.animation = fadeAnimation
                panel.openingAnimation = openingAnimation
                panel.initGui()
            }
        } catch (e: Exception) {
            println("Error initializing FDPDropdownClickGUI: ${e.message}")
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        try {
            if (keyCode == 1) {
                openingAnimation.direction = Direction.BACKWARDS
                fadeAnimation.direction = openingAnimation.direction
            }
            sideGui.keyTyped(typedChar, keyCode)
            categoryPanels?.forEach { it.keyTyped(typedChar, keyCode) }
        } catch (e: Exception) {
            println("Error handling key input: ${e.message}")
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        try {
            assumeNonVolatile {
                if (Mouse.isButtonDown(0) && mouseX in 5..50 && mouseY in (height - 50)..(height - 5)) {
                    mc.displayGuiScreen(GuiHudDesigner())
                }
                RenderUtils.drawImage(hudIcon, 9, height - 41, 32, 32)
                if (openingAnimation.isDone && openingAnimation.direction == Direction.BACKWARDS) {
                    mc.displayGuiScreen(null)
                    return@assumeNonVolatile
                }
                val sr = ScaledResolution(mc)
                val finalScale = (openingAnimation.output + 0.6f) * ClickGUIModule.scale
                SettingComponents.scale = finalScale.toFloat()
                val transformedMouseX = sr.scaledWidth / 2f + (mouseX - sr.scaledWidth / 2f) / finalScale
                val transformedMouseY = sr.scaledHeight / 2f + (mouseY - sr.scaledHeight / 2f) / finalScale

                val focusedConfigGui = sideGui.focused
                val effectiveMouseX = if (focusedConfigGui) 0 else transformedMouseX.toInt()
                val effectiveMouseY = if (focusedConfigGui) 0 else transformedMouseY.toInt()

                DrRenderUtils.scale(sr.scaledWidth / 2f, sr.scaledHeight / 2f, finalScale.toFloat()) {
                    categoryPanels?.forEach { it.drawScreen(effectiveMouseX, effectiveMouseY) }
                }
                sideGui.drawScreen(mouseX, mouseY, partialTicks, (255 * fadeAnimation.output).toInt().coerceIn(0, 255))
            }
            drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor, true))
            super.drawScreen(mouseX, mouseY, partialTicks)
        } catch (e: Exception) {
            println("Error during FDP GUI rendering: ${e.message}")
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        try {
            val oldFocus = sideGui.focused
            sideGui.mouseClicked(mouseX, mouseY, mouseButton)
            if (!oldFocus) {
                val sr = ScaledResolution(mc)
                val finalScale = (openingAnimation.output + 0.6f) * ClickGUIModule.scale
                val transformedMouseX = sr.scaledWidth / 2f + (mouseX - sr.scaledWidth / 2f) / finalScale
                val transformedMouseY = sr.scaledHeight / 2f + (mouseY - sr.scaledHeight / 2f) / finalScale
                categoryPanels?.forEach { it.mouseClicked(transformedMouseX.toInt(), transformedMouseY.toInt(), mouseButton) }
            }
        } catch (e: Exception) {
            println("Error handling mouse click: ${e.message}")
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        try {
            val oldFocus = sideGui.focused
            sideGui.mouseReleased(mouseX, mouseY, state)
            if (!oldFocus) {
                val sr = ScaledResolution(mc)
                val finalScale = (openingAnimation.output + 0.6f) * ClickGUIModule.scale
                val transformedMouseX = sr.scaledWidth / 2f + (mouseX - sr.scaledWidth / 2f) / finalScale
                val transformedMouseY = sr.scaledHeight / 2f + (mouseY - sr.scaledHeight / 2f) / finalScale
                categoryPanels?.forEach { it.mouseReleased(transformedMouseX.toInt(), transformedMouseY.toInt(), state) }
            }
        } catch (e: Exception) {
            println("Error handling mouse release: ${e.message}")
        }
    }

    fun resetGui() {
        isInitialized = false
        categoryPanels?.clear()
        categoryPanels = null
    }

    override fun doesGuiPauseGame() = false
}