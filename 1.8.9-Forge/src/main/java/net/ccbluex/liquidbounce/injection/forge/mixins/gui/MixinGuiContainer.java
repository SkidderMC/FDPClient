package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.world.ChestStealer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
@SideOnly(Side.CLIENT)
public class MixinGuiContainer {
    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    private void drawScreen(CallbackInfo callbackInfo){
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
                mc.fontRendererObj.drawString("STEALING CHEST",(scaledResolution.getScaledWidth()/2)-50
                        ,(scaledResolution.getScaledHeight()/2)+30,0xffffffff);
                callbackInfo.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
