/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.SideGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category.yzyCategory
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.manager.GUIManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.Pair
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.IOException

/**
 * @author opZywl - YZY GUI
 */
class yzyGUI(private val clickGui: ClickGUIModule) : GuiScreen() {

    private val panels: MutableList<Panel> = mutableListOf()
    private val guiManager: GUIManager = FDPClient.guiManager
    private val sideGui = SideGui()
    private val hudIcon = ResourceLocation("${CLIENT_NAME.lowercase()}/custom_hud_icon.png")
    private var lastMS: Long = System.currentTimeMillis()
    private var yShift = 0
    private var slide: Double = 0.0
    private var progress: Double = 0.0
    val alpha = 255

    init {
        var panelX = 5
        yzyCategory.entries.forEach { category ->
            val positions = guiManager.getPositions(category)
            val panel = if (!guiManager.positions.containsKey(category)) {
                Panel(this, category, panelX, 5).also { panelX += it.width + 5 }
            } else {
                Panel(this, category, positions.key, positions.value ?: 5).apply {
                    isExtended = guiManager.extendeds[category] == true
                }
            }
            guiManager.positions[category] = Pair(panel.x, panel.y)
            guiManager.extendeds[category] = panel.isExtended
            panels.add(panel)
            panel.isExtended = guiManager.isExtended(category)
        }
    }

    override fun initGui() {
        super.initGui()
        slide = 0.0
        progress = 0.0
        lastMS = System.currentTimeMillis()
        sideGui.initGui()
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        mc.gameSettings.guiScale = clickGui.lastScale
        panels.forEach { it.onGuiClosed() }
        mc.entityRenderer.loadEntityShader(null)
    }

    private fun handleScroll(wheel: Int) {
        if (wheel != 0) panels.forEach { it.y += wheel }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {

        assumeNonVolatile = true

        if (Mouse.hasWheel()) {
            val wheel = Mouse.getDWheel()
            val handledScroll = panels.asReversed().any { it.handleScroll(mouseX, mouseY, wheel) }
            if (!handledScroll) handleScroll(wheel)
        }

        drawImage(hudIcon, 9, height - 41, 32, 32)

        val delta = (System.currentTimeMillis() - lastMS).toInt()
        panels.forEach {
            it.updateFade(delta)
            it.drawScreen(mouseX, mouseY, partialTicks)
        }

        sideGui.drawScreen(mouseX, mouseY, partialTicks, alpha)
        lastMS = System.currentTimeMillis()

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))

        assumeNonVolatile = false
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val adjustedMouseY = mouseY + yShift
        panels.forEach { it.mouseClicked(mouseX, adjustedMouseY, mouseButton) }
        sideGui.mouseClicked(mouseX, mouseY, mouseButton)

        if (mouseX in 9 until 41 && mouseY in (height - 41) until height) {
            mc.displayGuiScreen(GuiHudDesigner())
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        val adjustedMouseY = mouseY + yShift
        panels.forEach { it.mouseReleased(mouseX, adjustedMouseY, state) }
        sideGui.mouseReleased(mouseX, mouseY, state)
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        panels.forEach { it.keyTyped(typedChar, keyCode) }
        sideGui.keyTyped(typedChar, keyCode)
    }

    override fun doesGuiPauseGame(): Boolean = false
}