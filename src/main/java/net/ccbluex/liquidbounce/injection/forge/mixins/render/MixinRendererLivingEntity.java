/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.features.module.modules.client.Rotations;
import net.ccbluex.liquidbounce.features.module.modules.visual.*;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.awt.Color;
import static net.ccbluex.liquidbounce.utils.MinecraftInstance.mc;
import static net.ccbluex.liquidbounce.utils.render.ColorUtils.inAlpha;
import static net.ccbluex.liquidbounce.utils.render.RenderUtils.*;
import static net.minecraft.client.renderer.GlStateManager.*;
import static net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords;
import static org.lwjgl.opengl.GL11.*;

@Mixin(RendererLivingEntity.class)
@SideOnly(Side.CLIENT)
public abstract class MixinRendererLivingEntity extends MixinRender {

    @Shadow
    protected abstract<T extends EntityLivingBase> float handleRotationFloat(T livingBase, float partialTicks);

    @Shadow
    private static final Logger logger = LogManager.getLogger();

    @Shadow
    protected abstract<T extends EntityLivingBase> float getSwingProgress(T livingBase, float partialTickTime);

    @Shadow
    protected abstract <T extends EntityLivingBase> void renderLivingAt(T entityLivingBaseIn, double x, double y, double z);

    @Shadow
    protected abstract <T extends EntityLivingBase> void preRenderCallback(T entitylivingbaseIn, float partialTickTime);

    @Shadow
    protected abstract <T extends EntityLivingBase> void rotateCorpse(T p_rotateCorpse_1_, float p_rotateCorpse_2_, float p_rotateCorpse_3_, float p_rotateCorpse_4_);

    @Shadow
    protected boolean renderOutlines = false;

    @Shadow
    protected abstract <T extends EntityLivingBase> boolean setScoreTeamColor(T entityLivingBaseIn);

    @Shadow
    protected abstract void unsetBrightness();

    @Shadow
    protected abstract void unsetScoreTeamColor();

