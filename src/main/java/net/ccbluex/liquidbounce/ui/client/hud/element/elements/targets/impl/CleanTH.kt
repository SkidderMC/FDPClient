/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.TargetHUD
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.cf.FontLoaders
import net.ccbluex.liquidbounce.utils.PlayerUtils
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.opengl.GL11
import net.minecraft.entity.EntityLivingBase
import java.awt.Color
import java.text.DecimalFormat

class CleanTH(inst: TargetHUD) : TargetStyle("Normal", inst, true) {
    override fun drawTarget(target: EntityLivingBase) {
        RenderUtils.drawRoundedCornerRect(
            -1.5f, 2.5f, 152.5f, 52.5f,
            5.0f, Color(0, 0, 0, 26).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -1f, 2f, 152f, 52f,
            5.0f, Color(0, 0, 0, 26).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -0.5f, 1.5f, 151.5f, 51.5f,
            5.0f, Color(0, 0, 0, 40).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            -0f, 1f, 151.0f, 51.0f,
            5.0f, Color(0, 0, 0, 60).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            0.5f, 0.5f, 150.5f, 50.5f,
            5.0f, Color(0, 0, 0, 50).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            1f, 0f, 150.0f, 50.0f,
            5.0f, Color(0, 0, 0, 50).rgb
        )
        // head size based on hurt
        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) {
            1f
        } else if (hurtPercent < 0.5f) {
            1 - (0.1f * hurtPercent * 2)
        } else {
            0.9f + (0.1f * (hurtPercent - 0.5f) * 2)
        }
        val size = 35
        // draw head
        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        // 绘制头部图片
        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(target.skin)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 40f, 8f, 8, 8, 30, 30, 64f, 64f)

        GL11.glPopMatrix()
        // draw name of target
        FontLoaders.F20.DisplayFonts("${target.name}", 45f, 12f, Color.WHITE.rgb, FontLoaders.F20)
        val df = DecimalFormat("0.00");
        // draw armour percent
        FontLoaders.F14.DisplayFonts(
            "Armor ${(df.format(PlayerUtils.getAr(target) * 100))}%",
            45f,
            24f,
            Color(200, 200, 200).rgb,
            FontLoaders.F14
        )
        // draw bar
        RenderUtils.drawRoundedCornerRect(45f, 32f, 145f, 42f, 5f, Color(0, 0, 0, 100).rgb)
        RenderUtils.drawRoundedCornerRect(
            45f,
            32f,
            45f + (easingHP / target.maxHealth) * 100f,
            42f,
            5f,
            ClientTheme.getColorWithAlpha(1,fadeAlpha(255)).rgb
        )
        // draw hp as text
        FontLoaders.F14.DisplayFont2(
            FontLoaders.F14,
            "${((df.format((easingHP / target.maxHealth) * 100)))}%",
            80f,
            34f,
            Color(255, 255, 255).rgb,
            true
        )
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        return Border(0F, 0F, 120F , 48F)
    }
}