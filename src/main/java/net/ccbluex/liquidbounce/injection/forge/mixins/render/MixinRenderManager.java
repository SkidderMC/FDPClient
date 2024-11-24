/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.features.module.modules.combat.HitBox;
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeCam;
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity;
import net.ccbluex.liquidbounce.utils.PacketUtilsKt;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
@SideOnly(Side.CLIENT)
public abstract class MixinRenderManager {

    @Shadow
    public double renderPosX;

    @Shadow
    public double renderPosY;

    @Shadow
    public double renderPosZ;

    @Redirect(method = "renderDebugBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getEntityBoundingBox()Lnet/minecraft/util/AxisAlignedBB;", ordinal = 0), require = 1, allow = 1)
    private AxisAlignedBB getEntityBoundingBox(Entity entity) {
        final HitBox hitBox = HitBox.INSTANCE;

        if (!hitBox.handleEvents()) {
            return entity.getEntityBoundingBox();
        }

        float size = hitBox.determineSize(entity);
        return entity.getEntityBoundingBox().expand(size, size, size);
    }

    @Inject(method = "renderEntityStatic", at = @At(value = "HEAD"))
    private void renderEntityStatic(Entity entity, float tickDelta, boolean bool, CallbackInfoReturnable<Boolean> cir) {
        FreeCam.INSTANCE.restoreOriginalPosition();

        if (entity instanceof EntityPlayerSP)
            return;

        if (entity instanceof EntityLivingBase) {
            IMixinEntity iEntity = (IMixinEntity) entity;

            if (iEntity.getTruePos()) {
                PacketUtilsKt.interpolatePosition(iEntity);
            }
        }
    }

    @Inject(method = "renderEntityStatic", at = @At("TAIL"))
    private void injectFreeCam(Entity p_renderEntityStatic_1_, float p_renderEntityStatic_2_, boolean p_renderEntityStatic_3_, CallbackInfoReturnable<Boolean> cir) {
        FreeCam.INSTANCE.useModifiedPosition();
    }
}