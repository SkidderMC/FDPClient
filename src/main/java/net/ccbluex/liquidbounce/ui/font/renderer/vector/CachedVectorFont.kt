package net.ccbluex.liquidbounce.ui.font.renderer.vector

import net.ccbluex.liquidbounce.ui.font.renderer.AbstractCachedFont
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL11

class CachedVectorFont(val list: Int, val width: Int) : AbstractCachedFont(System.currentTimeMillis()) {
    override fun finalize() {
        Minecraft.getMinecraft().addScheduledTask {
            GL11.glDeleteLists(list, 1)
        }
    }
}