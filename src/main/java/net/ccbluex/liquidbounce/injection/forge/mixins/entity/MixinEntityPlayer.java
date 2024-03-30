/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import com.mojang.authlib.GameProfile;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.combat.KeepSprint;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.CooldownHelper;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

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
     * The Inventory.
     */
    @Shadow
    public InventoryPlayer inventory;
    private ItemStack fDPClient$cooldownStack;
    private int fDPClient$cooldownStackSlot;
    private final ItemStack[] fDPClient$mainInventory = new ItemStack[36];
    private final ItemStack[] fDPClient$armorInventory = new ItemStack[4];


    /**
     * @author opZywl
     * @reason Improves
     */
    @Inject(method = "dropItem", at = @At("HEAD"))
    private void dropItem(ItemStack p_dropItem_1_, boolean p_dropItem_2_, boolean p_dropItem_3_, CallbackInfoReturnable<EntityItem> cir) {
        for (int i = 0; i < this.fDPClient$mainInventory.length; ++i) {
            if (!MinecraftInstance.mc.isIntegratedServerRunning() && ProtocolBase.getManager().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_16))
                PacketUtils.sendPacketNoEvent(new C0APacketAnimation());
            if (this.fDPClient$mainInventory[i] != null) {
                this.fDPClient$mainInventory[i] = null;
            }
        }

        for (int j = 0; j < this.fDPClient$armorInventory.length; ++j) {
            if (!MinecraftInstance.mc.isIntegratedServerRunning() && ProtocolBase.getManager().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_16))
                PacketUtils.sendPacketNoEvent(new C0APacketAnimation());
            if (this.fDPClient$armorInventory[j] != null) {
                this.fDPClient$armorInventory[j] = null;
            }
        }
    }
    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void injectCooldown(final CallbackInfo callbackInfo) {
        if (this.getGameProfile() == Minecraft.getMinecraft().thePlayer.getGameProfile()) {
            CooldownHelper.INSTANCE.incrementLastAttackedTicks();
            CooldownHelper.INSTANCE.updateGenericAttackSpeed(getHeldItem());

            if (fDPClient$cooldownStackSlot != inventory.currentItem || !ItemStack.areItemStacksEqual(fDPClient$cooldownStack, getHeldItem())) {
                CooldownHelper.INSTANCE.resetLastAttackedTicks();
            }

            fDPClient$cooldownStack = getHeldItem();
            fDPClient$cooldownStackSlot = inventory.currentItem;
        }
    }
    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setSprinting(Z)V", shift = At.Shift.AFTER))
    public void onAttackTargetEntityWithCurrentItem(CallbackInfo callbackInfo) {
        final KeepSprint ks = FDPClient.moduleManager.getModule(KeepSprint.class);
            if (Objects.requireNonNull(ks).getState()) {
                final float s = 0.6f + 0.4f * ks.getS().getValue();
                this.motionX = this.motionX / 0.6 * s;
                this.motionZ = this.motionZ / 0.6 * s;
                if (Minecraft.getMinecraft().thePlayer.moveForward > 0) {
                    this.setSprinting(ks.getAws().getValue());
                }
            }
    }
}
