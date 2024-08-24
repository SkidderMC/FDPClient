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

        val nameWidth = Fonts.minecraftFont.getStringWidth("[" + entity.name + "]")
        val healthWidth = Fonts.minecraftFont.getStringWidth("" + entity.health.toInt() + " §4❤" + EnumChatFormatting.RESET + '/' + entity.maxHealth.toInt() + " §4❤")

        val maxWidth = (36F + nameWidth).coerceAtLeast(38F + healthWidth).coerceAtLeast(90F)

        return Border(0F, 0F, maxWidth, 36F)
    }

}