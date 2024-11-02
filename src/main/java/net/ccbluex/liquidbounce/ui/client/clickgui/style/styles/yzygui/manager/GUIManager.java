/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category.yzyCategory;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.utils.FileUtils;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.utils.JsonUtils;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.utils.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author opZywl - yzyGUI Manager
 */
public final class GUIManager {

    private final Map<yzyCategory, Pair<Integer, Integer>> positions = new HashMap<>();
    private final Map<yzyCategory, Boolean> extendeds = new HashMap<>();

    private final File guiDir = new File(FileManager.INSTANCE.getDir(), "zywlgui");

    public File getCategoryFile(final yzyCategory category) {
        return new File(guiDir, category.name().toLowerCase() + ".zywl");
    }

    public void register() {
        Arrays.asList(yzyCategory.values()).forEach(category -> {
            final File categoryFile = this.getCategoryFile(category);

            if (categoryFile.exists()) {
                try (final Reader reader = new FileReader(categoryFile)) {
                    final JsonElement element = new JsonParser().parse(reader);

                    if (element.isJsonObject()) {
                        final JsonObject object = element.getAsJsonObject();

                        for (final Map.Entry<String, JsonElement> entry : object.entrySet()) {
                            switch (entry.getKey()) {
                                case "x": {
                                    final int positionX = entry.getValue().getAsInt();

                                    positions.put(category, new Pair<>(positionX, null));

                                    break;
                                }

                                case "y": {
                                    final int positionY = entry.getValue().getAsInt();

                                    final Pair<Integer, Integer> positions = this.positions.get(category);

                                    positions.setValue(positionY);

                                    break;
                                }

                                case "extended": {
                                    final boolean extended = entry.getValue().getAsBoolean();

                                    extendeds.put(category, extended);

                                    break;
                                }
                            }
                        }
                    }
                } catch (final IOException exception) {
                    this.save(category);
                }
            }
        });
    }

    public void save(final yzyCategory category) {
        final File categoryFile = this.getCategoryFile(category);

        FileUtils.delete(categoryFile);

        try {
            if (categoryFile.createNewFile()) {
                final JsonObject object = new JsonObject();

                object.addProperty("x", positions.get(category).getKey());
                object.addProperty("y", positions.get(category).getValue());
                object.addProperty("extended", extendeds.get(category));

                FileUtils.write(categoryFile, JsonUtils.PRETTY_GSON.toJson(object));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        Arrays.stream(yzyCategory.values()).forEach(this::save);
    }

    public boolean isExtended(final yzyCategory category) {
        return extendeds.getOrDefault(category, false);
    }

    public Pair<Integer, Integer> getPositions(final yzyCategory category) {
        return positions.get(category);
    }

    public Map<yzyCategory, Pair<Integer, Integer>> getPositions() {
        return positions;
    }

    public Map<yzyCategory, Boolean> getExtendeds() {
        return extendeds;
    }

}
