/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import me.zywl.fdpclient.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.visual.CustomModel;
import net.ccbluex.liquidbounce.features.module.modules.visual.PlayerEdit;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {
    @Unique
    private final ResourceLocation fDPClient$rabbit = new ResourceLocation("fdpclient/cosmetic/skins/rabbit.png");
    @Unique
    private final ResourceLocation fDPClient$freddy = new ResourceLocation("fdpclient/cosmetic/skins/freddy.png");
    @Unique
    private final ResourceLocation fDPClient$amogus = new ResourceLocation("fdpclient/cosmetic/skins/amogus.png");
    /**
     * Render living at.
     *
     * @param entityLivingBaseIn the entity living base in
     * @param x                  the x
     * @param y                  the y
     * @param z                  the z
     * @param callbackInfo       the callback info
     */
    @Inject(method = "renderLivingAt*", at = @At("HEAD"))
    protected void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z, CallbackInfo callbackInfo) {
        final PlayerEdit playerEdit = Objects.requireNonNull(FDPClient.moduleManager.getModule(PlayerEdit.class));

        if (playerEdit.getState() & entityLivingBaseIn.equals(MinecraftInstance.mc.thePlayer) && PlayerEdit.editPlayerSizeValue.get()) {
            GlStateManager.scale(PlayerEdit.playerSizeValue.get(), PlayerEdit.playerSizeValue.get(), PlayerEdit.playerSizeValue.get());
        }
    }
    /**
     * Gets entity texture.
     *
     * @param entity the entity
     * @param ci     the ci
     */
    @Inject(method = {"getEntityTexture*"}, at = {@At("HEAD")}, cancellable = true)
    public void getEntityTexture(AbstractClientPlayer entity, CallbackInfoReturnable<ResourceLocation> ci) {
        if ((CustomModel.onlyMe.get() && entity == Minecraft.getMinecraft().thePlayer || CustomModel.onlyOther.get() && entity != Minecraft.getMinecraft().thePlayer) && Objects.requireNonNull(FDPClient.moduleManager.getModule(CustomModel.class)).getState()) {
            if (CustomModel.mode.get().contains("Rabbit")) {
                ci.setReturnValue(fDPClient$rabbit);
            }
            if (CustomModel.mode.get().contains("Freddy")) {
                ci.setReturnValue(fDPClient$freddy);
            }
            if (CustomModel.mode.get().contains("Amogus")) {
                ci.setReturnValue(fDPClient$amogus);
            }
        }
    }
}
