package net.ccbluex.liquidbounce.features.special;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Listenable;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.minecraft.network.handshake.client.C00Handshake;

public class ServerSpoof implements Listenable {
    public static boolean enable=false;
    public static String address="redesky.com";

    @EventTarget
    public void onPacket(PacketEvent event) {
        if(enable&&event.getPacket() instanceof C00Handshake){
            C00Handshake packet=(C00Handshake) event.getPacket();
            String[] ipList=address.split(":");
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
