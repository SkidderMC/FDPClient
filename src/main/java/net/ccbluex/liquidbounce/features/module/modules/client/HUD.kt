/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.utils.render.RenderUtils.width
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.Gui
import java.awt.Color

@ModuleInfo(name = "HUD", category = ModuleCategory.CLIENT, array = false, defaultOn = true)
object HUD : Module() {

    private val waterMark = BoolValue("Watermark", true)

    val crossHairValue = BoolValue("CrossHair", false)

    // UI EFFECT
    private val uiEffectValue = BoolValue("UIEffect", true)
    val buttonShadowValue = BoolValue("ShadowButton", true).displayable  { uiEffectValue.get() }
    val UiShadowValue = ListValue("UIEffectMode", arrayOf("Shadow", "Glow", "None"), "Shadow").displayable  { uiEffectValue.get() }
    val inventoryParticle = BoolValue("InventoryParticle", false).displayable  { uiEffectValue.get() }

    // CAMERA
    private val viewValue = BoolValue("BetterView", true)
    val smoothCamera = BoolValue("Smooth", true).displayable  { viewValue.get() }
    val cameraPositionValue = BoolValue("CameraPosition", false).displayable  { viewValue.get() }
    val cameraPositionYawValue = FloatValue("Yaw", 10F, -50F, 50F).displayable  { cameraPositionValue.get() }
    val cameraPositionPitchValue = FloatValue("Pitch", 10F, -50F, 50F).displayable  { cameraPositionValue.get() }
    val cameraPositionFovValue = FloatValue("DistanceFov", 4F, 1F, 50F).displayable  { cameraPositionValue.get() }

    // ArrayList
    private val arrayList = BoolValue("ArrayList", true)
    val shadowValue = ListValue("TextShadowMode", arrayOf("LiquidBounce", "Outline", "Default", "Autumn"), "Default").displayable { arrayList.get() }
    private val arrayListValue = BoolValue("ArrayListAnimation", true).displayable { arrayList.get() }
    val arraylistXAxisAnimSpeedValue = IntegerValue("ArraylistXAxisAnimSpeed", 10, 5, 20).displayable { arrayListValue.get() }
    val arraylistXAxisAnimTypeValue = EaseUtils.getEnumEasingList("ArraylistXAxisAnimType").displayable { arrayListValue.get() }
    val arraylistXAxisAnimOrderValue = EaseUtils.getEnumEasingOrderList("ArraylistXAxisHotbarAnimOrder").displayable { !arraylistXAxisAnimTypeValue.equals("NONE") }
    val arraylistYAxisAnimSpeedValue = IntegerValue("ArraylistYAxisAnimSpeed", 10, 5, 20).displayable { arrayListValue.get() }
    val arraylistYAxisAnimTypeValue = EaseUtils.getEnumEasingList("ArraylistYAxisAnimType")
    val arraylistYAxisAnimOrderValue = EaseUtils.getEnumEasingOrderList("ArraylistYAxisHotbarAnimOrder").displayable { !arraylistYAxisAnimTypeValue.equals("NONE") }

    val fontEpsilonValue = FloatValue("FontVectorEpsilon", 0.5f, 0f, 1.5f)

    private var lastFontEpsilon = 0f

    /**
     * Renders the HUD.
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner) return
        FDPClient.hud.render(false, event.partialTicks)
        if (waterMark.get()) renderWatermark()

        if (crossHairValue.get()) {
            val lrs1 = width()
            val lrs2 = RenderUtils.height()
            if (mc.thePlayer.isSprinting) {
                Gui.drawRect(lrs1 / 2 - 3 - 3, lrs2 / 2, lrs1 / 2 + 6 - 8, lrs2 / 2 + 2 - 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2 - 6).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 - 2).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.7582394f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2 + 6 - 3, lrs2 / 2, lrs1 / 2 + 10 - 3, lrs2 / 2 + 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2 + 3).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 + 7).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.7635231f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2 - 6, lrs1 / 2 + 1, lrs2 / 2 - 2, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2 + 1 - 1).toFloat(), (lrs2 / 2 - 6).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 - 2).toFloat(), 0.7572856f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2 + 6 - 3, lrs1 / 2 + 1, lrs2 / 2 + 7, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2).toFloat(), (lrs2 / 2 + 6 - 3).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 + 10 - 3).toFloat(), 0.75438696f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2, lrs1 / 2 + 1, lrs2 / 2 + 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.75438696f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
            } else {
                Gui.drawRect(lrs1 / 2 + 10 - 19, lrs2 / 2, lrs1 / 2 + 5 - 10, lrs2 / 2 + 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2 - 9).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 - 5).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.7582394f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2 + 6, lrs2 / 2, lrs1 / 2 + 10, lrs2 / 2 + 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2 + 6).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 + 5 + 5).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.7635231f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2 - 9, lrs1 / 2 + 1, lrs2 / 2 - 5, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2).toFloat(), (lrs2 / 2 - 9).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 - 5).toFloat(), 0.7572856f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2 + 2 + 4, lrs1 / 2 + 1, lrs2 / 2 + 10, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2).toFloat(), (lrs2 / 2 + 3 + 3).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 + 3 + 7).toFloat(), 0.75438696f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
                Gui.drawRect(lrs1 / 2, lrs2 / 2, lrs1 / 2 + 1, lrs2 / 2 + 1, Color(255, 255, 255, 255).rgb)
                RenderUtils.drawBorderedRect((lrs1 / 2).toFloat(), (lrs2 / 2).toFloat(), (lrs1 / 2 + 1).toFloat(), (lrs2 / 2 + 1).toFloat(), 0.75438696f, Color(0, 0, 0, 255).rgb, Color(255, 190, 255, 0).rgb)
            }
        }

    }

    /**
     * Renders the watermark.
     */
    private fun renderWatermark() {
        var width = 3
        val colors = ColorManager.getClientColors()
        mc.fontRendererObj.drawStringWithShadow(
            "FDP",
            3.0f,
            3.0f,
            colors[0].rgb
        )
        width += mc.fontRendererObj.getStringWidth("FDP")
        mc.fontRendererObj.drawStringWithShadow(
            "CLIENT",
            width.toFloat(),
            3.0f,
            colors[1].rgb
        )
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        FDPClient.hud.update()
        if (mc.currentScreen == null && lastFontEpsilon != fontEpsilonValue.get()) {
            lastFontEpsilon = fontEpsilonValue.get()
            alert("You need to reload FDPClient to apply changes!")
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        lastFontEpsilon = fontEpsilonValue.get()
    }

    @EventTarget
    fun onScreen(event: ScreenEvent?) {
        if (mc.theWorld == null || mc.thePlayer == null) {
            return
        }

        if (state && mc.entityRenderer.isShaderActive) {
            mc.entityRenderer.stopUseShader()
        }
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        FDPClient.hud.handleKey('a', event.key)
    }

}
