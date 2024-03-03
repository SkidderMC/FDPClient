/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color

class ZavzTH(inst: Targets) : TargetStyle("Zavz", inst, true) {
    override fun drawTarget(entity: EntityLivingBase) {
        val fonts = Fonts.SFApple40
        val leagth = if (fonts.getStringWidth(entity.name) < fonts.getStringWidth("HurtTime : ${entity.hurtTime}")) fonts.getStringWidth("HurtTime : ${entity.hurtTime}") else fonts.getStringWidth(entity.name)
        updateAnim(entity.health)
        RenderUtils.drawRoundedRect(0F,0F, 70F + leagth, 42F, 4F, Color(0,0,0,fadeAlpha(80)).rgb, 2F,  ClientTheme.getColorWithAlpha(1, fadeAlpha(255)).rgb)
        RenderUtils.drawRoundedGradientRectCorner(50F, 32F, 52F +  ((8F + leagth) * easingHealth / 20),37F, 2F, ClientTheme.getColorWithAlpha(0, fadeAlpha(255)).rgb, ClientTheme.getColorWithAlpha(90, fadeAlpha(255)).rgb)
        GlStateManager.enableBlend()
        fonts.drawString(entity.name, 51F, 5F, Color(255,255,255,fadeAlpha(255)).rgb, true)
        fonts.drawString("HurtTime : ${entity.hurtTime}", 51F, 18F, Color(255,255,255,fadeAlpha(255)).rgb, true)
        GL11.glPushMatrix()
        GL11.glTranslatef(7f, 7f, 0f)
        GL11.glColor4f(1f, 1 - entity.hurtPercent, 1 - entity.hurtPercent, 1f)
        Stencil.write(false)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        RenderUtils.fastRoundedRect(-2F, -3F, 33F, 33F, 8F)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        Stencil.erase(true)
        RenderUtils.drawHead(entity.skin, -2, -3, 35, 35, Color(255,255,255,fadeAlpha(255)).rgb)
        Stencil.dispose()
        GL11.glPopMatrix()
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        return Border(0F,0F, 70F + Fonts.SFApple40.getStringWidth(entity!!.name), 42F)
    }
}