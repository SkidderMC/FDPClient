/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import java.io.*;
import java.util.Map;
import java.util.regex.*;

/** Preserves modified UTF-8 (including Kotlin metadata) while remapping SRG identifiers. */
public final class ClassFileNames {
    private static final Pattern MEMBER = Pattern.compile("(?<![A-Za-z0-9_$])(?:func|field)_[0-9]+_[A-Za-z_]+(?![A-Za-z0-9_$])");

    private ClassFileNames() { }

    public static byte[] remap(byte[] original, Map<String, String> names) throws IOException {
        return remap(original, names, false);
    }

    public static byte[] remap(byte[] original, Map<String, String> names, boolean privateMixin) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(original));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(original.length + 256);
        DataOutputStream out = new DataOutputStream(bytes);
        int magic = in.readInt();
        if (magic != 0xCAFEBABE) throw new IOException("Invalid class file");
        out.writeInt(magic);
        out.writeInt(in.readInt());
        int count = in.readUnsignedShort();
        out.writeShort(count);
        for (int i = 1; i < count; i++) {
            int tag = in.readUnsignedByte();
            out.writeByte(tag);
            int length;
            switch (tag) {
                case 1:
                    String value = in.readUTF();
                    String target = names.get(value);
                    if (target == null && (value.contains("func_") || value.contains("field_"))) {
                        Matcher matcher = MEMBER.matcher(value);
                        StringBuffer replaced = new StringBuffer();
                        while (matcher.find()) {
                            String name = names.get(matcher.group());
                            matcher.appendReplacement(replaced, Matcher.quoteReplacement(name == null ? matcher.group() : name));
                        }
                        matcher.appendTail(replaced);
                        target = replaced.toString();
                    }
                    String renamed = target == null ? value : target;
                    if (privateMixin) renamed = renamed
                        // Keep the search prefix split so Shadow cannot relocate the search text itself.
                        .replace("org/".concat("spongepowered/asm/"), "net/ccbluex/liquidbounce/libraries/mixin/")
                        .replace("org.".concat("spongepowered.asm."), "net.ccbluex.liquidbounce.libraries.mixin.")
                        .replace("org/".concat("objectweb/asm/"), "net/ccbluex/liquidbounce/libraries/asm/")
                        .replace("org.".concat("objectweb.asm."), "net.ccbluex.liquidbounce.libraries.asm.")
                        .replace("okhttp3/", "net/ccbluex/liquidbounce/libraries/lunar/okhttp3/")
                        .replace("okhttp3.", "net.ccbluex.liquidbounce.libraries.lunar.okhttp3.")
                        .replace("okio/", "net/ccbluex/liquidbounce/libraries/lunar/okio/")
                        .replace("okio.", "net.ccbluex.liquidbounce.libraries.lunar.okio.")
                        .replace("jdk/nashorn/", "org/openjdk/nashorn/")
                        .replace("jdk.nashorn.", "org.openjdk.nashorn.")
                        .replace("jdk/internal/dynalink/beans/StaticClass", "jdk/dynalink/beans/StaticClass")
                        .replace("jdk.internal.dynalink.beans.StaticClass", "jdk.dynalink.beans.StaticClass");
                    if (privateMixin && (renamed.equals("okio") || renamed.equals("okhttp3"))) renamed = "net.ccbluex.liquidbounce.libraries.lunar." + renamed;
                    out.writeUTF(renamed);
                    continue;
                case 3: case 4: case 9: case 10: case 11: case 12: case 17: case 18:
                    length = 4; break;
                case 5: case 6:
                    length = 8; i++; break;
                case 7: case 8: case 16: case 19: case 20:
                    length = 2; break;
                case 15:
                    length = 3; break;
                default:
                    throw new IOException("Unknown constant pool tag " + tag);
            }
            for (int j = 0; j < length; j++) out.writeByte(in.readUnsignedByte());
        }
        int offset = original.length - in.available();
        out.write(original, offset, original.length - offset);
        return bytes.toByteArray();
    }
}
