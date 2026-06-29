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
 * manifest on every launch. That mirror's HTTPS cert chains to a root (ISRG) the old Java 8 bundled
 * by most launchers does not trust, so the probe throws SSL/PKIX errors. We do two things here:
 * <ul>
 *   <li>While a deliberate (re)install is in progress (the {@code fdp.mcef.installing} flag set by
 *       {@code NextGenBrowserRuntime}) every resource MUST really be fetched, so we never short-circuit -
 *       we only downgrade the fetch to plain HTTP so it works on Java 8.</li>
 *   <li>In steady state, when the WHOLE native runtime is already on disk, we serve the local config
 *       and treat any other resource as already-present (null), so the dead mirror is never re-probed.</li>
 * </ul>
 * The old guard short-circuited as soon as {@code libcef.dll} existed. Since the engine writes the
 * local config before downloading, and {@code libcef.dll} is only the 5th of 19 resources, that guard
 * fired MID-DOWNLOAD and silently nulled every remaining resource (cef.pak, icudtl.dat, the blobs,
 * locales) - leaving a broken half-install that could never start. The install flag plus the
 * full-runtime check below remove that race entirely.
 */
@Mixin(targets = "net.montoyo.mcef.utilities.Util", remap = false)
public class MixinMcefUtil {

    // Keep in sync with NextGenBrowserRuntime.MCEF_INSTALLING_PROPERTY.
    private static final String INSTALLING_PROPERTY = "fdp.mcef.installing";

    private static final String[] REQUIRED_NATIVES = {
            "jcef.dll", "libcef.dll", "icudtl.dat", "cef.pak", "snapshot_blob.bin", "natives_blob.bin"
    };

    @Inject(method = "openStream", at = @At("HEAD"), cancellable = true, remap = false)
    private static void fdp$skipDeadMirror(String url, String name, CallbackInfoReturnable<SizedInputStream> cir) {
        try {
            File dir = Minecraft.getMinecraft().mcDataDir;
            boolean installing = "true".equals(System.getProperty(INSTALLING_PROPERTY));

            // Steady state only: the runtime is fully present, so serve the local config as a normal
            // success and report every other resource as already-on-disk (null = a failed fetch), which
            // stops the engine from re-probing the dead mirror. Never do this while installing - it would
            // sabotage the in-progress download the moment the first big native lands on disk.
            if (!installing && fdp$runtimeComplete(dir)) {
                File localConfig = new File(dir, "mcef2.json");
                if (localConfig.isFile()) {
                    if (name != null && name.contains("mcef2")) {
                        cir.setReturnValue(new SizedInputStream(new FileInputStream(localConfig), localConfig.length()));
                    } else {
                        cir.setReturnValue(null);
                    }
                    return;
                }
            }

            // Fresh install / active reinstall (or any leftover HTTPS fetch): the mirror serves the exact
            // same files over plain HTTP, so downgrade and stream directly - no TLS, no cert chain, works
            // on every runtime. Scoped to the MCEF mirror only. http URLs fall through to the original.
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

    private static boolean fdp$runtimeComplete(File dir) {
        for (String fileName : REQUIRED_NATIVES) {
            File file = new File(dir, fileName);
            if (!file.isFile() || file.length() <= 0L) {
                return false;
            }
        }
        return true;
    }
}
