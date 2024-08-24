/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.FDPClient.hud
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRectWithBorder
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import java.awt.Color

object HUDModule : Module("HUD", Category.CLIENT, defaultInArray = false, gameDetecting = false, hideModule = true) {

    val blackHotbar by BoolValue("BlackHotbar", true)

    // InventoryPlus
    val inventoryOnHotbar = BoolValue("InventoryOnHotbar", false)

    // CROSSHAIR
    val csgoCrosshairValue by BoolValue("CSGO-Crosshair", false)

    // UI EFFECT
    val uiEffectValue by BoolValue("UIEffect", true)
    val buttonShadowValue by BoolValue("ShadowButton", true){ uiEffectValue }
    val UiShadowValue by ListValue("UIEffectMode", arrayOf("Shadow", "Glow", "None"), "Shadow") { uiEffectValue }

    private val blur by BoolValue("Blur", false)

    val inventoryParticle by BoolValue("InventoryParticle", false)

    // UI
    private val intefaceColor by BoolValue("Interface Color", false)
    val colorRed by IntegerValue("R", 0, 0..255) { intefaceColor }
    val colorGreen by IntegerValue("G", 160, 0..255) { intefaceColor }
    val colorBlue by IntegerValue("B", 255, 0..255) { intefaceColor }
    private val colorRainbowValue = BoolValue("Rainbow", true) { intefaceColor }

    // CLIENT THEME
    val colorOptions = arrayOf(
        "Zywl", "Water", "Magic", "DarkNight", "Sun", "Tree", "Flower", "Loyoi", "Soniga", "May",
        "Mint", "Cero", "Azure", "Rainbow", "Astolfo", "Pumpkin", "Polarized", "Sundae", "Terminal",
        "Coral", "Fire", "Aqua", "Peony"
    )

    var ClientColorMode by ListValue("ColorMode", colorOptions, "Zywl") { intefaceColor }

    var ThemeFadeSpeed by IntegerValue("Fade-speed", 1, 1..10) { intefaceColor }
    var updown by BoolValue(
        "Fade-Type",
        false
    ) { intefaceColor }

    val guiColor
        get() = if (colorRainbowValue.get()) ColorUtils.rainbow().rgb
        else Color(colorRed, colorGreen, colorBlue).rgb


    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner)
            return

        hud.render(false)

        if (csgoCrosshairValue) {
            val screenWidth = getWindowWidth()
            val screenHeight = getWindowHeight()

            if (mc.thePlayer.isSprinting) {
                drawSprintingCrosshair(screenWidth, screenHeight)
            } else {
                drawNormalCrosshair(screenWidth, screenHeight)
            }
        }
    }

    private fun drawSprintingCrosshair(screenWidth: Int, screenHeight: Int) {
        val whiteColor = Color(255, 255, 255, 255).rgb
        val borderColor = Color(0, 0, 0, 255).rgb
        val accentColor = Color(255, 190, 255, 0).rgb

        // Left part
        Gui.drawRect(screenWidth / 2 - 6, screenHeight / 2, screenWidth / 2 - 2, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2 - 6).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 - 2).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.7582394f, borderColor, accentColor)

        // Right part
        Gui.drawRect(screenWidth / 2 + 3, screenHeight / 2, screenWidth / 2 + 7, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2 + 3).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 + 7).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.7635231f, borderColor, accentColor)

        // Top part
        Gui.drawRect(screenWidth / 2, screenHeight / 2 - 6, screenWidth / 2 + 1, screenHeight / 2 - 2, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2 - 6).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 - 2).toFloat(), 0.7572856f, borderColor, accentColor)

        // Bottom part
        Gui.drawRect(screenWidth / 2, screenHeight / 2 + 3, screenWidth / 2 + 1, screenHeight / 2 + 7, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2 + 3).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 + 7).toFloat(), 0.75438696f, borderColor, accentColor)

        // Center part
        Gui.drawRect(screenWidth / 2, screenHeight / 2, screenWidth / 2 + 1, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.75438696f, borderColor, accentColor)
    }

    private fun drawNormalCrosshair(screenWidth: Int, screenHeight: Int) {
        val whiteColor = Color(255, 255, 255, 255).rgb
        val borderColor = Color(0, 0, 0, 255).rgb
        val accentColor = Color(255, 190, 255, 0).rgb

        // Left part
        Gui.drawRect(screenWidth / 2 - 9, screenHeight / 2, screenWidth / 2 - 5, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2 - 9).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 - 5).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.7582394f, borderColor, accentColor)

        // Right part
        Gui.drawRect(screenWidth / 2 + 6, screenHeight / 2, screenWidth / 2 + 10, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2 + 6).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 + 10).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.7635231f, borderColor, accentColor)

        // Top part
        Gui.drawRect(screenWidth / 2, screenHeight / 2 - 9, screenWidth / 2 + 1, screenHeight / 2 - 5, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2 - 9).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 - 5).toFloat(), 0.7572856f, borderColor, accentColor)

        // Bottom part
        Gui.drawRect(screenWidth / 2, screenHeight / 2 + 7, screenWidth / 2 + 1, screenHeight / 2 + 10, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2 + 7).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 + 10).toFloat(), 0.75438696f, borderColor, accentColor)

        // Center part
        Gui.drawRect(screenWidth / 2, screenHeight / 2, screenWidth / 2 + 1, screenHeight / 2 + 1, whiteColor)
        drawRectWithBorder((screenWidth / 2).toFloat(), (screenHeight / 2).toFloat(), (screenWidth / 2 + 1).toFloat(), (screenHeight / 2 + 1).toFloat(), 0.75438696f, borderColor, accentColor)
    }

    private fun getWindowWidth(): Int {
        return ScaledResolution(Minecraft.getMinecraft()).scaledWidth
    }

    private fun getWindowHeight(): Int {
        return ScaledResolution(Minecraft.getMinecraft()).scaledHeight
    }


    @EventTarget
    fun onUpdate(event: UpdateEvent) = hud.update()

    @EventTarget
    fun onKey(event: KeyEvent) = hud.handleKey('a', event.key)

    @EventTarget(ignoreCondition = true)
    fun onScreen(event: ScreenEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) return
        if (state && blur && !mc.entityRenderer.isShaderActive && event.guiScreen != null &&
                !(event.guiScreen is GuiChat || event.guiScreen is GuiHudDesigner)) mc.entityRenderer.loadShader(
            ResourceLocation(CLIENT_NAME.lowercase() + "/blur.json")
        ) else if (mc.entityRenderer.shaderGroup != null &&
            "fdpclient/blur.json" in mc.entityRenderer.shaderGroup.shaderGroupName) mc.entityRenderer.stopUseShader()
    }

    init {
        state = true
    }
}