/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.TargetHUD
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import java.awt.Color

class SimpleTH(inst: TargetHUD) : TargetStyle("Simple", inst, true) {
    override fun drawTarget(entity: EntityLivingBase) {
        val fonts = Fonts.minecraftFont
        val leagth = fonts.getStringWidth(entity.name)
        updateAnim(entity.health)
        RenderUtils.drawRect(0F, 0F, 40F + leagth, 24F, Color(0,0,0,fadeAlpha(180)))
        RenderUtils.drawRect(28F, 20F,  28F + ((leagth + 6F) * (easingHealth / entity.maxHealth)), 21F, ColorUtils.reAlpha(BlendUtils.getHealthColor(entity.health, entity.maxHealth), fadeAlpha(255)).rgb)
        GlStateManager.enableBlend()
        fonts.drawString(entity.name, 31F, 5F, Color(255,255,255,fadeAlpha(255)).rgb, true)
        RenderUtils.drawHead(entity.skin, 2, 2, 20,20, Color(255,255,255,fadeAlpha(255)).rgb)
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        return Border(0F,0F, 40F + Fonts.minecraftFont.getStringWidth(entity!!.name), 24F)
    }
}