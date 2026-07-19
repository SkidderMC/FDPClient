/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import org.lwjgl.opengl.GL11.GL_RENDERER
import org.lwjgl.opengl.GL11.GL_VENDOR
import org.lwjgl.opengl.GL11.GL_VERSION
import org.lwjgl.opengl.GL11.glGetString

object GlEnvironment {

    @JvmStatic
    val isGlEsWrapper by lazy {
        runCatching {
            val version = glGetString(GL_VERSION).orEmpty()
            val renderer = glGetString(GL_RENDERER).orEmpty()
            val vendor = glGetString(GL_VENDOR).orEmpty()

            version.contains("gl4es", true) ||
                renderer.contains("gl4es", true) ||
                vendor.contains("PojavLauncher", true) ||
                version.contains("OpenGL ES", true)
        }.getOrDefault(false)
    }
}
