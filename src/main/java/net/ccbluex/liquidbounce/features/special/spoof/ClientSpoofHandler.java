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
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

import java.util.Objects;

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
                if (Objects.requireNonNull(FDPClient.moduleManager.getModule(HUD.class)).getState() && Objects.requireNonNull(FDPClient.moduleManager.getModule(HUD.class)).getTpDebugValue().get())
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

    @Override
    public boolean handleEvents() {
        return true;
    }
}