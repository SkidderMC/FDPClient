/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.TargetHUD
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import org.lwjgl.opengl.GL11
import net.minecraft.entity.EntityLivingBase
import java.awt.Color
import kotlin.math.roundToInt

class TenacityTH(inst: TargetHUD) : TargetStyle("Tenacity5", inst, true) {
    override fun drawTarget(target: EntityLivingBase) {
        val additionalWidth = Fonts.font40.getStringWidth(target.name).coerceAtLeast(75)

        //colors
        val c1 = ColorUtils.interpolateColorsBackAndForth(17, 0, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);
        val c2 = ColorUtils.interpolateColorsBackAndForth(17, 90, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);
        val c3 = ColorUtils.interpolateColorsBackAndForth(17, 270, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);
        val c4 = ColorUtils.interpolateColorsBackAndForth(17, 180, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);
        val renderX = Element.renderX
        val renderY = Element.renderY
        val scale = Element.scale
        // glow
        GL11.glTranslated(-renderX * scale, -renderY * scale, 0.0)
        GL11.glPushMatrix()
        ShadowUtils.shadow(8F, { GL11.glPushMatrix(); GL11.glTranslated(renderX * scale, renderY * scale, 0.0); RoundedUtil.drawGradientRound(0f * scale, 5f * scale, 59f + additionalWidth.toFloat() * scale, 45f * scale, 6F, c1, c2, c3, c4); GL11.glPopMatrix(); }, {})
        GL11.glPopMatrix()
        GL11.glTranslated(renderX * scale, renderY * scale, 0.0)

        // background
        RoundedUtil.drawGradientRound(0f, 5f, 59f + additionalWidth.toFloat(), 45f, 6F, c1, c2, c3, c4);

        // circle player avatar
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        mc.textureManager.bindTexture(target.skin)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 7, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 7, 40f, 8f, 8, 8, 30, 30, 64f, 64f)
        GL11.glPopMatrix()

        // text
        Fonts.font40.drawCenteredString(target.name, 47 + (additionalWidth / 2f), 1f + Fonts.font40.FONT_HEIGHT, Color.WHITE.rgb, false)
        val infoStr = ((((easingHP / target.maxHealth) * 100).roundToInt()).toString() + "% - " + ((mc.thePlayer.getDistanceToEntityBox(target)).roundToInt()).toString() + "M")
        Fonts.fontTenacity40.drawString(infoStr, 47f + ((additionalWidth - Fonts.fontTenacity40.getStringWidth(infoStr)) / 2f), 45f - (Fonts.fontTenacity40.FONT_HEIGHT), Color.WHITE.rgb, false)

        //hp bar
        RenderUtils.drawRoundedCornerRect(46f, 24f, 46f + additionalWidth, 29f, 2.5f, Color(60, 60, 60, 130).rgb)
        RenderUtils.drawRoundedCornerRect(46f, 24f, 46f + (easingHP / target.maxHealth) * additionalWidth, 29f, 2.5f, Color(240, 240, 240, 250).rgb)
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        return Border(0F, 5F, 125F, 45F)
    }
}