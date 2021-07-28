package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanguageManager.class)
public class MixinLanguageManager {
    @Inject(method = "setCurrentLanguage", at = @At("HEAD"))
    public void setCurrentLanguage(Language p_setCurrentLanguage_1_, CallbackInfo ci) {
        net.ccbluex.liquidbounce.ui.i18n.LanguageManager.INSTANCE.switchLanguage(p_setCurrentLanguage_1_.getLanguageCode());
    }
}
