package net.ccbluex.liquidbounce.features.special;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Listenable;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.minecraft.network.handshake.client.C00Handshake;

public class ServerSpoof implements Listenable {
    public static boolean enable=true;
    public static String ip="redesky.com";

    @EventTarget
    public void onPacket(PacketEvent event) {
        System.out.println("[ServerSpoof] ENABLE="+enable+", isPacket="+(event.getPacket() instanceof C00Handshake));
        if(enable&&event.getPacket() instanceof C00Handshake){
            C00Handshake packet=(C00Handshake) event.getPacket();
            System.out.println("[ServerSpoof] "+packet.ip+":"+packet.port+" -> "+ip);
            String[] ipList=ip.split(":");
            packet.ip=ipList[0];
            if(ipList.length>1){
                packet.port=Integer.parseInt(ipList[1]);
            }else{
                packet.port=25565;
            }
        }
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}
