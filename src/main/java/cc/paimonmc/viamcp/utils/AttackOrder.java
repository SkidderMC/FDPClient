package cc.paimonmc.viamcp.utils;

import cc.paimonmc.viamcp.ViaMCP;
import cc.paimonmc.viamcp.protocols.ProtocolCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

public class AttackOrder {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static final int VER_1_8_ID = 47;

    public static void sendConditionalSwing(final MovingObjectPosition mop) {
        if (mop != null && mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
            mc.thePlayer.swingItem();
        }
    }

    public static void sendFixedAttack(final EntityPlayer entityIn, final Entity target) {
        // Using this instead of ViaMCP.PROTOCOL_VERSION so does not need to be changed between 1.8.x and 1.12.2 base
        // getVersion() can be null, but not in this case, as ID 47 exists, if not removed
        if (ViaMCP.getInstance().getVersion() <= ProtocolCollection.getProtocolById(VER_1_8_ID).getVersion()) {
            send1_8Attack(entityIn, target);
        } else {
            send1_9Attack(entityIn, target);
        }
    }

    private static void send1_8Attack(EntityPlayer entityIn, Entity target)
    {
        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(entityIn, target);
    }

    private static void send1_9Attack(EntityPlayer entityIn, Entity target)
    {
        mc.playerController.attackEntity(entityIn, target);
        mc.thePlayer.swingItem();
    }
}
