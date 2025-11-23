package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.config.Value
import net.minecraft.client.gui.Gui

abstract class Downward<V : Value<*>>(var setting: V, var moduleRender: NlModule) : Gui() {

    var x = 0f
    var y = 0f

    private var width = 0
    private var height = 0

    abstract fun draw(mouseX: Int, mouseY: Int)

    abstract fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int)

    open fun keyTyped(typedChar: Char, keyCode: Int) {}

    abstract fun mouseReleased(mouseX: Int, mouseY: Int, state: Int)

    fun getHeight(): Int = height

    fun getWidth(): Int = width

    fun setX(x: Int) {
        this.x = x.toFloat()
    }

    fun setY(y: Int) {
        this.y = y.toFloat()
    }

    fun getScrollY(): Int = moduleRender.scrollY
}
