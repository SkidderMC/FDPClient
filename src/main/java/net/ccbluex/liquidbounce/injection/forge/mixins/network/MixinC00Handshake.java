/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import net.ccbluex.liquidbounce.features.special.ClientSpoof;
import net.minecraft.client.Minecraft;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.handshake.client.C00Handshake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(C00Handshake.class)
public class MixinC00Handshake {

    @Shadow
    public int port;
    @Shadow
    public String ip;
    @Shadow
    private int protocolVersion;
    @Shadow
    private EnumConnectionState requestedState;

    /**
     * @author CCBlueX
     */
    @ModifyConstant(method = "writePacketData", constant = @Constant(stringValue = "\u0000FML\u0000"))
    private String injectAntiForge(String constant) {
        return ClientSpoof.enabled && !Minecraft.getMinecraft().isIntegratedServerRunning() ? "" : "\u0000FML\u0000";
    }
}