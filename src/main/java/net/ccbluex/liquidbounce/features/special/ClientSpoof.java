/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.special;

import io.netty.buffer.Unpooled;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Listenable;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

public class ClientSpoof extends MinecraftInstance implements Listenable {

    public static final boolean enabled = true;

    @EventTarget
    public void handle(final PacketEvent event) {
        final Packet<?> packet = event.getPacket();
        final net.ccbluex.liquidbounce.features.module.modules.client.ClientSpoof clientSpoof = FDPClient.moduleManager.getModule(net.ccbluex.liquidbounce.features.module.modules.client.ClientSpoof.class);

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning() && clientSpoof.getModeValue().equals("Vanilla")) {
            try {
                if (packet.getClass().getName().equals("net.minecraftforge.fml.common.network.internal.FMLProxyPacket"))
                    event.cancelEvent();

                if (packet instanceof C17PacketCustomPayload) {
                    final C17PacketCustomPayload customPayload = (C17PacketCustomPayload) packet;

                    if (!customPayload.getChannelName().startsWith("MC|"))
                        event.cancelEvent();
                    else if (customPayload.getChannelName().equalsIgnoreCase("MC|Brand"))
                        customPayload.data = (new PacketBuffer(Unpooled.buffer()).writeString("vanilla"));
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning() && clientSpoof.getModeValue().equals("LabyMod")) {
            try {

                if (packet.getClass().getName().equals("net.minecraftforge.fml.common.network.internal.FMLProxyPacket"))
                    event.cancelEvent();

                if (packet instanceof S3FPacketCustomPayload) {
                    final S3FPacketCustomPayload payload = (S3FPacketCustomPayload) packet;
                    if (payload.getChannelName().equals("REGISTER")) {
                        mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("labymod3:main", this.getInfo()));
                        mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("LMC", this.getInfo()));

                    }
                }

            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning() && clientSpoof.getModeValue().equals("CheatBreaker")) {
            try {
                if (packet.getClass().getName().equals("net.minecraftforge.fml.common.network.internal.FMLProxyPacket"))
                    event.cancelEvent();

                if (packet instanceof C17PacketCustomPayload) {
                    final C17PacketCustomPayload customPayload = (C17PacketCustomPayload) packet;

                    if (!customPayload.getChannelName().startsWith("MC|"))
                        event.cancelEvent();
                    else if (customPayload.getChannelName().equalsIgnoreCase("MC|Brand"))
                        customPayload.data = (new PacketBuffer(Unpooled.buffer()).writeString("CB"));
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning() && clientSpoof.getModeValue().equals("PvPLounge")) {
            try {
                if (packet.getClass().getName().equals("net.minecraftforge.fml.common.network.internal.FMLProxyPacket"))
                    event.cancelEvent();

                if (packet instanceof C17PacketCustomPayload) {
                    final C17PacketCustomPayload customPayload = (C17PacketCustomPayload) packet;

                    if (!customPayload.getChannelName().startsWith("MC|"))
                        event.cancelEvent();
                    else if (customPayload.getChannelName().equalsIgnoreCase("MC|Brand"))
                        customPayload.data = (new PacketBuffer(Unpooled.buffer()).writeString("PLC18"));
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private PacketBuffer getInfo() {
        return new PacketBuffer(Unpooled.buffer())
                .writeString("INFO")
                .writeString("{  \n" +
                        "   \"version\": \"3.9.25\",\n" +
                        "   \"ccp\": {  \n" +
                        "      \"enabled\": true,\n" +
                        "      \"version\": 2\n" +
                        "   },\n" +
                        "   \"shadow\":{  \n" +
                        "      \"enabled\": true,\n" +
                        "      \"version\": 1\n" +
                        "   },\n" +
                        "   \"addons\": [  \n" +
                        "      {  \n" +
                        "         \"uuid\": \"null\",\n" +
                        "         \"name\": \"null\"\n" +
                        "      }\n" +
                        "   ],\n" +
                        "   \"mods\": [\n" +
                        "      {  \n" +
                        "         \"hash\":\"sha256:null\",\n" +
                        "         \"name\":\"null.jar\"\n" +
                        "      }\n" +
                        "   ]\n" +
                        "}");
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}
