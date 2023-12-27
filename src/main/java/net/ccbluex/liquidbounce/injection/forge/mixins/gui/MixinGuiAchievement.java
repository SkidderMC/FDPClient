package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.client.VanillaTweaks;
import net.minecraft.client.gui.achievement.GuiAchievement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiAchievement.class)
public class MixinGuiAchievement {
    @Inject(method = "updateAchievementWindow", at = @At("HEAD"), cancellable = true)
    private void injectAchievements(CallbackInfo ci) {
        VanillaTweaks vanillaTweaks = FDPClient.moduleManager.getModule(VanillaTweaks.class);

        if (vanillaTweaks != null) {
            vanillaTweaks.getNoAchievements();
            if (vanillaTweaks.getNoAchievements().get()) {
                ci.cancel();
            }
        }
    }
}
