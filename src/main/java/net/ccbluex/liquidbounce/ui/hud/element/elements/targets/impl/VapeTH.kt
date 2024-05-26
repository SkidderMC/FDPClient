/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.hud.element.Border
import net.ccbluex.liquidbounce.ui.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.minecraft.entity.EntityLivingBase
import java.awt.Color

class VapeTH(inst: Targets) : TargetStyle("Vape", inst, true) {
    
    override fun drawTarget(entity: EntityLivingBase) {
        RenderUtils.drawEntityOnScreen(16, 55, 25, entity)

        Fonts.fontBold35.drawString(entity.name, 36.5f, 12.6f / 2f - Fonts.fontBold35.height / 2f, -1)

        val targetHealth = entity.health
        val targetMaxHealth = entity.maxHealth
        val targetAbsorptionAmount = entity.absorptionAmount
        val targetHealthDWithAbs = targetHealth / (targetMaxHealth + targetAbsorptionAmount).coerceAtLeast(1.0f)
        val targetHealthD = targetHealth / targetMaxHealth.coerceAtLeast(1.0f)
        val color: Color = ColorUtils.interpolateColorC(Color.RED, Color(5, 134, 105), targetHealthD)

        RoundedUtil.drawRound(37f, 12.6f, 68f, 2.9f, 1f, Color(43, 42, 43))
        RoundedUtil.drawRound(37f, 12.6f, 68f * targetHealthDWithAbs, 2.9f, 1f, color)
        if (targetAbsorptionAmount > 0) {
            val absLength = 49f * (targetAbsorptionAmount / (targetMaxHealth + targetAbsorptionAmount))
            RoundedUtil.drawRound(37f + 68f * targetHealthDWithAbs,
                12.6f,
                absLength,
                2.9f,
                1f,
                Color(0xFFAA00))
        }

        val hp = (targetHealth + targetAbsorptionAmount).toString() + "  HP"
        Fonts.fontBold35.drawString(hp,
            105f - Fonts.fontBold35.getStringWidth(hp),
            (12.6f - Fonts.fontBold35.height) / 2f,
            -1)
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        val entityNameWidth = if (entity != null) Fonts.fontBold35.getStringWidth(entity.name) else 0
        return Border(0F, 0F, 110F + entityNameWidth, 40F)
    }
    
}