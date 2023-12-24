/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.network;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Listenable;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.ClientSpoof;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import io.netty.buffer.Unpooled;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.raphimc.vialoader.util.VersionEnum;

import java.util.Objects;

public class ClientSpoofHandler extends MinecraftInstance implements Listenable {
    @EventTarget
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();
        final ClientSpoof clientSpoof = FDPClient.moduleManager.getModule(ClientSpoof.class);

        if (ProtocolBase.getManager().getTargetVersion().isNewerThan(VersionEnum.r1_9_3tor1_9_4)) {
            if (packet instanceof C08PacketPlayerBlockPlacement) {
                ((C08PacketPlayerBlockPlacement) packet).facingX = 0.5F;
                ((C08PacketPlayerBlockPlacement) packet).facingY = 0.5F;
                ((C08PacketPlayerBlockPlacement) packet).facingZ = 0.5F;
            }
        }

        if (!MinecraftInstance.mc.isIntegratedServerRunning()) {
            if (packet instanceof C17PacketCustomPayload) {
                if (((C17PacketCustomPayload) event.getPacket()).getChannelName().equalsIgnoreCase("MC|Brand")) {
                    if (Objects.requireNonNull(clientSpoof).modeValue.get().equals("Vanilla"))
                        PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("vanilla")));
                    if (Objects.requireNonNull(clientSpoof).modeValue.get().equals("Fabric"))
                        PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("fabric")));
                    if (Objects.requireNonNull(clientSpoof).modeValue.get().equals("LabyMod")) {
                        mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("labymod3:main", this.getInfo()));
                        mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("LMC", this.getInfo()));
                    }
                    if (Objects.requireNonNull(clientSpoof).modeValue.get().equals("Custom")) {
                        try {
                            final C17PacketCustomPayload customPayload = (C17PacketCustomPayload) packet;
                            customPayload.data = (new PacketBuffer(Unpooled.buffer()).writeString(clientSpoof.CustomClient.get()));
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (Objects.requireNonNull(clientSpoof).modeValue.get().equals("CheatBreaker"))
                        PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("CB")));
                    if (Objects.requireNonNull(clientSpoof).modeValue.get().equals("PvPLounge"))
                        PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("PLC18")));
                    if (Objects.requireNonNull(clientSpoof).modeValue.get().equals("Lunar"))
                        PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("lunarclient:v2.12.3-2351")));
                }
                event.cancelEvent();
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