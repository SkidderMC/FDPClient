/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.performance;

import net.ccbluex.liquidbounce.injection.access.IMixinWorldAccess;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = {RenderGlobal.class})
public abstract class MixinRenderGlobal implements IMixinWorldAccess {

    @Shadow
    protected abstract void markBlocksForUpdate(int var1, int var2, int var3, int var4, int var5, int var6);

    @Override
    public void fDPClient$notifyLightSet(int n, int n2, int n3) {
        this.markBlocksForUpdate(n - 1, n2 - 1, n3 - 1, n + 1, n2 + 1, n3 + 1);
    }
}
