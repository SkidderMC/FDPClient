/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Render.class)
public abstract class MixinRender {
    @Shadow
    protected abstract <T extends Entity> boolean bindEntityTexture(T entity);

    @Redirect(method={"renderLivingLabel(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V"}, at=@At(value="FIELD", target="Lnet/minecraft/client/renderer/entity/RenderManager;playerViewX:F"))
    private float renderLivingLabel(RenderManager renderManager) {
        return Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -renderManager.playerViewX : renderManager.playerViewX;
    }
}
