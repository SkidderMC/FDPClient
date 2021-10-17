/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.render.Chams;
import net.ccbluex.liquidbounce.features.module.modules.render.ESP;
import net.ccbluex.liquidbounce.features.module.modules.render.NameTags;
import net.ccbluex.liquidbounce.features.module.modules.render.TrueSight;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity extends MixinRender {

    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "doRender", at = @At("HEAD"))
    private <T extends EntityLivingBase> void injectChamsPre(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        final Chams chams = LiquidBounce.moduleManager.getModule(Chams.class);

        if (chams.getState() && chams.getTargetsValue().get() && EntityUtils.INSTANCE.isSelected(entity, false)) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(1.0F, -1000000F);
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    private <T extends EntityLivingBase> void injectChamsPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        final Chams chams = LiquidBounce.moduleManager.getModule(Chams.class);

        if (chams.getState() && chams.getTargetsValue().get() && EntityUtils.INSTANCE.isSelected(entity, false)) {
            GL11.glPolygonOffset(1.0F, 1000000F);
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        }
    }

    @Inject(method = "canRenderName", at = @At("HEAD"), cancellable = true)
    private <T extends EntityLivingBase> void canRenderName(T entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if ((LiquidBounce.moduleManager.getModule(NameTags.class).getState() && EntityUtils.INSTANCE.isSelected(entity, false)))
            callbackInfoReturnable.setReturnValue(false);
    }

    /**
     * @author CCBlueX
     */
    @Overwrite
    protected <T extends EntityLivingBase> void renderModel(T entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float scaleFactor) {
        boolean visible = !entitylivingbaseIn.isInvisible();
        final TrueSight trueSight = LiquidBounce.moduleManager.getModule(TrueSight.class);
        boolean semiVisible = !visible && (!entitylivingbaseIn.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) || (trueSight.getState() && trueSight.getEntitiesValue().get()));

        if(visible || semiVisible) {
            if(!this.bindEntityTexture(entitylivingbaseIn))
                return;

            if(semiVisible) {
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 771);
                GlStateManager.alphaFunc(516, 0.003921569F);
            }

            final ESP esp = LiquidBounce.moduleManager.getModule(ESP.class);
            if(esp.getState() && EntityUtils.INSTANCE.isSelected(entitylivingbaseIn, false)) {
                Minecraft mc = Minecraft.getMinecraft();
                boolean fancyGraphics = mc.gameSettings.fancyGraphics;
                mc.gameSettings.fancyGraphics = false;

                float gamma = mc.gameSettings.gammaSetting;
                mc.gameSettings.gammaSetting = 100000F;

                if (esp.getModeValue().equals("wireframe")) {
                    GL11.glPushMatrix();
                    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GL11.glEnable(GL11.GL_LINE_SMOOTH);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    RenderUtils.glColor(esp.getColor(entitylivingbaseIn));
                    GL11.glLineWidth(esp.getWireframeWidth().get());
                    this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
                    GL11.glPopAttrib();
                    GL11.glPopMatrix();
                }

                mc.gameSettings.fancyGraphics = fancyGraphics;
                mc.gameSettings.gammaSetting = gamma;
            }

            this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);

            final Chams chams = LiquidBounce.moduleManager.getModule(Chams.class);
            if (chams.getState() && chams.getTargetsValue().get() && EntityUtils.INSTANCE.isSelected(entitylivingbaseIn, false)) {
                GL11.glPushMatrix();
                GL11.glPushAttrib(1048575);
                GL11.glDisable(2929);
                GL11.glDisable(3553);
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDisable(2896);
                GL11.glPolygonMode(1032, 6914);
                chams.setColor();

                this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);

                GL11.glEnable(2896);
                GL11.glDisable(3042);
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glColor3d(1.0, 1.0, 1.0);
                GL11.glPopAttrib();
                GL11.glPopMatrix();
            }

            if(semiVisible) {
                GlStateManager.disableBlend();
                GlStateManager.alphaFunc(516, 0.1F);
                GlStateManager.popMatrix();
                GlStateManager.depthMask(true);
            }
        }
    }
}