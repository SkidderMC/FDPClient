/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
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

    @Override
    public void load() {
        super.load();

        final ViaProviders providers = Via.getManager().getProviders();

        providers.use(VersionProvider.class, new ProtocolVersionProvider());
    }

}