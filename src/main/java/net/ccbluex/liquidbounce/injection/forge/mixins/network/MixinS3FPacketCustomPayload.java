package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(S3FPacketCustomPayload.class)
public class MixinS3FPacketCustomPayload {
    @Shadow
    private PacketBuffer data;

    @Inject(method = "processPacket(Lnet/minecraft/network/play/INetHandlerPlayClient;)V", at = @At("TAIL"))
    private void releaseData(INetHandlerPlayClient handler, CallbackInfo ci) {
        if (this.data != null) {
            this.data.release();
        }
    }
}