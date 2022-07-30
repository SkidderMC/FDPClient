package net.skiddermc.fdpclient.injection.forge.mixins.splash;

import net.minecraftforge.fml.client.SplashProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=SplashProgress.class, remap=false)
public abstract class MixinSplashProgress {



 //                pojav support    
 //   @Shadow(aliases="SplashProgress", remap=false)
 //    private static boolean enabled;
 //
 //    @Inject(method="start", at=@At(value="FIELD", target="Lnet/minecraftforge/fml/client/SplashProgress;enabled:Z", opcode=178, remap=false, ordinal=0), remap=false, require=1, allow=1)
 //    private static void start(CallbackInfo callbackInfo) {
 //       enabled = true;
 //    }

    @Inject(method="finish", at=@At(value="INVOKE", target="Lorg/lwjgl/opengl/Drawable;makeCurrent()V", shift=At.Shift.AFTER, remap=false, ordinal=0), remap=false, cancellable=true, require=1, allow=1)
    private static void finish(CallbackInfo callbackInfo) {
        callbackInfo.cancel();
    }
}
