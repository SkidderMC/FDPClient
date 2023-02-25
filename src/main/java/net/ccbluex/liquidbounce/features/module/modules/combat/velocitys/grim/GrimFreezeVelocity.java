/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.grim;

import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import org.jetbrains.annotations.NotNull;

public class GrimFreezeVelocity extends VelocityMode {

    double motionX, motionY, motionZ, x, y, z;
    boolean isFreezing;

    public GrimFreezeVelocity() {
        super("GrimFreeze");
    }

    @Override
    public void onVelocityPacket(@NotNull PacketEvent event) {
        isFreezing = true;
        motionX = mc.thePlayer.motionX;
        motionY = mc.thePlayer.motionY;
        motionZ = mc.thePlayer.motionZ;
        x = mc.thePlayer.posX;
        y = mc.thePlayer.posY;
        z = mc.thePlayer.posZ;
    }

    @Override
    public void onPacket(@NotNull PacketEvent event) {
        if (isFreezing && event.getPacket() instanceof C03PacketPlayer) event.cancelEvent();
        if (isFreezing && event.getPacket() instanceof S08PacketPlayerPosLook) {
            x = ((S08PacketPlayerPosLook) event.getPacket()).getX();
            y = ((S08PacketPlayerPosLook) event.getPacket()).getY();
            z = ((S08PacketPlayerPosLook) event.getPacket()).getZ();
            motionX = 0.0;
            motionY = 0.0;
            motionZ = 0.0;
        }
    }

    @Override
    public void onUpdate(@NotNull UpdateEvent event) {
        if (isFreezing) {
            if (mc.thePlayer.hurtTime == 0) {
                isFreezing = false;
                mc.thePlayer.motionX = motionX;
                mc.thePlayer.motionY = motionY;
                mc.thePlayer.motionZ = motionZ;
            }
            else {
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionY = 0;
                mc.thePlayer.motionZ = 0;
                mc.thePlayer.setPositionAndRotation(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            }
        }
    }
}
