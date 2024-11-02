/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileUtils {

    @SuppressWarnings("all")
    public static void delete(final File file) {
        file.delete();
    }

    public static void write(final File file, final String text) {
        final PrintWriter writer;
        try {
            writer = new PrintWriter(
                    new FileWriter(file, true),
                    true
            );

            writer.println(text);
            writer.close();
        } catch (final IOException ignored) {
        }
    }

    public static List<String> getLines(final File file) {
        final Stream<String> stream;
        try {
            stream = Files.lines(Paths.get(file.getPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final List<String> lines = stream.collect(Collectors.toList());

        stream.close();

        return lines;
    }

}
