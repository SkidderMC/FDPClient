/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font

import org.lwjgl.opengl.GL11.glDeleteLists

data class CachedFont(val displayList: Int, var lastUsage: Long, var deleted: Boolean = false) {
    protected fun finalize() {
        if (!deleted) {
            glDeleteLists(displayList, 1)
        }
    }
}