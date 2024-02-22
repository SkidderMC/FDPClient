package net.ccbluex.liquidbounce.handler.protocol.api;


import net.raphimc.vialoader.impl.viaversion.VLInjector;

public class ProtocolVLInjector extends VLInjector {

    public String getDecoderName() {
        return "via-decoder";
    }

    @Override
    public boolean lateProtocolVersionSetting() {
        return super.lateProtocolVersionSetting();
    }

    public String getEncoderName() {
        return "via-encoder";
    }

}