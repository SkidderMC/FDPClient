package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import net.raphimc.vialegacy.protocols.classic.protocola1_0_15toc0_28_30.providers.ClassicMPPassProvider;
import net.raphimc.vialegacy.protocols.release.protocol1_3_1_2to1_2_4_5.providers.OldAuthProvider;
import net.raphimc.vialegacy.protocols.release.protocol1_7_2_5to1_6_4.providers.EncryptionProvider;
import net.raphimc.vialegacy.protocols.release.protocol1_8to1_7_6_10.providers.GameProfileFetcher;
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
        providers.use(MovementTransmitterProvider.class, new ProtocolMovementTransmitterProvider());
        providers.use(OldAuthProvider.class, new ProtocolOldAuthProvider());
        providers.use(GameProfileFetcher.class, platform.getGameProfileFetcher());
        providers.use(EncryptionProvider.class, new ProtocolEncryptionProvider());
        providers.use(ClassicMPPassProvider.class, new ProtocolClassicMPPassProvider());
    }

}