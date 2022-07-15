package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.FontValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

class Novoline2(inst: Targets): TargetStyle("Novoline2", inst, true) {

    private val fontValue = FontValue("Font", Fonts.font40)
    private var easingHP = 0f

    override fun drawTarget(target: EntityLivingBase) {
        val font = fontValue.get()
        val color = ColorUtils.healthColor(getHealth(target), target.maxHealth)
        val darkColor = ColorUtils.darker(color, 0.6F)

        RenderUtils.drawRect(0F, 0F, 140F, 40F, Color(40, 40, 40).rgb)
        font.drawString(target.name, 35, 5, Color.WHITE.rgb)
        RenderUtils.drawHead(target.skin, 2, 2, 30, 30)
        RenderUtils.drawRect(35F, 17F, ((getHealth(target) / target.maxHealth) * 100) + 35F,
            35F, Color(252, 96, 66).rgb)

        font.drawString((decimalFormat.format((easingHP / target.maxHealth) * 100)) + "%", 40, 20, Color.WHITE.rgb)
    }

    override fun drawTarget(entity: EntityPlayer) {
        TODO("Not yet implemented")
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        TODO("Not yet implemented")
    }

    override fun getBorder(entity: EntityLivingBase?): Border? {
        entity ?: return Border(0F, 0F, 120F, 48F)
        val tWidth = (45F + Fonts.font40.getStringWidth(entity.name).coerceAtLeast(Fonts.font40.getStringWidth(decimalFormat.format(entity.health)))).coerceAtLeast(120F)
        return Border(0F, 0F, tWidth, 48F)
    }

}