/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.math;

import net.minecraft.util.MathHelper
import org.spongepowered.asm.mixin.Mixin;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MathHelper.class)
@SideOnly(Side.CLIENT)
public class MixinMathHelper {
  @Inject(method = "sin", at = "HEAD")
  private void sin(float value, CallbackInfo callbackInfo){
    
  }
  
  @Inject(method = "cos", at = "HEAD")
  private void cos(float value, CallbackInfo callbackInfo){
    
  }
}
