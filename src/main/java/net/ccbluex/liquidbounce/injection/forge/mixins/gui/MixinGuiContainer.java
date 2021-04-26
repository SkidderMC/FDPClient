package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.render.Animations;
import net.ccbluex.liquidbounce.features.module.modules.world.ChestStealer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
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
public abstract class MixinGuiContainer extends MixinGuiScreen {
    @Shadow
    private int xSize;
    @Shadow
    private int ySize;
    @Shadow
    private int guiLeft;
    @Shadow
    private int guiTop;

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
                String tipString="STEALING CHEST";
                mc.fontRendererObj.drawString(tipString,
                        (width/2)-(mc.fontRendererObj.getStringWidth(tipString)/2),
                        (height/2)+30,0xffffffff);
                callbackInfo.cancel();
            }else{
                mc.currentScreen.drawWorldBackground(0);

                Animations inventoryAnimation = (Animations) LiquidBounce.moduleManager.getModule(Animations.class);
                if(inventoryAnimation != null && inventoryAnimation.getState()) {
                    float pct = Math.max(inventoryAnimation.getTimeValue().get() - (System.currentTimeMillis() - guiOpenTime), 0) / ((float)inventoryAnimation.getTimeValue().get());
                    if (pct != 0) {
                        GL11.glPushMatrix();

                        switch (inventoryAnimation.getMoveValue().get().toLowerCase()){
                            case "slide":{
                                GL11.glTranslatef(0F, -(guiTop + ySize) * pct, 0F);
                                break;
                            }
                            case "zoom":{
                                float scale=1-pct;
                                GL11.glScalef(scale,scale,scale);
                                GL11.glTranslatef(((guiLeft+(xSize*0.5F*pct))/scale)-guiLeft,((guiTop+(ySize*0.5F*pct))/scale)-guiTop, 0F);
                            }
                        }

                        translated = true;
                    }
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