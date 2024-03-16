/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

public class ProtocolFixes {
    private static final Minecraft mc = MinecraftInstance.mc;

    public static void sendConditionalSwing(final MovingObjectPosition mop) {
        if (mop != null && mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
            mc.thePlayer.swingItem();
        }
    }

    public static void sendFixedAttack(final EntityPlayer entityIn, final Entity target) {
        if (ProtocolBase.getManager().getTargetVersion().newerThan(ProtocolVersion.v1_8)) {
            mc.playerController.attackEntity(entityIn, target);
            mc.thePlayer.swingItem();
        } else {
            mc.thePlayer.swingItem();
            mc.playerController.attackEntity(entityIn, target);
        }
    }
}