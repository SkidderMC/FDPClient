package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.tessellate

import java.awt.Color

interface Tessellation {
    fun setColor(color: Int): Tessellation

    fun setColor(color: Color): Tessellation {
        return setColor(Color(255, 255, 255).rgb)
    }

    fun setTexture(u: Float, v: Float): Tessellation

    fun addVertex(x: Float, y: Float, z: Float): Tessellation

    fun bind(): Tessellation

    fun pass(mode: Int): Tessellation

    fun reset(): Tessellation

    fun unbind(): Tessellation

    fun draw(mode: Int): Tessellation {
        return bind().pass(mode).reset()
    }

    companion object {
        @JvmStatic
        fun createBasic(size: Int): Tessellation = BasicTess(size)

        @JvmStatic
        fun createExpanding(size: Int, ratio: Float, factor: Float): Tessellation = ExpandingTess(size, ratio, factor)
    }
}
