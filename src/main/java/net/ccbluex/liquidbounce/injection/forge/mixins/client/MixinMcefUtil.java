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
            // Already installed: serve the local config so the engine reads it as a normal success and
            // never re-probes the dead mirror (no SSL/PKIX spam, no local-fallback warning). Any other
            // resource is already on disk, so null - exactly what a failed download returns.
            if (new File(dir, "libcef.dll").isFile() && localConfig.isFile()) {
                if (name != null && name.contains("mcef2")) {
                    cir.setReturnValue(new SizedInputStream(new FileInputStream(localConfig), localConfig.length()));
                } else {
                    cir.setReturnValue(null);
                }
                return;
            }

            // Fresh install: the old Java 8 bundled by most launchers lacks the ISRG root certificate,
            // so the mirror's HTTPS cert fails PKIX validation and the whole runtime download dies
            // before it starts (even the config-manifest fetch, which ignores FORCE_MIRROR). The mirror
            // serves the exact same files over plain HTTP, so downgrade the fetch to HTTP and stream it
            // directly - no TLS, no cert chain, works on every runtime. Scoped to the MCEF mirror only.
            if (url != null && url.startsWith("https://")) {
                String httpUrl = "http://" + url.substring(8);
                java.net.URLConnection conn = new java.net.URL(httpUrl).openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                cir.setReturnValue(new SizedInputStream(conn.getInputStream(), conn.getContentLengthLong()));
            }
        } catch (Throwable ignored) {
        }
    }
}
