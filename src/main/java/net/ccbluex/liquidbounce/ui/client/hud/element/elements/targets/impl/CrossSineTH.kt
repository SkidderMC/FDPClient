/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.utils.ClientThemesUtils.getColorWithAlpha
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedGradientRectCorner
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.fastRoundedRect
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color

class CrossSineTH(inst: Targets) : TargetStyle("CrossSine", inst, true) {

    override fun drawTarget(entity: EntityLivingBase) {
        val fonts = Fonts.font40
        val leagth = if (fonts.getStringWidth(entity.name) < fonts.getStringWidth("HurtTime : ${entity.hurtTime}")) fonts.getStringWidth("HurtTime : ${entity.hurtTime}") else fonts.getStringWidth(entity.name)
        updateAnim(entity.health)
        drawRoundedRect(0F,0F, 70F + leagth, 42F, 4F, Color(0,0,0,fadeAlpha(80)).rgb, 2F,  getColorWithAlpha(1, fadeAlpha(255)).rgb)
        drawRoundedGradientRectCorner(50F, 32F, 52F +  ((8F + leagth) * easingHealth / 20),37F, 2F, getColorWithAlpha(0, fadeAlpha(255)).rgb, getColorWithAlpha(90, fadeAlpha(255)).rgb)
        enableBlend()
        fonts.drawString(entity.name, 51F, 5F, Color(255,255,255,fadeAlpha(255)).rgb, true)
        fonts.drawString("HurtTime : ${entity.hurtTime}", 51F, 18F, Color(255,255,255,fadeAlpha(255)).rgb, true)
        glPushMatrix()
        glTranslatef(7f, 7f, 0f)
        glColor4f(1f, 1 - entity.hurtPercent, 1 - entity.hurtPercent, 1f)
        Stencil.write(false)
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        fastRoundedRect(-2F, -3F, 33F, 33F, 8F)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        Stencil.erase(true)
        RenderUtils.drawHead(entity.skin, -2, -3, 35, 35, Color(255,255,255,fadeAlpha(255)).rgb)
        Stencil.dispose()
        glPopMatrix()
        disableAlpha()
        disableBlend()
        resetColor()
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        val entityNameWidth = if (entity != null) Fonts.font40.getStringWidth(entity.name) else 0
        return Border(0F, 0F, 70F + entityNameWidth, 42F)
    }
}