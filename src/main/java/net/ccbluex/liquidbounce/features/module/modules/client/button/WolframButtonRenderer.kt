/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.utils.render.EaseUtils.easeInOutQuad
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shadowRenderUtils
import net.ccbluex.liquidbounce.font.FontLoaders
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import java.awt.Color

class WolframButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {

    private var animation = 0.0
    private var lastUpdate = System.currentTimeMillis()

    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {
        val time = System.currentTimeMillis()
        val pct = (time - lastUpdate) / 500.0

        if (button.hovered) {
            if (animation < 1) {
                animation += pct
            }
            if (animation > 1) {
                animation = 1.0
            }
        } else {
            if (animation > 0) {
                animation -= pct
            }
            if (animation < 0) {
                animation = 0.0
            }
        }
        val percent = easeInOutQuad(animation)
        if (button.enabled) {
            val half = FontLoaders.F18.DisplayFontWidths(FontLoaders.F18,button.displayString)  / 2.0
            val center = button.xPosition + (button.width / 2.0)
            RenderUtils.drawRect(
                center - percent * half,
                (button.yPosition + button.height - 1).toDouble(),
                center + percent * half,
                (button.yPosition + button.height).toDouble(),
                Color.WHITE.rgb)
        }
        lastUpdate = time
    }
}