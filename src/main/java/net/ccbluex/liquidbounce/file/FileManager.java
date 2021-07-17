/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.file;

import com.google.gson.*;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.special.AntiForge;
import net.ccbluex.liquidbounce.features.special.AutoReconnect;
import net.ccbluex.liquidbounce.features.special.ServerSpoof;
import net.ccbluex.liquidbounce.features.special.macro.Macro;
import net.ccbluex.liquidbounce.file.configs.*;
import net.ccbluex.liquidbounce.ui.client.GuiBackground;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.value.Value;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

public class FileManager extends MinecraftInstance {

    public final File dir = new File(mc.mcDataDir, LiquidBounce.CLIENT_NAME + "-1.8");
    public final File cacheDir = new File(mc.mcDataDir,".cache/"+LiquidBounce.CLIENT_NAME);
    public final File fontsDir = new File(dir, "fonts");
    public final File configsDir = new File(dir, "configs");
    public final File soundsDir = new File(dir, "sounds");
    public final File legacySettingsDir = new File(dir, "legacy-settings");

    public final FileConfig clickGuiConfig = new ClickGuiConfig(new File(dir, "clickgui.json"));
    public final AccountsConfig accountsConfig = new AccountsConfig(new File(dir, "accounts.json"));
    public final FriendsConfig friendsConfig = new FriendsConfig(new File(dir, "friends.json"));
    public final FileConfig xrayConfig = new XRayConfig(new File(dir, "xray-blocks.json"));
    public final FileConfig hudConfig = new HudConfig(new File(dir, "hud.json"));

    public final File backgroundFile = new File(dir, "userbackground.png");

    public boolean firstStart = false;

    public static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Constructor of file manager
     * Setup everything important
     */
    public FileManager() {
        setupFolder();
        loadBackground();
    }

    /**
     * Setup folder
     */
    public void setupFolder() {
        if(!dir.exists()) {
            dir.mkdir();
            firstStart = true;
        }

        if(!fontsDir.exists())
            fontsDir.mkdir();

        if(!configsDir.exists())
            configsDir.mkdir();

        if(!soundsDir.exists()){
            soundsDir.mkdir();
        }

        if(!cacheDir.exists()){
            cacheDir.mkdirs();
        }
    }

    /**
     * Load all configs in file manager
     */
    public void loadAllConfigs() {
        for(final Field field : getClass().getDeclaredFields()) {
            if(field.getType() == FileConfig.class) {
                try {
                    if(!field.isAccessible())
                        field.setAccessible(true);

                    final FileConfig fileConfig = (FileConfig) field.get(this);
                    loadConfig(fileConfig);
                }catch(final IllegalAccessException e) {
                    ClientUtils.getLogger().error("Failed to load config file of field " + field.getName() + ".", e);
                }
            }
        }
    }

    /**
     * Load a list of configs
     *
     * @param configs list
     */
    public void loadConfigs(final FileConfig... configs) {
        for(final FileConfig fileConfig : configs)
            loadConfig(fileConfig);
    }

    /**
     * Load one config
     *
     * @param config to load
     */
    public void loadConfig(final FileConfig config) {
        if(!config.hasConfig()) {
            ClientUtils.getLogger().info("[FileManager] Skipped loading config: " + config.getFile().getName() + ".");

            saveConfig(config, true);
            return;
        }

        try {
            config.loadConfig();
            ClientUtils.getLogger().info("[FileManager] Loaded config: " + config.getFile().getName() + ".");
        }catch(final Throwable t) {
            ClientUtils.getLogger().error("[FileManager] Failed to load config file: " + config.getFile().getName() + ".", t);
        }
    }

    /**
     * Save all configs in file manager
     */
    public void saveAllConfigs() {
        for(final Field field : getClass().getDeclaredFields()) {
            if(field.getType() == FileConfig.class) {
                try {
                    if(!field.isAccessible())
                        field.setAccessible(true);

                    final FileConfig fileConfig = (FileConfig) field.get(this);
                    saveConfig(fileConfig);
                }catch(final IllegalAccessException e) {
                    ClientUtils.getLogger().error("[FileManager] Failed to save config file of field " +
                            field.getName() + ".", e);
                }
            }
        }
    }

