/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.event.AttackEvent;
import net.ccbluex.liquidbounce.event.ClickWindowEvent;
import net.ccbluex.liquidbounce.event.ClientSlotChangeEvent;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.features.module.modules.exploit.AbortBreaking;
import net.ccbluex.liquidbounce.utils.attack.CooldownHelper;
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar;
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
@SideOnly(Side.CLIENT)
public class MixinPlayerControllerMP {

    @Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;syncCurrentPlayItem()V"), cancellable = true)
    private void attackEntity(EntityPlayer entityPlayer, Entity targetEntity, CallbackInfo callbackInfo) {
        final AttackEvent event = new AttackEvent(targetEntity);
        EventManager.INSTANCE.call(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
            return;
        }

        CooldownHelper.INSTANCE.resetLastAttackedTicks();
    }

    @Inject(method = "getIsHittingBlock", at = @At("HEAD"), cancellable = true)
    private void getIsHittingBlock(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (AbortBreaking.INSTANCE.handleEvents()) callbackInfoReturnable.setReturnValue(false);
    }

    @Inject(method = "windowClick", at = @At("HEAD"), cancellable = true)
    private void windowClick(int windowId, int slotId, int mouseButtonClicked, int mode, EntityPlayer playerIn, CallbackInfoReturnable<ItemStack> callbackInfo) {
        final ClickWindowEvent event = new ClickWindowEvent(windowId, slotId, mouseButtonClicked, mode);
        EventManager.INSTANCE.call(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
            return;
        }

        // Only reset click delay, if a click didn't get canceled
        InventoryUtils.INSTANCE.getCLICK_TIMER().reset();
    }

    @Redirect(method = "syncCurrentPlayItem", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/InventoryPlayer;currentItem:I"))
    private int hookSilentHotbarA(InventoryPlayer instance) {
        SilentHotbar silentHotbar = SilentHotbar.INSTANCE;

        int prevSlot = instance.currentItem;
        int serverSlot = silentHotbar.getCurrentSlot();

        ClientSlotChangeEvent event = new ClientSlotChangeEvent(prevSlot, serverSlot);
        EventManager.INSTANCE.call(event);

        return event.getModifiedSlot();
    }

    @Redirect(method = "sendUseItem", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/InventoryPlayer;currentItem:I"))
    private int hookSilentHotbarB(InventoryPlayer instance) {
        return SilentHotbar.INSTANCE.getCurrentSlot();
    }
}
