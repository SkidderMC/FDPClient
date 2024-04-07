/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.special.spoof;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Listenable;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.ClientSpoof;
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.utils.IconUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import org.apache.commons.compress.utils.IOUtils;
import org.lwjgl.opengl.Display;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

import static net.ccbluex.liquidbounce.utils.IconUtils.readImageToBuffer;

public class ClientSpoofHandler extends MinecraftInstance implements Listenable {

    public static final boolean enabled = true;

    private static boolean flagged = false;
    public static int flagTicks;
    @EventTarget
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C03PacketPlayer && flagged) {
            if (mc.thePlayer.ticksExisted % 2 == 0)
                flagTicks++;
            if (flagTicks < 3) {
                if (RotationUtils.targetRotation != null) {
                    event.cancelEvent();
                    PacketUtils.sendPacketNoEvent(
                            new C03PacketPlayer.C06PacketPlayerPosLook(
                                    mc.thePlayer.posX,
                                    mc.thePlayer.posY,
                                    mc.thePlayer.posZ,
                                    mc.thePlayer.rotationYaw,
                                    mc.thePlayer.rotationPitch,
                                    mc.thePlayer.onGround
                            )
                    );
                    RotationUtils.Companion.reset();
                }
            } else {
                if (Objects.requireNonNull(FDPClient.moduleManager.getModule(HUDModule.class)).getState() && Objects.requireNonNull(FDPClient.moduleManager.getModule(HUDModule.class)).getTpDebugValue().get())
                    ClientUtils.INSTANCE.displayChatMessage(FDPClient.CLIENT_CHAT + "tp");
                flagTicks = 1;
                flagged = false;
            }
        }

        if (ProtocolBase.getManager().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_10)) {
            if (packet instanceof C08PacketPlayerBlockPlacement) {
                ((C08PacketPlayerBlockPlacement) packet).facingX = 0.5F;
                ((C08PacketPlayerBlockPlacement) packet).facingY = 0.5F;
                ((C08PacketPlayerBlockPlacement) packet).facingZ = 0.5F;
            }
        }
    }

    public static final String LUNAR_DISPLAY_NAME = "lunarclient:v2.14.5-2411";

    public static String handleClientBrand() {
        final ClientSpoof clientSpoof = FDPClient.moduleManager.getModule(ClientSpoof.class);
        final boolean enabled = clientSpoof != null && clientSpoof.getState();
        if (!enabled)
            return "vanilla";
        switch (clientSpoof.getModeValue().getValue().toLowerCase()) {
            case "lunar":
                return LUNAR_DISPLAY_NAME;
            default:
                return "vanilla";
        }
    }

    // zywl... w h y -bread
    public static void checkIconAndTitle() {
        IconUtils iu = new IconUtils();
        Util.EnumOS util$enumos = Util.getOSType();
        final ClientSpoof clientSpoof = FDPClient.moduleManager.getModule(ClientSpoof.class);
        final boolean enabled = clientSpoof != null && clientSpoof.getState();

        if (enabled) {
            if (util$enumos != Util.EnumOS.OSX && clientSpoof.getModeValue().getValue().equalsIgnoreCase("lunar")) {
                setVanillaIcon();
            }

            if (clientSpoof.getModeValue().getValue().equalsIgnoreCase("lunar")) {
                Display.setTitle("Lunar Client 1.8.9 (" + LUNAR_DISPLAY_NAME.replace("lunarclient:", "") + ")");
            } else {
                if (util$enumos != Util.EnumOS.OSX) {
                    iu.setIcon();
                }
                Display.setTitle(FDPClient.CLIENT_NAME + " " + FDPClient.CLIENT_VERSION);
            }
        } else {
            if (util$enumos != Util.EnumOS.OSX) {
                iu.setIcon();
            }
            Display.setTitle(FDPClient.CLIENT_NAME + " " + FDPClient.CLIENT_VERSION);
        }
    }

    private static void setVanillaIcon() {
        InputStream inputstream = null;
        InputStream inputstream1 = null;

        try {
            inputstream = mc.mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_16x16.png"));
            inputstream1 = mc.mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png"));

            if (inputstream != null && inputstream1 != null) {
                Display.setIcon(new ByteBuffer[]{readImageToBuffer(inputstream), readImageToBuffer(inputstream1)});
            }
        } catch (IOException ioexception) {
            ClientUtils.getLogger().error("Couldn\'t set icon", ioexception);
        } finally {
            IOUtils.closeQuietly(inputstream);
            IOUtils.closeQuietly(inputstream1);
        }
    }

    public static String handleLaunchedVersion() {
        final ClientSpoof clientSpoof = FDPClient.moduleManager.getModule(ClientSpoof.class);
        final boolean enabled = clientSpoof != null && clientSpoof.getState();
        if (!enabled)
            return "FDPClient";
        switch (clientSpoof.getModeValue().getValue().toLowerCase()) {
            case "vanilla":
            case "lunar":
                return "1.8.9";
            default:
                return "FDPClient";
        }
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}