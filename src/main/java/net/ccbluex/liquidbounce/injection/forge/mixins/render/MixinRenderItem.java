/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.visual.Glint;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

    @Shadow
    protected abstract void renderModel(IBakedModel model, int color);

    @Redirect(method = "renderEffect", at = @At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/RenderItem;renderModel(Lnet/minecraft/client/resources/model/IBakedModel;I)V"))
    private void renderModel(RenderItem renderItem, IBakedModel model, int color) {
        this.renderModel(model, Objects.requireNonNull(FDPClient.moduleManager.getModule(Glint.class)).getState() ? Objects.requireNonNull(FDPClient.moduleManager.getModule(Glint.class)).getColor().getRGB() : -8372020);
    }
}

