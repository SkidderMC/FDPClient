/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.misc.SilentDisconnect;
import net.ccbluex.liquidbounce.features.special.ProxyManager;
import net.ccbluex.liquidbounce.utils.BlinkUtils;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.util.MessageDeserializer;
import net.minecraft.util.MessageDeserializer2;
import net.minecraft.util.MessageSerializer;
import net.minecraft.util.MessageSerializer2;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;
import java.net.Proxy;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {

    /**
     * show player head in tab bar
     */
    @Inject(method = "getIsencrypted", at = @At("HEAD"), cancellable = true)
    private void getIsencrypted(CallbackInfoReturnable<Boolean> cir) {
        if(Animations.INSTANCE.getFlagRenderTabOverlay()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        if(PacketUtils.INSTANCE.getPacketType(packet) != PacketUtils.PacketType.SERVERSIDE)
            return;

        final PacketEvent event = new PacketEvent(packet, PacketEvent.Type.RECEIVE);
        FDPClient.eventManager.callEvent(event);

        if(event.isCancelled()) {
            callback.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, CallbackInfo callback) {
        if(PacketUtils.INSTANCE.getPacketType(packet) != PacketUtils.PacketType.CLIENTSIDE)
            return;

        if(!PacketUtils.INSTANCE.handleSendPacket(packet)){
            final PacketEvent event = new PacketEvent(packet, PacketEvent.Type.SEND);
            FDPClient.eventManager.callEvent(event);

            if(event.isCancelled()) {
                callback.cancel();
            } else if (BlinkUtils.INSTANCE.pushPacket(packet))
                callback.cancel();
        }
    }

    @Inject(method = "createNetworkManagerAndConnect", at = @At("HEAD"), cancellable = true)
    private static void createNetworkManagerAndConnect(InetAddress address, int serverPort, boolean useNativeTransport, CallbackInfoReturnable<NetworkManager> cir) {
        if(!ProxyManager.INSTANCE.isEnable()) {
            return;
        }
        final NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);

        Bootstrap bootstrap = new Bootstrap();

        EventLoopGroup eventLoopGroup;
        Proxy proxy = ProxyManager.INSTANCE.getProxyInstance();
        eventLoopGroup = new OioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
        bootstrap.channelFactory(new ProxyManager.ProxyOioChannelFactory(proxy));

        bootstrap.group(eventLoopGroup).handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) {
                ClientUtils.INSTANCE.logWarn("ILLEGAL CHANNEL INITIALIZATION: This should be patched to net/minecraft/network/NetworkManager$5!");
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException var3) {
                    var3.printStackTrace();
                }
                channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("splitter", new MessageDeserializer2()).addLast("decoder", new MessageDeserializer(EnumPacketDirection.CLIENTBOUND)).addLast("prepender", new MessageSerializer2()).addLast("encoder", new MessageSerializer(EnumPacketDirection.SERVERBOUND)).addLast("packet_handler", networkmanager);
            }
        });

        bootstrap.connect(address, serverPort).syncUninterruptibly();

        cir.setReturnValue(networkmanager);
        cir.cancel();
    }

    @Redirect(method = "checkDisconnected", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;)V"))
    public void checkDisconnectedLoggerWarn(Logger instance, String s) {
        if(!FDPClient.moduleManager.getModule(SilentDisconnect.class).getState()) {
            instance.warn(s); // it will spam "handleDisconnection() called twice" in console if SilentDisconnect is enabled
        }
    }
}