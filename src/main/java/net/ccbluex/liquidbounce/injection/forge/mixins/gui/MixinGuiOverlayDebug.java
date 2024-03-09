package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.gui.GuiOverlayDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GuiOverlayDebug.class)
public class MixinGuiOverlayDebug {

    @Inject(method = "getDebugInfoRight", at = @At(value = "TAIL"))
    public void addProtocolVersion(CallbackInfoReturnable<List<String>> cir) {
        final ProtocolVersion version = ProtocolBase.getManager().getTargetVersion();

        cir.getReturnValue().add("");

        if (!MinecraftInstance.mc.isIntegratedServerRunning())
            cir.getReturnValue().add("Protocol: " + version.getName());
        else cir.getReturnValue().add("Protocol: 1.8.x");

        cir.getReturnValue().add("");

        cir.getReturnValue().add(FDPClient.CLIENT_NAME + " Client " + FDPClient.CLIENT_VERSION);
    }
}
