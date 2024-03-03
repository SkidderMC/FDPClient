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
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawHead
import net.minecraft.entity.EntityLivingBase

class SlowlyTH(inst: Targets) : TargetStyle("Slowly", inst, true) {

    override fun drawTarget(entity: EntityLivingBase) {
        val font = Fonts.minecraftFont
        val healthString = "${decimalFormat2.format(entity.health)} ❤"
        val length = 60.coerceAtLeast(font.getStringWidth(entity.name)).coerceAtLeast(font.getStringWidth(healthString))
            .toFloat() + 10F

        updateAnim(entity.health)

        RenderUtils.drawRect(0F, 0F, 32F + length, 36F, targetInstance.bgColor.rgb)

        if (mc.netHandler.getPlayerInfo(entity.uniqueID) != null)
            drawHead(
                mc.netHandler.getPlayerInfo(entity.uniqueID).locationSkin,
                1,
                1,
                30,
                30,
                (1F - targetInstance.getFadeProgress()).toInt()
            )

        font.drawStringWithShadow(entity.name, 33F, 2F, getColor(-1).rgb)
        font.drawStringWithShadow(
            healthString,
            length + 31F - font.getStringWidth(healthString).toFloat(),
            22F,
            targetInstance.barColor.rgb
        )

        RenderUtils.drawRect(
            0F,
            32F,
            (easingHealth / entity.maxHealth).coerceIn(0F, entity.maxHealth) * (length + 32F),
            36F,
            targetInstance.barColor.rgb
        )
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        entity ?: return Border(0F, 0F, 102F, 36F)
        val font = Fonts.minecraftFont
        val healthString = "${decimalFormat2.format(entity.health)} ❤"
        val length = 60.coerceAtLeast(font.getStringWidth(entity.name)).coerceAtLeast(font.getStringWidth(healthString))
            .toFloat() + 10F
        return Border(0F, 0F, 32F + length, 36F)
    }

}