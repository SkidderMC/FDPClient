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
import java.io.FileInputStream;

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
            File localConfig = new File(dir, "mcef2.json");
            if (!new File(dir, "libcef.dll").isFile() || !localConfig.isFile()) {
                return;
            }

            // The only remote fetch performed once the runtime is on disk is the config manifest.
            // Serve the local copy so the engine reads it as a normal success (no dead-mirror probe,
            // no SSL/PKIX spam, not even the local-fallback warning). Anything else yields null, which
            // is exactly what a failed download returns, since every resource is already present.
            if (name != null && name.contains("mcef2")) {
                cir.setReturnValue(new SizedInputStream(new FileInputStream(localConfig), localConfig.length()));
            } else {
                cir.setReturnValue(null);
            }
        } catch (Throwable ignored) {
        }
    }
}
