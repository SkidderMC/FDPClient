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
import net.ccbluex.liquidbounce.utils.extensions.darker
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityOnScreen
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.minecraft.client.renderer.GlStateManager.color
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.abs

class FDPClassicTH(inst: Targets) : TargetStyle("Classic", inst, true) {
    private var lastTarget: EntityPlayer? = null

    override fun drawTarget(entity: EntityLivingBase) {
        val font = Fonts.minecraftFont
        val healthString = "${decimalFormat2.format(entity.health)} "

        if (entity != lastTarget || easingHealth < 0 || easingHealth > entity.maxHealth ||
            abs(easingHealth - entity.health) < 0.01
        ) {
            easingHealth = entity.health
        }
        val width = (38 + Fonts.minecraftFont.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()

        updateAnim(entity.health)

        // Draw rect box
        drawRect(2F, -1F, width - 3F, 42F, targetInstance.bgColor.rgb)

        // Health bar
        val healthLength = (entity.health / entity.maxHealth).coerceIn(0F, 1F)
        drawRect(
            36F,
            32.5F,
            42F + 69F,
            39F,
            targetInstance.barColor.darker(0.3f)
        )
        drawRect(
            36F,
            32.5F,
            36F + (easingHealth / entity.maxHealth).coerceIn(0F, entity.maxHealth) * (healthLength + 74F),
            39F,
            targetInstance.barColor.rgb
        )

        updateAnim(entity.health)
        // Name
        font.drawStringWithShadow(entity.name, 36F, 4F, Color(255, 255, 255).rgb)

        // HP
        glPushMatrix()
        glScalef(1.5F, 1.5F, 1.5F)
        font.drawStringWithShadow("$healthString❤", 24F, 11F, targetInstance.barColor.rgb)
        glPopMatrix()

        resetColor()
        color(1.0f, 1.0f, 1.0f)
        drawEntityOnScreen(19, 38, 19, entity)
        resetColor()

    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        entity ?: return Border(0F, 0F, 118F, 32F)
        val width = (38 + Fonts.minecraftFont.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()
        return Border(2F, -1F, width - 3F, 42F)
    }
}