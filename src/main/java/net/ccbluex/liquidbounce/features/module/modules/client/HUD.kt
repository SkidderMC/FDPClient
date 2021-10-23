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
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "HUD", category = ModuleCategory.CLIENT, array = false, defaultOn = true)
object HUD : Module() {
    val betterHotbarValue = BoolValue("BetterHotbar", true)
    val hotbarAlphaValue = IntegerValue("HotbarAlpha", 150, 0, 255).displayable { betterHotbarValue.get() }
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

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen is GuiHudDesigner) return
        LiquidBounce.hud.render(false, event.partialTicks)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        LiquidBounce.hud.update()
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
}