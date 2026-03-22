/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11.*

/**
 * Unified Stencil utilities for managing OpenGL stencil buffer operations.
 *
 * This class consolidates all stencil-related functionality previously scattered across:
 * - utils/render/Stencil.java
 * - ui/client/clickgui/style/styles/fdpdropdown/utils/render/StencilUtil.java
 * - utils/render/RenderUtils.kt (partial implementation)
 *
 * @author Zywl
 */
object StencilUtils : MinecraftInstance {

    /**
     * Sets up the framebuffer with depth and stencil extensions (24/8 bit).
     * Deletes old render buffer and creates a new one with combined depth/stencil support.
     *
     * @param fbo The framebuffer to setup
     */
    @JvmStatic
    fun setupFBO(fbo: Framebuffer) {
        // Delete old render buffer extensions such as depth
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer)

        // Generate a new render buffer ID for the depth and stencil extension
        val stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT()

        // Bind new render buffer by ID
        EXTFramebufferObject.glBindRenderbufferEXT(
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencilDepthBufferID
        )

        // Add the depth and stencil extension with combined format
        EXTFramebufferObject.glRenderbufferStorageEXT(
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
            mc.displayWidth,
            mc.displayHeight
        )

        // Attach the stencil attachment
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencilDepthBufferID
        )

        // Attach the depth attachment
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencilDepthBufferID
        )
    }

    /**
     * Checks if the framebuffer needs setup and performs it if necessary.
     * This should be called before any stencil operations to ensure the FBO is ready.
     *
     * @param fbo The framebuffer to check, defaults to Minecraft's main framebuffer
     */
    @JvmStatic
    fun checkSetupFBO(fbo: Framebuffer? = mc.framebuffer) {
        if (fbo != null && fbo.depthBuffer > -1) {
            setupFBO(fbo)
            // Reset the ID to prevent multiple FBO setups
            fbo.depthBuffer = -1
        }
    }

    /**
     * Initializes the stencil buffer for writing.
     *
     * @param renderClipLayer Whether to render the clip layer visibly
     */
    @JvmStatic
    fun write(renderClipLayer: Boolean) {
        checkSetupFBO()
        glClearStencil(0)
        glClear(GL_STENCIL_BUFFER_BIT)
        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_ALWAYS, 1, 0xFFFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)

        if (!renderClipLayer) {
            GlStateManager.colorMask(false, false, false, false)
        }
    }

    /**
     * Advanced write method with full control over stencil operations.
     *
     * @param renderClipLayer Whether to render the clip layer visibly
     * @param fb The framebuffer to use
     * @param clearStencil Whether to clear the stencil buffer first
     * @param invert Whether to invert the stencil reference value
     */
    @JvmStatic
    fun write(renderClipLayer: Boolean, fb: Framebuffer, clearStencil: Boolean, invert: Boolean) {
        checkSetupFBO(fb)

        if (clearStencil) {
            glClearStencil(0)
            glClear(GL_STENCIL_BUFFER_BIT)
            glEnable(GL_STENCIL_TEST)
        }

        glStencilFunc(GL_ALWAYS, if (invert) 0 else 1, 0xFFFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)

        if (!renderClipLayer) {
            GlStateManager.colorMask(false, false, false, false)
        }
    }

    /**
     * Initializes the stencil buffer for writing (alternative API).
     * Binds the framebuffer and sets up stencil for masking operations.
     */
    @JvmStatic
    fun initStencilToWrite() {
        mc.framebuffer.bindFramebuffer(false)
        checkSetupFBO()
        glClear(GL_STENCIL_BUFFER_BIT)
        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_ALWAYS, 1, 1)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glColorMask(false, false, false, false)
    }

    /**
     * Configures the stencil buffer for reading/testing.
     * Only renders where the stencil value equals the reference value.
     *
     * @param ref The reference value to compare against (usually 1)
     */
    @JvmStatic
    fun readStencilBuffer(ref: Int) {
        glColorMask(true, true, true, true)
        glStencilFunc(GL_EQUAL, ref, 1)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
    }

    /**
     * Configures stencil for erasing or inverting the mask.
     *
     * @param invert Whether to invert the stencil test
     */
    @JvmStatic
    fun erase(invert: Boolean) {
        glStencilFunc(if (invert) GL_EQUAL else GL_NOTEQUAL, 1, 0xFFFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)
        GlStateManager.colorMask(true, true, true, true)
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        glAlphaFunc(GL_GREATER, 0.0f)
    }

    /**
     * Disables stencil testing and cleans up state.
     * Call this when done with stencil operations.
     */
    @JvmStatic
    fun dispose() {
        glDisable(GL_STENCIL_TEST)
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
    }

    /**
     * Disables stencil testing (alternative API).
     */
    @JvmStatic
    fun uninitStencilBuffer() {
        glDisable(GL_STENCIL_TEST)
    }

    /**
     * Clips rendering within a defined stencil region.
     * Useful for clipping any top-layered rectangle that falls outside a bottom-layered rectangle.
     *
     * @param main Lambda to render the clipping region
     * @param toClip Lambda to render content that should be clipped
     */
    @JvmStatic
    inline fun withClipping(main: () -> Unit, toClip: () -> Unit) {
        net.ccbluex.liquidbounce.utils.client.ClientUtils.disableFastRender()
        checkSetupFBO()
        glPushMatrix()

        GlStateManager.disableAlpha()

        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_ALWAYS, 1, 1)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glStencilMask(1)
        glClear(GL_STENCIL_BUFFER_BIT)

        main()

        glStencilFunc(GL_EQUAL, 1, 1)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glStencilMask(0)

        toClip()

        glStencilMask(0xFF)
        glDisable(GL_STENCIL_TEST)

        GlStateManager.enableAlpha()

        glPopMatrix()
    }

    /**
     * Creates an outline effect using stencil buffer.
     * Renders content, then renders an outline around it using inverted stencil test.
     *
     * @param main Lambda to render the main content
     * @param toOutline Lambda to render the outline effect
     */
    @JvmStatic
    inline fun withOutline(main: () -> Unit, toOutline: () -> Unit) {
        net.ccbluex.liquidbounce.utils.client.ClientUtils.disableFastRender()
        checkSetupFBO()
        glPushMatrix()

        GlStateManager.disableAlpha()

        glEnable(GL_STENCIL_TEST)
        glClear(GL_STENCIL_BUFFER_BIT)

        glStencilFunc(GL_ALWAYS, 1, 1)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)
        glStencilMask(1)

        main()

        glStencilFunc(GL_EQUAL, 0, 1)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glStencilMask(0)

        toOutline()

        glStencilMask(0xFF)
        glDisable(GL_STENCIL_TEST)

        GlStateManager.enableAlpha()

        glPopMatrix()
    }
}
