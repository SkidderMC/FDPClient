/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.ClientUtils.mc
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11.*

object StencilUtils {

    /**
     * Initializes Stencil
     */
    fun initStencil(fbo: Framebuffer) {
        fbo.bindFramebuffer(false)
        checkSetupFBO(fbo)

        glClear(GL_STENCIL_BUFFER_BIT)
        glEnable(GL_STENCIL_TEST)
    }

    /**
     * Uninitializes Stencil
     */
    fun uninitStencil() = glDisable(GL_STENCIL_TEST)

    /**
     * Writes to Stencil
     */
    fun writeToStencil() {
        glColorMask(false, false, false, false)
        glStencilFunc(GL_ALWAYS, 1, 1)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
    }

    /**
     * Reads from Stencil
     */
    fun readFromStencil() {
        glColorMask(true, true, true, true)
        glStencilFunc(GL_EQUAL, 1, 1)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
    }

    private fun checkSetupFBO(fbo: Framebuffer) {

        // Checks if screen has been resized or new FBO has been created
        if (fbo.depthBuffer > -1) {

            // Sets up the FBO with depth and stencil extensions (24/8 bit)
            setupFBO(fbo)

            // Reset the ID to prevent multiple FBO's
            fbo.depthBuffer = -1
        }
    }

    /**
     * Sets up the FBO with depth and stencil
     *
     * @param fbo Framebuffer
     */
    private fun setupFBO(fbo: Framebuffer) {

        // Deletes old render buffer extensions such as depth
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer)

        // Generates a new render buffer ID for the depth and stencil extension
        val depthBufferId = EXTFramebufferObject.glGenRenderbuffersEXT()

        // Binds new render buffer by ID
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthBufferId)

        // Adds the depth and stencil extension
        EXTFramebufferObject.glRenderbufferStorageEXT(
            EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
            mc.displayWidth, mc.displayHeight
        )

        // Adds the stencil attachment
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthBufferId
        )

        // Adds the depth attachment
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthBufferId
        )
    }
}