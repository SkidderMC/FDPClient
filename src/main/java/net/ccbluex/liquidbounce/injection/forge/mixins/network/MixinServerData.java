/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.ccbluex.liquidbounce.handler.protocol.api.ExtendedServerData;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerData.class)
public class MixinServerData implements ExtendedServerData {

    @Unique
    private ProtocolVersion viaForge$version;

    @Inject(method = "getNBTCompound", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;setString(Ljava/lang/String;Ljava/lang/String;)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    public void saveVersion(CallbackInfoReturnable<NBTTagCompound> cir, NBTTagCompound nbttagcompound) {
        if (viaForge$version != null) {
            nbttagcompound.setString("viaForge$version", viaForge$version.getName());
        }
    }

    @Inject(method = "getServerDataFromNBTCompound", at = @At(value = "TAIL"))
    private static void getVersion(NBTTagCompound nbtCompound, CallbackInfoReturnable<ServerData> cir) {
        if (nbtCompound.hasKey("viaForge$version")) {
            ProtocolVersion version;
            if (nbtCompound.getInteger("viaForge$version") != 0) {
                version = ProtocolVersion.getProtocol(nbtCompound.getInteger("viaForge$version"));
            } else {
                version = ProtocolVersion.getClosest(nbtCompound.getString("viaForge$version"));
            }
            ((ExtendedServerData) cir.getReturnValue()).viaForge$setVersion(version);
        }
    }

    @Inject(method = "copyFrom", at = @At("HEAD"))
    public void track(ServerData serverDataIn, CallbackInfo ci) {
        if (serverDataIn instanceof ExtendedServerData) {
            viaForge$version = ((ExtendedServerData) serverDataIn).viaForge$getVersion();
        }
    }

    @Override
    public ProtocolVersion viaForge$getVersion() {
        return viaForge$version;
    }

    @Override
    public void viaForge$setVersion(ProtocolVersion version) {
        viaForge$version = version;
    }

}