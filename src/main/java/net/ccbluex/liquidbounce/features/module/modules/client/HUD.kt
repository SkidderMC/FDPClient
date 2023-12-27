/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.ScreenEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.ColorManager
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

    private val waterMark = BoolValue("Watermark", true)

    private val crossHairValue = BoolValue("CrossHair", true)

    /**
     * Renders the HUD.
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner) return
        FDPClient.hud.render(false, event.partialTicks)
        if (waterMark.get()) renderWatermark()

        if (crossHairValue.get()) {
            val lrs1 = RenderUtils.width()
            val lrs2 = RenderUtils.height()

            if (mc.thePlayer.isSprinting) {
                drawCrosshair(lrs1 / 2 - 3 - 3, lrs2 / 2, lrs1 / 2 + 6 - 8, lrs2 / 2 + 2 - 1)
                drawCrosshair(lrs1 / 2 + 6 - 3, lrs2 / 2, lrs1 / 2 + 10 - 3, lrs2 / 2 + 1)
                drawCrosshair(lrs1 / 2, lrs2 / 2 - 6, lrs1 / 2 + 1, lrs2 / 2 - 2)
                drawCrosshair(lrs1 / 2, lrs2 / 2 + 6 - 3, lrs1 / 2 + 1, lrs2 / 2 + 7)
            } else {
                drawCrosshair(lrs1 / 2 + 10 - 19, lrs2 / 2, lrs1 / 2 + 5 - 10, lrs2 / 2 + 1)
                drawCrosshair(lrs1 / 2, lrs2 / 2 - 9, lrs1 / 2 + 1, lrs2 / 2 - 5)
                drawCrosshair(lrs1 / 2, lrs2 / 2 + 2 + 4, lrs1 / 2 + 1, lrs2 / 2 + 10)
            }
            drawCrosshair(lrs1 / 2, lrs2 / 2, lrs1 / 2 + 1, lrs2 / 2 + 1)
        }

    }

    private fun drawCrosshair(x1: Int, y1: Int, x2: Int, y2: Int) {
        val borderColor = Color(0, 0, 0, 255).rgb
        val backgroundColor = Color(255, 255, 255, 255).rgb
        val borderWidth = 1
        val borderAlpha = 0.7582394f

        Gui.drawRect(x1, y1, x2, y2, backgroundColor)
        RenderUtils.drawBorderedRect((x1 - borderWidth).toDouble(), y1.toDouble(), x1.toDouble(), y2.toDouble(), borderAlpha.toDouble(), borderColor, Color(255, 190, 255, 0).rgb)
        RenderUtils.drawBorderedRect(x2.toDouble(), y1.toDouble(), (x2 + borderWidth).toDouble(), y2.toDouble(), borderAlpha.toDouble(), borderColor, Color(255, 190, 255, 0).rgb)
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
