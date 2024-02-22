package net.ccbluex.liquidbounce.handler.protocol;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;
import net.ccbluex.liquidbounce.handler.protocol.api.*;
import net.raphimc.vialoader.ViaLoader;
import net.raphimc.vialoader.impl.platform.*;
import net.raphimc.vialoader.netty.CompressionReorderEvent;

public class ProtocolBase {

    private ProtocolVersion targetVersion = ProtocolVersion.v1_8;
    public static final AttributeKey<UserConnection> LOCAL_VIA_USER = AttributeKey.valueOf("local_via_user");
    public static final AttributeKey<VFNetworkManager> VF_NETWORK_MANAGER = AttributeKey.valueOf("encryption_setup");
    private final VFPlatform platform;
    private static ProtocolBase manager;

    public ProtocolBase(VFPlatform platform) {
        this.platform = platform;
    }

    public static void init(final VFPlatform platform) {
        if (manager != null) {
            return;
        }

        final ProtocolVersion version = ProtocolVersion.getProtocol(platform.getGameVersion());
        if (version == ProtocolVersion.unknown) {
            throw new IllegalArgumentException("Unknown Version " + platform.getGameVersion());
        }

        manager = new ProtocolBase(platform);

        ViaLoader.init(new ViaVersionPlatformImpl(null), new ProtocolVLLoader(platform), new ProtocolVLInjector(), null, ViaBackwardsPlatformImpl::new, ViaRewindPlatformImpl::new, ViaLegacyPlatformImpl::new, ViaAprilFoolsPlatformImpl::new);
    }

    public void inject(final Channel channel, final VFNetworkManager networkManager) {
        if (channel instanceof SocketChannel) {
            final UserConnection user = new UserConnectionImpl(channel, true);
            new ProtocolPipelineImpl(user);

            channel.attr(LOCAL_VIA_USER).set(user);
            channel.attr(VF_NETWORK_MANAGER).set(networkManager);

            channel.pipeline().addLast(new ProtocolVLLegacyPipeline(user, targetVersion));
        }
    }

    public ProtocolVersion getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersionSilent(final ProtocolVersion targetVersion) {
        this.targetVersion = targetVersion;
    }

    public void setTargetVersion(final ProtocolVersion targetVersion) {
        this.targetVersion = targetVersion;
    }

    public void reorderCompression(final Channel channel) {
        channel.pipeline().fireUserEventTriggered(CompressionReorderEvent.INSTANCE);
    }

    public VFPlatform getPlatform() {
        return platform;
    }

    public static ProtocolBase getManager() {
        return manager;
    }
}
