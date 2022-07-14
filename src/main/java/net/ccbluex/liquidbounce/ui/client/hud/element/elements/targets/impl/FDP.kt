package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.FontValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer

import org.lwjgl.opengl.GL11
import java.awt.Color

class FDP(inst: Targets): TargetStyle("FDP", inst, true) {

    private val fontValue = FontValue("Font", Fonts.font40)
    private var easingHP = 0f

    override fun drawTarget(target: EntityPlayer) {
        val font = fontValue.get()

        RenderUtils.drawRoundedCornerRect(0f, 0f, 150f, 47f, 4f, Color(0, 0, 0, 100).rgb)

        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.1f * hurtPercent * 2)
        } else {
            0.9f + (0.1f * (hurtPercent - 0.5f) * 2)
        }
        val size = 35

        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        // 绘制头部图片
        RenderUtils.quickDrawHead(target.skin, 0, 0, size, size)
        GL11.glPopMatrix()

        font.drawString("Name ${target.name}", 45, 5, Color.WHITE.rgb)
        font.drawString("Health ${getHealth(target)}", 45, 5 + font.FONT_HEIGHT, Color.WHITE.rgb)
        RenderUtils.drawRoundedCornerRect(45f, (5 + font.FONT_HEIGHT  + font.FONT_HEIGHT).toFloat(), 45f + (easingHP / target.maxHealth) * 100f, 42f, 3f, ColorUtils.rainbow().rgb)

    }

    private fun getHealth(entity: EntityLivingBase?): Float {
        return entity?.health ?: 0f
    }


    override fun getBorder(entity: EntityPlayer?): Border? {
        entity ?: return Border(0F, 0F, 120F, 48F)
        val tWidth = (45F + Fonts.font40.getStringWidth(entity.name).coerceAtLeast(Fonts.font40.getStringWidth(decimalFormat.format(entity.health)))).coerceAtLeast(120F)
        return Border(0F, 0F, tWidth, 48F)
    }

}
