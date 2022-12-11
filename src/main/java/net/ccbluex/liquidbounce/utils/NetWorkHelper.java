/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import io.netty.util.concurrent.GenericFutureListener;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class NetWorkHelper extends MinecraftInstance {
    public static void sendPacket(Packet packet) {
        mc.getNetHandler().getNetworkManager().sendPacket(packet);
    }

    public static void sendPacketNoEvent(Packet packet) {
        try {
            PacketUtils.sendPacketNoEvent(packet);
        } catch (NullPointerException e) {

        }
    }

    public static void sendPacketSilent(Packet packet) {
        mc.getNetHandler().getNetworkManager().sendPacket(packet, null, new GenericFutureListener[0]);
    }

    public static void sendPacketUnlogged(Packet<? extends INetHandler> packet) {
        mc.getNetHandler().getNetworkManager().sendPacket(packet);
    }

    public static void sendBlocking(boolean callEvent, boolean placement) {
        if(mc.thePlayer == null)
            return;

        if(placement) {
            C08PacketPlayerBlockPlacement packet = new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.getHeldItem(), 0, 0, 0);

            if(callEvent) {
                sendPacket(packet);
            } else {
                sendPacketNoEvent(packet);
            }
        } else {
            C08PacketPlayerBlockPlacement packet = new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem());
            if(callEvent) {
                sendPacket(packet);
            } else {
                sendPacketNoEvent(packet);
            }
        }
    }

    public static void releaseUseItem(boolean callEvent) {
        if(mc.thePlayer == null)
            return;

        C07PacketPlayerDigging packet = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
        if(callEvent) {
            sendPacket(packet);
        } else {
            sendPacketNoEvent(packet);
        }
    }
}
