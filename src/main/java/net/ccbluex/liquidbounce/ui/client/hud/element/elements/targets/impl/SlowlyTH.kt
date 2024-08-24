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
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.EntityLivingBase

class SlowlyTH(inst: Targets) : TargetStyle("Slowly", inst, true) {

    override fun drawTarget(entity: EntityLivingBase) {
        val font = Fonts.minecraftFont
        val healthString = "${decimalFormat2.format(entity.health)} ❤"
        val length = maxOf(
            60,
            font.getStringWidth(entity.name),
            font.getStringWidth(healthString)
        ).toFloat() + 10F

        updateAnim(entity.health)

        RenderUtils.drawRect(0F, 0F, 32F + length, 36F, targetInstance.bgColor.rgb)

        val playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)
        playerInfo?.locationSkin?.let { skinLocation ->
            drawHead(skinLocation, 1, 1, 30, 30, (1F - targetInstance.getFadeProgress()).toInt().toFloat())
        }

        font.drawStringWithShadow(entity.name, 33F, 2F, getColor(-1).rgb)
        font.drawStringWithShadow(
            healthString,
            length + 31F - font.getStringWidth(healthString).toFloat(),
            22F,
            targetInstance.barColor.rgb
        )

        val healthBarWidth = (easingHealth / entity.maxHealth)
            .coerceIn(0F, 1F) * (length + 32F)
        RenderUtils.drawRect(0F, 32F, healthBarWidth, 36F, targetInstance.barColor.rgb)
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        val font = Fonts.minecraftFont
        val length = entity?.let {
            val healthString = "${decimalFormat2.format(it.health)} ❤"
            maxOf(
                60,
                font.getStringWidth(it.name),
                font.getStringWidth(healthString)
            ).toFloat() + 10F
        } ?: 102F
        return Border(0F, 0F, 32F + length, 36F)
    }
}
