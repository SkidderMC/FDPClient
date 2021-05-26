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
import net.minecraft.network.Packet;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.login.server.S01PacketEncryptionRequest;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.security.PublicKey;
import java.util.ArrayList;

@SideOnly(Side.CLIENT)
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
        Display.setTitle(LiquidBounce.CLIENT_NAME + " " + LiquidBounce.CLIENT_VERSION + " | " + LiquidBounce.MINECRAFT_VERSION + (LiquidBounce.IN_DEV ? " | DEVELOPMENT BUILD" : ""));
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

//    private static Method flushOutboundQueueMethod;
//    private static Method dispatchPacketMethod;
//    private static Field readWriteLockField;
//    private static Field outboundPacketsQueueField;
//    private static Class inboundHandlerTuplePacketListenerClass;
//
//    static {
//        Class<? extends NetworkManager> clazz=NetworkManager.class;
//        for (Class clazz1:clazz.getDeclaredClasses()){
//            if(clazz1.getName().contains("InboundHandlerTuplePacketListener")){
//                inboundHandlerTuplePacketListenerClass=clazz1;
//            }
//        }
//
//        for(Method method:clazz.getDeclaredMethods()){
//            if(method.getName().equals("func_150733_h")){
//                method.setAccessible(true);
//                flushOutboundQueueMethod=method;
//            }else if(method.getName().equals("func_150732_b")){
//                method.setAccessible(true);
//                dispatchPacketMethod=method;
//            }
//        }
//
//        for (Field field:clazz.getDeclaredFields()){
//            if(field.getName().equals("field_181680_j")){
//                field.setAccessible(true);
//                readWriteLockField=field;
//            }else if(field.getName().equals("field_150745_j")){
//                field.setAccessible(true);
//                outboundPacketsQueueField=field;
//            }
//        }
//    }

    private static ArrayList<Packet> packets=new ArrayList<>();

    public static boolean handlePacket(Packet packet){
        if(packets.contains(packet)){
            packets.remove(packet);
            System.out.println("CONTAIN");
            System.out.println(packet.getClass().getName());
            return true;
        }
        return false;
    }

    public static void sendPacketNoEvent(Packet packet) {
        packets.add(packet);
        mc.getNetHandler().addToSendQueue(packet);
//        NetworkManager networkManager=mc.thePlayer.sendQueue.getNetworkManager();
//        try {
//            if(networkManager.isChannelOpen()){
//                flushOutboundQueueMethod.invoke(networkManager);
//                dispatchPacketMethod.invoke(networkManager, packet, null);
//            }else{
//                ReentrantReadWriteLock readWriteLock= (ReentrantReadWriteLock) readWriteLockField.get(networkManager);
//                readWriteLock.writeLock().lock();
//                try {
//                    ((Queue)outboundPacketsQueueField.get(networkManager))
//                            .add(inboundHandlerTuplePacketListenerClass.getConstructors()[0].newInstance(packet, null));
//                } finally {
//                    readWriteLock.writeLock().unlock();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}