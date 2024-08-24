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
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt

class ModernTH(inst: Targets) : TargetStyle("Modern", inst, true) {

    override fun drawTarget(entity: EntityLivingBase) {

        val font = Fonts.font35

        updateAnim(entity.health)

        val additionalWidth = ((font.getStringWidth(entity.name) * 1.1).toInt().coerceAtLeast(70) + font.getStringWidth("Name: ") * 1.1 + 7.0).roundToInt()
        val healthBarWidth = additionalWidth - (font.getStringWidth("20") * 1.15).roundToInt() - 16
        RenderUtils.drawRoundedCornerRect(0f, 0f, 50f + additionalWidth, 50f, 7f, Color(0, 0, 0, 130).rgb)
        //RenderUtils.drawShadow(2f, 2f, 48f + additionalWidth, 48f)

        // circle player avatar
        val hurtPercent = entity.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
            1 - (0.2f * hurtPercent * 2)
        } else {
            0.8f + (0.2f * (hurtPercent - 0.5f) * 2)
        }
        val size = 35

        //draw head
        GL11.glPushMatrix()
        GL11.glTranslatef(9f, 10f, 0f)
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

        // draw name
        GL11.glPushMatrix()
        GL11.glScalef(1.1f, 1.1f, 1.1f)
        font.drawString("Name: ${entity.name}", 45, 14, getColorWithAlpha(0, fadeAlpha(255)).rgb)
        font.drawString("Name:", 45, 14, Color.WHITE.rgb)
        GL11.glPopMatrix()

        // draw health
        RenderUtils.drawRoundedCornerRect(50f, 31f, 50f + healthBarWidth , 39f, 3f, Color(20, 20, 20, 255).rgb)
        RenderUtils.drawRoundedCornerRect(50f, 31f, 50f + (healthBarWidth * (easingHealth / entity.maxHealth)) , 39f, 4f, getColorWithAlpha(0, fadeAlpha(255)).rgb)
        RenderUtils.drawRoundedCornerRect(52f, 31f, 48f + (healthBarWidth * (easingHealth / entity.maxHealth)) , 34f, 2f, Color(255, 255, 255, 30).rgb)
        RenderUtils.drawRoundedCornerRect(52f, 36f, 48f + (healthBarWidth * (easingHealth / entity.maxHealth)) , 39f, 2f, Color(0, 0, 0, 30).rgb)
        GL11.glPushMatrix()
        GL11.glScalef(1.15f, 1.15f, 1.15f)
        font.drawString(getHealth(entity).roundToInt().toString(), ((38 + additionalWidth - font.getStringWidth((getHealth(entity) * 1.15).roundToInt().toString())) / 1.15).roundToInt()   , 29 - (font.FONT_HEIGHT/2),  getColorWithAlpha(0, fadeAlpha(255)).rgb)
        GL11.glPopMatrix()
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        entity ?: return Border(0F, 0F, 50F, 50F)

        val font = Fonts.font35

        val additionalWidth = ((font.getStringWidth(entity.name) * 1.1).toInt().coerceAtLeast(70) + font.getStringWidth("Name: ") * 1.1 + 7.0).roundToInt()

        return Border(0F, 0F, 50F + additionalWidth, 50F)
    }
}