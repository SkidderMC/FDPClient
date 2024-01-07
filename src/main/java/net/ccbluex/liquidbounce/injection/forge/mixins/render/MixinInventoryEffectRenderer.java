/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.injection.forge.mixins.gui.MixinGuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * The type Mixin inventory effect renderer.
 */
@Mixin(InventoryEffectRenderer.class)
public abstract class MixinInventoryEffectRenderer extends MixinGuiContainer {

    @Shadow
    private boolean hasActivePotionEffects;

    /**
     * Update active potion effects.
     *
     * @author opZywl
     * @reason Effects
     */
    @Overwrite
    public void updateActivePotionEffects() {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.hasActivePotionEffects = !this.mc.thePlayer.getActivePotionEffects().isEmpty();
    }
}