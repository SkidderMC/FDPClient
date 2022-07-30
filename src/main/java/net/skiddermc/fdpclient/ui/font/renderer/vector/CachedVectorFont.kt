package net.skiddermc.fdpclient.ui.font.renderer.vector

import net.skiddermc.fdpclient.ui.font.renderer.AbstractCachedFont
import org.lwjgl.opengl.GL11

class CachedVectorFont(val list: Int, val width: Int) : AbstractCachedFont(System.currentTimeMillis()) {
    override fun finalize() {
       // GL11.glDeleteLists(list, 1)
    }
}
