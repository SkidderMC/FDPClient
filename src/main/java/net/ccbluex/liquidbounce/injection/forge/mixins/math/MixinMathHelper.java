/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.math;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.client.BetterFPS;
import net.ccbluex.liquidbounce.utils.misc.betterfps.BetterFPSCore;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MathHelper.class)
@SideOnly(Side.CLIENT)
public class MixinMathHelper {
    private static BetterFPS betterFPS = null;
    private static final float[] SIN_TABLE = make(new float[65536], (e) ->
    {
        for (int i = 0; i < e.length; ++i)
        {
            e[i] = (float) Math.sin(i * Math.PI * 2.0 / 65536.0);
        }
    });
    
    @Inject(method = "sin", at = @At("HEAD"), cancellable = true)
    private static void sin(float value, CallbackInfoReturnable<Float> callbackInfoReturnable){
        if(LiquidBounce.INSTANCE.isStarting())
            return;

        if(betterFPS==null) betterFPS= (BetterFPS) LiquidBounce.moduleManager.getModule(BetterFPS.class);

        BetterFPSCore core = LiquidBounce.betterFPSCore;

        switch (betterFPS.getSinMode().get().toLowerCase()){
            case "taylor": {
                callbackInfoReturnable.setReturnValue(core.getTaylor().sin(value));
                break;
            }
            case "libgdx": {
                callbackInfoReturnable.setReturnValue(core.getLibGDX().sin(value));
                break;
            }
            case "rivensfull": {
                callbackInfoReturnable.setReturnValue(core.getRivens_full().sin(value));
                break;
            }
            case "rivenshalf": {
                callbackInfoReturnable.setReturnValue(core.getRivens_half().sin(value));
                break;
            }
            case "rivens": {
                callbackInfoReturnable.setReturnValue(core.getRivens().sin(value));
                break;
            }
            case "java": {
                callbackInfoReturnable.setReturnValue((float) Math.sin(value));
                break;
            }
            case "1.16": {
                callbackInfoReturnable.setReturnValue(SIN_TABLE[(int)(value * 10430.378F) & 65535]);
                break;
            }
        }
    }

    @Inject(method = "cos", at = @At("HEAD"), cancellable = true)
    private static void cos(float value, CallbackInfoReturnable<Float> callbackInfoReturnable){
        if(LiquidBounce.INSTANCE.isStarting())
            return;

        if(betterFPS==null) betterFPS= (BetterFPS) LiquidBounce.moduleManager.getModule(BetterFPS.class);

        BetterFPSCore core = LiquidBounce.betterFPSCore;
        switch (betterFPS.getCosMode().get().toLowerCase()){
            case "taylor": {
                callbackInfoReturnable.setReturnValue(core.getTaylor().cos(value));
                break;
            }
            case "libgdx": {
                callbackInfoReturnable.setReturnValue(core.getLibGDX().cos(value));
                break;
            }
            case "rivensfull": {
                callbackInfoReturnable.setReturnValue(core.getRivens_full().cos(value));
                break;
            }
            case "rivenshalf": {
                callbackInfoReturnable.setReturnValue(core.getRivens_half().cos(value));
                break;
            }
            case "rivens": {
                callbackInfoReturnable.setReturnValue(core.getRivens().cos(value));
                break;
            }
            case "java": {
                callbackInfoReturnable.setReturnValue((float) Math.cos(value));
                break;
            }
            case "1.16": {
                callbackInfoReturnable.setReturnValue(SIN_TABLE[(int)(value * 10430.378F + 16384.0F));
                break;
            }
        }
    }
    
    public static <T> T make(Supplier<T> supplier)
    {
        return supplier.get();
    }
}
