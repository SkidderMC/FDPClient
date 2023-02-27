package net.ccbluex.liquidbounce.features.special;

import io.netty.buffer.Unpooled;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Listenable;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

public class ClientSpoof extends MinecraftInstance implements Listenable {

    public static final boolean enabled = true;

    @EventTarget
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();
        final net.ccbluex.liquidbounce.features.module.modules.client.ClientSpoof clientSpoof = LiquidBounce.moduleManager.getModule(net.ccbluex.liquidbounce.features.module.modules.client.ClientSpoof.class);

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning() && clientSpoof.modeValue.get().equals("Vanilla")) {
            try {
                if (packet instanceof C17PacketCustomPayload) {
                    final C17PacketCustomPayload customPayload = (C17PacketCustomPayload) packet;
                    customPayload.data = (new PacketBuffer(Unpooled.buffer()).writeString("vanilla"));
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning() && clientSpoof.modeValue.get().equals("LabyMod")) {
            try {
                if (packet instanceof C17PacketCustomPayload) {
                    final C17PacketCustomPayload customPayload = (C17PacketCustomPayload) packet;
                    customPayload.data = (new PacketBuffer(Unpooled.buffer()).writeString("LMC"));
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning() && clientSpoof.modeValue.get().equals("CheatBreaker")) {
            try {
                if (packet instanceof C17PacketCustomPayload) {
                    final C17PacketCustomPayload customPayload = (C17PacketCustomPayload) packet;
                    customPayload.data = (new PacketBuffer(Unpooled.buffer()).writeString("CB"));
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning() && clientSpoof.modeValue.get().equals("Lunar")) {
            try {
                if (packet instanceof C17PacketCustomPayload) {
                    final C17PacketCustomPayload customPayload = (C17PacketCustomPayload) packet;
                    customPayload.data = (new PacketBuffer(Unpooled.buffer()).writeString("Lunar-Client"));
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning() && clientSpoof.modeValue.get().equals("PvPLounge")) {
            try {
                if (packet instanceof C17PacketCustomPayload) {
                    final C17PacketCustomPayload customPayload = (C17PacketCustomPayload) packet;
                    customPayload.data = (new PacketBuffer(Unpooled.buffer()).writeString("PLC18"));
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}