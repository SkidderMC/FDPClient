/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.EventState;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.TabGUIModule;
import net.ccbluex.liquidbounce.utils.rotation.PostRotationExecutor;
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.ccbluex.liquidbounce.utils.client.PPSCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        final PacketEvent event = new PacketEvent(packet, EventState.RECEIVE);
        EventManager.INSTANCE.call(event);

        if (event.isCancelled()) {
            callback.cancel();
            return;
        }

        PPSCounter.INSTANCE.registerType(PPSCounter.PacketType.RECEIVED);
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, CallbackInfo callback) {
        final PacketEvent event = new PacketEvent(packet, EventState.SEND);
        EventManager.INSTANCE.call(event);

        if (event.isCancelled()) {
            PostRotationExecutor.INSTANCE.discardRotationPacket(packet);
            callback.cancel();
            return;
        }

        PPSCounter.INSTANCE.registerType(PPSCounter.PacketType.SEND);
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("RETURN"))
    private void sent(Packet<?> packet, CallbackInfo callback) {
        PostRotationExecutor.INSTANCE.onPacketSendCompleted(packet);
    }

    /**
     * The single choke point where a packet is truly written to the wire — both the event path
     * and silent sends (Blink/FakeLag flushes) end up here. Lets the rotation manager track the
     * rotation the server has actually received, as opposed to the one merely built client-side.
     */
    @Inject(method = "dispatchPacket", at = @At("HEAD"))
    private void dispatched(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>>[] listeners, CallbackInfo callback) {
        RotationUtils.INSTANCE.onPacketDispatched(packet);
    }


    /**
     * show player head in tab bar
     */
    @Inject(method = "getIsencrypted", at = @At("HEAD"), cancellable = true)
    private void getIsencrypted(CallbackInfoReturnable<Boolean> cir) {
        if(TabGUIModule.INSTANCE.getFlagRenderTabOverlay()) {
            cir.setReturnValue(true);
        }
    }
}
