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
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.util.ResourceLocation
import java.awt.Color

object HUDModule : Module("HUD", Category.CLIENT, defaultInArray = false, gameDetecting = false, hideModule = true) {
    val blackHotbar by BoolValue("BlackHotbar", true)
    val inventoryParticle by BoolValue("InventoryParticle", false)
    private val blur by BoolValue("Blur", false)

    // UI EFFECT
    val uiEffectValue by BoolValue("UIEffect", true)
    val buttonShadowValue by BoolValue("ShadowButton", true){ uiEffectValue }
    val UiShadowValue by ListValue("UIEffectMode", arrayOf("Shadow", "Glow", "None"), "Shadow") { uiEffectValue }

    // UI
    private val intefaceColor by BoolValue("Interface Color", true)
    val colorRed by IntegerValue("R", 0, 0..255) { intefaceColor }
    val colorGreen by IntegerValue("G", 160, 0..255) { intefaceColor }
    val colorBlue by IntegerValue("B", 255, 0..255) { intefaceColor }
    private val colorRainbowValue = BoolValue("Rainbow", true) { intefaceColor }

    val guiColor
        get() = if (colorRainbowValue.get()) ColorUtils.rainbow().rgb
        else Color(colorRed, colorGreen, colorBlue).rgb


    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner)
            return

        hud.render(false)
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