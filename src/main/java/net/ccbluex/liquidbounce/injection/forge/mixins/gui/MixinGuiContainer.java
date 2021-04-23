package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.world.ChestStealer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
@SideOnly(Side.CLIENT)
public class MixinGuiContainer {
    @Shadow
    private int guiTop;
    @Shadow
    private int ySize;

    private long guiOpenTime=-1;
    private boolean translated=false;

    @Inject(method = "initGui", at = @At("RETURN"), cancellable = true)
    private void initGui(CallbackInfo callbackInfo) {
        guiOpenTime=System.currentTimeMillis();
    }

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    private void drawScreenHead(CallbackInfo callbackInfo){
        ChestStealer chestStealer=(ChestStealer) LiquidBounce.moduleManager.getModule(ChestStealer.class);
        try {
            Minecraft mc=Minecraft.getMinecraft();
            GuiScreen guiScreen=mc.currentScreen;
            if(chestStealer.getState()&&chestStealer.getSilenceValue().get()&&guiScreen instanceof GuiChest){
                //mouse focus
                mc.setIngameFocus();
                mc.currentScreen=guiScreen;
                //hide GUI
                ScaledResolution scaledResolution=new ScaledResolution(mc);
                String tipString="STEALING CHEST";
                mc.fontRendererObj.drawString(tipString,
                        (scaledResolution.getScaledWidth()/2)-(mc.fontRendererObj.getStringWidth(tipString)/2),
                        (scaledResolution.getScaledHeight()/2)+30,0xffffffff);
                callbackInfo.cancel();
            }else{
                mc.currentScreen.drawWorldBackground(0);
                float pct=Math.max(300-(System.currentTimeMillis()-guiOpenTime),0)/300F;
                if(pct!=0) {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(0F, -(guiTop + ySize) * pct, 0F);
                    translated = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void drawScreenReturn(CallbackInfo callbackInfo){
        if(translated){
            GL11.glPopMatrix();
            translated=false;
        }
    }
}
