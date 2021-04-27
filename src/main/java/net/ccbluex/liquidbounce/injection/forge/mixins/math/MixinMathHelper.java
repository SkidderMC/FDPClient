/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.math;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.misc.BetterFPS;
import net.ccbluex.liquidbounce.utils.misc.betterfps.BetterFPSCore;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MathHelper.class)
@SideOnly(Side.CLIENT)
public class MixinMathHelper {
  @Inject(method = "sin", at = @At("HEAD"), cancellable = true)
  private void sin(float value, CallbackInfoReturnable<Float> callbackInfoReturnable){
    if(!LiquidBounce.INSTANCE.getStartSUCCESS())
      return;

    String mode = BetterFPS.getSinMode();
    BetterFPSCore core = LiquidBounce.betterFPSCore;
    switch (mode.toLowerCase()){
      case "taylor":
        callbackInfoReturnable.setReturnValue(core.getTaylor().sin(value));
        break;
      case "libgdx":
        callbackInfoReturnable.setReturnValue(core.getLibGDX().sin(value));
        break;
      case "rivensfull":
        callbackInfoReturnable.setReturnValue(core.getRivens_full().sin(value));
        break;
      case "rivenshalf":
        callbackInfoReturnable.setReturnValue(core.getRivens_half().sin(value));
        break;
      case "rivens":
        callbackInfoReturnable.setReturnValue(core.getRivens().sin(value));
        break;
    }
  }
  
  @Inject(method = "cos", at = @At("HEAD"), cancellable = true)
  private void cos(float value, CallbackInfoReturnable<Float> callbackInfoReturnable){
    if(!LiquidBounce.INSTANCE.getStartSUCCESS())
      return;

    String mode = BetterFPS.getCosMode();
    BetterFPSCore core = LiquidBounce.betterFPSCore;
    switch (mode.toLowerCase()){
      case "taylor":
        callbackInfoReturnable.setReturnValue(core.getTaylor().cos(value));
        break;
      case "libgdx":
        callbackInfoReturnable.setReturnValue(core.getLibGDX().cos(value));
        break;
      case "rivensfull":
        callbackInfoReturnable.setReturnValue(core.getRivens_full().cos(value));
        break;
      case "rivenshalf":
        callbackInfoReturnable.setReturnValue(core.getRivens_half().cos(value));
        break;
      case "rivens":
        callbackInfoReturnable.setReturnValue(core.getRivens().cos(value));
        break;
    }
  }
}
