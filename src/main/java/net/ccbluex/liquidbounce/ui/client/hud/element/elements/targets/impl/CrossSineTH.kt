/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedGradientRectCorner
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawHead
import net.ccbluex.liquidbounce.utils.render.RenderUtils.fastRoundedRect
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.minecraft.client.renderer.GlStateManager.disableAlpha
import net.minecraft.client.renderer.GlStateManager.disableBlend
import net.minecraft.client.renderer.GlStateManager.enableBlend
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color

class CrossSineTH(inst: Targets) : TargetStyle("CrossSine", inst, true) {

    override fun drawTarget(entity: EntityLivingBase) {
        val fonts = Fonts.fontSemibold40
        val textWidth = if (fonts.getStringWidth(entity.name) < fonts.getStringWidth("HurtTime : ${entity.hurtTime}"))
            fonts.getStringWidth("HurtTime : ${entity.hurtTime}")
        else
            fonts.getStringWidth(entity.name)
        updateAnim(entity.health)
        val bg = targetInstance.bgColor
        val adjustedBg = Color(bg.red, bg.green, bg.blue, fadeAlpha(bg.alpha))
        drawRoundedRect(
            0F, 0F,
            70F + textWidth, 42F,
            4F,
            adjustedBg.rgb,
            0.001F,
            adjustedBg.rgb
        )
        val gradientStart = Color(
            (targetInstance.barColor.red * 0.8).toInt().coerceAtMost(255),
            (targetInstance.barColor.green * 0.8).toInt().coerceAtMost(255),
            (targetInstance.barColor.blue * 0.8).toInt().coerceAtMost(255),
            targetInstance.barColor.alpha
        )
        val gradientEnd = Color(
            (targetInstance.barColor.red * 1.2).toInt().coerceAtMost(255),
            (targetInstance.barColor.green * 1.2).toInt().coerceAtMost(255),
            (targetInstance.barColor.blue * 1.2).toInt().coerceAtMost(255),
            targetInstance.barColor.alpha
        )
        drawRoundedGradientRectCorner(
            50F, 32F,
            52F + ((8F + textWidth) * easingHealth / 20),
            37F, 2F,
            gradientStart.rgb,
            gradientEnd.rgb
        )
        enableBlend()
        fonts.drawString(entity.name, 51F, 5F, Color(255, 255, 255, fadeAlpha(255)).rgb, true)
        fonts.drawString("HurtTime : ${entity.hurtTime}", 51F, 18F, Color(255, 255, 255, fadeAlpha(255)).rgb, true)
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
        drawHead(entity.skin, -2, -3, 35, 35, Color(255, 255, 255, fadeAlpha(255)).rgb)
        Stencil.dispose()
        glPopMatrix()
        disableAlpha()
        disableBlend()
        resetColor()
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        val fonts = Fonts.fontSemibold40
        val textWidth = if (entity != null) {
            if (fonts.getStringWidth(entity.name) < fonts.getStringWidth("HurtTime : ${entity.hurtTime}"))
                fonts.getStringWidth("HurtTime : ${entity.hurtTime}")
            else
                fonts.getStringWidth(entity.name)
        } else 0
        return Border(0F, 0F, 70F + textWidth, 42F)
    }
}
