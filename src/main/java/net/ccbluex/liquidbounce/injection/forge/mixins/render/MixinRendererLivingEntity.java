package net.ccbluex.liquidbounce.injection.forge.mixins.render;


import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.visual.*;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Objects;

/**
 * The type Mixin renderer living entity.
 */
@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity extends MixinRender {
    @Final
    @Shadow
    private static final Logger logger = LogManager.getLogger();
    /**
     * The Main model.
     */
    @Shadow
    protected ModelBase mainModel;
    /**
     * The Render outlines.
     */
    @Shadow
    protected boolean renderOutlines = false;

    /**
     * Gets death max rotation.
     *
     * @param <T>                      the type parameter
     * @param p_getDeathMaxRotation_1_ the p get death max rotation 1
     * @return the death max rotation
     */
    @Shadow
    protected <T extends EntityLivingBase> float getDeathMaxRotation(T p_getDeathMaxRotation_1_) {
        return 90.0F;
    }

    /**
     * Interpolate rotation float.
     *
     * @param par1 the par 1
     * @param par2 the par 2
     * @param par3 the par 3
     * @return the float
     */
    @Shadow
    protected abstract float interpolateRotation(float par1, float par2, float par3);

    /**
     * Sets do render brightness.
     *
     * @param <T>                the type parameter
     * @param entityLivingBaseIn the entity living base in
     * @param partialTicks       the partial ticks
     * @return the do render brightness
     */
    @Shadow
    protected abstract <T extends EntityLivingBase> boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks);

    /**
     * Sets score team color.
     *
     * @param <T>                the type parameter
     * @param entityLivingBaseIn the entity living base in
     * @return the score team color
     */
    @Shadow
    protected abstract <T extends EntityLivingBase> boolean setScoreTeamColor(T entityLivingBaseIn);

    /**
     * Gets swing progress.
     *
     * @param <T>             the type parameter
     * @param livingBase      the living base
     * @param partialTickTime the partial tick time
     * @return the swing progress
     */
    @Shadow
    protected abstract <T extends EntityLivingBase> float getSwingProgress(T livingBase, float partialTickTime);

    /**
     * Unset score team color.
     */
    @Shadow
    protected abstract void unsetScoreTeamColor();

    /**
     * Pre render callback.
     *
     * @param <T>                the type parameter
     * @param entitylivingbaseIn the entitylivingbase in
     * @param partialTickTime    the partial tick time
     */
    @Shadow
    protected abstract <T extends EntityLivingBase> void preRenderCallback(T entitylivingbaseIn, float partialTickTime);

    /**
     * Unset brightness.
     */
    @Shadow
    protected abstract void unsetBrightness();

    /**
     * Render living at.
     *
     * @param <T>                the type parameter
     * @param entityLivingBaseIn the entity living base in
     * @param x                  the x
     * @param y                  the y
     * @param z                  the z
     */
    @Shadow
    protected abstract <T extends EntityLivingBase> void renderLivingAt(T entityLivingBaseIn, double x, double y, double z);

    /**
     * Handle rotation float float.
     *
     * @param <T>          the type parameter
     * @param livingBase   the living base
     * @param partialTicks the partial ticks
     * @return the float
     */
    @Shadow
    protected abstract <T extends EntityLivingBase> float handleRotationFloat(T livingBase, float partialTicks);

    /**
     * Render layers.
     *
     * @param <T>                the type parameter
     * @param entitylivingbaseIn the entitylivingbase in
     * @param p_177093_2_        the p 177093 2
     * @param p_177093_3_        the p 177093 3
     * @param partialTicks       the partial ticks
     * @param p_177093_5_        the p 177093 5
     * @param p_177093_6_        the p 177093 6
     * @param p_177093_7_        the p 177093 7
     * @param p_177093_8_        the p 177093 8
     */
    @Shadow
    protected abstract <T extends EntityLivingBase> void renderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_);

    /**
     * Rotate corpse.
     *
     * @param <T>               the type parameter
     * @param p_rotateCorpse_1_ the p rotate corpse 1
     * @param p_rotateCorpse_2_ the p rotate corpse 2
     * @param p_rotateCorpse_3_ the p rotate corpse 3
     * @param p_rotateCorpse_4_ the p rotate corpse 4
     * @author As_pw
     * @reason RotateCorpse
     */
    @Overwrite
    protected <T extends EntityLivingBase> void rotateCorpse(T p_rotateCorpse_1_, float p_rotateCorpse_2_, float p_rotateCorpse_3_, float p_rotateCorpse_4_) {
        final PlayerEdit playerEdit = Objects.requireNonNull(FDPClient.moduleManager.getModule(PlayerEdit.class));
        GlStateManager.rotate(180.0F - p_rotateCorpse_3_, 0.0F, 1.0F, 0.0F);
        if (p_rotateCorpse_1_.deathTime > 0) {
            float f = ((float) p_rotateCorpse_1_.deathTime + p_rotateCorpse_4_ - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt_float(f);
            if (f > 1.0F) {
                f = 1.0F;
            }

            GlStateManager.rotate(f * this.getDeathMaxRotation(p_rotateCorpse_1_), 0.0F, 0.0F, 1.0F);
        } else {
            String s = EnumChatFormatting.getTextWithoutFormattingCodes(p_rotateCorpse_1_.getName());
            if (s != null && (PlayerEdit.rotatePlayer.get() && p_rotateCorpse_1_.equals(MinecraftInstance.mc.thePlayer) && playerEdit.getState()) && (!(p_rotateCorpse_1_ instanceof EntityPlayer) || ((EntityPlayer) p_rotateCorpse_1_).isWearing(EnumPlayerModelParts.CAPE))) {
                GlStateManager.translate(0.0F, p_rotateCorpse_1_.height + PlayerEdit.yPos.get() - 1.8F, 0.0F);
                GlStateManager.rotate(PlayerEdit.xRot.get(), 0.0F, 0.0F, 1.0F);
            }
        }
    }

    @Inject(method = "doRender", at = @At("HEAD"))
    private <T extends EntityLivingBase> void injectChamsPre(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        final Chams chams = FDPClient.moduleManager.getModule(Chams.class);

        if (chams.getState() && chams.getTargetsValue().get() && chams.getLegacyMode().get() && ((chams.getLocalPlayerValue().get() && entity == Minecraft.getMinecraft().thePlayer) || EntityUtils.INSTANCE.isSelected(entity, false))) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(1.0F, -1000000F);
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    private <T extends EntityLivingBase> void injectChamsPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        final Chams chams = FDPClient.moduleManager.getModule(Chams.class);

        if (chams.getState() && chams.getTargetsValue().get() && chams.getLegacyMode().get() && ((chams.getLocalPlayerValue().get() && entity == Minecraft.getMinecraft().thePlayer) || EntityUtils.INSTANCE.isSelected(entity, false))
        ) {
            GL11.glPolygonOffset(1.0F, 1000000F);
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        }
    }

    @Inject(method = "canRenderName", at = @At("HEAD"), cancellable = true)
    private <T extends EntityLivingBase> void canRenderName(T entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {

        if ((FDPClient.moduleManager.getModule(NameTags.class).getState() && EntityUtils.INSTANCE.isSelected(entity, false)))
            callbackInfoReturnable.setReturnValue(false);
    }


    /**
     * @author opZywl
     * @reason Fix Renderer
     */
    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    protected <T extends EntityLivingBase> void renderModel(T p_renderModel_1_, float p_renderModel_2_, float p_renderModel_3_, float p_renderModel_4_, float p_renderModel_5_, float p_renderModel_6_, float p_renderModel_7_, CallbackInfo ci) {
        boolean visible = !p_renderModel_1_.isInvisible();
        final Chams chams = FDPClient.moduleManager.getModule(Chams.class);
        final TrueSight trueSight = FDPClient.moduleManager.getModule(TrueSight.class);
        boolean chamsFlag = (chams.getState() && chams.getTargetsValue().get() && !chams.getLegacyMode().get() && ((chams.getLocalPlayerValue().get() && p_renderModel_1_ == Minecraft.getMinecraft().thePlayer) || EntityUtils.INSTANCE.isSelected(p_renderModel_1_, false)));
        boolean semiVisible = !visible && (!p_renderModel_1_.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) || (trueSight.getState() && trueSight.getEntitiesValue().get()));

        if(visible || semiVisible) {
            if(!this.bindEntityTexture(p_renderModel_1_))
                return;

            if(semiVisible) {
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 771);
                GlStateManager.alphaFunc(516, 0.003921569F);
            }

            final int blend = 3042;
            final int depth = 2929;
            final int srcAlpha = 770;
            final int srcAlphaPlus1 = srcAlpha + 1;
            final int polygonOffsetLine = 10754;
            final int texture2D = 3553;
            final int lighting = 2896;

            boolean textured = chams.getTexturedValue().get();

            Color chamsColor = new Color(0x00000000);

            switch (chams.getColorModeValue().get()) {
                case "Custom":
                    chamsColor = new Color(chams.getRedValue().get(), chams.getGreenValue().get(), chams.getBlueValue().get());
                    break;
                case "Slowly":
                    chamsColor = ColorUtils.slowlyRainbow(System.nanoTime(), 0, chams.getSaturationValue().get(), chams.getBrightnessValue().get());
                    break;
                case "Fade":
                    chamsColor = ColorUtils.fade(new Color(chams.getRedValue().get(), chams.getGreenValue().get(), chams.getBlueValue().get(), chams.getAlphaValue().get()), 0, 100);
                    break;
            }

            chamsColor = ColorUtils.reAlpha(chamsColor, chams.getAlphaValue().get());

            if (chamsFlag) {
                Color chamsColor2 = new Color(0x00000000);

                switch (chams.getBehindColorModeValue().get()) {
                    case "Same":
                        chamsColor2 = chamsColor;
                        break;
                    case "Opposite":
                        chamsColor2 = ColorUtils.getOppositeColor(chamsColor);
                        break;
                    case "Red":
                        chamsColor2 = new Color(0xffEF2626);
                        break;
                }

                GL11.glPushMatrix();
                GL11.glEnable(polygonOffsetLine);
                GL11.glPolygonOffset(1.0F, 1000000.0F);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

                if (!textured) {
                    GL11.glEnable(blend);
                    GL11.glDisable(texture2D);
                    GL11.glDisable(lighting);
                    GL11.glBlendFunc(srcAlpha, srcAlphaPlus1);
                    GL11.glColor4f(chamsColor2.getRed() / 255.0F, chamsColor2.getGreen() / 255.0F, chamsColor2.getBlue() / 255.0F, chamsColor2.getAlpha() / 255.0F);
                }

                GL11.glDisable(depth);
                GL11.glDepthMask(false);
            }

            this.mainModel.render(p_renderModel_1_, p_renderModel_2_, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);

            if (chamsFlag) {
                GL11.glEnable(depth);
                GL11.glDepthMask(true);

                if (!textured) {
                    GL11.glColor4f(chamsColor.getRed() / 255.0F, chamsColor.getGreen() / 255.0F, chamsColor.getBlue() / 255.0F, chamsColor.getAlpha() / 255.0F);
                }

                this.mainModel.render(p_renderModel_1_, p_renderModel_2_, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);

                if (!textured) {
                    GL11.glEnable(texture2D);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glDisable(blend);
                    GL11.glEnable(lighting);
                }

                GL11.glPolygonOffset(1.0f, -1000000.0f);
                GL11.glDisable(polygonOffsetLine);
                GL11.glPopMatrix();
            }

            if (semiVisible) {
                GlStateManager.disableBlend();
                GlStateManager.alphaFunc(516, 0.1F);
                GlStateManager.popMatrix();
                GlStateManager.depthMask(true);
            }
        }

        ci.cancel();
    }

    @Redirect(method={"renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V"}, at=@At(value="FIELD", target="Lnet/minecraft/client/renderer/entity/RenderManager;playerViewX:F"))
    private float renderName(RenderManager renderManager) {
        return Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -renderManager.playerViewX : renderManager.playerViewX;
    }
}