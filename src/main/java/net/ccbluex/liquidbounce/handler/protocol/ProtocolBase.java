/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.protocol;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;
import net.ccbluex.liquidbounce.handler.protocol.api.*;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.raphimc.vialoader.ViaLoader;
import net.raphimc.vialoader.impl.platform.*;
import net.raphimc.vialoader.netty.CompressionReorderEvent;
import net.raphimc.vialoader.util.VersionEnum;

import java.util.ArrayList;
import java.util.List;

public class ProtocolBase {

    private VersionEnum targetVersion = VersionEnum.r1_8;
    public static final AttributeKey<UserConnection> LOCAL_VIA_USER = AttributeKey.valueOf("local_via_user");
    public static final AttributeKey<VFNetworkManager> VF_NETWORK_MANAGER = AttributeKey.valueOf("encryption_setup");
    private static ProtocolBase manager;
    public static List<VersionEnum> versions = new ArrayList<>();

    public ProtocolBase() {
    }

    public static void init(final VFPlatform platform) {
        if (manager != null) {
            return;
        }

        final VersionEnum version = VersionEnum.fromProtocolId(platform.getGameVersion());

        if (version == VersionEnum.UNKNOWN)
            throw new IllegalArgumentException("Unknown Protocol Found (" + platform.getGameVersion() + ")");

        manager = new ProtocolBase();

        ViaLoader.init(new ViaVersionPlatformImpl(null), new ProtocolVLLoader(platform), new ProtocolVLInjector(), null, ViaBackwardsPlatformImpl::new, ViaRewindPlatformImpl::new, ViaLegacyPlatformImpl::new, ViaAprilFoolsPlatformImpl::new);

        versions.addAll(VersionEnum.SORTED_VERSIONS);

        versions.removeIf(i -> i == VersionEnum.UNKNOWN || i.isOlderThan(VersionEnum.r1_7_2tor1_7_5));

        ClientUtils.getLogger().info("ViaVersion Injected");
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

    public VersionEnum getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersionSilent(final VersionEnum targetVersion) {
        this.targetVersion = targetVersion;
    }

    public void setTargetVersion(final VersionEnum targetVersion) {
        this.targetVersion = targetVersion;
    }

    public void reorderCompression(final Channel channel) {
        channel.pipeline().fireUserEventTriggered(CompressionReorderEvent.INSTANCE);
    }

    public static ProtocolBase getManager() {
        return manager;
    }
}
