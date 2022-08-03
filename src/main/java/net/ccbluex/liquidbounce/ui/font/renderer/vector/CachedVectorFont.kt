package net.ccbluex.liquidbounce.ui.font.renderer.vector

import net.ccbluex.liquidbounce.ui.font.renderer.AbstractCachedFont

class CachedVectorFont(val list: Int, val width: Int) : AbstractCachedFont(System.currentTimeMillis()) {
    override fun finalize() {
       // GL11.glDeleteLists(list, 1)
    }
}
