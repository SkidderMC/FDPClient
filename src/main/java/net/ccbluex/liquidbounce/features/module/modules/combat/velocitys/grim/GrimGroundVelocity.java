package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.grim;

import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import org.jetbrains.annotations.NotNull;

public class GrimGroundVelocity extends VelocityMode {

    public GrimGroundVelocity() {
        super("GrimGround");
    }

    @Override
    public void onVelocityPacket(@NotNull PacketEvent event) {
        if (mc.thePlayer.onGround) {
            if (mc.thePlayer.hurtTime > 0 && !mc.thePlayer.isDead && !mc.thePlayer.isPotionActive(Potion.moveSpeed) && !mc.thePlayer.isInWater()) {
                mc.thePlayer.motionX *= 0.4;
                mc.thePlayer.motionY /= 1.45;
                mc.thePlayer.motionZ *= 0.4;
            }
            if (mc.thePlayer.hurtTime < 1) ((S12PacketEntityVelocity) event.getPacket()).motionY = 0;
            if (mc.thePlayer.hurtTime < 5) {
                ((S12PacketEntityVelocity) event.getPacket()).motionX = 0;
                ((S12PacketEntityVelocity) event.getPacket()).motionZ = 0;
            }
        }
    }
}
