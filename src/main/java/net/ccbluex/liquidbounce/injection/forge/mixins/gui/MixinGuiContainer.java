/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.KeyEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.player.InventoryCleaner;
import net.ccbluex.liquidbounce.features.module.modules.world.ChestStealer;
import net.ccbluex.liquidbounce.utils.extensions.RendererExtensionKt;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends MixinGuiScreen {
    @Shadow
    protected int xSize;
    @Shadow
    protected int ySize;
    @Shadow
    protected int guiLeft;
    @Shadow
    protected int guiTop;

    private long guiOpenTime = -1;

    private boolean translated = false;

    private GuiButton stealButton, chestStealerButton, invManagerButton, killAuraButton;

    private float progress = 0F;

    private long lastMS = 0L;

    @Inject(method = "initGui", at = @At("HEAD"), cancellable = true)
    public void injectInitGui(CallbackInfo callbackInfo){
        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
        final HUD hud = (HUD) LiquidBounce.moduleManager.getModule(HUD.class);

        int firstY = 0;

        if (guiScreen instanceof GuiChest) {
            switch (hud.getContainerButton().get()) {
                case "TopLeft":
                    if (LiquidBounce.moduleManager.getModule(KillAura.class).getState()) {
                        buttonList.add(killAuraButton = new GuiButton(1024576, 5, 5, 140, 20, "Disable KillAura"));
                        firstY += 20;
                    }
                    if (LiquidBounce.moduleManager.getModule(InventoryCleaner.class).getState()) {
                        buttonList.add(invManagerButton = new GuiButton(321123, 5, 5 + firstY, 140, 20, "Disable InvCleaner"));
                        firstY += 20;
                    }
                    if (LiquidBounce.moduleManager.getModule(ChestStealer.class).getState()) {
                        buttonList.add(chestStealerButton = new GuiButton(727, 5, 5 + firstY, 140, 20, "Disable Stealer"));
                        firstY += 20;
                    }
                    buttonList.add(stealButton = new GuiButton(1234123, 5, 5 + firstY, 140, 20, "Steal this chest"));
                    break;
                case "TopRight":
                    if (LiquidBounce.moduleManager.getModule(KillAura.class).getState()) {
                        buttonList.add(killAuraButton = new GuiButton(1024576, width - 145, 5, 140, 20, "Disable KillAura"));
                        firstY += 20;
                    }
                    if (LiquidBounce.moduleManager.getModule(InventoryCleaner.class).getState()) {
                        buttonList.add(invManagerButton = new GuiButton(321123, width - 145, 5 + firstY, 140, 20, "Disable InvCleaner"));
                        firstY += 20;
                    }
                    if (LiquidBounce.moduleManager.getModule(ChestStealer.class).getState()) {
                        buttonList.add(chestStealerButton = new GuiButton(727, width - 145, 5 + firstY, 140, 20, "Disable Stealer"));
                        firstY += 20;
                    }
                    buttonList.add(stealButton = new GuiButton(1234123, width - 145, 5 + firstY, 140, 20, "Steal this chest"));
                    break;
            }
        }

        lastMS = System.currentTimeMillis();
        progress = 0F;
    }

    @Override
    protected void injectedActionPerformed(GuiButton button) {
        ChestStealer chestStealer = (ChestStealer) LiquidBounce.moduleManager.getModule(ChestStealer.class);

        if (button.id == 1024576)
            LiquidBounce.moduleManager.getModule(KillAura.class).setState(false);
        if (button.id == 321123)
            LiquidBounce.moduleManager.getModule(InventoryCleaner.class).setState(false);
        if (button.id == 727)
            chestStealer.setState(false);
        if (button.id == 1234123 && !chestStealer.getState()) {
            chestStealer.setContentReceived(mc.thePlayer.openContainer.windowId);
            chestStealer.setOnce(true);
            chestStealer.setState(true);
        }
    }

    @Inject(method = "initGui", at = @At("RETURN"))
    private void initGuiReturn(CallbackInfo callbackInfo) {
        guiOpenTime = System.currentTimeMillis();
    }

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    private void drawScreenHead(CallbackInfo callbackInfo) {
        ChestStealer chestStealer = LiquidBounce.moduleManager.getModule(ChestStealer.class);
        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen guiScreen = mc.currentScreen;
        if (chestStealer.getState() && chestStealer.getSilentValue().get() && guiScreen instanceof GuiChest) {
            GuiChest chest = (GuiChest) guiScreen;
            if (!(chestStealer.getChestTitleValue().get() && (chest.lowerChestInventory == null || !chest.lowerChestInventory.getName().contains(new ItemStack(Item.itemRegistry.getObject(new ResourceLocation("minecraft:chest"))).getDisplayName())))) {
                // mouse focus
                mc.setIngameFocus();
                mc.currentScreen = guiScreen;
                // hide GUI
                if (chestStealer.getSilentTitleValue().get()) {
                    RendererExtensionKt.drawCenteredString(mc.fontRendererObj, "Stealing", width / 2, (height / 2) + 30, 0xffffffff, false);
                }
                callbackInfo.cancel();
            }
        } else {
            mc.currentScreen.drawWorldBackground(0);

            final Animations animations = Animations.INSTANCE;
            double pct = Math.max(animations.getInvTimeValue().get() - (System.currentTimeMillis() - guiOpenTime), 0) / ((double) animations.getInvTimeValue().get());
            if (pct != 0) {
                GL11.glPushMatrix();

                pct = EaseUtils.INSTANCE.apply(EaseUtils.EnumEasingType.valueOf(animations.getInvEaseModeValue().get()),
                        EaseUtils.EnumEasingOrder.valueOf(animations.getInvEaseOrderModeValue().get()), pct);

                switch (animations.getInvModeValue().get().toLowerCase()) {
                    case "slide": {
                        GL11.glTranslated(0, -(guiTop + ySize) * pct, 0);
                        break;
                    }
                    case "zoom": {
                        double scale = 1 - pct;
                        GL11.glScaled(scale, scale, scale);
                        GL11.glTranslated(((guiLeft + (xSize * 0.5 * pct)) / scale) - guiLeft,
                                ((guiTop + (ySize * 0.5d * pct)) / scale) - guiTop,
                                0);
                    }
                }

                translated = true;
            }
        }
        try {

            if (stealButton != null) stealButton.enabled = !chestStealer.getState();
            if (killAuraButton != null) killAuraButton.enabled = LiquidBounce.moduleManager.getModule(KillAura.class).getState();
            if (chestStealerButton != null) chestStealerButton.enabled = chestStealer.getState();
            if (invManagerButton != null) invManagerButton.enabled = LiquidBounce.moduleManager.getModule(InventoryCleaner.class).getState();

            if(chestStealer.getState() && chestStealer.getSilentValue().get() && guiScreen instanceof GuiChest) {
                mc.setIngameFocus();
                mc.currentScreen = guiScreen;

                //hide GUI
                if (chestStealer.getShowStringValue().get() && !chestStealer.getStillDisplayValue().get()) {
                    String tipString = "Stealing... Press Esc to stop.";

                    mc.fontRendererObj.drawString(tipString,
                            (width/2)-(mc.fontRendererObj.getStringWidth(tipString)/2)-1,
                            (height/2)+30,0,false);
                    mc.fontRendererObj.drawString(tipString,
                            (width/2)-(mc.fontRendererObj.getStringWidth(tipString)/2)+1,
                            (height/2)+30,0,false);
                    mc.fontRendererObj.drawString(tipString,
                            (width/2)-(mc.fontRendererObj.getStringWidth(tipString)/2),
                            (height/2)+30-1,0,false);
                    mc.fontRendererObj.drawString(tipString,
                            (width/2)-(mc.fontRendererObj.getStringWidth(tipString)/2),
                            (height/2)+30+1,0,false);
                    mc.fontRendererObj.drawString(tipString,
                            (width/2)-(mc.fontRendererObj.getStringWidth(tipString)/2),
                            (height/2)+30,0xffffffff,false);
                }

                if (!chestStealer.getOnce() && !chestStealer.getStillDisplayValue().get())
                    callbackInfo.cancel();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }


    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void drawScreenReturn(CallbackInfo callbackInfo) {
        if (translated) {
            GL11.glPopMatrix();
            translated = false;
        }
    }

    @Inject(method = "keyTyped", at = @At("HEAD"))
    private void keyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        ChestStealer chestStealer = LiquidBounce.moduleManager.getModule(ChestStealer.class);
       try {
           if (chestStealer.getState() && chestStealer.getSilentTitleValue().get() && mc.currentScreen instanceof GuiChest)
               LiquidBounce.eventManager.callEvent(new KeyEvent(keyCode == 0 ? typedChar + 256 : keyCode));
       }catch (Exception e){

       }
    }
}
