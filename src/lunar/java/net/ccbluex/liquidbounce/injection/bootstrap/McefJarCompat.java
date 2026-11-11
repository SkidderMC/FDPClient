/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/** Repairs JCEF's MCP 1.12 renderer before ForgeGradle applies the 1.8.9 SRG mappings. */
public final class McefJarCompat {
    private McefJarCompat() { }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Expected the packaged client JAR");
        Path artifact = Paths.get(args[0]).toAbsolutePath();
        Path temporary = Files.createTempFile(artifact.getParent(), "fdp-mcef-", ".jar");
        boolean found = false;
        try {
            try (JarFile source = new JarFile(artifact.toFile());
                 JarOutputStream target = new JarOutputStream(Files.newOutputStream(temporary))) {
                Enumeration<JarEntry> entries = source.entries();
                byte[] buffer = new byte[65536];
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    target.putNextEntry(new JarEntry(entry.getName()));
                    if (!entry.isDirectory()) {
                        try (InputStream input = source.getInputStream(entry)) {
                            if (entry.getName().equals("org/cef/browser/CefRenderer.class")) {
                                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                int length;
                                while ((length = input.read(buffer)) != -1) bytes.write(buffer, 0, length);
                                target.write(LunarMcefTransformer.transform(bytes.toByteArray()));
                                found = true;
                            } else {
                                int length;
                                while ((length = input.read(buffer)) != -1) target.write(buffer, 0, length);
                            }
                        }
                    }
                    target.closeEntry();
                }
            }
            if (!found) throw new IOException("The packaged client has no JCEF renderer");
            Files.move(temporary, artifact, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
