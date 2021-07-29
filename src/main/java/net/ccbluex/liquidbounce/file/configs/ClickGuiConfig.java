/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.file.FileConfig;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.ui.client.clickgui.Panel;
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.Element;
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ModuleElement;
import net.ccbluex.liquidbounce.utils.ClientUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ClickGuiConfig extends FileConfig {

    /**
     * Constructor of config
     *
     * @param file of config
     */
    public ClickGuiConfig(final File file) {
        super(file);
    }

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Override
    protected void loadConfig() throws IOException {
        final JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(getFile())));

        if(jsonElement instanceof JsonNull)
            return;

        final JsonObject jsonObject = (JsonObject) jsonElement;

        for (final Panel panel : LiquidBounce.clickGui.panels) {
            if(!jsonObject.has(panel.getCategory().getConfigName()))
                continue;

            try {
                final JsonObject panelObject = jsonObject.getAsJsonObject(panel.getCategory().getConfigName());

                panel.setOpen(panelObject.get("open").getAsBoolean());
                panel.setVisible(panelObject.get("visible").getAsBoolean());
                panel.setX(panelObject.get("posX").getAsInt());
                panel.setY(panelObject.get("posY").getAsInt());

                for(final Element element : panel.getElements()) {
                    if(!(element instanceof ModuleElement))
                        continue;

                    final ModuleElement moduleElement = (ModuleElement) element;

                    if(!panelObject.has(moduleElement.getModule().getName()))
                        continue;

                    try {
                        final JsonObject elementObject = panelObject.getAsJsonObject(moduleElement.getModule().getName());

                        moduleElement.setShowSettings(elementObject.get("Settings").getAsBoolean());
                    }catch(final Exception e) {
                        ClientUtils.getLogger().error("Error while loading clickgui module element with the name '" + moduleElement.getModule().getName() + "' (Panel Name: " + panel.getCategory().getConfigName() + ").", e);
                    }
                }
            }catch(final Exception e) {
                ClientUtils.getLogger().error("Error while loading clickgui panel with the name '" + panel.getCategory().getConfigName() + "'.", e);
            }
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Override
    protected void saveConfig() throws IOException {
        final JsonObject jsonObject = new JsonObject();

        for (final Panel panel : LiquidBounce.clickGui.panels) {
            final JsonObject panelObject = new JsonObject();

            panelObject.addProperty("open", panel.getOpen());
            panelObject.addProperty("visible", panel.isVisible());
            panelObject.addProperty("posX", panel.getX());
            panelObject.addProperty("posY", panel.getY());

            for(final Element element : panel.getElements()) {
                if(!(element instanceof ModuleElement))
                    continue;

                final ModuleElement moduleElement = (ModuleElement) element;

                final JsonObject elementObject = new JsonObject();

                elementObject.addProperty("Settings", moduleElement.isShowSettings());

                panelObject.add(moduleElement.getModule().getName(), elementObject);
            }

            jsonObject.add(panel.getCategory().getConfigName(), panelObject);
        }

        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getFile()), StandardCharsets.UTF_8));
        writer.write(FileManager.PRETTY_GSON.toJson(jsonObject));
        writer.close();
    }
}