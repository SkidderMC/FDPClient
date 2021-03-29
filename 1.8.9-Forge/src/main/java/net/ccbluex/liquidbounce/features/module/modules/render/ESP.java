/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.script.api.global.Chat;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.WorldToScreen;
import net.ccbluex.liquidbounce.utils.render.shader.FramebufferShader;
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader;
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Timer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;

import static net.ccbluex.liquidbounce.utils.render.WorldToScreen.getMatrix;
import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "ESP", description = "Allows you to see targets through walls.", category = ModuleCategory.RENDER)
public class ESP extends Module {
    public static boolean renderNameTags = true;
    public final ListValue modeValue = new ListValue("Mode", new String[]{"Box", "OtherBox", "WireFrame", "2D", "CSGO", "Outline", "ShaderOutline", "ShaderGlow"}, "Box");
    public final FloatValue outlineWidth = new FloatValue("Outline-Width", 3F, 0.5F, 5F);
    public final FloatValue wireframeWidth = new FloatValue("WireFrame-Width", 2F, 0.5F, 5F);
    private final FloatValue shaderOutlineRadius = new FloatValue("ShaderOutline-Radius", 1.35F, 1F, 2F);
    private final FloatValue shaderGlowRadius = new FloatValue("ShaderGlow-Radius", 2.3F, 2F, 3F);
    public final FloatValue CSGOWidth = new FloatValue("CSGO-Width", 2F, 0.5F, 5F);
    private final BoolValue CSGONameTag = new BoolValue("CSGO-NameTag", true);
    private final IntegerValue colorRedValue = new IntegerValue("R", 255, 0, 255);
    private final IntegerValue colorGreenValue = new IntegerValue("G", 255, 0, 255);
    private final IntegerValue colorBlueValue = new IntegerValue("B", 255, 0, 255);
    private final BoolValue colorRainbow = new BoolValue("Rainbow", false);
    private final BoolValue colorTeam = new BoolValue("Team", false);

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        final String mode = modeValue.get();

        Matrix4f mvMatrix = getMatrix(GL11.GL_MODELVIEW_MATRIX);
        Matrix4f projectionMatrix = getMatrix(GL11.GL_PROJECTION_MATRIX);

        boolean csgo = mode.equalsIgnoreCase("csgo");

        if (csgo) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glOrtho(0, mc.displayWidth, mc.displayHeight, 0, -1.0f, 1.0);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();

