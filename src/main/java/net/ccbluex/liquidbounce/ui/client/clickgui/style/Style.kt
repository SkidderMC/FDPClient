/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style

import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.file.FileManager.valuesConfig
import net.ccbluex.liquidbounce.ui.client.clickgui.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ButtonElement
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ModuleElement
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.asResourceLocation
import net.ccbluex.liquidbounce.utils.client.playSound
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import org.lwjgl.input.Mouse
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.max

abstract class Style : MinecraftInstance {
    protected var sliderValueHeld: Value<*>? = null
        get() {
            if (!Mouse.isButtonDown(0)) field = null
            return field
        }

    abstract fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel)
    abstract fun drawHoverText(mouseX: Int, mouseY: Int, text: String)
    abstract fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement)
    abstract fun drawModuleElementAndClick(mouseX: Int, mouseY: Int, moduleElement: ModuleElement, mouseButton: Int?): Boolean

    fun clickSound() {
        mc.playSound("gui.button.press".asResourceLocation())
    }

    fun showSettingsSound() {
        mc.playSound("random.bow".asResourceLocation())
    }

    protected fun round(v: Float): Float {
        var bigDecimal = BigDecimal(v.toString())
        bigDecimal = bigDecimal.setScale(2, 4)
        return bigDecimal.toFloat()
    }

    protected fun getHoverColor(color: Color, hover: Int, inactiveModule: Boolean = false): Int {
        val r = color.red - hover * 2
        val g = color.green - hover * 2
        val b = color.blue - hover * 2
        val alpha = if (inactiveModule) color.alpha.coerceAtMost(128) else color.alpha

        return Color(max(r, 0), max(g, 0), max(b, 0), alpha).rgb
    }

    fun <T> Value<T>.setAndSaveValueOnButtonRelease(new: T) {
        set(new, false)

        with(WaitTickUtils) {
            if (!hasScheduled(this)) {
                conditionalSchedule(this, 10) {
                    (sliderValueHeld == null).also { if (it) saveConfig(valuesConfig) }
                }
            }
        }
    }

    fun withDelayedSave(f: () -> Unit) {
        f()

        with(WaitTickUtils) {
            if (!hasScheduled(this)) {
                conditionalSchedule(this, 10) {
                    (sliderValueHeld == null).also { if (it) saveConfig(valuesConfig) }
                }
            }
        }
    }
}