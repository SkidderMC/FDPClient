/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.FontValue
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt

class FDPTH(inst: Targets) : TargetStyle("FDP", inst, true) {

    private val fontValue = FontValue("Font", Fonts.font40).displayable { targetInstance.styleValue.equals("FDP") }

    override fun drawTarget(target: EntityLivingBase) {
        val font = fontValue.get()
        val addedLen = (60 + font.getStringWidth(target.name) * 1.60f).toFloat()

        RenderUtils.drawRect(0f, 0f, addedLen, 47f, Color(0, 0, 0, 120).rgb)
        RenderUtils.drawRoundedCornerRect(0f, 0f, (easingHP / target.maxHealth) * addedLen, 47f, 3f, Color(0, 0, 0, 90).rgb)

        RenderUtils.drawShadow(0f, 0f, addedLen, 47f)

        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.1f * hurtPercent * 2)
        } else {
            0.9f + (0.1f * (hurtPercent - 0.5f) * 2)
        }
        val size = 35

        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        RenderUtils.quickDrawHead(target.skin, 0, 0, size, size)
        GL11.glPopMatrix()

        GL11.glPushMatrix()
        GL11.glScalef(1.5f, 1.5f, 1.5f)
        font.drawString(target.name, 39, 8, Color.WHITE.rgb)
        GL11.glPopMatrix()
        font.drawString("Health ${getHealth(target).roundToInt()}", 56, 12 + (font.FONT_HEIGHT * 1.5).toInt(), Color.WHITE.rgb)
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        return Border(0F,0F, 150F + Fonts.SFApple40.getStringWidth(entity!!.name), 47F)
    }
}