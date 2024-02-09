/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.EntityLivingBase
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.pow

abstract class TargetStyle(val name: String, val targetInstance: Targets, val shaderSupport: Boolean): MinecraftInstance() {

    var easingHealth = 0F

    val decimalFormat = DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH))
    val decimalFormat2 = DecimalFormat("##0.0", DecimalFormatSymbols(Locale.ENGLISH))
    val decimalFormat3 = DecimalFormat("0.#", DecimalFormatSymbols(Locale.ENGLISH))
    val easingHP = 0f
    var Health = 0F

    fun getHealth(entity: EntityLivingBase?): Float {
        return entity?.health ?: 0f
    }
    abstract fun drawTarget(entity: EntityLivingBase)
    abstract fun getBorder(entity: EntityLivingBase?): Border?

    open fun updateAnim(targetHealth: Float) {
        easingHealth += ((targetHealth - easingHealth) / 2.0F.pow(10.0F - targetInstance.globalAnimSpeed.get())) * RenderUtils.deltaTime
    }
    fun fadeAlpha(alpha: Int) : Int {
        return alpha - (targetInstance.getFadeProgress() * alpha).toInt()
    }

    /**
     * Get all values of element
     */
    open val values: List<Value<*>>
        get() = javaClass.declaredFields.map { valueField ->
            valueField.isAccessible = true
            valueField[this]
        }.filterIsInstance<Value<*>>()

    fun getColor(color: Color) = ColorUtils.reAlpha(color, color.alpha / 255F * (1F - targetInstance.getFadeProgress()))
    fun getColor(color: Int) = getColor(Color(color))

}