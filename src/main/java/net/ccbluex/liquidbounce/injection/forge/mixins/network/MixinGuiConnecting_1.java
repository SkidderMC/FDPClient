package net.ccbluex.liquidbounce.injection.forge.mixins.network;


import net.ccbluex.liquidbounce.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.protocol.api.ExtendedServerData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.raphimc.vialoader.util.VersionEnum;
import org.spongepowered.asm.mixin.Mixin;

import java.net.InetAddress;

@Mixin(targets = "net.minecraft.client.multiplayer.GuiConnecting$1")
public class MixinGuiConnecting_1 {

    public NetworkManager trackVersion(InetAddress address, int i, boolean b) {
        if (Minecraft.getMinecraft().getCurrentServerData() instanceof ExtendedServerData) {
            final VersionEnum version = ((ExtendedServerData) Minecraft.getMinecraft().getCurrentServerData()).viaForge$getVersion();
            if (version != null) {
                ProtocolBase.getManager().setTargetVersionSilent(version);
            }
        }

        return NetworkManager.createNetworkManagerAndConnect(address, i, b);
    }

}
