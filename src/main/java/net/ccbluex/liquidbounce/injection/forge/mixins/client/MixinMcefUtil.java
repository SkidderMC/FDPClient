/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import net.minecraft.client.Minecraft;
import net.montoyo.mcef.utilities.SizedInputStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

/**
 * The bundled in-game browser engine unconditionally probes its remote mirror for the "mcef2.new"
 * manifest on every launch. That mirror is unreachable, so the probe throws SSL/PKIX errors before
 * falling back to the local configuration. When the native runtime is already present on disk there
 * is nothing to fetch, so we short-circuit the remote stream quietly (returning null, exactly what a
 * failed fetch would yield) and let the local configuration take over without the error noise.
 */
@Mixin(targets = "net.montoyo.mcef.utilities.Util", remap = false)
public class MixinMcefUtil {

    @Inject(method = "openStream", at = @At("HEAD"), cancellable = true, remap = false)
    private static void fdp$skipDeadMirror(String url, String name, CallbackInfoReturnable<SizedInputStream> cir) {
        try {
            File dir = Minecraft.getMinecraft().mcDataDir;
            if (new File(dir, "libcef.dll").isFile() && new File(dir, "mcef2.json").isFile()) {
                cir.setReturnValue(null);
            }
        } catch (Throwable ignored) {
        }
    }
}
