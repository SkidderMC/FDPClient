/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.splash;

import net.ccbluex.liquidbounce.utils.SplashProgress;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets="net.minecraftforge.fml.client.SplashProgress$3", remap=false)
public class MixinSplashProgressRunnable {
    @Inject(method="run()V", at=@At(value="HEAD"), remap=false, cancellable=true)
    private void run(@NotNull CallbackInfo callbackInfo) {
        callbackInfo.cancel();
        SplashProgress.INSTANCE.drawSplash();
    }
}
