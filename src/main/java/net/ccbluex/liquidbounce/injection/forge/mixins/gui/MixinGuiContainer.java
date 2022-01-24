package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.KeyEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.world.ChestStealer;
import net.ccbluex.liquidbounce.utils.extensions.RendererExtensionKt;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.minecraft.client.Minecraft;
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
                    RendererExtensionKt.drawCenteredString(mc.fontRendererObj, "%ui.chest.stealing%", width / 2, (height / 2) + 30, 0xffffffff, false);
                }
                callbackInfo.cancel();
            }
        } else {
            mc.currentScreen.drawWorldBackground(0);

            final Animations animations = Animations.INSTANCE;
            double pct = Math.max(animations.getInvTimeValue().get() - (System.currentTimeMillis() - guiOpenTime), 0) / ((double) animations.getInvTimeValue().get());
            if (pct != 0) {
                GL11.glPushMatrix();

                pct = EaseUtils.INSTANCE.apply(EaseUtils.EnumEasingType.valueOf(animations.getInvEaseMode().get()),
                        EaseUtils.EnumEasingOrder.valueOf(animations.getInvEaseOrderMode().get()), pct);

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
        if (chestStealer.getState() && chestStealer.getSilentTitleValue().get() && mc.currentScreen instanceof GuiChest)
            LiquidBounce.eventManager.callEvent(new KeyEvent(keyCode == 0 ? typedChar + 256 : keyCode));
    }
}
