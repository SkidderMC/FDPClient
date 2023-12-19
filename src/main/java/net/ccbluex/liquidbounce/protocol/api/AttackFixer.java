package net.ccbluex.liquidbounce.protocol.api;

import net.ccbluex.liquidbounce.protocol.ProtocolBase;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.raphimc.vialoader.util.VersionEnum;

public class AttackFixer {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void sendConditionalSwing(final MovingObjectPosition mop) {
        if (mop != null && mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
            mc.thePlayer.swingItem();
        }
    }

    public static void sendFixedAttack(final EntityPlayer entityIn, final Entity target) {
        if (ProtocolBase.getManager().getTargetVersion().getProtocol() != VersionEnum.r1_8.getProtocol()) {
            send1_9Attack(entityIn, target);
        } else {
            send1_8Attack(entityIn, target);
        }
    }

    private static void send1_8Attack(EntityPlayer entityIn, Entity target) {
        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(entityIn, target);
    }

    private static void send1_9Attack(EntityPlayer entityIn, Entity target) {
        mc.playerController.attackEntity(entityIn, target);
        mc.thePlayer.swingItem();
    }
}