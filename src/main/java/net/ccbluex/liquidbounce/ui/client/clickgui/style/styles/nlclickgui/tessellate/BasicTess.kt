package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.tessellate

import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

open class BasicTess internal constructor(capacity: Int) : Tessellation {
    internal var index: Int = 0
    internal var raw: IntArray
    internal var buffer: ByteBuffer
    internal var fBuffer: FloatBuffer
    internal var iBuffer: IntBuffer
    private var colors = 0
    private var texU = 0f
    private var texV = 0f
    private var color = false
    private var texture = false

    init {
        var cap = capacity * 6
        raw = IntArray(cap)
        buffer = ByteBuffer.allocateDirect(cap * 4).order(ByteOrder.nativeOrder())
        fBuffer = buffer.asFloatBuffer()
        iBuffer = buffer.asIntBuffer()
    }

    override fun setColor(color: Int): Tessellation {
        this.color = true
        colors = color
        return this
    }

    override fun setTexture(u: Float, v: Float): Tessellation {
        texture = true
        texU = u
        texV = v
        return this
    }

    override fun addVertex(x: Float, y: Float, z: Float): Tessellation {
        val dex = index * 6
        raw[dex] = java.lang.Float.floatToRawIntBits(x)
        raw[dex + 1] = java.lang.Float.floatToRawIntBits(y)
        raw[dex + 2] = java.lang.Float.floatToRawIntBits(z)
        raw[dex + 3] = colors
        raw[dex + 4] = java.lang.Float.floatToRawIntBits(texU)
        raw[dex + 5] = java.lang.Float.floatToRawIntBits(texV)
        ++index
        return this
    }

    override fun bind(): Tessellation {
        val dex = index * 6
        iBuffer.put(raw, 0, dex)
        buffer.position(0)
        buffer.limit(dex * 4)
        if (color) {
            buffer.position(12)
            GL11.glColorPointer(4, true, 24, buffer)
        }
        if (texture) {
            fBuffer.position(4)
            GL11.glTexCoordPointer(2, 24, fBuffer)
        }
        fBuffer.position(0)
        GL11.glVertexPointer(3, 24, fBuffer)
        return this
    }

    override fun pass(mode: Int): Tessellation {
        GL11.glDrawArrays(mode, 0, index)
        return this
    }

    override fun unbind(): Tessellation {
        iBuffer.position(0)
        return this
    }

    override fun reset(): Tessellation {
        iBuffer.clear()
        index = 0
        color = false
        texture = false
        return this
    }
}
