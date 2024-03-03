/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.ui.hud.element.Border
import net.ccbluex.liquidbounce.ui.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import java.awt.Color

class NormalTH(inst: Targets) : TargetStyle("Normal", inst, true) {
    val numberValue = BoolValue("Show Number", false).displayable { targetInstance.styleValue.equals("Normal") }
    val percentValue = BoolValue("Percent", false).displayable { targetInstance.styleValue.equals("Normal") && numberValue.get() }
    override fun drawTarget(entity: EntityLivingBase) {
        val fonts = Fonts.fontTenacityBold40
        val leaght = fonts.getStringWidth(entity.name)
        updateAnim(entity.health)
        RenderUtils.drawRoundedRect(0F, 0F, 42F + leaght, 23F, 0F, Color(32, 32, 32, fadeAlpha(255)).rgb)
        RenderUtils.drawAnimatedGradient(
            0.0, 0.0, (42.0 + leaght) * (easingHealth / entity.maxHealth), 1.0,ClientTheme.setColor("START",fadeAlpha(255)).rgb, ClientTheme.setColor("END",fadeAlpha(255)).rgb
        )
        if (numberValue.get()) {
            GlStateManager.enableBlend()
            fonts.drawStringFade((if (percentValue.get()) decimalFormat3.format((easingHealth / entity.maxHealth) * 100) + "%" else "${decimalFormat3.format(easingHealth)}❤"), (42F + leaght) * (easingHealth / entity.maxHealth), -8F, ClientTheme.getColorWithAlpha(1,fadeAlpha(255)))
            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()
        }
        GlStateManager.enableBlend()
        RenderUtils.drawHead(entity.skin, 2, 3, 18, 18, Color(255,255,255,fadeAlpha(255)).rgb)
        fonts.drawString(entity.name, 28F, 7F, Color(255, 255, 255, fadeAlpha(255)).rgb)
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        return Border(0F, 0F, 42F + Fonts.fontTenacityBold40.getStringWidth(entity!!.name), 23F)
    }
}