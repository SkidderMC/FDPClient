package net.ccbluex.liquidbounce.ui.font.renderer.glyph

import net.ccbluex.liquidbounce.ui.font.renderer.AbstractCachedFont
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation

class CachedGlyphFont(val resourceLocation: ResourceLocation, val width: Int) : AbstractCachedFont(System.currentTimeMillis()) {
    override fun finalize() {
        val mc = Minecraft.getMinecraft()
        mc.addScheduledTask {
            mc.textureManager.deleteTexture(resourceLocation)
        }
    }
}