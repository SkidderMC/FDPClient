/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.splash;

import net.minecraftforge.fml.client.SplashProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=SplashProgress.class, remap=false)
public abstract class MixinSplashProgress {

    @Inject(method={"finish"}, at={@At(value="INVOKE", target="Lorg/lwjgl/opengl/Drawable;makeCurrent()V", shift=At.Shift.AFTER, remap=false, ordinal=0)}, remap=false, cancellable=true, require=1, allow=1)
    private static void finish(CallbackInfo callbackInfo) {
        callbackInfo.cancel();
    }
}