/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.item;

import net.ccbluex.liquidbounce.injection.access.IItemStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The type Mixin item stack.
 */
@Mixin(ItemStack.class)
public class MixinItemStack implements IItemStack {
    @Unique
    private long fDPClient$itemDelay;
    @Unique
    private String fDPClient$cachedDisplayName;

    @Inject(method = "<init>(Lnet/minecraft/item/Item;IILnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void init(final CallbackInfo callbackInfo) {
        this.fDPClient$itemDelay = System.currentTimeMillis();
    }

    @Override
    public long getItemDelay() {
        return fDPClient$itemDelay;
    }

    @Redirect(
            method = "getTooltip",
            at = @At(value = "INVOKE", target = "Ljava/lang/Integer;toHexString(I)Ljava/lang/String;")
    )
    private String fixHexColorString(int i) {
        return String.format("%06X", i);
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void returnCachedDisplayName(CallbackInfoReturnable<String> cir) {
        if (fDPClient$cachedDisplayName != null) {
            cir.setReturnValue(fDPClient$cachedDisplayName);
        }
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"))
    private void cacheDisplayName(CallbackInfoReturnable<String> cir) {
        fDPClient$cachedDisplayName = cir.getReturnValue();
    }

    @Inject(method = "setStackDisplayName", at = @At("HEAD"))
    private void resetCachedDisplayName(String displayName, CallbackInfoReturnable<ItemStack> cir) {
        fDPClient$cachedDisplayName = null;
    }
}