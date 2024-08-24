/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedCornerRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawShadow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.quickDrawHead
import net.ccbluex.liquidbounce.value.FontValue
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.roundToInt

class FDPTH(inst: Targets) : TargetStyle("FDP", inst, true) {

    private val fontValue by FontValue("Font", Fonts.font40) { targetInstance.styleValue.equals("FDP") }

    override fun drawTarget(entity: EntityLivingBase) {
        val font = fontValue
        val addedLen = (60 + font.getStringWidth(entity.name) * 1.60f)

        drawRect(0f, 0f, addedLen, 47f, Color(0, 0, 0, 120).rgb)
        drawRoundedCornerRect(0f, 0f, (easingHP / entity.maxHealth) * addedLen, 47f, 3f, Color(0, 0, 0, 90).rgb)

        drawShadow(0f, 0f, addedLen, 47f)

        val hurtPercent = entity.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.1f * hurtPercent * 2)
        } else {
            0.9f + (0.1f * (hurtPercent - 0.5f) * 2)
        }
        val size = 35

        glPushMatrix()
        glTranslatef(5f, 5f, 0f)
        glScalef(scale, scale, scale)
        glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        quickDrawHead(entity.skin, 0, 0, size, size)
        glPopMatrix()

        glPushMatrix()
        glScalef(1.5f, 1.5f, 1.5f)
        font.drawString(entity.name, 39, 8, Color.WHITE.rgb)
        glPopMatrix()
        font.drawString("Health ${getHealth(entity).roundToInt()}", 56, 12 + (font.FONT_HEIGHT * 1.5).toInt(), Color.WHITE.rgb)
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        entity ?: return Border(0F, 0F, 150F, 47F)

        val font = fontValue
        val nameWidth = font.getStringWidth(entity.name)
        val addedLen = (60 + nameWidth * 1.60f)

        return Border(0F, 0F, addedLen, 47F)
    }
}