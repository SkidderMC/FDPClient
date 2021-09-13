/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.render.XRay;
import net.ccbluex.liquidbounce.file.FileConfig;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraft.block.Block;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class XRayConfig extends FileConfig {

    /**
     * Constructor of config
     *
     * @param file of config
     */
    public XRayConfig(final File file) {
        super(file);
    }

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Override
    protected void loadConfig() throws IOException {
        final XRay xRay = LiquidBounce.moduleManager.getModule(XRay.class);

        final JsonArray jsonArray = new JsonParser().parse(new BufferedReader(new FileReader(getFile()))).getAsJsonArray();

        xRay.getXrayBlocks().clear();

        for(final JsonElement jsonElement : jsonArray) {
            try {
                final Block block = Block.getBlockFromName(jsonElement.getAsString());

                if (xRay.getXrayBlocks().contains(block)) {
                    ClientUtils.getLogger().error("[FileManager] Skipped xray block '" + block.getRegistryName() + "' because the block is already added.");
                    continue;
                }

                xRay.getXrayBlocks().add(block);
            }catch(final Throwable throwable) {
                ClientUtils.getLogger().error("[FileManager] Failed to add block to xray.", throwable);
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
        final XRay xRay = LiquidBounce.moduleManager.getModule(XRay.class);

        final JsonArray jsonArray = new JsonArray();

        for (final Block block : xRay.getXrayBlocks())
            jsonArray.add(FileManager.PRETTY_GSON.toJsonTree(Block.getIdFromBlock(block)));

        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getFile()), StandardCharsets.UTF_8));
        writer.write(FileManager.PRETTY_GSON.toJson(jsonArray));
        writer.close();
    }
}
