package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.aac

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

public class AAC4Critical extends CriticalMode {
	public AAC4Critical() {
		super("AAC4")
	}
	
	@Override
	public void onAttack(AttackEvent event) {
        double[] offsets = new double[] {0.0000000000000036,0.0};
        sendPackets(offsets,false);
	}
	
	public void sendPackets(double[] offsets,boolean onGround) {
        for (double offset : offsets) {
            C03PacketPlayer.C04PacketPlayerPosition packetPlayer = new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,mc.thePlayer.posY + offset,mc.thePlayer.posZ,onGround);
            mc.getNetHandler().addToSendQueue(packetPlayer);
        }
    }
}