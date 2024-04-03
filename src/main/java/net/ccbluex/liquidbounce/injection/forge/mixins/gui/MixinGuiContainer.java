/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import lombok.Setter;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.KeyEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.player.InvManager;
import net.ccbluex.liquidbounce.features.module.modules.player.Stealer;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * The type Mixin gui container.
 */
@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends MixinGuiScreen {
    /**
     * The X size.
     */
    @Shadow
    protected int xSize;
    /**
     * The Y size.
     */
    @Shadow
    protected int ySize;
    /**
     * The Gui left.
     */
    @Shadow
    protected int guiLeft;
    /**
     * The Gui top.
     */
    @Shadow
    protected int guiTop;
    @Shadow
    private int dragSplittingButton;
    @Shadow
    private int dragSplittingRemnant;
    @Setter
    private GuiButton fDPClient$stealButton, fDPClient$chestStealerButton, fDPClient$invManagerButton, fDPClient$killAuraButton;
    private float fDPClient$progress = 0F;
    private long fDPClient$lastMS = 0L;
    private boolean fDPClient$translated = false;
    /**
     * Check hotbar keys boolean.
     *
     * @param keyCode the key code
     * @return the boolean
     */
    @Shadow
    protected abstract boolean checkHotbarKeys(int keyCode);

    /**
     * Inject init gui.
     *
     * @param callbackInfo the callback info
     */
    @Inject(method = "initGui", at = @At("HEAD"))
    public void injectInitGui(CallbackInfo callbackInfo) {
        GuiScreen guiScreen = MinecraftInstance.mc.currentScreen;

        if (guiScreen instanceof GuiChest) {
            buttonList.add(fDPClient$killAuraButton = new GuiButton(1024576, 5, 5, 150, 20, "Disable KillAura"));
            buttonList.add(fDPClient$chestStealerButton = new GuiButton(727, 5, 27, 150, 20, "Disable Stealer"));
            buttonList.add(fDPClient$invManagerButton = new GuiButton(321123, 5, 49, 150, 20, "Disable Manager"));
        }

        fDPClient$lastMS = System.currentTimeMillis();
        fDPClient$progress = 0F;
    }

    @Override
    protected void fDPClient$injectedActionPerformed(GuiButton button) {
        final KillAura killAura = Objects.requireNonNull(FDPClient.moduleManager.getModule(KillAura.class));
        final InvManager invManager = Objects.requireNonNull(FDPClient.moduleManager.getModule(InvManager.class));
        final Stealer stealer = Objects.requireNonNull(FDPClient.moduleManager.getModule(Stealer.class));
        if (button.id == 1024576)
            killAura.setState(false);
        if (button.id == 321123)
            invManager.setState(false);
        if (button.id == 727)
            stealer.setState(false);
    }

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    private void drawScreenHead(CallbackInfo callbackInfo) {
        Stealer stealer = Objects.requireNonNull(FDPClient.moduleManager.getModule(Stealer.class));
        KillAura killAura = Objects.requireNonNull(FDPClient.moduleManager.getModule(KillAura.class));
        InvManager invManager = Objects.requireNonNull(FDPClient.moduleManager.getModule(InvManager.class));
        final Minecraft mc = MinecraftInstance.mc;

        if (fDPClient$progress >= 1F) fDPClient$progress = 1F;
        else fDPClient$progress = (float) (System.currentTimeMillis() - fDPClient$lastMS) / (float) 200;

        if ((!(mc.currentScreen instanceof GuiChest)
                || !stealer.getState()
                || !stealer.getSilenceValue().get()
                || stealer.getStillDisplayValue().get()))
            RenderUtils.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);

        try {
            GuiScreen guiScreen = mc.currentScreen;

            if (fDPClient$stealButton != null) fDPClient$stealButton.enabled = !stealer.getState();
            if (fDPClient$killAuraButton != null)
                fDPClient$killAuraButton.enabled = killAura.getState();
            if (fDPClient$chestStealerButton != null) fDPClient$chestStealerButton.enabled = stealer.getState();
            if (fDPClient$invManagerButton != null)
                fDPClient$invManagerButton.enabled = invManager.getState();

            if (stealer.getState() && stealer.getSilenceValue().get() && guiScreen instanceof GuiChest) {
                mc.setIngameFocus();
                mc.currentScreen = guiScreen;

                //hide GUI
                if (stealer.getShowStringValue().get() && !stealer.getStillDisplayValue().get()) {
                    String tipString = "Stealing... Press Esc to stop.";

                    mc.fontRendererObj.drawString(tipString,
                            (width / 2F) - (mc.fontRendererObj.getStringWidth(tipString) / 2F) - 0.5F,
                            (height / 2F) + 30, 0, false);
                    mc.fontRendererObj.drawString(tipString,
                            (width / 2F) - (mc.fontRendererObj.getStringWidth(tipString) / 2F) + 0.5F,
                            (height / 2F) + 30, 0, false);
                    mc.fontRendererObj.drawString(tipString,
                            (width / 2F) - (mc.fontRendererObj.getStringWidth(tipString) / 2F),
                            (height / 2F) + 29.5F, 0, false);
                    mc.fontRendererObj.drawString(tipString,
                            (width / 2F) - (mc.fontRendererObj.getStringWidth(tipString) / 2F),
                            (height / 2F) + 30.5F, 0, false);
                    mc.fontRendererObj.drawString(tipString,
                            (width / 2F) - (mc.fontRendererObj.getStringWidth(tipString) / 2F),
                            (height / 2F) + 30, 0xffffffff, false);
                }

                if (!stealer.getOnce() && !stealer.getStillDisplayValue().get())
                    callbackInfo.cancel();
            }
        } catch (Exception e) {
            // Basic exception handling with a print stack trace
            e.printStackTrace();
        }

        // The corrected placement of the else block
        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChest)) {
            mc.currentScreen.drawWorldBackground(0);

            final Animations animations = Animations.INSTANCE;
            long fDPClient$guiOpenTime = -1;
            double pct = Math.max(animations.getInvTimeValue().get() - (System.currentTimeMillis() - fDPClient$guiOpenTime), 0) / ((double) animations.getInvTimeValue().get());
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
                        break;
                    }
                }

                fDPClient$translated = true;
                GL11.glPopMatrix(); // Make sure to pop the matrix to avoid affecting other render calls
            }
        }
    }


    /**
     * Draw screen return.
     *
     * @param callbackInfo the callback info
     */
    @Inject(method = "drawScreen", at = @At("RETURN"))
    public void drawScreenReturn(CallbackInfo callbackInfo) {
        if (fDPClient$translated) {
            GL11.glPopMatrix();
            fDPClient$translated = false;
        }
        final Animations animMod = Objects.requireNonNull(FDPClient.moduleManager.getModule(Animations.class));
        Stealer stealer = Objects.requireNonNull(FDPClient.moduleManager.getModule(Stealer.class));
        final Minecraft mc = MinecraftInstance.mc;
        boolean checkFullSilence = stealer.getState() && stealer.getSilenceValue().get() && !stealer.getStillDisplayValue().get();

        if (animMod.getState() && !(mc.currentScreen instanceof GuiChest && checkFullSilence))
            GL11.glPopMatrix();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void checkCloseClick(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (mouseButton - 100 == mc.gameSettings.keyBindInventory.getKeyCode()) {
            mc.thePlayer.closeScreen();
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void checkHotbarClicks(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        checkHotbarKeys(mouseButton - 100);
    }

    @Inject(method = "updateDragSplitting", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void fixRemnants(CallbackInfo ci) {
        if (this.dragSplittingButton == 2) {
            this.dragSplittingRemnant = mc.thePlayer.inventory.getItemStack().getMaxStackSize();
            ci.cancel();
        }
    }

    @Inject(method = "keyTyped", at = @At("HEAD"))
    private void keyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        try {
            if (Objects.requireNonNull(FDPClient.moduleManager.getModule(Stealer.class)).getState() && Objects.requireNonNull(FDPClient.moduleManager.getModule(Stealer.class)).getSilentTitleValue().get() && mc.currentScreen instanceof GuiChest)
                FDPClient.eventManager.callEvent(new KeyEvent(keyCode == 0 ? typedChar + 256 : keyCode));
        } catch (Exception ignored){

        }
    }

}