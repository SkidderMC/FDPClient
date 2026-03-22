/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import kotlin.math.round

/**
 * Float Element - YZY GUI
 * @author opZywl
 */
class FloatElement(
    private val setting: FloatValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
) : NumericSliderElement(parent, x, y, width, height) {

    override val label: String
        get() = setting.name

    override val minimum: Float
        get() = setting.minimum

    override val maximum: Float
        get() = setting.maximum

    override val currentValue: Float
        get() = setting.get()

    override fun setValue(value: Float) {
        setting.set(value)
    }

    override fun formatValue(value: Float) = round(value).toInt().toString()

    override fun normalizeDraggedValue(value: Float) = round(value)
}
