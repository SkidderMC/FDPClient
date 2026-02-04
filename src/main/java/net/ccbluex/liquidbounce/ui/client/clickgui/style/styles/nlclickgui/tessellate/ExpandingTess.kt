package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.tessellate

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ExpandingTess internal constructor(initial: Int, private val ratio: Float, private val factor: Float) : BasicTess(initial) {
    override fun addVertex(x: Float, y: Float, z: Float): Tessellation {
        var capacity = raw.size
        if ((index * 6).toFloat() >= capacity.toFloat() * ratio) {
            capacity = (capacity.toFloat() * factor).toInt()
            val newBuffer = IntArray(capacity)
            System.arraycopy(raw, 0, newBuffer, 0, raw.size)
            raw = newBuffer
            buffer = ByteBuffer.allocateDirect(capacity * 4).order(ByteOrder.nativeOrder())
            iBuffer = buffer.asIntBuffer()
            fBuffer = buffer.asFloatBuffer()
        }
        return super.addVertex(x, y, z)
    }
}
