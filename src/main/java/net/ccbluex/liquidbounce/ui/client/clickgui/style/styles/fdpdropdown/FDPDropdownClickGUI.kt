/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
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
import net.minecraft.client.renderer.GlStateManager.color
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

    override fun initGui() {
        if (categoryPanels == null || Main.reloadModules) {
            categoryPanels = mutableListOf<DropdownCategory>().apply {
                Category.entries.forEach { category ->
                    add(DropdownCategory(category))
                }
            }
            Main.reloadModules = false
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
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == 1) {
            openingAnimation.direction = Direction.BACKWARDS
            fadeAnimation.direction = openingAnimation.direction
        }
        sideGui.keyTyped(typedChar, keyCode)
        categoryPanels?.forEach { it.keyTyped(typedChar, keyCode) }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        assumeNonVolatile = true

        if (Mouse.isButtonDown(0) && mouseX in 5..50 && mouseY in (height - 50)..(height - 5)) {
            mc.displayGuiScreen(GuiHudDesigner())
        }
        RenderUtils.drawImage(hudIcon, 9, (height - 41), 32, 32)

        if (openingAnimation.isDone && openingAnimation.direction == Direction.BACKWARDS) {
            mc.displayGuiScreen(null)
            return
        }

        val focusedConfigGui = sideGui.focused
        val fakeMouseX = if (focusedConfigGui) 0 else mouseX
        val fakeMouseY = if (focusedConfigGui) 0 else mouseY

        val sr = ScaledResolution(mc)
        val hoveringConfig = DrRenderUtils.isHovering(
            (width - 120).toFloat(),
            (height - 65).toFloat(),
            75f,
            25f,
            fakeMouseX,
            fakeMouseY
        )
        configHover.direction = if (hoveringConfig) Direction.FORWARDS else Direction.BACKWARDS

        val alphaAnimation = (255 * fadeAnimation.output).toInt().coerceIn(0, 255)
        color(1f, 1f, 1f, 1f)

        SettingComponents.scale = (openingAnimation.output + 0.6f).toFloat()

        // We scale around screen center
        DrRenderUtils.scale(
            sr.scaledWidth / 2f,
            sr.scaledHeight / 2f,
            (openingAnimation.output + 0.6f).toFloat()
        ) {
            categoryPanels?.forEach { it.drawScreen(fakeMouseX, fakeMouseY) }
            sideGui.drawScreen(mouseX, mouseY, partialTicks, alphaAnimation)
        }

        val borderColor = ClickGUIModule.generateColor(0)
        drawThinGuiBorder(borderColor)

        // Example bloom
        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor, true))

        assumeNonVolatile = false
    }

    /**
     * Draws a 1-pixel border around (0,0)-(width,height) using the given color.
     * This prevents the entire screen from being filled.
     */
    private fun drawThinGuiBorder(borderColor: Color) {
        val w = width.toFloat()
        val h = height.toFloat()

        // top edge
        DrRenderUtils.drawRect2(
            0.0,    // x
            0.0,    // y
            w.toDouble(),
            1.0,    // thickness = 1
            borderColor.rgb
        )
        // bottom edge
        DrRenderUtils.drawRect2(
            0.0,
            (h - 1).toDouble(),
            w.toDouble(),
            1.0,
            borderColor.rgb
        )
        // left edge
        DrRenderUtils.drawRect2(
            0.0,
            0.0,
            1.0,
            h.toDouble(),
            borderColor.rgb
        )
        // right edge
        DrRenderUtils.drawRect2(
            (w - 1).toDouble(),
            0.0,
            1.0,
            h.toDouble(),
            borderColor.rgb
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val oldFocus = sideGui.focused
        sideGui.mouseClicked(mouseX, mouseY, mouseButton)
        if (!oldFocus) {
            categoryPanels?.forEach { it.mouseClicked(mouseX, mouseY, mouseButton) }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        val oldFocus = sideGui.focused
        sideGui.mouseReleased(mouseX, mouseY, state)
        if (!oldFocus) {
            categoryPanels?.forEach { it.mouseReleased(mouseX, mouseY, state) }
        }
    }
}