            glDisable(GL_DEPTH_TEST);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);

            GL11.glLineWidth(1.0f);
        }

        for (final Entity entity : mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity, true)) {
                final EntityLivingBase entityLiving = (EntityLivingBase) entity;

                Color color = getColor(entityLiving);

                switch (mode.toLowerCase()) {
                    case "box":
                    case "otherbox":
                        RenderUtils.drawEntityBox(entity, color, !mode.equalsIgnoreCase("otherbox"));
                        break;
                    case "2d": {
                        final RenderManager renderManager = mc.getRenderManager();
                        final Timer timer = mc.timer;

                        final double posX = entityLiving.lastTickPosX + (entityLiving.posX - entityLiving.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX;
                        final double posY = entityLiving.lastTickPosY + (entityLiving.posY - entityLiving.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY;
                        final double posZ = entityLiving.lastTickPosZ + (entityLiving.posZ - entityLiving.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ;

                        RenderUtils.draw2D(entityLiving, posX, posY, posZ, color.getRGB(), Color.BLACK.getRGB());
                        break;
                    }
                    case "csgo": {
                        final RenderManager renderManager = mc.getRenderManager();
                        final Timer timer = mc.timer;

                        AxisAlignedBB bb = entityLiving.getEntityBoundingBox()
                                .offset(-entityLiving.posX, -entityLiving.posY, -entityLiving.posZ)
                                .offset(entityLiving.lastTickPosX + (entityLiving.posX - entityLiving.lastTickPosX) * timer.renderPartialTicks,
                                        entityLiving.lastTickPosY + (entityLiving.posY - entityLiving.lastTickPosY) * timer.renderPartialTicks,
                                        entityLiving.lastTickPosZ + (entityLiving.posZ - entityLiving.lastTickPosZ) * timer.renderPartialTicks)
                                .offset(-renderManager.renderPosX, -renderManager.renderPosY, -renderManager.renderPosZ);

                        double[][] boxVertices = {
                                {bb.minX, bb.minY, bb.minZ},
                                {bb.minX, bb.maxY, bb.minZ},
                                {bb.maxX, bb.maxY, bb.minZ},
                                {bb.maxX, bb.minY, bb.minZ},
                                {bb.minX, bb.minY, bb.maxZ},
                                {bb.minX, bb.maxY, bb.maxZ},
                                {bb.maxX, bb.maxY, bb.maxZ},
                                {bb.maxX, bb.minY, bb.maxZ},
                        };

                        float minX = mc.displayWidth;
                        float minY = mc.displayHeight;

                        float maxX = 0;
                        float maxY = 0;

                        for (double[] boxVertex : boxVertices) {
                            Vector2f screenPos = WorldToScreen.worldToScreen(new Vector3f((float) boxVertex[0], (float) boxVertex[1], (float) boxVertex[2]), mvMatrix, projectionMatrix, mc.displayWidth, mc.displayHeight);

                            if (screenPos == null) {
                                continue;
                            }

                            minX = Math.min(screenPos.x, minX);
                            minY = Math.min(screenPos.y, minY);

                            maxX = Math.max(screenPos.x, maxX);
                            maxY = Math.max(screenPos.y, maxY);
                        }

                        //out of screen
                        if (!(minX==mc.displayWidth || minY==mc.displayHeight || maxX==0 || maxY==0)) {
                            float width=CSGOWidth.get()*((maxY - minY)/50);
                            RenderUtils.drawRect(minX-width,minY-width,minX,maxY,color);
                            RenderUtils.drawRect(maxX,minY-width,maxX+width,maxY+width,color);
                            RenderUtils.drawRect(minX-width,maxY,maxX,maxY+width,color);
                            RenderUtils.drawRect(minX-width,minY-width,maxX,minY,color);

                            //hp bar
                            float hpSize=((maxY+width)-minY)*(entityLiving.getHealth()/entityLiving.getMaxHealth());
                            RenderUtils.drawRect(minX-(width*3),minY-width,minX-(width*2),maxY+width,Color.RED);
                            RenderUtils.drawRect(minX-(width*3),maxY-hpSize,minX-(width*2),maxY+width,Color.GREEN);

                            //nametags
                            boolean nametag=CSGONameTag.get();
                            if(nametag) {
                                String drawStr = entityLiving.getName();
                                float sScale = (maxY - minY)/mc.fontRendererObj.getStringWidth(drawStr);
                                RenderUtils.drawText(drawStr, (int) (minX - 8 - width), (int) (minY - width*sScale*2),sScale);
                            }
                        }
                        break;
                    }
                }
            }
        }

        if (csgo) {
            glEnable(GL_DEPTH_TEST);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();

            GL11.glPopAttrib();
        }
    }

    @EventTarget
    public void onRender2D(final Render2DEvent event) {
        final String mode = modeValue.get().toLowerCase();

        final FramebufferShader shader = mode.equalsIgnoreCase("shaderoutline")
                ? OutlineShader.OUTLINE_SHADER : mode.equalsIgnoreCase("shaderglow")
                ? GlowShader.GLOW_SHADER : null;

        if (shader == null) return;

        shader.startDraw(event.getPartialTicks());

        renderNameTags = false;

        try {
            for (final Entity entity : mc.theWorld.loadedEntityList) {
                if (!EntityUtils.isSelected(entity, false))
                    continue;

                mc.getRenderManager().renderEntityStatic(entity, mc.timer.renderPartialTicks, true);
            }
        } catch (final Exception ex) {
            ClientUtils.getLogger().error("An error occurred while rendering all entities for shader esp", ex);
        }

        renderNameTags = true;

        final float radius = mode.equalsIgnoreCase("shaderoutline")
                ? shaderOutlineRadius.get() : mode.equalsIgnoreCase("shaderglow")
                ? shaderGlowRadius.get() : 1F;

        shader.stopDraw(getColor(null), radius, 1F);
    }

    @Override
    public String getTag() {
        return modeValue.get();
    }

    public Color getColor(final Entity entity) {
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase entityLivingBase = (EntityLivingBase) entity;

            if (entityLivingBase.hurtTime > 0)
                return Color.RED;

            if (EntityUtils.isFriend(entityLivingBase))
                return Color.BLUE;

            if (colorTeam.get()) {
                final char[] chars = entityLivingBase.getDisplayName().getFormattedText().toCharArray();
                int color = Integer.MAX_VALUE;

                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] != '§' || i + 1 >= chars.length)
                        continue;

                    final int index = GameFontRenderer.getColorIndex(chars[i + 1]);

                    if (index < 0 || index > 15)
                        continue;

                    color = ColorUtils.hexColors[index];
                    break;
                }

                return new Color(color);
            }
        }

        return colorRainbow.get() ? ColorUtils.rainbow() : new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get());
    }
}
