/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.config.IntValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel

class IntegerElement(
    private val setting: IntValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
) : NumericSliderElement(parent, x, y, width, height) {

    override val label: String
        get() = setting.name

    override val minimum: Float
        get() = setting.minimum.toFloat()

    override val maximum: Float
        get() = setting.maximum.toFloat()

    override val currentValue: Float
        get() = setting.get().toFloat()

    override fun setValue(value: Float) {
        setting.set(value.toInt())
    }

    override fun formatValue(value: Float) = value.toInt().toString()
}
