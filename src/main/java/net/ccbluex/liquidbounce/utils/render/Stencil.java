/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render;

import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.EXTFramebufferObject;

import static org.lwjgl.opengl.GL11.*;

/**
 * The type Stencil.
 */
public class Stencil {

    /**
     * The Mc.
     */
    static Minecraft mc = MinecraftInstance.mc;

    /**
     * Dispose.
     */
    public static void dispose() {
        glDisable(GL_STENCIL_TEST);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    /**
     * Erase.
     *
     * @param invert the invert
     */
    public static void erase(boolean invert) {
        glStencilFunc(invert ? GL_EQUAL : GL_NOTEQUAL, 1, 65535);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        glAlphaFunc(GL_GREATER, 0.0f);
    }

    /**
     * Write.
     *
     * @param renderClipLayer the render clip layer
     */
    public static void write(boolean renderClipLayer) {
        Stencil.checkSetupFBO();
        glClearStencil(0);
        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);
        glStencilFunc(GL_ALWAYS, 1, 65535);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        if (!renderClipLayer)
            GlStateManager.colorMask(false, false, false, false);
    }

    /**
     * Write.
     *
     * @param renderClipLayer the render clip layer
     * @param fb              the fb
     * @param clearStencil    the clear stencil
     * @param invert          the invert
     */
    public static void write(boolean renderClipLayer, Framebuffer fb, boolean clearStencil, boolean invert) {
        Stencil.checkSetupFBO(fb);
        if (clearStencil) {
            glClearStencil(0);
            glClear(GL_STENCIL_BUFFER_BIT);
            glEnable(GL_STENCIL_TEST);
        }
        glStencilFunc(GL_ALWAYS, invert ? 0 : 1, 65535);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        if (!renderClipLayer)
            GlStateManager.colorMask(false, false, false, false);
    }

    /**
     * Check setup fbo.
     */
    public static void checkSetupFBO() {
        Framebuffer fbo = mc.getFramebuffer();
        if (fbo != null && fbo.depthBuffer > -1) {
            Stencil.setupFBO(fbo);
            fbo.depthBuffer = -1;
        }
    }

    /**
     * Check setup fbo.
     *
     * @param fbo the fbo
     */
    public static void checkSetupFBO(Framebuffer fbo) {
        if (fbo != null && fbo.depthBuffer > -1) {
            Stencil.setupFBO(fbo);
            fbo.depthBuffer = -1;
        }
    }

    /**
     * Sets fbo.
     *
     * @param fbo the fbo
     */
    public static void setupFBO(Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(36161, stencil_depth_buffer_ID);
        EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, mc.displayWidth, mc.displayHeight);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencil_depth_buffer_ID);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencil_depth_buffer_ID);
    }

}