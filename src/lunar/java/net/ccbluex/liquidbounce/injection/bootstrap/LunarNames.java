/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/** Reads the member map shipped by Lunar; no Lunar classes or credentials are needed. */
public final class LunarNames {
    public static final String RESOURCE = "lunar/searge2lunar_1.8.9.kin";
    private static final int MAGIC = 0x05E8F046;

    private LunarNames() { }

    public static Map<String, String> load(ClassLoader game) throws IOException {
        InputStream resource = game.getResourceAsStream(RESOURCE);
        if (resource == null) resource = ClassLoader.getSystemResourceAsStream(RESOURCE);
        if (resource != null) {
            try (InputStream in = resource) { return read(in); }
        }
        // Some Lunar launchers expose the mappings on the process classpath only.
        for (String path : System.getProperty("java.class.path", "").split(File.pathSeparator)) {
            if (!new File(path).getName().equals("lunar-platform-mappings-v1_8.jar")) continue;
            try (ZipFile zip = new ZipFile(path)) {
                ZipEntry entry = zip.getEntry(RESOURCE);
                if (entry != null) {
                    try (InputStream in = zip.getInputStream(entry)) { return read(in); }
                }
            }
        }
        throw new IOException("Lunar Client 1.8.9 mappings were not found (" + RESOURCE + ").");
    }

    public static Map<String, String> read(InputStream source) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = source.read(buffer)) != -1) {
            if (bytes.size() + n > 32 * 1024 * 1024) throw new IOException("Lunar mapping exceeds 32 MiB");
            bytes.write(buffer, 0, n);
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        if (in.readInt() != MAGIC) throw new IOException("Unrecognized Lunar mapping header");
        in.readUnsignedByte(); // The current format has a flags/version byte before the class table.
        long count = in.readLong();
        if (count < 0 || count > in.available() / 12) throw new IOException("Invalid Lunar class count: " + count);
        Map<String, String> names = new HashMap<>();
        for (long i = 0; i < count; i++) readClass(in, names, 0);
        if (in.available() != 0) throw new IOException("Unexpected data after Lunar class table");
        if (!names.containsKey("func_71410_x")) throw new IOException("Lunar map has no Minecraft.getMinecraft mapping");
        return Collections.unmodifiableMap(names);
    }

    private static void readClass(DataInputStream in, Map<String, String> names, int depth) throws IOException {
        if (depth > 256) throw new IOException("Lunar class nesting exceeds 256 levels");
        in.readUTF();
        in.readUTF();
        int count = count(in, 12);
        for (int i = 0; i < count; i++) readClass(in, names, depth + 1);
        readMembers(in, names);
        readMembers(in, names);
    }

    private static int count(DataInputStream in, int minimumBytes) throws IOException {
        int count = in.readInt();
        if (count < 0 || count > in.available() / minimumBytes) throw new IOException("Invalid Lunar table count: " + count);
        return count;
    }

    private static void readMembers(DataInputStream in, Map<String, String> names) throws IOException {
        int count = count(in, 6);
        for (int i = 0; i < count; i++) {
            String source = in.readUTF();
            in.readUTF(); // descriptor; SRG member identifiers are globally unique
            String target = in.readUTF();
            if (!source.startsWith("func_") && !source.startsWith("field_")) continue;
            if (target.isEmpty()) throw new IOException("Empty Lunar member name for " + source);
            String previous = names.put(source, target);
            if (previous != null && !previous.equals(target)) throw new IOException("Ambiguous Lunar member: " + source);
        }
    }
}
