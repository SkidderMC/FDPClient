/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import com.google.common.base.Predicates;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.Backtrack;
import net.ccbluex.liquidbounce.features.module.modules.other.OverrideRaycast;
import net.ccbluex.liquidbounce.features.module.modules.player.Reach;
import net.ccbluex.liquidbounce.features.module.modules.visual.*;
import net.ccbluex.liquidbounce.utils.Rotation;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.minecraft.client.renderer.GlStateManager.rotate;
import static net.minecraft.client.renderer.GlStateManager.translate;
import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

@Mixin(EntityRenderer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinEntityRenderer {

    @Shadow
    private Entity pointedEntity;

    @Shadow
    private Minecraft mc;

    @Shadow
    private float thirdPersonDistanceTemp;

    @Shadow
    private float thirdPersonDistance;

    @Shadow
    private boolean cloudFog;

    @Mutable
    @Final
    @Shadow
    private final int[] lightmapColors;
    @Mutable
    @Final
    @Shadow
    private final DynamicTexture lightmapTexture;

    @Shadow
    private final float torchFlickerX;

    @Shadow
    private final float bossColorModifier;
    @Shadow
    private final float bossColorModifierPrev;

    @Shadow
    private boolean lightmapUpdateNeeded;


    protected MixinEntityRenderer(int[] lightmapColors, DynamicTexture lightmapTexture, float torchFlickerX, float bossColorModifier, float bossColorModifierPrev, Minecraft mc, float thirdPersonDistanceTemp, float thirdPersonDistance) {
        this.lightmapColors = lightmapColors;
        this.lightmapTexture = lightmapTexture;
        this.torchFlickerX = torchFlickerX;
        this.bossColorModifier = bossColorModifier;
        this.bossColorModifierPrev = bossColorModifierPrev;
        this.mc = mc;
        this.thirdPersonDistanceTemp = thirdPersonDistanceTemp;
        this.thirdPersonDistance = thirdPersonDistance;
    }

    @Inject(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z", shift = At.Shift.BEFORE))
    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo callbackInfo) {
        EventManager.INSTANCE.callEvent(new Render3DEvent(partialTicks));
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void injectHurtCameraEffect(CallbackInfo callbackInfo) {
        if (HurtCam.INSTANCE.handleEvents()) {
            callbackInfo.cancel();
        }
    }


    @Unique
    private float NightVisionBrightness(EntityLivingBase p_getNightVisionBrightness_1_, float p_getNightVisionBrightness_2_) {
        int i = p_getNightVisionBrightness_1_.getActivePotionEffect(Potion.nightVision).getDuration();
        return i > 200 ? 1.0F : 0.7F + MathHelper.sin(((float) i - p_getNightVisionBrightness_2_) * 3.1415927F * 0.2F) * 0.3F;
    }

    /**
     * @author Zywl
     * @reason Custom Camera View
     */
    @Overwrite
    private void orientCamera(float partialTicks) {
            final CameraView cameraView = CameraView.INSTANCE;
            Entity entity = this.mc.getRenderViewEntity();
            float f = entity.getEyeHeight();
            double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
            double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
            double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
            float f1;
            if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
                f = (float) ((double) f + 1.0D);
                GlStateManager.translate(0.0F, 0.3F, 0.0F);
                if (!this.mc.gameSettings.debugCamEnable) {
                    BlockPos blockpos = new BlockPos(entity);
                    IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);
                    ForgeHooksClient.orientBedCamera(this.mc.theWorld, blockpos, iblockstate, entity);
                    GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                    GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
                }
            } else if (this.mc.gameSettings.thirdPersonView > 0) {
                double d3 = cameraView.getState() ? cameraView.getFovValue() : this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * partialTicks;
                if (this.mc.gameSettings.debugCamEnable) {
                    GlStateManager.translate(0.0F, 0.0F, -cameraView.getFovValue());
                } else {
                    f1 = entity.rotationYaw;
                    float f2 = entity.rotationPitch;
                    if (this.mc.gameSettings.thirdPersonView == 2) {
                        f2 += 180.0F;
                    }

                    double d4 = (double) (-MathHelper.sin(f1 / 180.0F * 3.1415927F) * MathHelper.cos(f2 / 180.0F * 3.1415927F)) * d3;
                    double d5 = (double) (MathHelper.cos(f1 / 180.0F * 3.1415927F) * MathHelper.cos(f2 / 180.0F * 3.1415927F)) * d3;
                    double d6 = (double) (-MathHelper.sin(f2 / 180.0F * 3.1415927F)) * d3;

                    for (int i = 0; i < 8; ++i) {
                        float f3 = (float) ((i & 1) * 2 - 1);
                        float f4 = (float) ((i >> 1 & 1) * 2 - 1);
                        float f5 = (float) ((i >> 2 & 1) * 2 - 1);
                        f3 *= 0.1F;
                        f4 *= 0.1F;
                        f5 *= 0.1F;
                        MovingObjectPosition movingobjectposition = this.mc.theWorld.rayTraceBlocks(new Vec3(d0 + (double) f3, d1 + (double) f4, d2 + (double) f5), new Vec3(d0 - d4 + (double) f3 + (double) f5, d1 - d6 + (double) f4, d2 - d5 + (double) f5));
                        if (movingobjectposition != null) {
                            double d7 = movingobjectposition.hitVec.distanceTo(new Vec3(d0, d1, d2));
                            if (d7 < d3 && (!cameraView.getState() || (cameraView.getState() && !cameraView.getClipValue()))) {
                                d3 = d7;
                            }
                        }
                    }

                    if (this.mc.gameSettings.thirdPersonView == 2) {
                        rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    }

                    rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                    rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                    translate(0.0F, 0.0F, (float) (-d3));

                    rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                    rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
                }
            } else {
                translate(0.0F, 0.0F, -0.1F);
            }

            if (!this.mc.gameSettings.debugCamEnable) {
                float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
                float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                f1 = 0.0F;
                if (entity instanceof EntityAnimal) {
                    EntityAnimal entityanimal = (EntityAnimal) entity;
                    yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
                }

                Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);
                EntityViewRenderEvent.CameraSetup event = new EntityViewRenderEvent.CameraSetup((EntityRenderer) (Object) this, entity, block, partialTicks, yaw, pitch, f1);
                EVENT_BUS.post(event);
                rotate(event.roll, 0.0F, 0.0F, 1.0F);
                rotate(event.pitch, 1.0F, 0.0F, 0.0F);
                rotate(event.yaw, 0.0F, 1.0F, 0.0F);
            }

            translate(0.0F, -f, 0.0F);
            d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
            d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
            d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
            this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
    }

    @Inject(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;setupViewBobbing(F)V", shift = At.Shift.BEFORE))
    private void setupCameraViewBobbingBefore(final CallbackInfo callbackInfo) {
        if (Tracers.INSTANCE.handleEvents()) {
            glPushMatrix();
        }
    }

    @Inject(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;setupViewBobbing(F)V", shift = At.Shift.AFTER))
    private void setupCameraViewBobbingAfter(final CallbackInfo callbackInfo) {
        if (Tracers.INSTANCE.handleEvents()) {
            glPopMatrix();
        }
    }

    /**
     * @author CCBlueX
     */
    @Inject(method = "getMouseOver", at = @At("HEAD"), cancellable = true)
    private void getMouseOver(float p_getMouseOver_1_, CallbackInfo ci) {
        Entity entity = mc.getRenderViewEntity();
        if (entity != null && mc.theWorld != null) {
            mc.mcProfiler.startSection("pick");
            mc.pointedEntity = null;

            final Reach reach = Reach.INSTANCE;

            double d0 = reach.handleEvents() ? reach.getMaxRange() : mc.playerController.getBlockReachDistance();
            Vec3 vec3 = entity.getPositionEyes(p_getMouseOver_1_);
            Rotation rotation = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            Vec3 vec31 = RotationUtils.INSTANCE.getVectorForRotation(RotationUtils.INSTANCE.getCurrentRotation() != null && OverrideRaycast.INSTANCE.shouldOverride() ? RotationUtils.INSTANCE.getCurrentRotation() : rotation);
            double p_rayTrace_1_ = (reach.handleEvents() ? reach.getBuildReach() : d0);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * p_rayTrace_1_, vec31.yCoord * p_rayTrace_1_, vec31.zCoord * p_rayTrace_1_);
            mc.objectMouseOver = entity.worldObj.rayTraceBlocks(vec3, vec32, false, false, true);
            double d1 = d0;
            boolean flag = false;
            if (mc.playerController.extendedReach()) {
                // d0 = 6;
                d1 = 6;
            } else if (d0 > 3) {
                flag = true;
            }

            if (mc.objectMouseOver != null) {
                d1 = mc.objectMouseOver.hitVec.distanceTo(vec3);
            }

            if (reach.handleEvents()) {
                double p_rayTrace_1_2 = reach.getBuildReach();
                Vec3 vec322 = vec3.addVector(vec31.xCoord * p_rayTrace_1_2, vec31.yCoord * p_rayTrace_1_2, vec31.zCoord * p_rayTrace_1_2);
                final MovingObjectPosition movingObjectPosition = entity.worldObj.rayTraceBlocks(vec3, vec322, false, false, true);

                if (movingObjectPosition != null) d1 = movingObjectPosition.hitVec.distanceTo(vec3);
            }

            pointedEntity = null;
            Vec3 vec33 = null;
            List<Entity> list = mc.theWorld.getEntities(Entity.class, Predicates.and(EntitySelectors.NOT_SPECTATING, p_apply_1_ -> p_apply_1_ != null && p_apply_1_.canBeCollidedWith() && p_apply_1_ != entity));
            double d2 = d1;

            for (Entity entity1 : list) {
                float f1 = entity1.getCollisionBorderSize();

                final ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
                boxes.add(entity1.getEntityBoundingBox().expand(f1, f1, f1));

                Backtrack.INSTANCE.loopThroughBacktrackData(entity1, () -> {
                    boxes.add(entity1.getEntityBoundingBox().expand(f1, f1, f1));
                    return false;
                });

                for (final AxisAlignedBB axisalignedbb : boxes) {
                    MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                    if (axisalignedbb.isVecInside(vec3)) {
                        if (d2 >= 0) {
                            pointedEntity = entity1;
                            vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                            d2 = 0;
                        }
                    } else if (movingobjectposition != null) {
                        double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                        if (d3 < d2 || d2 == 0) {
                            if (entity1 == entity.ridingEntity && !entity.canRiderInteract()) {
                                if (d2 == 0) {
                                    pointedEntity = entity1;
                                    vec33 = movingobjectposition.hitVec;
                                }
                            } else {
                                pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                                d2 = d3;
                            }
                        }
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > (reach.handleEvents() ? reach.getCombatReach() : 3)) {
                pointedEntity = null;
                mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, Objects.requireNonNull(vec33), null, new BlockPos(vec33));
            }

            if (pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null)) {
                mc.objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    mc.pointedEntity = pointedEntity;
                }
            }

            mc.mcProfiler.endSection();
        }

        ci.cancel();
    }

    /**
     * @author opZywl
     * @reason Update Light Map
     */
    @Overwrite
    private void updateLightmap(float f2) {
        final Ambience ambience = Ambience.INSTANCE;
        if (this.lightmapUpdateNeeded) {
            this.mc.mcProfiler.startSection("lightTex");
            World world = this.mc.theWorld;
            if (world != null) {
                float f3 = world.getSunBrightness(1.0f);
                float f4 = f3 * 0.95f + 0.05f;
                for (int i2 = 0; i2 < 256; ++i2) {
                    float f5;
                    float f6;
                    float f7 = world.provider.getLightBrightnessTable()[i2 / 16] * f4;
                    float f8 = world.provider.getLightBrightnessTable()[i2 % 16] * (this.torchFlickerX * 0.1f + 1.5f);
                    if (world.getLastLightningBolt() > 0) {
                        f7 = world.provider.getLightBrightnessTable()[i2 / 16];
                    }
                    float f9 = f7 * (f3 * 0.65f + 0.35f);
                    float f10 = f7 * (f3 * 0.65f + 0.35f);
                    float f11 = f8 * ((f8 * 0.6f + 0.4f) * 0.6f + 0.4f);
                    float f12 = f8 * (f8 * f8 * 0.6f + 0.4f);
                    float f13 = f9 + f8;
                    float f14 = f10 + f11;
                    float f15 = f7 + f12;
                    f13 = f13 * 0.96f + 0.03f;
                    f14 = f14 * 0.96f + 0.03f;
                    f15 = f15 * 0.96f + 0.03f;
                    if (this.bossColorModifier > 0.0f) {
                        float f16 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * f2;
                        f13 = f13 * (1.0f - f16) + f13 * 0.7f * f16;
                        f14 = f14 * (1.0f - f16) + f14 * 0.6f * f16;
                        f15 = f15 * (1.0f - f16) + f15 * 0.6f * f16;
                    }
                    if (world.provider.getDimensionId() == 1) {
                        f13 = 0.22f + f8 * 0.75f;
                        f14 = 0.28f + f11 * 0.75f;
                        f15 = 0.25f + f12 * 0.75f;
                    }
                    if (this.mc.thePlayer.isPotionActive(Potion.nightVision)) {
                        f6 = this.NightVisionBrightness(this.mc.thePlayer, f2);
                        f5 = 1.0f / f13;
                        if (f5 > 1.0f / f14) {
                            f5 = 1.0f / f14;
                        }
                        if (f5 > 1.0f / f15) {
                            f5 = 1.0f / f15;
                        }
                        f13 = f13 * (1.0f - f6) + f13 * f5 * f6;
                        f14 = f14 * (1.0f - f6) + f14 * f5 * f6;
                        f15 = f15 * (1.0f - f6) + f15 * f5 * f6;
                    }
                    if (f13 > 1.0f) {
                        f13 = 1.0f;
                    }
                    if (f14 > 1.0f) {
                        f14 = 1.0f;
                    }
                    if (f15 > 1.0f) {
                        f15 = 1.0f;
                    }
                    f6 = this.mc.gameSettings.gammaSetting;
                    f5 = 1.0f - f13;
                    float f17 = 1.0f - f14;
                    float f18 = 1.0f - f15;
                    f5 = 1.0f - f5 * f5 * f5 * f5;
                    f17 = 1.0f - f17 * f17 * f17 * f17;
                    f18 = 1.0f - f18 * f18 * f18 * f18;
                    f13 = f13 * (1.0f - f6) + f5 * f6;
                    f14 = f14 * (1.0f - f6) + f17 * f6;
                    f15 = f15 * (1.0f - f6) + f18 * f6;
                    f13 = f13 * 0.96f + 0.03f;
                    f14 = f14 * 0.96f + 0.03f;
                    f15 = f15 * 0.96f + 0.03f;
                    if (f13 > 1.0f) {
                        f13 = 1.0f;
                    }
                    if (f14 > 1.0f) {
                        f14 = 1.0f;
                    }
                    if (f15 > 1.0f) {
                        f15 = 1.0f;
                    }
                    if (f13 < 0.0f) {
                        f13 = 0.0f;
                    }
                    if (f14 < 0.0f) {
                        f14 = 0.0f;
                    }
                    if (f15 < 0.0f) {
                        f15 = 0.0f;
                    }
                    int n2 = (int) (f13 * 255.0f);
                    int n3 = (int) (f14 * 255.0f);
                    int n4 = (int) (f15 * 255.0f);
                    this.lightmapColors[i2] = ambience.getState() && ambience.getWorldColor() ? new Color(ambience.getWorldColorRed(), ambience.getWorldColorGreen(), ambience.getWorldColorBlue()).getRGB() : 0xFF000000 | n2 << 16 | n3 << 8 | n4;
                }
                this.lightmapTexture.updateDynamicTexture();
                this.lightmapUpdateNeeded = false;
                this.mc.mcProfiler.endSection();
            }
        }
    }

    /**
     * Properly implement the confusion option from AntiBlind module
     */
    @Redirect(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean injectAntiBlindA(EntityPlayerSP instance, Potion potion) {
        AntiBlind module = AntiBlind.INSTANCE;

        return (!module.handleEvents() || !module.getConfusionEffect()) && instance.isPotionActive(potion);
    }

    @Redirect(method = {"setupFog", "updateFogColor"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean injectAntiBlindB(EntityLivingBase instance, Potion potion) {
        if (instance != mc.thePlayer) {
            return instance.isPotionActive(potion);
        }

        AntiBlind module = AntiBlind.INSTANCE;

        return (!module.handleEvents() || !module.getConfusionEffect()) && instance.isPotionActive(potion);
    }
}
