/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.handler.protocol.api.VFNetworkManager;
import net.ccbluex.liquidbounce.utils.BlinkUtils;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.minecraft.network.*;
import net.minecraft.util.CryptManager;
import net.minecraft.util.LazyLoadBase;
import net.raphimc.vialoader.netty.VLLegacyPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.net.InetAddress;


/**
 * The type Mixin network manager.
 */
@Mixin(NetworkManager.class)
public class MixinNetworkManager implements VFNetworkManager {

    @Shadow
    private Channel channel;

    @Shadow
    private INetHandler packetListener;

    @Unique
    private Cipher viaForge$decryptionCipher;

    @Unique
    private ProtocolVersion viaForge$targetVersion;

    @Inject(method = "func_181124_a", at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/Bootstrap;group(Lio/netty/channel/EventLoopGroup;)Lio/netty/bootstrap/AbstractBootstrap;"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private static void trackSelfTarget(InetAddress address, int serverPort, boolean useNativeTransport, CallbackInfoReturnable<NetworkManager> cir, NetworkManager networkmanager, Class oclass, LazyLoadBase lazyloadbase) {
        ((VFNetworkManager) networkmanager).viaForge$setTrackedVersion(ProtocolBase.getManager().getTargetVersion());
    }

    @Inject(method = "enableEncryption", at = @At("HEAD"), cancellable = true)
    private void storeEncryptionCiphers(SecretKey key, CallbackInfo ci) {
        if (ProtocolBase.getManager().getTargetVersion().equals(ProtocolVersion.v1_16_4)) {
            ci.cancel();
            this.viaForge$decryptionCipher = CryptManager.createNetCipherInstance(2, key);
            this.channel.pipeline().addBefore(VLLegacyPipeline.VIALEGACY_PRE_NETTY_LENGTH_REMOVER_NAME, "encrypt", new NettyEncryptingEncoder(CryptManager.createNetCipherInstance(1, key)));
        }
    }

    @Inject(method = "setCompressionTreshold", at = @At("RETURN"))
    public void reorderPipeline(int p_setCompressionTreshold_1_, CallbackInfo ci) {
        ProtocolBase.getManager().reorderCompression(channel);
    }

    @Override
    public void viaForge$setupPreNettyDecryption() {
        this.channel.pipeline().addBefore(VLLegacyPipeline.VIALEGACY_PRE_NETTY_LENGTH_REMOVER_NAME, "decrypt", new NettyEncryptingDecoder(this.viaForge$decryptionCipher));
    }

    @Override
    public ProtocolVersion viaForge$getTrackedVersion() {
        return viaForge$targetVersion;
    }

    @Override
    public void viaForge$setTrackedVersion(ProtocolVersion version) {
        viaForge$targetVersion = version;
    }

    /**
     * show player head in tab bar
     */
    @Inject(method = "getIsencrypted", at = @At("HEAD"), cancellable = true)
    private void getIsencrypted(CallbackInfoReturnable<Boolean> cir) {
        if(Animations.INSTANCE.getFlagRenderTabOverlay()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * @author opZywl
     * @reason Packet Tracking
     */
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
}