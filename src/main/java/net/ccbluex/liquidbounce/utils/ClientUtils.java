/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import com.google.gson.JsonObject;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.lang.reflect.Field;

public final class ClientUtils extends MinecraftInstance {

    private static final Logger logger = LogManager.getLogger("FDPClient");

    private static Field fastRenderField;

    public static final EnumOSType osType;

    static {
        String os=System.getProperty("os.name").toLowerCase();
        if(os.contains("win")){
            osType=EnumOSType.WINDOWS;
        }else if(os.contains("mac")){
            osType=EnumOSType.MACOS;
        }else if(os.contains("nix") || os.contains("nux") || os.contains("aix")){
            osType=EnumOSType.LINUX;
        }else {
            osType=EnumOSType.UNKNOWN;
        }

        try {
            fastRenderField = GameSettings.class.getDeclaredField("ofFastRender");

            if(!fastRenderField.isAccessible())
                fastRenderField.setAccessible(true);
        }catch(final NoSuchFieldException ignored) {
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void logInfo(String msg){
        logger.info(msg);
    }

    public static void logWarn(String msg){
        logger.warn(msg);
    }

    public static void logError(String msg){
        logger.error(msg);
    }

    public static void logDebug(String msg){
        logger.debug(msg);
    }

    public static void setTitle(){
        Display.setTitle(LiquidBounce.CLIENT_NAME + " " + LiquidBounce.CLIENT_VERSION + " | Mc " + LiquidBounce.MINECRAFT_VERSION);
    }

    public static void disableFastRender() {
        try {
            if(fastRenderField != null) {
                if(!fastRenderField.isAccessible())
                    fastRenderField.setAccessible(true);

                fastRenderField.setBoolean(mc.gameSettings, false);
            }
        }catch(final IllegalAccessException ignored) {
        }
    }

    public static void displayAlert(final String message){
        displayChatMessage("§8["+LiquidBounce.COLORED_NAME+"§8] §f"+message);
    }

    public static void displayChatMessage(final String message) {
        if (mc.thePlayer == null) {
            getLogger().info("(MCChat)" + message);
            return;
        }

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", message);

        mc.thePlayer.addChatMessage(IChatComponent.Serializer.jsonToComponent(jsonObject.toString()));
    }

    public enum EnumOSType {
        WINDOWS("win"),
        LINUX("linux"),
        MACOS("mac"),
        UNKNOWN("unk");

        public final String friendlyName;

        EnumOSType(String friendlyName){
            this.friendlyName=friendlyName;
        }
    }
}