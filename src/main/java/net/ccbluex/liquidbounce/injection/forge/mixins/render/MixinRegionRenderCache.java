package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RegionRenderCache;
import net.minecraft.util.BlockPos;

@Mixin(RegionRenderCache.class)
public class MixinRegionRenderCache {

    @Shadow
    @Final
    private static IBlockState DEFAULT_STATE;

    @Shadow
    private IBlockState[] blockStates;
    
    @Inject(method = "getBlockState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RegionRenderCache;blockStates:[Lnet/minecraft/block/state/IBlockState;", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void getBlockState(BlockPos pos, CallbackInfoReturnable<IBlockState> cir, int positionIndex) {
        if (positionIndex < 0 || positionIndex >= this.blockStates.length) {
            cir.setReturnValue(DEFAULT_STATE);
        }
    }
}
