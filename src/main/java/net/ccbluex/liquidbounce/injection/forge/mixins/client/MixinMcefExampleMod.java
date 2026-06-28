/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The bundled in-game browser engine ships a demo mod that registers itself on startup and, on a
 * client tick, opens its own example browser screen. That demo screen was built against a different
 * mappings set and references a field that does not exist here, so it dies with
 * {@code NoSuchFieldError: fontRenderer} and takes the game down with it.
 *
 * The client never uses the demo - it drives the browser itself - so we neutralize the demo's tick
 * handler: with it cancelled at the head, the demo can never open its screen and cannot crash.
 * {@code require = 0} keeps this defensive: if a future engine build drops or renames the demo, the
 * absent injection simply does nothing instead of breaking startup.
 */
@Mixin(targets = "net.montoyo.mcef.example.ExampleMod", remap = false)
public class MixinMcefExampleMod {

    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void fdp$disableExampleBrowser(CallbackInfo ci) {
        ci.cancel();
    }
}
