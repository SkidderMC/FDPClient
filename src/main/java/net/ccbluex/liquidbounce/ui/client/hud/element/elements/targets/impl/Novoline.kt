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
import net.ccbluex.liquidbounce.utils.render.Animation
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.EntityLivingBase
import java.awt.Color
import kotlin.math.roundToInt

class Novoline(inst: Targets): TargetStyle("Novoline", inst, true) {

    override var hpEaseAnimation: Animation? = null
    override var easingHP = 0f
    private var ease = 0f
        get() {
            if (hpEaseAnimation != null) {
                field = hpEaseAnimation!!.value.toFloat()
                if (hpEaseAnimation!!.state == Animation.EnumAnimationState.STOPPED) {
                    hpEaseAnimation = null
                }
            }
            return field
        }
        set(value) {
            if (hpEaseAnimation == null || (hpEaseAnimation != null && hpEaseAnimation!!.to != value.toDouble())) {
                hpEaseAnimation = Animation(EaseUtils.EnumEasingType.valueOf(hpAnimTypeValue.get()), EaseUtils.EnumEasingOrder.valueOf(hpAnimOrderValue.get()), field.toDouble(), value.toDouble(), animSpeedValue.get() * 100L).start()
            }
        }

    override fun drawTarget(target: EntityLivingBase) {
        val font = fontValue.get()
        val color = ColorUtils.healthColor(getHealth(target), target.maxHealth)
        val darkColor = ColorUtils.darker(color, 0.6F)
        val hpPos = 33F + ((getHealth(target) / target.maxHealth * 10000).roundToInt() / 100)

        RenderUtils.drawRect(0F, 0F, 140F, 40F, Color(40, 40, 40).rgb)
        font.drawString(target.name, 33, 5, Color.WHITE.rgb)
        RenderUtils.drawEntityOnScreen(20, 35, 15, target)
        RenderUtils.drawRect(hpPos, 18F, 33F + ((easingHP / target.maxHealth * 10000).roundToInt() / 100), 25F, darkColor)
        RenderUtils.drawRect(33F, 18F, hpPos, 25F, color)
        font.drawString("❤", 33, 30, Color.RED.rgb)
        font.drawString(decimalFormat.format(getHealth(target)), 43, 30, Color.WHITE.rgb)
    }

    override fun getHealth(entity: EntityLivingBase?): Float {
        return entity?.health ?: 0f
    }


    override fun getBorder(entity:EntityLivingBase?): Border {
        return Border(0F, 0F, 140F, 40F)
    }

}