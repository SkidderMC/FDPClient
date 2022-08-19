package net.ccbluex.liquidbounce.ui.font.renderer.glyph

import net.ccbluex.liquidbounce.ui.font.renderer.AbstractCachedFont

class CachedGlyphFont(val tex: Int, val width: Int) : AbstractCachedFont(System.currentTimeMillis()) {
    override fun finalize() {
        // GL11.glDeleteTextures(tex)
    }
}
