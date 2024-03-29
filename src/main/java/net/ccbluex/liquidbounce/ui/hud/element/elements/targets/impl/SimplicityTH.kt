/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.hud.element.Border
import net.ccbluex.liquidbounce.ui.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.PlayerUtils
import net.ccbluex.liquidbounce.utils.render.Colors
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import kotlin.math.pow

class SimplicityTH(inst: Targets) : TargetStyle("Simplicity", inst, true) {

    override fun drawTarget(entity: EntityLivingBase) {
        updateAnim(entity.health)
        GlStateManager.pushMatrix()
        var width = 100.0
        width = PlayerUtils.getIncremental(width, -50.0)
        Fonts.font35.drawStringWithShadow("\u00a7l" + entity.name, (38).toFloat(), 2.0f, -1)
        if (width < 80.0) {
            width = 80.0
        }
        if (width > 80.0) {
            width = 80.0
        }
        RenderUtils.drawGradientSideways(
            37.5,
            11.toDouble(),
            37.5 + (easingHealth / entity.maxHealth) * width,
            (19).toDouble(),
            ClientTheme.setColor("START", fadeAlpha(255)).rgb,
            ClientTheme.setColor("END", fadeAlpha(255)).rgb
        )
        RenderUtils.rectangleBorderedx(
            37.0,
            10.5,
            38.0 + (easingHealth / entity.maxHealth) * width,
            19.5,
            0.5,
            Colors.getColor(0, 0),
            Colors.getColor(0)
        )
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
    }

    override fun getBorder(entity: EntityLivingBase?): Border {
        return Border(37F, 0F, 118F, 20F)
    }
}