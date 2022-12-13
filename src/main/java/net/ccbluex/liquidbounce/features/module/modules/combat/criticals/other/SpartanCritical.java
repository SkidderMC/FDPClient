package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.other;

import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode;
public class Spartan extends CriticalMode {
	//Bypass MorePackets
    private int attacked = 3;
    public Spartan() {
        super("Spartan");
    }

    @Override
    public void onAttack(AttackEvent event) {
        attacked++;
        if (attacked >= 3) {
            sendPacket(0.0001,true);
            sendPacket(0.0,false);
            attacked = 0;
        }
    }
	
    public void sendPacket(double offset,boolean onGround) {
        C03PacketPlayer.C04PacketPlayerPosition packetPlayer = new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,mc.thePlayer.posY + offset,mc.thePlayer.posZ,onGround);
        mc.getNetHandler().addToSendQueue(packetPlayer);
    }
}