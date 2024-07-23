/*
FDPClient Hacked Client
A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
https://github.com/SkidderMC/FDPClient/
*/
package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.buttonShadowValue
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.uiEffectValue
import net.ccbluex.liquidbounce.utils.UIEffectRenderer.drawShadowWithCustomAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import java.awt.Color
import kotlin.math.abs

class BlackoutButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {

    val hud = HUDModule

    private var fading = 100F
    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        fading = fade(if(button.hovered) { 50F } else { 100F }, fading, 0.6F )
        RenderUtils.drawRect(button.xPosition.toFloat(), button.yPosition.toFloat(), button.xPosition.toFloat() + button.width.toFloat(), button.yPosition.toFloat() + button.height.toFloat(), Color(0, 0, 0, fading.toInt().coerceIn(50, 100)))
        if (hud.handleEvents() && uiEffectValue && buttonShadowValue) { drawShadowWithCustomAlpha(button.xPosition.toFloat(), button.yPosition.toFloat(), button.width.toFloat(), button.height.toFloat(), 240f) }
    }

    fun fade(target: Float, current: Float, smooth: Float): Float {
        if (current == target) { return current }
        val factor = 0.1F.coerceAtLeast(abs(target - current) * (smooth / 5F).coerceIn(0.0F, 1.0F))
        return when {
            target > current ->  current + factor
            else -> current - factor
        }
    }
}
