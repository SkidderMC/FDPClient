/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FontValue
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

class Arris(inst: Targets): TargetStyle("Arris", inst, true) {

    val arrisRoundedValue = BoolValue("ArrisRounded", true).displayable { targetInstance.plusValue.get().equals("arris", true) }

    private var easingHP = 0f
    private val fontValue = FontValue("Font", Fonts.font40)

    override fun drawTarget(target: EntityPlayer) {
        val font = fontValue.get()

        val hp = decimalFormat.format(easingHP)
        val additionalWidth = font.getStringWidth("${target.name}  $hp hp").coerceAtLeast(75)
        if(arrisRoundedValue.get()){
            RenderUtils.drawRoundedCornerRect(0f, 0f, 45f + additionalWidth, 40f, 7f, Color(0, 0, 0, 110).rgb)
        } else {
            RenderUtils.drawRect(0f, 0f, 45f + additionalWidth, 1f, ColorUtils.rainbow())
            RenderUtils.drawRect(0f, 1f, 45f + additionalWidth, 40f, Color(0, 0, 0, 110).rgb)
        }

        RenderUtils.drawHead(target.skin, 5, 5, 30, 30)

        // info text
        font.drawString(target.name, 40, 5, Color.WHITE.rgb)
        "$hp hp".also {
            font.drawString(it, 40 + additionalWidth - font.getStringWidth(it), 5, Color.LIGHT_GRAY.rgb)
        }

        // hp bar
        val yPos = 5 + font.FONT_HEIGHT + 3f
        RenderUtils.drawRect(40f, yPos, 40 + (easingHP / target.maxHealth) * additionalWidth, yPos + 4, Color.GREEN.rgb)
        RenderUtils.drawRect(40f, yPos + 9, 40 + (target.totalArmorValue / 20F) * additionalWidth, yPos + 13, Color(77, 128, 255).rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        entity ?: return Border(0F, 0F, 120F, 48F)
        val tWidth = (45F + Fonts.font40.getStringWidth(entity.name).coerceAtLeast(Fonts.font40.getStringWidth(decimalFormat.format(entity.health)))).coerceAtLeast(120F)
        return Border(0F, 0F, tWidth, 48F)
    }

}