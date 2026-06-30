/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.FDPClient.hud
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Element.Companion.MAX_GRADIENT_COLORS
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen.NextGenBrowserRuntime
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.client.ServerObserver
import net.ccbluex.liquidbounce.utils.render.*
import net.minecraft.client.gui.GuiChat
import net.minecraft.util.ResourceLocation
import java.awt.Color

object HUDModule : Module("HUD", Category.CLIENT, Category.SubCategory.CLIENT_GENERAL) {

    private val renderer by choices("Renderer", arrayOf("Native", "Web", "Both"), "Native")
        .describe("Select the native HUD, the live browser HUD, or both renderers.")

    val customHotbar by boolean("CustomHotbar", true)
        .describe("Replace the vanilla hotbar with a custom one.")
    val smoothHotbarSlot by boolean("SmoothHotbarSlot", false) { customHotbar }
        .describe("Smoothly animate the selected hotbar slot.")
    val roundedHotbarRadius by float("RoundedHotbar-Radius", 3F, 0F..5F) { customHotbar }
        .describe("Corner radius of the custom hotbar.")

    val hotbarMode by choices("Hotbar-Color", arrayOf("Custom", "Rainbow", "Gradient"), "Custom") { customHotbar }
        .describe("Coloring style of the custom hotbar.")
    val hbHighlightColors = ColorSettingsInteger(this, "Hotbar-Highlight-Colors", applyMax = true)
    { customHotbar }.with(a = 0)
    val hbBackgroundColors = ColorSettingsInteger(this, "Hotbar-Background-Colors")
    { customHotbar && hotbarMode == "Custom" }.with(a = 190)
    val gradientHotbarSpeed by float("Hotbar-Gradient-Speed", 1f, 0.5f..10f)
    { customHotbar && hotbarMode == "Gradient" }
        .describe("Speed of the hotbar gradient animation.")
    val maxHotbarGradientColors by int("Max-Hotbar-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS)
    { customHotbar && hotbarMode == "Gradient" }
        .describe("Maximum number of hotbar gradient colors.")
    val bgGradColors = ColorSettingsFloat.create(this, "Hotbar-Gradient")
    { customHotbar && hotbarMode == "Gradient" && it <= maxHotbarGradientColors }
    val hbHighlightBorder by float("HotbarBorder-Highlight-Width", 2F, 0.5F..5F) { customHotbar }
        .describe("Border width of the highlighted hotbar slot.")
    val hbHighlightBorderColors = ColorSettingsInteger(this, "HotbarBorder-Highlight-Colors")
    { customHotbar }.with(a = 255, g = 111, b = 255)
    val hbBackgroundBorder by float("HotbarBorder-Background-Width", 0.5F, 0.5F..5F) { customHotbar }
        .describe("Border width of the hotbar background.")
    val hbBackgroundBorderColors = ColorSettingsInteger(this, "HotbarBorder-Background-Colors")
    { customHotbar }.with(a = 0)

    val rainbowX by float("Rainbow-X", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Rainbow" }
        .describe("X offset of the rainbow hotbar effect.")
    val rainbowY by float("Rainbow-Y", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Rainbow" }
        .describe("Y offset of the rainbow hotbar effect.")
    val gradientX by float("Gradient-X", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Gradient" }
        .describe("X offset of the gradient hotbar effect.")
    val gradientY by float("Gradient-Y", -1000F, -2000F..2000F) { customHotbar && hotbarMode == "Gradient" }
        .describe("Y offset of the gradient hotbar effect.")

    // UI EFFECT
    val uiEffectValue by boolean("UIEffect", true)
        .describe("Enable extra visual effects on the UI.")
    val buttonShadowValue by boolean("ShadowButton", true){ uiEffectValue }
        .describe("Render a shadow behind UI buttons.")
    val UiShadowValue by choices("UIEffectMode", arrayOf("Shadow", "Glow", "None"), "Shadow") { uiEffectValue }
        .describe("Type of UI button effect to render.")

    private val blur by boolean("Blur", false)
        .describe("Apply a blur shader behind GUI screens.")
    val inventoryParticle by boolean("InventoryParticle", false)
        .describe("Render particles inside the inventory screen.")

    // UI
    private val interfaceColor by boolean("Bloom Color", false)
        .describe("Use a custom color for the interface.")
    val color by color("Color", Color(0, 160, 255)) { interfaceColor }
        .describe("Custom interface color.")

    val guiColor: Int
        get() = if (interfaceColor) {
            color.rgb
        } else {
            ClientThemesUtils.getColor().rgb
        }

    val tps: Float
        get() = ServerObserver.tps.takeIf(Double::isFinite)?.toFloat() ?: 20f

    val onRender2D = handler<Render2DEvent> {
        if (HideAppearance.handleEvents()) return@handler
        if (mc.currentScreen is GuiHudDesigner)
            return@handler

        if (renderer != "Web") hud.render(false)
        if (renderer != "Native") NextGenBrowserRuntime.renderHudOverlay()
    }

    val onUpdate = handler<UpdateEvent> {
        if (renderer != "Web") hud.update()
        NextGenBrowserRuntime.setHudVisible(renderer != "Native" && mc.theWorld != null && mc.thePlayer != null)
    }

    val onKey = handler<KeyStateEvent> { event ->
        if (!event.pressed) return@handler
        hud.handleKey('a', event.key)
    }

    val onScreen = handler<ScreenEvent>(always = true) { event ->
        if (mc.theWorld == null || mc.thePlayer == null) return@handler
        if (state && blur && !mc.entityRenderer.isShaderActive && event.guiScreen != null &&
            !(event.guiScreen is GuiChat || event.guiScreen is GuiHudDesigner)
        ) mc.entityRenderer.loadShader(
            ResourceLocation(CLIENT_NAME.lowercase() + "/blur.json")
        ) else if (
            mc.entityRenderer.shaderGroup != null &&
            "fdpclient/blur.json" in mc.entityRenderer.shaderGroup.shaderGroupName
        ) mc.entityRenderer.stopUseShader()
    }

    init {
        state = true
        group(
            "Hotbar",
            "CustomHotbar", "SmoothHotbarSlot", "RoundedHotbar-Radius", "Hotbar-Color",
            "Hotbar-Gradient-Speed", "Max-Hotbar-Gradient-Colors",
            "HotbarBorder-Highlight-Width", "HotbarBorder-Background-Width"
        )
        group("HotbarEffects", "Rainbow-X", "Rainbow-Y", "Gradient-X", "Gradient-Y")
        group("UIEffects", "UIEffect", "ShadowButton", "UIEffectMode")
        group("Screen", "Blur", "InventoryParticle")
        group("Interface", "Renderer", "Bloom Color", "Color")
    }

    override fun onDisable() {
        NextGenBrowserRuntime.setHudVisible(false)
    }
}
