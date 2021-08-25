/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file;

import com.google.gson.*;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.macro.Macro;
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.file.configs.*;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.value.Value;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;

public class FileManager extends MinecraftInstance {

    public final File dir = new File(mc.mcDataDir, LiquidBounce.CLIENT_NAME + "-1.8");
    public final File cacheDir = new File(mc.mcDataDir,".cache/"+LiquidBounce.CLIENT_NAME);
    public final File fontsDir = new File(dir, "fonts");
    public final File configsDir = new File(dir, "configs");
    public final File soundsDir = new File(dir, "sounds");
    public final File legacySettingsDir = new File(dir, "legacy-settings");
    public final File capesDir = new File(dir, "capes");

    public final AccountsConfig accountsConfig = new AccountsConfig(new File(dir, "accounts.json"));
    public final FriendsConfig friendsConfig = new FriendsConfig(new File(dir, "friends.json"));
    public final XRayConfig xrayConfig = new XRayConfig(new File(dir, "xray-blocks.json"));
    public final HudConfig hudConfig = new HudConfig(new File(dir, "hud.json"));
    public final SpecialConfig specialConfig = new SpecialConfig(new File(dir, "special.json"));

    public final File backgroundFile = new File(dir, "userbackground.png");

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
        if(!dir.exists())
            dir.mkdir();

        if(!fontsDir.exists())
            fontsDir.mkdir();

        if(!configsDir.exists())
            configsDir.mkdir();

        if(!soundsDir.exists())
            soundsDir.mkdir();

        if(!capesDir.exists())
            capesDir.mkdir();

        if(!cacheDir.exists())
            cacheDir.mkdirs();
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
        saveConfig(config, true);
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

    public boolean loadLegacy() throws IOException {
        boolean modified=false;

        File modulesFile=new File(dir, "modules.json");
        if(modulesFile.exists()){
            modified=true;
            FileReader fr=new FileReader(modulesFile);

            try {
                final JsonElement jsonElement = new JsonParser().parse(new BufferedReader(fr));

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
            } catch (Throwable t){
                t.printStackTrace();
            }

            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ClientUtils.logInfo("Deleted Legacy config "+modulesFile.getName()+" "+modulesFile.delete());
        }

        File valuesFile=new File(dir, "values.json");
        if(valuesFile.exists()){
            modified=true;
            FileReader fr=new FileReader(valuesFile);

            try {
                final JsonObject jsonObject = new JsonParser().parse(new BufferedReader(fr)).getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    final Module module = LiquidBounce.moduleManager.getModule(entry.getKey());

                    if (module != null) {
                        final JsonObject jsonModule = (JsonObject) entry.getValue();

                        for (final Value moduleValue : module.getValues()) {
                            final JsonElement element = jsonModule.get(moduleValue.getName());

                            if (element != null) moduleValue.fromJson(element);
                        }
                    }
                }
            } catch (Throwable t){
                t.printStackTrace();
            }

            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ClientUtils.logInfo("Deleted Legacy config "+valuesFile.getName()+" "+valuesFile.delete());
        }

        File macrosFile=new File(dir,"macros.json");
        if(macrosFile.exists()) {
            modified = true;
            FileReader fr=new FileReader(macrosFile);

            try {
                final JsonArray jsonArray = new JsonParser().parse(new BufferedReader(fr)).getAsJsonArray();

                for (JsonElement jsonElement : jsonArray) {
                    JsonObject macroJson = jsonElement.getAsJsonObject();
                    LiquidBounce.macroManager.getMacros()
                            .add(new Macro(macroJson.get("key").getAsInt(), macroJson.get("command").getAsString()));
                }
            } catch (Throwable t){
                t.printStackTrace();
            }

            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ClientUtils.logInfo("Deleted Legacy config "+macrosFile.getName()+" "+macrosFile.delete());
        }


        File shortcutsFile=new File(dir,"shortcuts.json");
        if(shortcutsFile.exists())
            shortcutsFile.delete();

        return modified;
    }
}
