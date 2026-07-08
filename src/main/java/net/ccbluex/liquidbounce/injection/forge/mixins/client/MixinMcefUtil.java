/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import net.minecraft.client.Minecraft;
import net.montoyo.mcef.utilities.SizedInputStream;
import net.montoyo.mcef.utilities.Log;
import net.montoyo.mcef.utilities.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The bundled in-game browser engine unconditionally probes its remote mirror for the "mcef2.new"
 * manifest on every launch. That mirror's HTTPS cert chains to a root (ISRG) the old Java 8 bundled
 * by most launchers does not trust, so the probe throws SSL/PKIX errors. We do two things here:
 * <ul>
 *   <li>While a deliberate (re)install is in progress (the {@code fdp.mcef.installing} flag set by
 *       {@code NextGenBrowserRuntime}) every resource MUST really be fetched, so we use an ordered,
 *       configurable mirror pool with HTTPS first and an HTTP compatibility fallback for Java 8.</li>
 *   <li>In steady state, when the WHOLE native runtime is already on disk, we serve the local config
 *       and treat any other resource as already-present (null), so the dead mirror is never re-probed.</li>
 * </ul>
 * The old guard short-circuited as soon as {@code libcef.dll} existed. Since the engine writes the
 * local config before downloading, and {@code libcef.dll} is only the 5th of 19 resources, that guard
 * fired MID-DOWNLOAD and silently nulled every remaining resource (cef.pak, icudtl.dat, the blobs,
 * locales) - leaving a broken half-install that could never start. The install flag plus the
 * full-runtime check below remove that race entirely.
 */
@Mixin(value = Util.class, remap = false)
public class MixinMcefUtil {

    // Keep in sync with NextGenBrowserRuntime.MCEF_INSTALLING_PROPERTY.
    private static final String INSTALLING_PROPERTY = "fdp.mcef.installing";
    private static final String MIRRORS_PROPERTY = "fdp.mcef.mirrors";
    private static final String MIRRORS_ENVIRONMENT = "FDP_MCEF_MIRRORS";

    private static final String[] DEFAULT_MIRRORS = {
            "https://montoyo.net/jcef",
            "http://montoyo.net/jcef"
    };

    private static final Set<String> BROKEN_MIRRORS = ConcurrentHashMap.newKeySet();

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

            // Active installs use an ordered mirror pool. Operators can prepend independent mirrors with
            // -Dfdp.mcef.mirrors=https://mirror-a/jcef,https://mirror-b/jcef, the FDP_MCEF_MIRRORS
            // environment variable, or config/fdpclient/mcef-mirrors.txt. The official HTTPS and HTTP
            // endpoints remain final fallbacks for old Java 8 runtimes with incomplete certificate stores.
            if (installing && url != null && !url.isEmpty()) {
                cir.setReturnValue(fdp$openFromMirrors(dir, url, name));
            }
        } catch (Throwable ignored) {
        }
    }

    private static SizedInputStream fdp$openFromMirrors(File gameDir, String resource, String name) {
        for (String mirror : fdp$mirrors(gameDir)) {
            if (BROKEN_MIRRORS.contains(mirror)) {
                continue;
            }

            String target = mirror + "/" + resource.replaceFirst("^/+", "");
            try {
                URLConnection connection = new URL(target).openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(20000);
                connection.setRequestProperty("User-Agent", "FDPClient-MCEF/1.11");

                if (connection instanceof HttpURLConnection) {
                    HttpURLConnection http = (HttpURLConnection) connection;
                    int status = http.getResponseCode();
                    if (status < 200 || status >= 300) {
                        http.disconnect();
                        throw new java.io.IOException("HTTP " + status);
                    }
                }

                InputStream stream = connection.getInputStream();
                long length = connection.getContentLengthLong();
                Log.info("[FDP] Downloading %s from MCEF mirror %s", name, mirror);
                return new SizedInputStream(stream, length);
            } catch (Throwable failure) {
                BROKEN_MIRRORS.add(mirror);
                Log.warning("[FDP] MCEF mirror unavailable for this session: %s", mirror);
            }
        }

        Log.error("[FDP] No configured MCEF mirror could provide %s", name);
        return null;
    }

    private static Set<String> fdp$mirrors(File gameDir) {
        LinkedHashSet<String> mirrors = new LinkedHashSet<>();
        fdp$addMirrors(mirrors, System.getProperty(MIRRORS_PROPERTY));
        fdp$addMirrors(mirrors, System.getenv(MIRRORS_ENVIRONMENT));

        File mirrorFile = new File(new File(new File(gameDir, "config"), "fdpclient"), "mcef-mirrors.txt");
        if (mirrorFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(mirrorFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        fdp$addMirrors(mirrors, line);
                    }
                }
            } catch (Throwable failure) {
                Log.warning("[FDP] Could not read custom MCEF mirror configuration.");
            }
        }

        for (String mirror : DEFAULT_MIRRORS) {
            mirrors.add(mirror);
        }
        return mirrors;
    }

    private static void fdp$addMirrors(Set<String> mirrors, String values) {
        if (values == null || values.trim().isEmpty()) {
            return;
        }

        for (String value : values.split("[,;\\r\\n]+")) {
            String mirror = value.trim().replaceAll("/+$", "");
            if (mirror.startsWith("https://") || mirror.startsWith("http://") || mirror.startsWith("file:/")) {
                mirrors.add(mirror);
            }
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
