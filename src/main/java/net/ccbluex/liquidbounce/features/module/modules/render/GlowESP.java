/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.features.value.IntegerValue;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.files.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.files.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.utils.MathUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.ShaderUtil;
import net.ccbluex.liquidbounce.features.value.BoolValue;
import net.ccbluex.liquidbounce.features.value.FloatValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.glUniform1;
@ModuleInfo(name = "GlowESP", category = ModuleCategory.RENDER)
public class GlowESP extends Module {

    public final FloatValue radius = new FloatValue("Radius", 2, 1F, 30F);
    public final FloatValue exposure = new FloatValue("Exposure", 2.2F, 1F, 3.5F);
    public final BoolValue seperate = new BoolValue("Seperate Texture", false);

    private final IntegerValue colorRedValue = new IntegerValue("Color-Red", 255, 0, 255);
    private final IntegerValue colorGreenValue = new IntegerValue("Color-Green", 255, 0, 255);
    private final IntegerValue colorBlueValue = new IntegerValue("Color-Blue", 255, 0, 255);
    public final BoolValue Players = new BoolValue("Players", false);
    public final BoolValue Animals = new BoolValue("Animals", false);
    public final BoolValue Mobs = new BoolValue("Mobs", false);

    public static boolean renderNameTags = true;
    private final ShaderUtil outlineShader = new ShaderUtil("shaders/outline.frag");
    private final ShaderUtil glowShader = new ShaderUtil("shaders/glow.frag");

    public Framebuffer framebuffer;
    public Framebuffer outlineFrameBuffer;
    public Framebuffer glowFrameBuffer;
    private final Frustum frustum2 = new Frustum();

    private final List<Entity> entities = new ArrayList<>();

    public static Animation fadeIn;
    
    private boolean isInView(Entity ent) {
        frustum2.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        return frustum2.isBoundingBoxInFrustum(ent.getEntityBoundingBox()) || ent.ignoreFrustumCheck;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        fadeIn = new DecelerateAnimation(250, 1);
    }

    public void createFrameBuffers() {
        framebuffer = RenderUtils.createFrameBuffer(framebuffer);
        outlineFrameBuffer = RenderUtils.createFrameBuffer(outlineFrameBuffer);
        glowFrameBuffer = RenderUtils.createFrameBuffer(glowFrameBuffer);
    }


    @EventTarget
    public void onrender3D(final Render3DEvent event) {
        createFrameBuffers();
        collectEntities();
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        renderEntities(event.getPartialTicks());
        framebuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.disableLighting();
    }

    @EventTarget
    public void onrender2D(final Render2DEvent event) {

        ScaledResolution sr = new ScaledResolution(mc);
        if (framebuffer != null && outlineFrameBuffer != null && entities.size() > 0) {
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.0f);
            GlStateManager.enableBlend();
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            outlineFrameBuffer.framebufferClear();
            outlineFrameBuffer.bindFramebuffer(true);
            outlineShader.init();
            setupOutlineUniforms(0, 1);
            RenderUtils.bindTexture(framebuffer.framebufferTexture);
            ShaderUtil.drawQuads();
            outlineShader.init();
            setupOutlineUniforms(1, 0);
            RenderUtils.bindTexture(framebuffer.framebufferTexture);
            ShaderUtil.drawQuads();
            outlineShader.unload();
            outlineFrameBuffer.unbindFramebuffer();

            GlStateManager.color(1, 1, 1, 1);
            glowFrameBuffer.framebufferClear();
            glowFrameBuffer.bindFramebuffer(true);
            glowShader.init();
            setupGlowUniforms(1, 0);
            RenderUtils.bindTexture(outlineFrameBuffer.framebufferTexture);
            ShaderUtil.drawQuads();
            glowShader.unload();
            glowFrameBuffer.unbindFramebuffer();

            mc.getFramebuffer().bindFramebuffer(true);
            glowShader.init();
            setupGlowUniforms(0, 1);
            if (seperate.get()) {
                GL13.glActiveTexture(GL13.GL_TEXTURE16);
                RenderUtils.bindTexture(framebuffer.framebufferTexture);
            }
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            RenderUtils.bindTexture(glowFrameBuffer.framebufferTexture);
            ShaderUtil.drawQuads();
            glowShader.unload();

        }

    }


    public void setupGlowUniforms(float dir1, float dir2) {
        Color color = getColor();
        glowShader.setUniformi("texture", 0);
        if (seperate.get()) {
            glowShader.setUniformi("textureToCheck", 16);
        }
        glowShader.setUniformf("radius", radius.get());
        glowShader.setUniformf("texelSize", 1.0f / mc.displayWidth, 1.0f / mc.displayHeight);
        glowShader.setUniformf("direction", dir1, dir2);
        glowShader.setUniformf("color", colorRedValue.get(),  colorGreenValue.get(),  colorBlueValue.get());
        glowShader.setUniformf("exposure", (float) (exposure.get() * fadeIn.getOutput()));
        glowShader.setUniformi("avoidTexture", seperate.get() ? 1 : 0);

        final FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
        for (int i = 1; i <= radius.getValue(); i++) {
            buffer.put(MathUtils.INSTANCE.calculateGaussianValue(i, radius.get() / 2));
        }
        buffer.rewind();

        glUniform1(glowShader.getUniform("weights"), buffer);
    }


    public void setupOutlineUniforms(float dir1, float dir2) {
        Color color = getColor();
        outlineShader.setUniformi("texture", 0);
        outlineShader.setUniformf("radius", radius.get() / 1.5f);
        outlineShader.setUniformf("texelSize", 1.0f / mc.displayWidth, 1.0f / mc.displayHeight);
        outlineShader.setUniformf("direction", dir1, dir2);
        outlineShader.setUniformf("color", colorRedValue.get(),  colorGreenValue.get(),  colorBlueValue.get());
    }

    public void renderEntities(float ticks) {
        entities.forEach(entity -> {
            renderNameTags = false;
            mc.getRenderManager().renderEntityStatic(entity, ticks, false);
            renderNameTags = true;
        });
    }

    private Color getColor() {
        final HUD hudMod = FDPClient.moduleManager.getModule(HUD.class);
        Color[] colors = hudMod.getClientColors();
        if (hudMod.getMovingcolors().get()) {
            return colors[0];
        } else {
            return ColorUtils.interpolateColorsBackAndForth(15, 0, colors[0], colors[1], hudMod.getHueInterpolation().get());
        }
    }

    public void collectEntities() {
        entities.clear();
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (!isInView(entity)) continue;
            if (entity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) continue;
            if (entity instanceof EntityAnimal && Animals.get()) {
                entities.add(entity);
            }

            if (entity instanceof EntityPlayer && Players.get()) {
                entities.add(entity);
            }

            if (entity instanceof EntityMob && Mobs.get()) {
                entities.add(entity);
            }
        }
    }

}
