/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.button.*
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule.*
import net.ccbluex.liquidbounce.ui.cape.GuiCapeManager.height
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.util.*

@ModuleInfo(name = "HUD", category = ModuleCategory.CLIENT, array = false, defaultOn = true)
object HUD : Module() {
    val shadowValue = ListValue("TextShadowMode", arrayOf("LiquidBounce", "Outline", "Default", "Autumn"), "Autumn")
    val clolormode = ListValue("ColorMode", arrayOf("Rainbow", "Light Rainbow", "Static", "Double Color", "Default"), "Light Rainbow")
    val hueInterpolation = BoolValue("hueInterpolation", false)
    val movingcolors = BoolValue("MovingColors", false)
    val inventoryParticle = BoolValue("InventoryParticle", false)
    private val blurValue = BoolValue("Blur", false)
    val HealthValue = BoolValue("Health", true)
    val waterMark = BoolValue("Watermark", true)
    val rainbowStartValue = FloatValue("RainbowStart", 0.55f, 0f, 1f)
    val rainbowStopValue = FloatValue("RainbowStop", 0.85f, 0f, 1f)
    val rainbowSaturationValue = FloatValue("RainbowSaturation", 0.45f, 0f, 1f)
    val rainbowBrightnessValue = FloatValue("RainbowBrightness", 0.85f, 0f, 1f)
    val rainbowSpeedValue = IntegerValue("RainbowSpeed", 1500, 500, 7000)
    val arraylistXAxisAnimSpeedValue = IntegerValue("ArraylistXAxisAnimSpeed", 10, 5, 20)
    val arraylistXAxisAnimTypeValue = EaseUtils.getEnumEasingList("ArraylistXAxisAnimType")
    val arraylistXAxisAnimOrderValue = EaseUtils.getEnumEasingOrderList("ArraylistXAxisHotbarAnimOrder").displayable { !arraylistXAxisAnimTypeValue.equals("NONE") }
    val arraylistYAxisAnimSpeedValue = IntegerValue("ArraylistYAxisAnimSpeed", 10, 5, 20)
    val arraylistYAxisAnimTypeValue = EaseUtils.getEnumEasingList("ArraylistYAxisAnimType")
    val arraylistYAxisAnimOrderValue = EaseUtils.getEnumEasingOrderList("ArraylistYAxisHotbarAnimOrder").displayable { !arraylistYAxisAnimTypeValue.equals("NONE") }
    val fontEpsilonValue = FloatValue("FontVectorEpsilon", 0.5f, 0f, 1.5f)
    private val buttonValue = ListValue("Button", arrayOf("Better", "RGBRounded", "Wolfram", "Rounded", "Hyperium", "RGB", "Badlion", "Flat", "FLine", "Rise", "Vanilla"), "Rounded")

    private var lastFontEpsilon = 0f

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner) return
        LiquidBounce.hud.render(false, event.partialTicks)
        if(waterMark.get()) renderWatermark()
        if (HealthValue.get()) mc.fontRendererObj.drawStringWithShadow(
            MathHelper.ceiling_float_int(mc.thePlayer.health).toString(),
            (width / 2 - 4).toFloat(), (height / 2 - 13).toFloat(), if (mc.thePlayer.health <= 15) Color(255, 0, 0).rgb else Color(0, 255, 0).rgb)
        GlStateManager.resetColor()
    }

    private fun renderWatermark() {
        var width = 3
        mc.fontRendererObj.drawStringWithShadow(
            "FDP",
            3.0f,
            3.0f,
            rainbow().rgb
        )
        width += mc.fontRendererObj.getStringWidth("FDP")
        mc.fontRendererObj.drawStringWithShadow(
            "CLIENT",
            width.toFloat(),
            3.0f,
            -1
        )
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        LiquidBounce.hud.update()
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
    fun onScreen(event: ScreenEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) {
            return
        }

        if (state && blurValue.get() && !mc.entityRenderer.isShaderActive && event.guiScreen != null && !(event.guiScreen is GuiChat || event.guiScreen is GuiHudDesigner)) {
            mc.entityRenderer.loadShader(ResourceLocation("fdpclient/blur.json"))
        } else if (mc.entityRenderer.shaderGroup != null && mc.entityRenderer.shaderGroup!!.shaderGroupName.contains("fdpclient/blur.json")) {
            mc.entityRenderer.stopUseShader()
        }
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        LiquidBounce.hud.handleKey('a', event.key)
    }
    fun getClientColors(): Array<Color>? {
        val firstColor: Color
        val secondColor: Color
        when (clolormode.get()
            .lowercase(Locale.getDefault())) {
            "light rainbow" -> {
                firstColor = ColorUtils.rainbowc(15, 1, .6f, 1F, 1F)!!
                secondColor = ColorUtils.rainbowc(15, 40, .6f, 1F, 1F)!!
            }
            "rainbow" -> {
                firstColor = ColorUtils.rainbowc(15, 1, 1F, 1F, 1F)!!
                secondColor = ColorUtils.rainbowc(15, 40, 1F, 1F, 1F)!!
            }
            "double color" -> {
                firstColor =
                    ColorUtils.interpolateColorsBackAndForth(15, 0, Color.PINK, Color.BLUE, hueInterpolation.get())!!
                secondColor =
                    ColorUtils.interpolateColorsBackAndForth(15, 90, Color.PINK, Color.BLUE, hueInterpolation.get())!!
            }
            "static" -> {
                firstColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
                secondColor = firstColor
            }
            else -> {
                firstColor = Color(-1)
                secondColor = Color(-1)
            }
        }
        return arrayOf(firstColor, secondColor)
    }

    fun getButtonRenderer(button: GuiButton): AbstractButtonRenderer? {
        return when (buttonValue.get().lowercase()) {
            "better" -> BetterButtonRenderer(button)
            "rounded" -> RoundedButtonRenderer(button)
            "fline" -> FLineButtonRenderer(button)
            "rise" -> RiseButtonRenderer(button)
            "hyperium" -> HyperiumButtonRenderer(button)
            "rgb" -> RGBButtonRenderer(button)
            "badlion" -> BadlionTwoButtonRenderer(button)
            "rgbrounded" -> RGBRoundedButtonRenderer(button)
            "wolfram" -> WolframButtonRenderer(button)
            else -> null // vanilla or unknown
        }
    }
}
