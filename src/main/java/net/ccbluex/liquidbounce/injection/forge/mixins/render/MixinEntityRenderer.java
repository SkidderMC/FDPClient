/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import com.google.common.base.Predicates;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.features.module.modules.client.HurtCam;
import net.ccbluex.liquidbounce.features.module.modules.combat.Backtrack;
import net.ccbluex.liquidbounce.features.module.modules.visual.VanillaTweaks;
import net.ccbluex.liquidbounce.features.module.modules.combat.Reach;
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeLook;
import net.ccbluex.liquidbounce.utils.Interpolator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Redirect;
import static org.objectweb.asm.Opcodes.GETFIELD;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

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
    private Entity pointedEntity;
    @Shadow
    private Minecraft mc;
    @Shadow
    public float thirdPersonDistanceTemp;
    @Shadow
    public float thirdPersonDistance;
    @Shadow
    private boolean cloudFog;
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

    float d3 = 0.0f;

    @Inject(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z", shift = At.Shift.BEFORE))
    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo callbackInfo) {
        FDPClient.eventManager.callEvent(new Render3DEvent(partialTicks));
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void injectHurtCameraEffect(CallbackInfo callbackInfo) {
        if(!Objects.requireNonNull(FDPClient.moduleManager.getModule(HurtCam.class)).getModeValue().get().equalsIgnoreCase("Vanilla")) {
            callbackInfo.cancel();
        }
    }

    private float getNightVisionBrightness(EntityLivingBase p_getNightVisionBrightness_1_, float p_getNightVisionBrightness_2_) {
        int i = p_getNightVisionBrightness_1_.getActivePotionEffect(Potion.nightVision).getDuration();
        return i > 200 ? 1.0F : 0.7F + MathHelper.sin(((float) i - p_getNightVisionBrightness_2_) * 3.1415927F * 0.2F) * 0.3F;
    }

    @Inject(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Vec3;distanceTo(Lnet/minecraft/util/Vec3;)D"), cancellable = true)
    private void cameraClip(float partialTicks, CallbackInfo callbackInfo) {
        final VanillaTweaks camera = FDPClient.moduleManager.getModule(VanillaTweaks.class);
        if (Objects.requireNonNull(camera).getState() && camera.getCameraClipValue().get()) {
            callbackInfo.cancel();

            Entity entity = this.mc.getRenderViewEntity();
            float f = entity.getEyeHeight();

            if(entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
                f += 1;
                GlStateManager.translate(0F, 0.3F, 0.0F);

                if(!this.mc.gameSettings.debugCamEnable) {
                    BlockPos blockpos = new BlockPos(entity);
                    IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);
                    net.minecraftforge.client.ForgeHooksClient.orientBedCamera(this.mc.theWorld, blockpos, iblockstate, entity);

                    GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                    GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
                }
            }else if(this.mc.gameSettings.thirdPersonView > 0) {
                double d3 = this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * partialTicks;

                if(this.mc.gameSettings.debugCamEnable) {
                    GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
                }else{
                    float f1 = entity.rotationYaw;
                    float f2 = entity.rotationPitch;

                    if(this.mc.gameSettings.thirdPersonView == 2)
                        f2 += 180.0F;

                    if(this.mc.gameSettings.thirdPersonView == 2)
                        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

                    GlStateManager.rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                    GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
                    GlStateManager.rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
                }
            } else {
                GlStateManager.translate(0.0F, 0.0F, -0.1F);
            }

            if(!this.mc.gameSettings.debugCamEnable) {
                float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
                float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                float roll = 0.0F;
                if(entity instanceof EntityAnimal) {
                    EntityAnimal entityanimal = (EntityAnimal) entity;
                    yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
                }

                Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);
                net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup event = new net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup((EntityRenderer) (Object) this, entity, block, partialTicks, yaw, pitch, roll);
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
                GlStateManager.rotate(event.roll, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(event.pitch, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(event.yaw, 0.0F, 1.0F, 0.0F);
            }

            GlStateManager.translate(0.0F, -f, 0.0F);
            double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
            double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + f;
            double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
            this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
        }
    }

    /**
     * @author opZywl
     */
    @Inject(method = "getMouseOver", at = @At("HEAD"), cancellable = true)
    private void getMouseOver(float partialTicks, CallbackInfo ci) {
        Entity entity = this.mc.getRenderViewEntity();
        if (entity != null && this.mc.theWorld != null) {
            this.mc.mcProfiler.startSection("pick");
            this.mc.pointedEntity = null;

            final Reach reach = Objects.requireNonNull(FDPClient.moduleManager.getModule(Reach.class));

            double d0 = reach.getState() ? reach.getMaxRange() : this.mc.playerController.getBlockReachDistance();
            this.mc.objectMouseOver = entity.rayTrace(d0, partialTicks);
            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            if (this.mc.playerController.extendedReach()) {
                d0 = 6.0D;
                d1 = 6.0D;
            } else if (d0 > 3.0D) {
                flag = true;
            }

            if (this.mc.objectMouseOver != null) {
                d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
            }

            if (reach.getState()) {
                final MovingObjectPosition movingObjectPosition = entity.rayTrace(reach.getBuildReachValue().get(), partialTicks);
                if (movingObjectPosition != null) d1 = movingObjectPosition.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = entity.getLook(partialTicks);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            this.pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0F;
            List<Entity> list = this.mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
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
                        if (d2 >= 0.0D) {
                            this.pointedEntity = entity1;
                            vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                            d2 = 0.0D;
                        }
                    } else if (movingobjectposition != null) {
                        double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                        if (d3 < d2 || d2 == 0.0D) {
                            if (entity1 == entity.ridingEntity && !entity.canRiderInteract()) {
                                if (d2 == 0.0D) {
                                    this.pointedEntity = entity1;
                                    vec33 = movingobjectposition.hitVec;
                                }
                            } else {
                                this.pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                                d2 = d3;
                            }
                        }
                    }
                }
            }

            if (this.pointedEntity != null && flag) {
                double maxDistance = reach.getState() ? reach.getCombatReachValue().get() : 3.0D;
                if (vec3.distanceTo(vec33) > maxDistance) {
                    this.pointedEntity = null;
                    this.mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, Objects.requireNonNull(vec33), null, new BlockPos(vec33));
                }
            }

            if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null)) {
                this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);
                if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame) {
                    this.mc.pointedEntity = this.pointedEntity;
                }
            }

            this.mc.mcProfiler.endSection();
        }

        ci.cancel();
    }

    /**
     * Update camera and render boolean.
     *
     * @param minecraft the minecraft
     * @return the boolean
     */
    @Redirect(method = "updateCameraAndRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;inGameHasFocus:Z", opcode = GETFIELD))
    public boolean updateCameraAndRender(Minecraft minecraft) {
        if (Objects.requireNonNull(FDPClient.moduleManager.getModule(FreeLook.class)).getState() && !Objects.requireNonNull(FDPClient.moduleManager.getModule(FreeLook.class)).getReverse().get()) {
            return FreeLook.overrideMouse();
        } else return mc.inGameHasFocus && Display.isActive();
    }

    /**
     * Gets rotation yaw.
     *
     * @param entity the entity
     * @return the rotation yaw
     */
    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationYaw:F", opcode = GETFIELD))
    public float getRotationYaw(Entity entity) {
        return FreeLook.perspectiveToggled ? FreeLook.cameraYaw : entity.rotationYaw;
    }

    /**
     * Gets prev rotation yaw.
     *
     * @param entity the entity
     * @return the prev rotation yaw
     */
    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationYaw:F", opcode = GETFIELD))
    public float getPrevRotationYaw(Entity entity) {
        return FreeLook.perspectiveToggled ? FreeLook.cameraYaw : entity.prevRotationYaw;
    }

    /**
     * Gets rotation pitch.
     *
     * @param entity the entity
     * @return the rotation pitch
     */
    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationPitch:F", opcode = GETFIELD))
    public float getRotationPitch(Entity entity) {
        return FreeLook.perspectiveToggled ? FreeLook.cameraPitch : entity.rotationPitch;
    }

    /**
     * Gets prev rotation pitch.
     *
     * @param entity the entity
     * @return the prev rotation pitch
     */
    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationPitch:F"))
    public float getPrevRotationPitch(Entity entity) {
        return FreeLook.perspectiveToggled ? FreeLook.cameraPitch : entity.prevRotationPitch;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void updateLightmap(float f2) {
        VanillaTweaks camera = FDPClient.moduleManager.getModule(VanillaTweaks.class);
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
                        f6 = this.getNightVisionBrightness(this.mc.thePlayer, f2);
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
                    assert camera != null;
                    this.lightmapColors[i2] = camera.getState() && camera.getWorldColorValue().get() ? new Color(camera.getWorldColorRValue().get(), camera.getWorldColorGValue().get(), camera.getWorldColorBValue().get()).getRGB() : 0xFF000000 | n2 << 16 | n3 << 8 | n4;
                }
                this.lightmapTexture.updateDynamicTexture();
                this.lightmapUpdateNeeded = false;
                this.mc.mcProfiler.endSection();
            }
        }
    }

    @Overwrite
    private void orientCamera(float partialTicks) {
        HUD hud = FDPClient.moduleManager.getModule(HUD.class);
        Entity entity = this.mc.getRenderViewEntity();
        float f = entity.getEyeHeight();
        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
            f = (float) ((double) f + 1.0);
            GlStateManager.translate(0.0f, 0.3f, 0.0f);
            if (!this.mc.gameSettings.debugCamEnable) {
                BlockPos blockpos = new BlockPos(entity);
                IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);
                ForgeHooksClient.orientBedCamera(this.mc.theWorld, blockpos, iblockstate, entity);
                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
            }
        } else if (this.mc.gameSettings.thirdPersonView > 0) {
            assert hud != null;
            float f2;
            if (hud.getSmoothCamera().get() && hud.getState()) {
                f2 = this.d3 = hud.getState() && hud.getCameraPositionValue().get() ? (float) Interpolator.LINEAR.interpolate((double) this.d3, (double) hud.getCameraPositionFovValue().get(), 5.0 / (double) Minecraft.getDebugFPS()) : (float) Interpolator.LINEAR.interpolate((double) this.d3, 4.0, 5.0 / (double) Minecraft.getDebugFPS());
            }else{
                f2 = this.d3 = hud.getState() && hud.getCameraPositionValue().get() ? hud.getCameraPositionFovValue().get(): (float) 4.0;
            }
            if (Double.isNaN(this.d3)) {
                this.d3 = 0.01f;
            }
            this.d3 = (float) MathHelper.clamp_float((float) this.d3, 0.01F, (float) (hud.getState() && hud.getCameraPositionValue().get() ? (double) hud.getCameraPositionFovValue().get() : 4.0));
            if (this.mc.gameSettings.debugCamEnable) {
                GlStateManager.translate(0.0f, 0.0f, -this.d3);
            } else {
                float f22;
                float f1;
                if (hud.getState() && hud.getCameraPositionValue().get()) {
                    f1 = entity.rotationYaw + hud.getCameraPositionYawValue().get();
                    f22 = entity.rotationPitch + hud.getCameraPositionPitchValue().get();
                } else {
                    f1 = entity.rotationYaw;
                    f22 = entity.rotationPitch;
                }
                if (this.mc.gameSettings.thirdPersonView == 2) {
                    f22 += 180.0f;
                }
                double d4 = (double) (-MathHelper.sin(f1 * ((float) Math.PI / 180)) * MathHelper.cos(f22 * ((float) Math.PI / 180))) * (double) this.d3;
                double d5 = (double) (MathHelper.cos(f1 * ((float) Math.PI / 180)) * MathHelper.cos(f22 * ((float) Math.PI / 180))) * (double) this.d3;
                double d6 = (double) (-MathHelper.sin(f22 * ((float) Math.PI / 180))) * (double) this.d3;
                for (int i = 0; i < 8; ++i) {
                    double d7;
                    this.mc.theWorld.rayTraceBlocks(new Vec3(d0 + (double) d4, d1 + (double) d5, d2 + (double) d6), new Vec3(d0 - d4 + (double) d4 + (double) d6, d1 - d6 + (double) d5, d2 - d5 + (double) d6));
                    MovingObjectPosition movingobjectposition;
                    float f3 = (i & 1) * 2 - 1;
                    float f4 = (i >> 1 & 1) * 2 - 1;
                    float f5 = (i >> 2 & 1) * 2 - 1;
                    if (hud.getState() || (movingobjectposition = this.mc.theWorld.rayTraceBlocks(new Vec3(d0 + (double) (f3 *= 0.1f), d1 + (double) (f4 *= 0.1f), d2 + (double) (f5 *= 0.1f)), new Vec3(d0 - d4 + (double) f3 + (double) f5, d1 - d6 + (double) f4, d2 - d5 + (double) f5))) == null || !((d7 = movingobjectposition.hitVec.distanceTo(new Vec3(d0, d1, d2))) < (double) this.d3))
                        continue;
                    this.d3 = (float) d7;
                }
                if (this.mc.gameSettings.thirdPersonView == 2) {
                    GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
                }
                GlStateManager.rotate(entity.rotationPitch - f22, 1.0f, 0.0f, 0.0f);
                GlStateManager.rotate(entity.rotationYaw - f1, 0.0f, 1.0f, 0.0f);
                GlStateManager.translate(0.0f, 0.0f, -this.d3);
                GlStateManager.rotate(f1 - entity.rotationYaw, 0.0f, 1.0f, 0.0f);
                GlStateManager.rotate(f22 - entity.rotationPitch, 1.0f, 0.0f, 0.0f);
            }
        } else {
            GlStateManager.translate(0.0f, 0.0f, 0.05f);
            this.d3 = 0.01f;
        }
        if (!this.mc.gameSettings.debugCamEnable) {
            float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
            float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            float f8 = 0.0F;
            if (entity instanceof EntityAnimal) {
                EntityAnimal entityanimal = (EntityAnimal) entity;
                yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
            }

            Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);
            EntityViewRenderEvent.CameraSetup event = new EntityViewRenderEvent.CameraSetup(new EntityRenderer(Minecraft.getMinecraft(), new IResourceManager() {
                @Override
                public Set<String> getResourceDomains() {
                    return null;
                }

                @Override
                public IResource getResource(ResourceLocation resourceLocation) {
                    return null;
                }

                @Override
                public List<IResource> getAllResources(ResourceLocation resourceLocation) {
                    return null;
                }
            }), entity, block, (double) partialTicks, yaw, pitch, f8);
            MinecraftForge.EVENT_BUS.post(event);
            GlStateManager.rotate(event.roll, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(event.pitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(event.yaw, 0.0F, 1.0F, 0.0F);
        } else if (!this.mc.gameSettings.debugCamEnable) {
            GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 1.0f, 0.0f, 0.0f);
            if (entity instanceof EntityAnimal) {
                EntityAnimal entityanimal = (EntityAnimal) entity;
                GlStateManager.rotate(entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0f, 0.0f, 1.0f, 0.0f);
            } else {
                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0f, 0.0f, 1.0f, 0.0f);
            }
        }
        GlStateManager.translate(0.0f, -f, 0.0f);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
        this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
    }

}