/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import com.google.gson.JsonObject;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.login.server.S01PacketEncryptionRequest;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.security.PublicKey;

public final class ClientUtils extends MinecraftInstance {

    private static final Logger logger = LogManager.getLogger("LiquidBounce");

    private static Field fastRenderField;

    static {
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

    public static void sendEncryption(final NetworkManager networkManager, final SecretKey secretKey, final PublicKey publicKey, final S01PacketEncryptionRequest encryptionRequest) {
        networkManager.sendPacket(new C01PacketEncryptionResponse(secretKey, publicKey, encryptionRequest.getVerifyToken()), p_operationComplete_1_ -> networkManager.enableEncryption(secretKey));
    }

    public static void displayAlert(final String message){
        displayChatMessage("§8[§c§lFDP§6§lClient§8] §f"+message);
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
}