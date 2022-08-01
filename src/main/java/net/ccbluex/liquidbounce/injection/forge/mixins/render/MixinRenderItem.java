package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.render.Glint;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

    @Shadow
    protected abstract void renderModel(IBakedModel model, int color);

    @Redirect(method = "renderEffect", at = @At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/RenderItem;renderModel(Lnet/minecraft/client/resources/model/IBakedModel;I)V"))
    private void renderModel(RenderItem renderItem, IBakedModel model, int color) {
        Glint glint = LiquidBounce.moduleManager.getModule(Glint.class);
        this.renderModel(model, glint.getState() ? glint.getColor().getRGB() : -8372020);
    }
}

