package net.ccbluex.liquidbounce.ui.font.renderer.vector

import net.ccbluex.liquidbounce.ui.font.renderer.AbstractCachedFont
import org.lwjgl.opengl.GL11

class CachedVectorFont(val displayList: Int, val width: Int) : AbstractCachedFont(System.currentTimeMillis()) {
    override fun finalize() {
        GL11.glDeleteLists(displayList, 1)
    }
}