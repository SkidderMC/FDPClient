/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.protocol.api;

import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.raphimc.vialoader.util.VersionEnum;

public class ProtocolFixer {
    private static final Minecraft mc = MinecraftInstance.mc;

    public static void sendConditionalSwing(final MovingObjectPosition mop) {
        if (mop != null && mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
            mc.thePlayer.swingItem();
        }
    }

    public static void sendFixedAttack(final EntityPlayer entityIn, final Entity target) {
        if (newerThan1_8()) {
            mc.playerController.attackEntity(entityIn, target);
            mc.thePlayer.swingItem();
        } else {
            mc.thePlayer.swingItem();
            mc.playerController.attackEntity(entityIn, target);
        }
    }

    public static boolean newerThanOrEqualsTo1_8() {
        return ProtocolBase.getManager().getTargetVersion().isNewerThanOrEqualTo(VersionEnum.r1_8) && !MinecraftInstance.mc.isIntegratedServerRunning() || MinecraftInstance.mc.isIntegratedServerRunning();
    }

    public static boolean newerThan1_8() {
        return ProtocolBase.getManager().getTargetVersion().isNewerThan(VersionEnum.r1_8) && !MinecraftInstance.mc.isIntegratedServerRunning();
    }

    public static boolean newerThanOrEqualsTo1_9() {
        return ProtocolBase.getManager().getTargetVersion().isNewerThanOrEqualTo(VersionEnum.r1_9) && !MinecraftInstance.mc.isIntegratedServerRunning();
    }

    public static boolean newerThanOrEqualsTo1_10() {
        return ProtocolBase.getManager().getTargetVersion().isNewerThanOrEqualTo(VersionEnum.r1_10) && !MinecraftInstance.mc.isIntegratedServerRunning();
    }

    public static boolean newerThanOrEqualsTo1_13() {
        return ProtocolBase.getManager().getTargetVersion().isNewerThanOrEqualTo(VersionEnum.r1_13) && !MinecraftInstance.mc.isIntegratedServerRunning();
    }

    public static boolean olderThanOrEqualsTo1_13_2() {
        return ProtocolBase.getManager().getTargetVersion().isOlderThanOrEqualTo(VersionEnum.r1_13_2) && !MinecraftInstance.mc.isIntegratedServerRunning();
    }

    public static boolean newerThanOrEqualsTo1_14() {
        return ProtocolBase.getManager().getTargetVersion().isNewerThanOrEqualTo(VersionEnum.r1_14) && !MinecraftInstance.mc.isIntegratedServerRunning();
    }

    public static boolean newerThanOrEqualsTo1_16() {
        return ProtocolBase.getManager().getTargetVersion().isNewerThanOrEqualTo(VersionEnum.r1_14) && !MinecraftInstance.mc.isIntegratedServerRunning();
    }
}