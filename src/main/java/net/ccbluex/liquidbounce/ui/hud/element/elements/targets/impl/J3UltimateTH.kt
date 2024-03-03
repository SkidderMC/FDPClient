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
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumChatFormatting

class J3UltimateTH(inst: Targets) : TargetStyle("J3Ultimate", inst, true) {

    override fun drawTarget(entity: EntityLivingBase) {
        val targetHealth = entity.health.toInt()
        val targetMaxHealth = entity.maxHealth.toInt()
        Fonts.minecraftFont.drawString("[" + entity.name + "]", 36.0f, 3.0f, 0xFFFFFF, false)
        Fonts.minecraftFont.drawString(
            "" + targetHealth + " §4❤" + EnumChatFormatting.RESET + '/' + targetMaxHealth + " §4❤",
            38,
            12,
            0xFFFFFF
        )
    }
    override fun getBorder(entity: EntityLivingBase?): Border {
        entity ?: return Border(0F, 0F, 90F, 36F)
        val tWidth = (45F + Fonts.font40.getStringWidth(entity.name).coerceAtLeast(Fonts.font72.getStringWidth(decimalFormat.format(entity.health)))).coerceAtLeast(90F)
        return Border(0F, 0F, tWidth, 36F)
    }

}