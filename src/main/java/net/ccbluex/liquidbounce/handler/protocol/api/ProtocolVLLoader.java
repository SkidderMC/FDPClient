package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import net.raphimc.vialoader.impl.viaversion.VLLoader;

public class ProtocolVLLoader extends VLLoader {
    private final VFPlatform platform;

    public ProtocolVLLoader(VFPlatform platform) {
        this.platform = platform;
    }

    public void load() {
        super.load();
        ViaProviders providers = Via.getManager().getProviders();
        providers.use(VersionProvider.class, new ProtocolVersionProvider());
    }
}