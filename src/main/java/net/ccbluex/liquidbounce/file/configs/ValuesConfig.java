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
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.modules.client.Target;
import net.ccbluex.liquidbounce.features.special.AntiForge;
import net.ccbluex.liquidbounce.features.special.AutoReconnect;
import net.ccbluex.liquidbounce.features.special.ServerSpoof;
import net.ccbluex.liquidbounce.file.FileConfig;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.ui.client.GuiBackground;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.features.Value;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class ValuesConfig extends FileConfig {

    /**
     * Constructor of config
     *
     * @param file of config
     */
    public ValuesConfig(final File file) {
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

        final Iterator<Map.Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();
        while(iterator.hasNext()) {
            final Map.Entry<String, JsonElement> entry = iterator.next();

            if (entry.getKey().equalsIgnoreCase("CommandPrefix")) {
                LiquidBounce.commandManager.setPrefix(entry.getValue().getAsCharacter());
            } else if (entry.getKey().equalsIgnoreCase("Target")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("Player"))
                    EntityUtils.targetPlayer = jsonValue.get("Player").getAsBoolean();
                if (jsonValue.has("Animal"))
                    EntityUtils.targetAnimals = jsonValue.get("Animal").getAsBoolean();
                if (jsonValue.has("Mob"))
                    EntityUtils.targetMobs = jsonValue.get("Mob").getAsBoolean();
                if (jsonValue.has("Invisible"))
                    EntityUtils.targetInvisible = jsonValue.get("Invisible").getAsBoolean();
                if (jsonValue.has("Dead"))
                    EntityUtils.targetDead = jsonValue.get("Dead").getAsBoolean();
            } else if (entry.getKey().equalsIgnoreCase("features")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("AntiForge"))
                    AntiForge.enabled = jsonValue.get("AntiForge").getAsBoolean();
                if (jsonValue.has("AntiForgeFML"))
                    AntiForge.blockFML = jsonValue.get("AntiForgeFML").getAsBoolean();
                if (jsonValue.has("AntiForgeProxy"))
                    AntiForge.blockProxyPacket = jsonValue.get("AntiForgeProxy").getAsBoolean();
                if (jsonValue.has("AntiForgePayloads"))
                    AntiForge.blockPayloadPackets = jsonValue.get("AntiForgePayloads").getAsBoolean();
                if (jsonValue.has("AutoReconnectDelay"))
                    AutoReconnect.INSTANCE.setDelay(jsonValue.get("AutoReconnectDelay").getAsInt());
            } else if (entry.getKey().equalsIgnoreCase("ServerSpoof")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("Enabled"))
                    ServerSpoof.enable=jsonValue.get("Enabled").getAsBoolean();
                if (jsonValue.has("ServerAddress"))
                    ServerSpoof.address =jsonValue.get("ServerAddress").getAsString();
            } else if (entry.getKey().equalsIgnoreCase("Background")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("Enabled"))
                    GuiBackground.Companion.setEnabled(jsonValue.get("Enabled").getAsBoolean());

                if (jsonValue.has("Particles"))
                    GuiBackground.Companion.setParticles(jsonValue.get("Particles").getAsBoolean());
            } else {
                final Module module = LiquidBounce.moduleManager.getModule(entry.getKey());

                if(module != null) {
                    final JsonObject jsonModule = (JsonObject) entry.getValue();

                    for(final Value moduleValue : module.getValues()) {
                        final JsonElement element = jsonModule.get(moduleValue.getName());

                        if(element != null) moduleValue.fromJson(element);
                    }
                }
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
        //change targets data to module
        final Target target=(Target) LiquidBounce.moduleManager.getModule(Target.class);
        if(target!=null){
            target.getPlayer().set(EntityUtils.targetPlayer);
            target.getAnimal().set(EntityUtils.targetAnimals);
            target.getMob().set(EntityUtils.targetMobs);
            target.getInvisible().set(EntityUtils.targetInvisible);
            target.getDead().set(EntityUtils.targetDead);
        }

        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("CommandPrefix", LiquidBounce.commandManager.getPrefix());

        final JsonObject jsonFeatures = new JsonObject();
        jsonFeatures.addProperty("AntiForge", AntiForge.enabled);
        jsonFeatures.addProperty("AntiForgeFML", AntiForge.blockFML);
        jsonFeatures.addProperty("AntiForgeProxy", AntiForge.blockProxyPacket);
        jsonFeatures.addProperty("AntiForgePayloads", AntiForge.blockPayloadPackets);
        jsonFeatures.addProperty("AutoReconnectDelay", AutoReconnect.INSTANCE.getDelay());
        jsonObject.add("features", jsonFeatures);

        final JsonObject serverSpoofObject = new JsonObject();
        serverSpoofObject.addProperty("Enabled", ServerSpoof.enable);
        serverSpoofObject.addProperty("ServerAddress", ServerSpoof.address);
        jsonObject.add("ServerSpoof", serverSpoofObject);

        final JsonObject backgroundObject = new JsonObject();
        backgroundObject.addProperty("Enabled", GuiBackground.Companion.getEnabled());
        backgroundObject.addProperty("Particles", GuiBackground.Companion.getParticles());
        jsonObject.add("Background", backgroundObject);

        LiquidBounce.moduleManager.getModules().stream().filter(module -> !module.getValues().isEmpty()).forEach(module -> {
            final JsonObject jsonModule = new JsonObject();
            module.getValues().forEach(value -> jsonModule.add(value.getName(), value.toJson()));
            jsonObject.add(module.getName(), jsonModule);
        });

        final PrintWriter printWriter = new PrintWriter(new FileWriter(getFile()));
        printWriter.println(FileManager.PRETTY_GSON.toJson(jsonObject));
        printWriter.close();
    }
}
