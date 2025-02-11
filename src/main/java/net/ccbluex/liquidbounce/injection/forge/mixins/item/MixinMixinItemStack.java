/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.item;


import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow;
import net.ccbluex.liquidbounce.injection.implementations.IMixinItemStack;
import net.ccbluex.liquidbounce.utils.client.ClassUtils;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.ccbluex.liquidbounce.utils.client.MinecraftInstance.mc;

@Mixin(ItemStack.class)
public class MixinMixinItemStack implements IMixinItemStack {

    @Shadow private Item item;
    private long itemDelay;

    @Inject(method = "<init>(Lnet/minecraft/item/Item;IILnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void init(final CallbackInfo callbackInfo) {
        itemDelay = System.currentTimeMillis();
    }

    @Override
    public long getItemDelay() {
        return itemDelay;
    }

    @Inject(method = "getItemUseAction", at = @At("RETURN"), cancellable = true)
    private void getItemUseAction(CallbackInfoReturnable<EnumAction> cir) {
        final KillAura killAura = KillAura.INSTANCE;
        final NoSlow noSlow = NoSlow.INSTANCE;

        if ((Object) this == mc.getItemRenderer().itemToRender) {
            boolean isForceBlocking = (item instanceof ItemSword && !killAura.getAutoBlock().equals("Off") &&
                    (killAura.getRenderBlocking() || killAura.getTarget() != null && (killAura.getBlinkAutoBlock() || killAura.getForceBlockRender()))
                    || noSlow.isUNCPBlocking()) && ClassUtils.INSTANCE.hasClass("com.orangemarshall.animations.BlockhitAnimation");

            if (isForceBlocking) {
                cir.setReturnValue(EnumAction.BLOCK);
            }
        }
    }
}