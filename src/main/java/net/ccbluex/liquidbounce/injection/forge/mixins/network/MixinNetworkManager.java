/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.special.proxy.ProxyManager;
import net.ccbluex.liquidbounce.features.special.proxy.ProxyOioChannelFactory;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.Proxy;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {
    @Shadow
    @Final
    public static LazyLoadBase<NioEventLoopGroup> CLIENT_NIO_EVENTLOOP;

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        final PacketEvent event = new PacketEvent(packet);
        LiquidBounce.eventManager.callEvent(event);

        if(event.isCancelled())
            callback.cancel();
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, CallbackInfo callback) {
        if(!ClientUtils.handlePacket(packet)){
            final PacketEvent event = new PacketEvent(packet);
            LiquidBounce.eventManager.callEvent(event);

            if(event.isCancelled())
                callback.cancel();
        }
    }

    @Overwrite
    public static NetworkManager createNetworkManagerAndConnect(InetAddress address, int serverPort, boolean useNativeTransport) {
        final NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);

        Bootstrap bootstrap=new Bootstrap();

        EventLoopGroup eventLoopGroup;
        Proxy proxy=ProxyManager.INSTANCE.getProxy();
        if(proxy.type().equals(Proxy.Type.DIRECT)){
            eventLoopGroup=CLIENT_NIO_EVENTLOOP.getValue();
            bootstrap.channel(NioSocketChannel.class);
        }else {
            if(!Epoll.isAvailable()||!useNativeTransport){
                System.out.println("Something goes wrong! Maybe you can disable proxy. [Epoll="+Epoll.isAvailable()+", UNT="+useNativeTransport+"]");
            }
            eventLoopGroup=new OioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
            bootstrap.channelFactory(new ProxyOioChannelFactory(proxy));
        }

        bootstrap.group(eventLoopGroup).handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException var3) {
                    var3.printStackTrace();
                }
                channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("splitter", new MessageDeserializer2()).addLast("decoder", new MessageDeserializer(EnumPacketDirection.CLIENTBOUND)).addLast("prepender", new MessageSerializer2()).addLast("encoder", new MessageSerializer(EnumPacketDirection.SERVERBOUND)).addLast("packet_handler", networkmanager);
            }
        });

        bootstrap.connect(address, serverPort).syncUninterruptibly();

        return networkmanager;
    }
}