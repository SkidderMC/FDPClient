package net.ccbluex.liquidbounce.ui.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.hud.element.Border
import net.ccbluex.liquidbounce.ui.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt


class Tenacity5TH(inst: Targets) : TargetStyle("Tenacity5", inst, true) {

    override fun drawTarget(entity: EntityLivingBase) {
        val hurtPercent = entity.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.2f * hurtPercent * 2)
        } else {
            0.8f + (0.2f * (hurtPercent - 0.5f) * 2)
        }
        val additionalWidth = Fonts.fontBold40.getStringWidth(entity.name).coerceAtLeast(75)

        //colours
        val c1 = ColorUtils.interpolateColorsBackAndForth(17, 0, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);
        val c2 = ColorUtils.interpolateColorsBackAndForth(17, 90, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);
        val c3 = ColorUtils.interpolateColorsBackAndForth(17, 270, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);
        val c4 = ColorUtils.interpolateColorsBackAndForth(17, 180, Color(230, 140, 255, 205), Color(101, 208, 252, 205), true);


        // background
        RoundedUtil.drawGradientRound(0f, 5f, 59f + additionalWidth.toFloat(), 45f, 6F, c1, c2, c3, c4);

        // circle player avatar
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        mc.textureManager.bindTexture(entity.skin)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 7, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 7, 40f, 8f, 8, 8, 30, 30, 64f, 64f)
        GL11.glPopMatrix()

        // text
        Fonts.fontBold40.drawCenteredString(entity.name, 47 + (additionalWidth / 2f), 1f + Fonts.fontBold40.FONT_HEIGHT, Color.WHITE.rgb, false)
        val infoStr = ((((easingHP / entity.maxHealth) * 100).roundToInt()).toString() + "% - " + ((mc.thePlayer.getDistanceToEntityBox(entity)).roundToInt()).toString() + "M")
        Fonts.SFApple40.drawString(infoStr, 47f + ((additionalWidth - Fonts.SFApple40.getStringWidth(infoStr)) / 2f), 45f - (Fonts.SFApple40.FONT_HEIGHT), Color.WHITE.rgb, false)

        //hp bar
        RenderUtils.drawRoundedCornerRect(46f, 24f, 46f + additionalWidth, 29f, 2.5f, Color(60, 60, 60, 130).rgb)
        RenderUtils.drawRoundedCornerRect(46f, 24f, 46f + (easingHP / entity.maxHealth) * additionalWidth, 29f, 2.5f, Color(240, 240, 240, 250).rgb)
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        entity ?: return Border(-2F, 3F, 62F + mc.thePlayer.name.let(Fonts.font40::getStringWidth).coerceAtLeast(75).toFloat(), 50F)

        val nameWidth = mc.fontRendererObj.getStringWidth(entity.name.toString())
        val maxWidth = (62F + nameWidth).coerceAtLeast(38F + Fonts.font40.getStringWidth(entity.name))

        return Border(-2F, 3F, maxWidth, 50F)
    }


}