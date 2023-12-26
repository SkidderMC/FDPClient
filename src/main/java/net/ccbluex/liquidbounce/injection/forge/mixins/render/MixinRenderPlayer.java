/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.visual.CustomModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {
    private final ResourceLocation rabbit = new ResourceLocation("fdpclient/models/rabbit.png");
    private final ResourceLocation freddy = new ResourceLocation("fdpclient/models/freddy.png");
    private final ResourceLocation amogus = new ResourceLocation("fdpclient/models/amogus.png");
    @Inject(method = "renderLivingAt", at = @At("HEAD"))
    protected void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z, CallbackInfo callbackInfo) {
        if(FDPClient.moduleManager.get(CustomModel.class).getState() & entityLivingBaseIn.equals(Minecraft.getMinecraft().thePlayer) && CustomModel.editPlayerSizeValue.get()) {
            GlStateManager.scale(CustomModel.playerSizeValue.get(), CustomModel.playerSizeValue.get(), CustomModel.playerSizeValue.get());
        }
    }
    @Inject(method = {"getEntityTexture"}, at = {@At("HEAD")}, cancellable = true)
    public void getEntityTexture(AbstractClientPlayer entity, CallbackInfoReturnable<ResourceLocation> ci) {
        if (CustomModel.customModel.get()) {
            FDPClient.moduleManager.getModule(CustomModel.class);
            if ((CustomModel.onlyMe.get() && entity == Minecraft.getMinecraft().thePlayer || CustomModel.onlyOther.get() && entity != Minecraft.getMinecraft().thePlayer) && FDPClient.moduleManager.getModule(CustomModel.class).getState()) {
                FDPClient.moduleManager.getModule(CustomModel.class);
                if (CustomModel.mode.get().contains("Rabbit")) {
                    ci.setReturnValue(rabbit);
                }
                FDPClient.moduleManager.getModule(CustomModel.class);
                if (CustomModel.mode.get().contains("Freddy")) {
                    ci.setReturnValue(freddy);
                }
                FDPClient.moduleManager.getModule(CustomModel.class);
                if (CustomModel.mode.get().contains("Amogus")) {
                    ci.setReturnValue(amogus);
                }
            }
        }
    }
}