    /**
     * Save a list of configs
     *
     * @param configs list
     */
    public void saveConfigs(final FileConfig... configs) {
        for(final FileConfig fileConfig : configs)
            saveConfig(fileConfig);
    }

    /**
     * Save one config
     *
     * @param config to save
     */
    public void saveConfig(final FileConfig config) {
        saveConfig(config, false);
    }

    /**
     * Save one config
     *
     * @param config         to save
     * @param ignoreStarting check starting
     */
    private void saveConfig(final FileConfig config, final boolean ignoreStarting) {
        if (!ignoreStarting && LiquidBounce.INSTANCE.isStarting())
            return;

        try {
            if(!config.hasConfig())
                config.createConfig();

            config.saveConfig();
            ClientUtils.getLogger().info("[FileManager] Saved config: " + config.getFile().getName() + ".");
        }catch(final Throwable t) {
            ClientUtils.getLogger().error("[FileManager] Failed to save config file: " +
                    config.getFile().getName() + ".", t);
        }
    }

    /**
     * Load background for background
     */
    public void loadBackground() {
        if(backgroundFile.exists()) {
            try {
                final BufferedImage bufferedImage = ImageIO.read(new FileInputStream(backgroundFile));

                if(bufferedImage == null)
                    return;

                LiquidBounce.INSTANCE.setBackground(new ResourceLocation(LiquidBounce.CLIENT_NAME.toLowerCase() + "/background.png"));
                mc.getTextureManager().loadTexture(LiquidBounce.INSTANCE.getBackground(), new DynamicTexture(bufferedImage));
                ClientUtils.getLogger().info("[FileManager] Loaded background.");
            }catch(final Exception e) {
                ClientUtils.getLogger().error("[FileManager] Failed to load background.", e);
            }
        }
    }

    public boolean loadLegacy() throws FileNotFoundException {
        boolean modified=false;

        File modulesFile=new File(dir, "modules.json");
        if(modulesFile.exists()){
            modified=true;

            final JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(modulesFile)));

            for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                final Module module = LiquidBounce.moduleManager.getModule(entry.getKey());

                if (module != null) {
                    final JsonObject jsonModule = (JsonObject) entry.getValue();

                    module.setState(jsonModule.get("State").getAsBoolean());
                    module.setKeyBind(jsonModule.get("KeyBind").getAsInt());

                    if (jsonModule.has("Array"))
                        module.setArray(jsonModule.get("Array").getAsBoolean());

                    if (jsonModule.has("AutoDisable"))
                        module.setAutoDisable(EnumAutoDisableType.valueOf(jsonModule.get("AutoDisable").getAsString()));
                }
            }

            modulesFile.delete();
        }

        File valuesFile=new File(dir, "values.json");
        if(valuesFile.exists()){
            modified=true;

            final JsonObject jsonObject = new JsonParser().parse(new BufferedReader(new FileReader(valuesFile))).getAsJsonObject();

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

            valuesFile.delete();
        }

        File macrosFile=new File(dir,"macros.json");
        if(macrosFile.exists()) {
            modified = true;

            final JsonArray jsonArray = new JsonParser().parse(new BufferedReader(new FileReader(macrosFile))).getAsJsonArray();

            for(JsonElement jsonElement:jsonArray){
                JsonObject macroJson=jsonElement.getAsJsonObject();
                LiquidBounce.macroManager.getMacros()
                        .add(new Macro(macroJson.get("key").getAsInt(),macroJson.get("command").getAsString()));
            }

            macrosFile.delete();
        }


        File shortcutsFile=new File(dir,"shortcuts.json");
        if(shortcutsFile.exists())
            shortcutsFile.delete();

        return modified;
    }
}
