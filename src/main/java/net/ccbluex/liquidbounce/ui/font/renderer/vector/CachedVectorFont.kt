package net.ccbluex.liquidbounce.ui.font.renderer.vector

import net.ccbluex.liquidbounce.ui.font.renderer.AbstractCachedFont
import net.ccbluex.liquidbounce.utils.render.glu.tess.CacheTessCallback

class CachedVectorFont(val vc: CacheTessCallback.VertexCache, val width: Int) : AbstractCachedFont(System.currentTimeMillis()) {
    override fun finalize() {
        vc.destroy()
    }
}