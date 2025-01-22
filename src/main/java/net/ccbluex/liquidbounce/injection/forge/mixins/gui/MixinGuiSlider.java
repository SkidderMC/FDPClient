/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.minecraft.client.gui.GuiSlider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiSlider.class)
public class MixinGuiSlider {

    @Redirect(method = "mouseDragged", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSlider;drawTexturedModalRect(IIIIII)V"), require = 2)
    public void cancelRectangleDrawing(GuiSlider guiSlider, int x, int y, int textureX, int textureY, int width, int height) {
    }

}