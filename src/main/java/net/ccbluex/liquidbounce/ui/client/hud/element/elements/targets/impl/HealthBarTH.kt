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
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.EntityLivingBase
import java.awt.Color

class HealthBarTH(inst: Targets) : TargetStyle("Bar", inst, true) {

    override fun drawTarget(target: EntityLivingBase) {
        Health = easingHP

        val width = (38 + Fonts.font40.getStringWidth(target.name))
            .coerceAtLeast(119)
            .toFloat()

        RenderUtils.drawBorderedRect(3F, 37F, 115F, 42F, 4.2F, Color(16, 16, 16, 255).rgb, Color(10, 10, 10, 100).rgb)
        RenderUtils.drawBorderedRect(3F, 37F, 115F, 42F, 1.2F, Color(255, 255, 255, 180).rgb, Color(255, 180, 255, 0).rgb)
        if (Health > getHealth(target))
            RenderUtils.drawRect(3F, 37F, (Health / target.maxHealth) * width - 4F,
                42F, Color(250, 0, 0, 120).rgb)

        RenderUtils.drawRect(3.2F, 37F, (getHealth(target) / target.maxHealth) * width - 4F,
            42F, Color(220, 0, 0, 220).rgb)
        if (Health < target.health)
            RenderUtils.drawRect((Health / target.maxHealth) * width, 37F,
                (getHealth(target) / target.maxHealth) * width, 42F, Color(44, 201, 144).rgb)
        RenderUtils.drawBorderedRect(3F, 37F, 115F, 42F, 1.2F, Color(255, 255, 255, 180).rgb, Color(255, 180, 255, 0).rgb)

        mc.fontRendererObj.drawStringWithShadow(target.name.toString(), 36F, 22F, 0xFFFFFF)
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        return Border(3F,22F, 115F + Fonts.SFApple40.getStringWidth(entity!!.name), 42F)
    }

}