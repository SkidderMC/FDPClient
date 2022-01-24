/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.misc.NameProtect;
import net.ccbluex.liquidbounce.features.module.modules.render.NoFOV;
import net.ccbluex.liquidbounce.ui.cape.GuiCapeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends MixinEntityPlayer {

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    private void getCape(CallbackInfoReturnable<ResourceLocation> callbackInfoReturnable) {
        if(!getUniqueID().equals(Minecraft.getMinecraft().thePlayer.getUniqueID()))
            return;


        if(GuiCapeManager.INSTANCE.getNowCape()!=null)
            callbackInfoReturnable.setReturnValue(GuiCapeManager.INSTANCE.getNowCape().getResource());
    }

    @Inject(method = "getFovModifier", at = @At("HEAD"), cancellable = true)
    private void getFovModifier(CallbackInfoReturnable<Float> callbackInfoReturnable) {
        final NoFOV fovModule = LiquidBounce.moduleManager.getModule(NoFOV.class);

        if(fovModule.getState()) {
            float newFOV = fovModule.getFovValue().get();

            if(!this.isUsingItem()) {
                callbackInfoReturnable.setReturnValue(newFOV);
                return;
            }

            if(this.getItemInUse().getItem() != Items.bow) {
                callbackInfoReturnable.setReturnValue(newFOV);
                return;
            }

            int i = this.getItemInUseDuration();
            float f1 = (float) i / 20.0f;
            f1 = f1 > 1.0f ? 1.0f : f1 * f1;
            newFOV *= 1.0f - f1 * 0.15f;
            callbackInfoReturnable.setReturnValue(newFOV);
        }
    }

    @Inject(method = "getLocationSkin()Lnet/minecraft/util/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void getSkin(CallbackInfoReturnable<ResourceLocation> callbackInfoReturnable) {
        final NameProtect nameProtect = LiquidBounce.moduleManager.getModule(NameProtect.class);

        if(nameProtect.getState() && nameProtect.getSkinProtectValue().get()) {
            if (!nameProtect.getAllPlayersValue().get() && !Objects.equals(getGameProfile().getName(), Minecraft.getMinecraft().thePlayer.getGameProfile().getName()))
                return;

            callbackInfoReturnable.setReturnValue(DefaultPlayerSkin.getDefaultSkin(getUniqueID()));
        }
    }
}
