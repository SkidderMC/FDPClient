package net.ccbluex.liquidbounce.injection.forge.mixins.fml;


import net.ccbluex.liquidbounce.utils.CustomSplashProgress;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.Semaphore;

@Mixin(value = FMLClientHandler.class, remap = false)
public class MixinFMLClientHandler {
    @Redirect(method = "beginMinecraftLoading", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;start()V"))
    private void beginMinecraftLoading() {
        CustomSplashProgress.start();
    }

    @Redirect(method = "haltGame", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;finish()V"))
    private void haltGame() {
        CustomSplashProgress.finish();
    }

    @Redirect(method = "finishMinecraftLoading", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;finish()V"))
    private void finishMinecraftLoading() {
        CustomSplashProgress.finish();
    }

    @Redirect(method = "onInitializationComplete", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/SplashProgress;finish()V"))
    private void onInitializationComplete() {
        CustomSplashProgress.finish();
    }

    @Redirect(method = "processWindowMessages", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/Semaphore;tryAcquire()Z"))
    private boolean processWindowMessages(Semaphore instance) {
        return CustomSplashProgress.mutex.tryAcquire();
    }

    @Redirect(method = "processWindowMessages", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/Semaphore;release()V"))
    private void processWindowMessages2(Semaphore instance) {
        CustomSplashProgress.mutex.release();
    }
}
