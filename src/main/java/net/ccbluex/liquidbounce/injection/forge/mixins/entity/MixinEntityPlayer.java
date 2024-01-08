/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import com.mojang.authlib.GameProfile;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.combat.KeepSprint;
import net.ccbluex.liquidbounce.utils.CooldownHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The type Mixin entity player.
 */
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {

    /**
     * The Capabilities.
     */
    @Shadow
    public PlayerCapabilities capabilities;
    /**
     * The Fly toggle timer.
     */
    @Shadow
    protected int flyToggleTimer;

    @Shadow
    public abstract ItemStack getHeldItem();

    /**
     * Gets game profile.
     *
     * @return the game profile
     */
    @Shadow
    public abstract GameProfile getGameProfile();

    /**
     * Can trigger walking boolean.
     *
     * @return the boolean
     */
    @Shadow
    protected abstract boolean canTriggerWalking();

    /**
     * Gets swim sound.
     *
     * @return the swim sound
     */
    @Shadow
    protected abstract String getSwimSound();

    /**
     * Gets food stats.
     *
     * @return the food stats
     */
    @Shadow
    public abstract FoodStats getFoodStats();

    /**
     * Gets item in use duration.
     *
     * @return the item in use duration
     */
    @Shadow
    public abstract int getItemInUseDuration();

    /**
     * Gets item in use.
     *
     * @return the item in use
     */
    @Shadow
    public abstract ItemStack getItemInUse();

    /**
     * Is using item boolean.
     *
     * @return the boolean
     */
    @Shadow
    public abstract boolean isUsingItem();

    /**
     * Is player sleeping boolean.
     *
     * @return the boolean
     */
    @Shadow
    public abstract boolean isPlayerSleeping();

    @Shadow(remap = false)
    public abstract float getDefaultEyeHeight();

    /**
     * The Inventory.
     */
    @Shadow
    public InventoryPlayer inventory;
    private ItemStack cooldownStack;
    private int cooldownStackSlot;
    private final ItemStack[] mainInventory = new ItemStack[36];
    private final ItemStack[] armorInventory = new ItemStack[4];


    /**
     * @author opZywl
     * @reason Improves
     */
    @Inject(method = "dropItem", at = @At("HEAD"))
    private void dropItem(ItemStack p_dropItem_1_, boolean p_dropItem_2_, boolean p_dropItem_3_, CallbackInfoReturnable<EntityItem> cir) {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            if (this.mainInventory[i] != null) {
                this.mainInventory[i] = null;
            }
        }

        for (int j = 0; j < this.armorInventory.length; ++j) {
            if (this.armorInventory[j] != null) {
                this.armorInventory[j] = null;
            }
        }
    }
    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void injectCooldown(final CallbackInfo callbackInfo) {
        if (this.getGameProfile() == Minecraft.getMinecraft().thePlayer.getGameProfile()) {
            CooldownHelper.INSTANCE.incrementLastAttackedTicks();
            CooldownHelper.INSTANCE.updateGenericAttackSpeed(getHeldItem());

            if (cooldownStackSlot != inventory.currentItem || !ItemStack.areItemStacksEqual(cooldownStack, getHeldItem())) {
                CooldownHelper.INSTANCE.resetLastAttackedTicks();
            }

            cooldownStack = getHeldItem();
            cooldownStackSlot = inventory.currentItem;
        }
    }
    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setSprinting(Z)V", shift = At.Shift.AFTER))
    public void onAttackTargetEntityWithCurrentItem(CallbackInfo callbackInfo) {
        final KeepSprint ks = FDPClient.moduleManager.getModule(KeepSprint.class);
            if (ks.getState()) {
                final float s = 0.6f + 0.4f * ks.getS().getValue();
                this.motionX = this.motionX / 0.6 * s;
                this.motionZ = this.motionZ / 0.6 * s;
                if (Minecraft.getMinecraft().thePlayer.moveForward > 0) {
                    this.setSprinting(ks.getAws().getValue());
                }
            }
    }
}
