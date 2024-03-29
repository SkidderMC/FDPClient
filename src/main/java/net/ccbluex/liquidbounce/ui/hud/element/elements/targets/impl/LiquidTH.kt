/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.hud.element.Border
import net.ccbluex.liquidbounce.ui.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.EntityLivingBase
import java.awt.Color

class LiquidTH(inst: Targets) : TargetStyle("LiquidBounce", inst, true) {
    
    override fun drawTarget(entity: EntityLivingBase) {
        val width = (38 + entity.name.let(Fonts.font40::getStringWidth))
            .coerceAtLeast(118)
            .toFloat()
        // Draw rect box
        RenderUtils.drawBorderedRect(0F, 0F, width, 36F, 3F, Color.BLACK.rgb, Color.BLACK.rgb)

        // Damage animation
        if (easingHP > getHealth(entity)) {
            RenderUtils.drawRect(0F, 34F, (easingHP / entity.maxHealth) * width,
                36F, Color(252, 185, 65).rgb)
        }

        // Health bar
        RenderUtils.drawRect(0F, 34F, (getHealth(entity) / entity.maxHealth) * width,
            36F, Color(252, 96, 66).rgb)

        // Heal animation
        if (easingHP < getHealth(entity)) {
            RenderUtils.drawRect((easingHP / entity.maxHealth) * width, 34F,
                (getHealth(entity) / entity.maxHealth) * width, 36F, Color(44, 201, 144).rgb)
        }

        entity.name.let { Fonts.font40.drawString(it, 36, 3, 0xffffff) }
        Fonts.font35.drawString("Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(entity))}", 36, 15, 0xffffff)

        // Draw info
        RenderUtils.drawHead(entity.skin, 2, 2, 30, 30, Color(255,255,255,fadeAlpha(255)).rgb)
        val playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)
        if (playerInfo != null) {
            Fonts.font35.drawString("Ping: ${playerInfo.responseTime.coerceAtLeast(0)}",
                36, 24, 0xffffff)
        }
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        entity ?: return Border(0F, 0F, 118F, 36F)

        val nameWidth = Fonts.font40.getStringWidth(entity.name)
        val distanceWidth = Fonts.font35.getStringWidth("Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(entity))}")
        val pingWidth = mc.netHandler.getPlayerInfo(entity.uniqueID)?.let { Fonts.font35.getStringWidth("Ping: ${it.responseTime.coerceAtLeast(0)}") } ?: 0

        val maxWidth = (38 + nameWidth).coerceAtLeast(36 + distanceWidth).coerceAtLeast(36 + pingWidth).toFloat()

        return Border(0F, 0F, maxWidth, 36F)
    }
    
}