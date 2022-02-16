package net.ccbluex.liquidbounce.ui.font.renderer.glyph

import net.ccbluex.liquidbounce.ui.font.renderer.AbstractCachedFont
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL11

class CachedGlyphFont(val tex: Int, val width: Int) : AbstractCachedFont(System.currentTimeMillis()) {
    override fun finalize() {
        // make sure we are on the main thread
        Minecraft.getMinecraft().addScheduledTask {
            GL11.glDeleteTextures(tex)
        }
    }
}