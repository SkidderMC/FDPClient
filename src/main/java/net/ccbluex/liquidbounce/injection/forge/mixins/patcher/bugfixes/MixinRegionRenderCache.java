/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.patcher.bugfixes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RegionRenderCache;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RegionRenderCache.class)
public class MixinRegionRenderCache {
    @Shadow
    @Final
    private static IBlockState DEFAULT_STATE;

    @Shadow
    private IBlockState[] blockStates;

    @Inject(
        method = "getBlockState",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RegionRenderCache;blockStates:[Lnet/minecraft/block/state/IBlockState;", ordinal = 0, shift = At.Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true
    )
    private void connectedTexturesBoundsCheck(BlockPos pos, CallbackInfoReturnable<IBlockState> cir, int positionIndex) {
        if (positionIndex < 0 || positionIndex >= this.blockStates.length) {
            cir.setReturnValue(DEFAULT_STATE);
        }
    }
}
