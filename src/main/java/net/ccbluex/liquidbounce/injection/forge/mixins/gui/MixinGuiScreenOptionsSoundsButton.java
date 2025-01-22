/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(GuiScreenOptionsSounds.Button.class)
public class MixinGuiScreenOptionsSoundsButton {

    @Inject(method = "mouseDragged", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", shift = At.Shift.BEFORE), cancellable = true)
    private void cancelRectangleDrawing(Minecraft p_mouseDragged_1_, int p_mouseDragged_2_, int p_mouseDragged_3_, CallbackInfo ci) {
        ci.cancel();
    }
}