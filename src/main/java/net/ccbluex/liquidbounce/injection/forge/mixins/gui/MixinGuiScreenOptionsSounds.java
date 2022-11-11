package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.GuiScreenOptionsSounds$Button")
public class MixinGuiScreenOptionsSounds {

    // don't send a packet for every frame the slider is dragged, instead save that for when the slider is released
    @Redirect(method = "mouseDragged(Lnet/minecraft/client/Minecraft;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/GameSettings;saveOptions()V"))
    private void cancelSaving(GameSettings instance) {
        // no-op
    }

    @Inject(method = "mouseReleased(II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundHandler;playSound(Lnet/minecraft/client/audio/ISound;)V"))
    private void save(int mouseX, int mouseY, CallbackInfo ci) {
        Minecraft.getMinecraft().gameSettings.saveOptions();
    }
}
