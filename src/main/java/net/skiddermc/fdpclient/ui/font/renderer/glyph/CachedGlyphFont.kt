package net.skiddermc.fdpclient.ui.font.renderer.glyph

import net.skiddermc.fdpclient.ui.font.renderer.AbstractCachedFont
import org.lwjgl.opengl.GL11

class CachedGlyphFont(val tex: Int, val width: Int) : AbstractCachedFont(System.currentTimeMillis()) {
    override fun finalize() {
        // GL11.glDeleteTextures(tex)
    }
}
