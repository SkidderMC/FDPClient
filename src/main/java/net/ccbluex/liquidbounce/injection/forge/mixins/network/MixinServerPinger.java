package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.handler.protocol.api.ExtendedServerData;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.network.NetworkManager;
import net.raphimc.vialoader.util.VersionEnum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;

@Mixin(OldServerPinger.class)
public class MixinServerPinger {

    @Unique
    private ServerData viaForge$serverData;

    @Inject(method = "ping", at = @At("HEAD"))
    public void trackServerData(ServerData server, CallbackInfo ci) {
        viaForge$serverData = server;
    }

    @Redirect(method = "ping", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;func_181124_a(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/NetworkManager;"), remap = false)
    public NetworkManager trackVersion(InetAddress address, int i, boolean b) {
        if (viaForge$serverData instanceof ExtendedServerData) {
            final VersionEnum version = ((ExtendedServerData) viaForge$serverData).viaForge$getVersion();
            if (version != null) {
                ProtocolBase.getManager().setTargetVersionSilent(version);
            }

            viaForge$serverData = null;
        }

        return NetworkManager.createNetworkManagerAndConnect(address, i, b);
    }

}
