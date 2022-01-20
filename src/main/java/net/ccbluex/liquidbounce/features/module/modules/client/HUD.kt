/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.button.AbstractButtonRenderer
import net.ccbluex.liquidbounce.features.module.modules.client.button.FLineButtonRenderer
import net.ccbluex.liquidbounce.features.module.modules.client.button.RiseButtonRenderer
import net.ccbluex.liquidbounce.features.module.modules.client.button.RoundedButtonRenderer
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.utils.render.Animation
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiChat
import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "HUD", category = ModuleCategory.CLIENT, array = false, defaultOn = true)
object HUD : Module() {
    val betterHotbarValue = BoolValue("BetterHotbar", true)
    val hotbarAlphaValue = IntegerValue("HotbarAlpha", 70, 0, 255).displayable { betterHotbarValue.get() }
    val hotbarEaseValue = BoolValue("HotbarEase", true)
    private val hotbarAnimSpeedValue = IntegerValue("HotbarAnimSpeed", 10, 5, 20).displayable { hotbarEaseValue.get() }
    private val hotbarAnimTypeValue = EaseUtils.getEnumEasingList("HotbarAnimType").displayable { hotbarEaseValue.get() }
    private val hotbarAnimOrderValue = EaseUtils.getEnumEasingOrderList("HotbarAnimOrder").displayable { hotbarEaseValue.get() }
    val inventoryParticle = BoolValue("InventoryParticle", false)
    private val blurValue = BoolValue("Blur", false)
    val fontChatValue = BoolValue("FontChat", false)
    val chatRectValue = BoolValue("ChatRect", true)
    val chatCombineValue = BoolValue("ChatCombine", true)
    val chatAnimValue = BoolValue("ChatAnimation", true)
    val rainbowStart = FloatValue("RainbowStart", 0.41f, 0f, 1f)
    val rainbowStop = FloatValue("RainbowStop", 0.58f, 0f, 1f)
    val rainbowSaturation = FloatValue("RainbowSaturation", 0.7f, 0f, 1f)
    val rainbowBrightness = FloatValue("RainbowBrightness", 1f, 0f, 1f)
    val rainbowSpeed = IntegerValue("RainbowSpeed", 1500, 500, 7000)
    val arraylistXAxisAnimSpeedValue = IntegerValue("ArraylistXAxisAnimSpeed", 10, 5, 20)
    val arraylistXAxisAnimTypeValue = EaseUtils.getEnumEasingList("ArraylistXAxisAnimType")
    val arraylistXAxisAnimOrderValue = EaseUtils.getEnumEasingOrderList("ArraylistXAxisHotbarAnimOrder")
    val arraylistYAxisAnimSpeedValue = IntegerValue("ArraylistYAxisAnimSpeed", 10, 5, 20)
    val arraylistYAxisAnimTypeValue = EaseUtils.getEnumEasingList("ArraylistYAxisAnimType")
    val arraylistYAxisAnimOrderValue = EaseUtils.getEnumEasingOrderList("ArraylistYAxisHotbarAnimOrder")
    val fontEpsilonValue = FloatValue("FontVectorEpsilon", 0.5f, 0f, 1.5f)
    val fontDoubleRenderValue = BoolValue("FontDoubleRender", true)
    val fontOnlyASCIIValue = BoolValue("FontOnlyASCII", false)
    private val buttonValue = ListValue("Button", arrayOf("FLine", "Rounded", "Rise", "Vanilla"), "FLine")

    private var lastFontEpsilon = 0f

    private var easeAnimation: Animation? = null
    private var easingValue = 0
        get() {
            if (easeAnimation != null) {
                field = easeAnimation!!.value.toInt()
                if (easeAnimation!!.state == Animation.EnumAnimationState.STOPPED) {
                    easeAnimation = null
                }
            }
            return field
        }
        set(value) {
            if (easeAnimation == null || (easeAnimation != null && easeAnimation!!.to != value.toDouble())) {
                easeAnimation = Animation(EaseUtils.EnumEasingType.valueOf(hotbarAnimTypeValue.get()), EaseUtils.EnumEasingOrder.valueOf(hotbarAnimOrderValue.get()), field.toDouble(), value.toDouble(), hotbarAnimSpeedValue.get() * 30L).start()
            }
        }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner) return
        LiquidBounce.hud.render(false, event.partialTicks)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        LiquidBounce.hud.update()
        if(mc.currentScreen == null && lastFontEpsilon != fontEpsilonValue.get()) {
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

    fun getHotbarEasePos(x: Int): Int {
        if(!state || !hotbarEaseValue.get()) return x
        easingValue = x
        return easingValue
    }

    fun getButtonRenderer(button: GuiButton): AbstractButtonRenderer? {
        return when (buttonValue.get().lowercase()) {
            "fline" -> FLineButtonRenderer(button)
            "rounded" -> RoundedButtonRenderer(button)
            "rise" -> RiseButtonRenderer(button)
            else -> null // vanilla or unknown
        }
    }
}