    @Shadow
    protected abstract <T extends EntityLivingBase> void renderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_);

    @Shadow
    protected abstract <T extends EntityLivingBase> boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks);

    @Shadow
    protected abstract float interpolateRotation(float par1, float par2, float par3);

    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "rotateCorpse", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;deathTime:I", shift = At.Shift.AFTER))
    protected <T extends EntityLivingBase> void rotateCorpse(T p_rotateCorpse_1_, float p_rotateCorpse_2_, float p_rotateCorpse_3_, float p_rotateCorpse_4_, CallbackInfo ci) {
        final CustomModel customModel = CustomModel.INSTANCE;
        String s = EnumChatFormatting.getTextWithoutFormattingCodes(p_rotateCorpse_1_.getName());
        if (s != null && (customModel.getRotatePlayer() && p_rotateCorpse_1_.equals(mc.thePlayer) && customModel.handleEvents()) && (!(p_rotateCorpse_1_ instanceof EntityPlayer) || ((EntityPlayer)p_rotateCorpse_1_).isWearing(EnumPlayerModelParts.CAPE))) {
            translate(0.0F, p_rotateCorpse_1_.height + 0.1F, 0.0F);
            rotate(180.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private <T extends EntityLivingBase> void injectChamsPre(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        final Chams chams = Chams.INSTANCE;
        final NoRender noRender = NoRender.INSTANCE;

        if (noRender.handleEvents() && noRender.shouldStopRender(entity)) {
            callbackInfo.cancel();
            return;
        }

        if (chams.getState() && chams.getTargets() && chams.getLegacyMode() && ((chams.getLocalPlayerValue() && entity == mc.thePlayer) || EntityUtils.INSTANCE.isSelected(entity, false))) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(1.0F, -1000000F);
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("RETURN"))
    private <T extends EntityLivingBase> void injectChamsPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        final Chams chams = Chams.INSTANCE;
        final NoRender noRender = NoRender.INSTANCE;

        if (chams.getState() && chams.getTargets() && chams.getLegacyMode() && ((chams.getLocalPlayerValue() && entity == mc.thePlayer) || EntityUtils.INSTANCE.isSelected(entity, false))) {
            if (!(noRender.getState() && noRender.shouldStopRender(entity))) {
                glPolygonOffset(1.0F, 1000000F);
                glDisable(GL_POLYGON_OFFSET_FILL);
            }
        }
    }

    @Inject(method = "canRenderName(Lnet/minecraft/entity/EntityLivingBase;)Z", at = @At("HEAD"), cancellable = true)
    private <T extends EntityLivingBase> void canRenderName(T entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (NameTags.INSTANCE.shouldRenderNameTags(entity)) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    /**
     * @author Randomguy && wxdbie
     * @reason FakeBode,Baby
     */
    @Overwrite
    public<T extends EntityLivingBase> void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        final CustomModel customModel = CustomModel.INSTANCE;
        pushMatrix();
        disableCull();
        this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
        this.mainModel.isRiding = entity.isRiding();
        this.mainModel.isChild = entity.isChild();

        try
        {
            float f = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
            float f1 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
            float f2 = f1 - f;

            if (entity.isRiding() && entity.ridingEntity instanceof EntityLivingBase)
            {
                EntityLivingBase entitylivingbase = (EntityLivingBase)entity.ridingEntity;
                f = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                f2 = f1 - f;
                float f3 = MathHelper.wrapAngleTo180_float(f2);

                if (f3 < -85.0F)
                {
                    f3 = -85.0F;
                }

                if (f3 >= 85.0F)
                {
                    f3 = 85.0F;
                }

                f = f1 - f3;

                if (f3 * f3 > 2500.0F)
                {
                    f += f3 * 0.2F;
                }
            }

            float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            this.renderLivingAt(entity, x, y, z);
            float f8 = this.handleRotationFloat(entity, partialTicks);
            this.rotateCorpse(entity, f8, f, partialTicks);
            GlStateManager.enableRescaleNormal();
            GlStateManager.scale(-1.0F, -1.0F, 1.0F);
            this.preRenderCallback(entity, partialTicks);
            GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
            float f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
            float f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

            if (entity.isChild())
            {
                f6 *= 3.0F;
            }

            if (f5 > 1.0F)
            {
                f5 = 1.0F;
            }

            GlStateManager.enableAlpha();
            this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
            this.mainModel.setRotationAngles(f6, f5, f8, f2, f7, 0.0625F, entity);

            if (this.renderOutlines)
            {
                boolean flag1 = this.setScoreTeamColor(entity);
                this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

                if (flag1)
                {
                    this.unsetScoreTeamColor();
                }
            }
            else
            {

                boolean flag = this.setDoRenderBrightness(entity, partialTicks);
                this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

                if (flag)
                {
                    this.unsetBrightness();
                }

                GlStateManager.depthMask(true);

                if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator())
                {
                    this.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, 0.0625F);
                }
            }


            final Rotations rotations = Rotations.INSTANCE;
            float renderpitch = (mc.gameSettings.thirdPersonView != 0 && rotations.getState() && rotations.getGhost() && entity == mc.thePlayer) ? (entity.prevRotationPitch + (((RotationUtils.INSTANCE.getServerRotation().getPitch() != 0.0f) ? RotationUtils.INSTANCE.getServerRotation().getPitch() : entity.rotationPitch) - entity.prevRotationPitch)) : (entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks);
            float renderyaw = (mc.gameSettings.thirdPersonView != 0 && rotations.getState() && rotations.getGhost() && entity == mc.thePlayer) ? (entity.prevRotationYaw + (((RotationUtils.INSTANCE.getServerRotation().getYaw() != 0.0f) ? RotationUtils.INSTANCE.getServerRotation().getYaw() : entity.rotationYaw) - entity.prevRotationYaw)) : (entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks);
            if(rotations.getState() && rotations.getGhost() && entity.equals(mc.thePlayer) && Rotations.INSTANCE.shouldRotate()) {
                glPushMatrix();
                glPushAttrib(1048575);
                glDisable(2929);
                glDisable(3553);
                glDisable(3553);
                glEnable(3042);
                glBlendFunc(770,771);
                glDisable(2896);
                glPolygonMode(1032,6914);
                glColor4f((float) rotations.getColorRedValue() / 255, (float) rotations.getColorGreenValue() / 255, rotations.getColorBlueValue(), (float) rotations.getAlphaValue() / 255);
                glRotatef(renderyaw - f, 0, 0.001f, 0);
                this.mainModel.render(mc.thePlayer, f6, f5, renderpitch, f2, renderpitch, 0.0625F);
                glEnable(2896);
                glDisable(3042);
                glEnable(3553);
                glEnable(2929);
                glColor3d(1,1,1);
                glPopAttrib();
                glPopMatrix();
            }

            GlStateManager.disableRescaleNormal();
        }
        catch (Exception exception)
        {
            logger.error("Couldn't render entity", exception);
        }

        setActiveTexture(OpenGlHelper.lightmapTexUnit);
        enableTexture2D();
        setActiveTexture(OpenGlHelper.defaultTexUnit);
        enableCull();
        popMatrix();

        if (!this.renderOutlines)
        {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    /**
     * @author Zywl
     * @reason Chams & truesight options
     */
    @Overwrite
    protected <T extends EntityLivingBase> void renderModel(T entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float scaleFactor) {
        boolean visible = !entitylivingbaseIn.isInvisible();
        final TrueSight trueSight = TrueSight.INSTANCE;
        final Chams chams = Chams.INSTANCE;
        boolean chamsFlag = (chams.handleEvents() && chams.getTargets() && !chams.getLegacyMode() && ((chams.getLocalPlayerValue() && entitylivingbaseIn == mc.thePlayer) || EntityUtils.INSTANCE.isSelected(entitylivingbaseIn, false)));
        boolean semiVisible = !visible && (!entitylivingbaseIn.isInvisibleToPlayer(mc.thePlayer) || (trueSight.handleEvents() && trueSight.getEntities()));
        if(visible || semiVisible) {
            if(!this.bindEntityTexture(entitylivingbaseIn))
                return;

            if (semiVisible) {
                pushMatrix();
                color(1f, 1f, 1f, 0.15F);
                depthMask(false);
                glEnable(GL_BLEND);
                blendFunc(770, 771);
                alphaFunc(516, 0.003921569F);
            }

            final ESP esp = ESP.INSTANCE;
            if (esp.handleEvents() && esp.shouldRender(entitylivingbaseIn) && EntityUtils.INSTANCE.isSelected(entitylivingbaseIn, false)) {
                boolean fancyGraphics = mc.gameSettings.fancyGraphics;
                mc.gameSettings.fancyGraphics = false;

                float gamma = mc.gameSettings.gammaSetting;
                mc.gameSettings.gammaSetting = 100000F;

                switch (esp.getMode().toLowerCase()) {
                    case "wireframe":
                        glPushMatrix();
                        glPushAttrib(GL_ALL_ATTRIB_BITS);
                        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                        glDisable(GL_TEXTURE_2D);
                        glDisable(GL_LIGHTING);
                        glDisable(GL_DEPTH_TEST);
                        glEnable(GL_LINE_SMOOTH);
                        glEnable(GL_BLEND);
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                        RenderUtils.INSTANCE.glColor(esp.getColor(entitylivingbaseIn));
                        glLineWidth(esp.getWireframeWidth());
                        mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
                        glPopAttrib();
                        glPopMatrix();
                        break;
                    case "outline":
                        ClientUtils.INSTANCE.disableFastRender();
                        resetColor();

                        final Color color = esp.getColor(entitylivingbaseIn);
                        setColor(color);
                        renderOne(esp.getOutlineWidth());
                        mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
                        setColor(color);
                        renderTwo();
                        mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
                        setColor(color);
                        renderThree();
                        mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
                        setColor(color);
                        renderFour(color);
                        mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
                        setColor(color);
                        renderFive();
                        setColor(Color.WHITE);
                }
                mc.gameSettings.fancyGraphics = fancyGraphics;
                mc.gameSettings.gammaSetting = gamma;
            }

            final int blend = 3042;
            final int depth = 2929;
            final int srcAlpha = 770;
            final int srcAlphaPlus1 = srcAlpha + 1;
            final int polygonOffsetLine = 10754;
            final int texture2D = 3553;
            final int lighting = 2896;

            boolean textured = chams.getTexturedValue();

            Color chamsColor = new Color(0x00000000);
            Color chamsBehindColor = new Color(chams.getBehindRedValue(), chams.getBehindGreenValue(), chams.getBehindBlueValue());

            switch (chams.getColorModeValue()) {
                case "Custom":
                    chamsColor = new Color(chams.getRedValue(), chams.getGreenValue(), chams.getBlueValue());
                    break;
                case "Fade":
                    chamsColor = ColorUtils.INSTANCE.fade(new Color(chams.getRedValue(), chams.getGreenValue(), chams.getBlueValue(), chams.getAlphaValue()), 0, 100);
                    break;
            }

            chamsColor = inAlpha(chamsColor, chams.getAlphaValue());
            chamsBehindColor = inAlpha(chamsBehindColor, chams.getBehindAlphaValue());

            if (chamsFlag) {
                Color chamsColor2 = new Color(0x00000000);

                switch (chams.getBehindColorModeValue()) {
                    case "Same":
                        chamsColor2 = chamsColor;
                        break;
                    case "Opposite":
                        chamsColor2 = ColorUtils.getOppositeColor(chamsColor);
                        break;
                    case "Custom":
                        chamsColor2 = chamsBehindColor;
                        break;
                }

                glPushMatrix();
                glEnable(polygonOffsetLine);
                glPolygonOffset(1.0F, 1000000.0F);
                setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

                if (!textured) {
                    glEnable(blend);
                    glDisable(texture2D);
                    glDisable(lighting);
                    glBlendFunc(srcAlpha, srcAlphaPlus1);
                    glColor4f(chamsColor2.getRed() / 255.0F, chamsColor2.getGreen() / 255.0F, chamsColor2.getBlue() / 255.0F, chamsColor2.getAlpha() / 255.0F);
                }

                glDisable(depth);
                glDepthMask(false);
            }

            this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);

            if (chamsFlag) {
                glEnable(depth);
                glDepthMask(true);

                if (!textured) {
                    glColor4f(chamsColor.getRed() / 255.0F, chamsColor.getGreen() / 255.0F, chamsColor.getBlue() / 255.0F, chamsColor.getAlpha() / 255.0F);
                }

                this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);

                if (!textured) {
                    glEnable(texture2D);
                    glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    glDisable(blend);
                    glEnable(lighting);
                }

                glPolygonOffset(1.0f, -1000000.0f);
                glDisable(polygonOffsetLine);
                glPopMatrix();
            }

            if (semiVisible) {
                disableBlend();
                alphaFunc(516, 0.1F);
                popMatrix();
                depthMask(true);
            }
        }

    }
}
