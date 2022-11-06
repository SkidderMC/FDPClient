/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element

import net.ccbluex.liquidbounce.injection.access.StaticStorage
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.BlurUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.Value
import net.minecraft.entity.player.EntityPlayer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * CustomHUD element
 */
abstract class Element(
    var x: Double = 2.0,
    var y: Double = 2.0,
    var scale: Float = 1F,
    var side: Side = Side.default()
) : MinecraftInstance() {

    val info = javaClass.getAnnotation(ElementInfo::class.java)
            ?: throw IllegalArgumentException("Passed element with missing element info")

    val name: String
        get() = info.name

    var renderX: Double
        get() = when (side.horizontal) {
            Side.Horizontal.LEFT -> x
            Side.Horizontal.MIDDLE -> (StaticStorage.scaledResolution.scaledWidth / 2) - x
            Side.Horizontal.RIGHT -> StaticStorage.scaledResolution.scaledWidth - x
        }
        set(value) = when (side.horizontal) {
            Side.Horizontal.LEFT -> {
                x += value
            }
            Side.Horizontal.MIDDLE, Side.Horizontal.RIGHT -> {
                x -= value
            }
        }

    var renderY: Double
        get() = when (side.vertical) {
            Side.Vertical.UP -> y
            Side.Vertical.MIDDLE -> (StaticStorage.scaledResolution.scaledHeight / 2) - y
            Side.Vertical.DOWN -> StaticStorage.scaledResolution.scaledHeight - y
        }
        set(value) = when (side.vertical) {
            Side.Vertical.UP -> {
                y += value
            }
            Side.Vertical.MIDDLE, Side.Vertical.DOWN -> {
                y -= value
            }
        }

    var border: Border? = null

    var drag = false
    var prevMouseX = 0F
    var prevMouseY = 0F

    protected open val blurValue = FloatValue("Blur", 0f, 0f, 100f).displayable { info.blur }

    /**
     * Get all values of element
     */
    open val values: List<Value<*>>
        get() = ClassUtils.getValues(this.javaClass, this).toMutableList().also { it.add(blurValue) }

    /**
     * Called when element created
     */
    open fun createElement() = true

    /**
     * Called when element destroyed
     */
    open fun destroyElement() {}

    /**
     * Draw element
     */
    abstract fun drawElement(partialTicks: Float): Border?

    /**
     * Update element
     */
    open fun updateElement() {}

    /**
     * Check if [x] and [y] is in element border
     */
    open fun isInBorder(x: Double, y: Double): Boolean {
        val border = border ?: return false

        val minX = min(border.x, border.x2)
        val minY = min(border.y, border.y2)

        val maxX = max(border.x, border.x2)
        val maxY = max(border.y, border.y2)

        return minX <= x && minY <= y && maxX >= x && maxY >= y
    }

    /**
     * render a blur effect at the boarder
     */
    open fun drawBoarderBlur(blurRadius: Float = blurValue.get()) {
        if (border == null || blurRadius == 0f || border!!.size == 0f) {
            return
        }

        val posX = this.renderX.toFloat() + border!!.x.coerceAtMost(border!!.x2)
        val posY = this.renderY.toFloat() + border!!.y.coerceAtMost(border!!.y2)
        val width = abs(border!!.x2 - border!!.x)
        val height = abs(border!!.y2 - border!!.y)

        BlurUtils.draw(posX * scale, posY * scale, width * scale, height * scale, blurRadius)
    }

    protected fun blur(x: Float, y: Float, x2: Float, y2: Float) {
        if (blurValue.get() == 0f) {
            return
        }

        BlurUtils.draw((renderX + (x.coerceAtMost(x2))).toFloat() * scale, (renderY + (y.coerceAtMost(y2))).toFloat() * scale, abs(x2 - x) * scale, abs(y2 - y) * scale, blurValue.get())
    }

    /**
     * Called when mouse clicked
     */
    open fun handleMouseClick(x: Double, y: Double, mouseButton: Int) {}

    /**
     * Called when key pressed
     */
    open fun handleKey(c: Char, keyCode: Int) {}

    /**
     * Called when damage sound received
     */
    open fun handleDamage(ent: EntityPlayer) {}
}

/**
 * Element info
 */
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ElementInfo(val name: String, val blur: Boolean = false, val single: Boolean = false, val force: Boolean = false, val disableScale: Boolean = false, val priority: Int = 0, val retrieveDamage: Boolean = false)

/**
 * CustomHUD Side
 *
 * Allows to change default x and y position by side
 */
class Side(var horizontal: Horizontal, var vertical: Vertical) {

    companion object {

        /**
         * Default element side
         */
        fun default() = Side(Horizontal.LEFT, Vertical.UP)
    }

    /**
     * Horizontal side
     */
    enum class Horizontal(val sideName: String) {

        LEFT("Left"),
        MIDDLE("Middle"),
        RIGHT("Right");

        companion object {
            fun getByName(name: String) = values().find { it.sideName == name }
        }
    }

    /**
     * Vertical side
     */
    enum class Vertical(val sideName: String) {

        UP("Up"),
        MIDDLE("Middle"),
        DOWN("Down");

        companion object {
            fun getByName(name: String) = values().find { it.sideName == name }
        }
    }
}

/**
 * Border of element
 */
data class Border(val x: Float, val y: Float, val x2: Float, val y2: Float) {

    val size = abs(x2 - x) * abs(y2 - y)

    fun draw() = RenderUtils.drawBorderedRect(x, y, x2, y2, 3F, Int.MIN_VALUE, 0)
}