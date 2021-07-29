/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.features.module.modules.render.AntiBlind;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.utils.ClassUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(GuiIngame.class)
public abstract class MixinGuiInGame {

    @Shadow
    protected abstract void renderHotbarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer player);

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void renderScoreboard(CallbackInfo callbackInfo) {
        if (LiquidBounce.moduleManager.getModule(HUD.class).getState())
            callbackInfo.cancel();
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void renderTooltip(ScaledResolution sr, float partialTicks, CallbackInfo callbackInfo) {
        final HUD hud = LiquidBounce.moduleManager.getModule(HUD.class);

        if(Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer && hud.getState() && hud.getBetterHotbarValue().get()) {
            EntityPlayer entityPlayer = (EntityPlayer) Minecraft.getMinecraft().getRenderViewEntity();

            int middleScreen = sr.getScaledWidth() / 2;
            int hotbarAlpha=hud.getHotbarAlphaValue().get();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            GuiIngame.drawRect(middleScreen - 91, sr.getScaledHeight() - 22, middleScreen + entityPlayer.inventory.currentItem * 20 - 91, sr.getScaledHeight(), ColorUtils.reAlpha(Color.BLACK,hotbarAlpha).getRGB());
            GuiIngame.drawRect(middleScreen + entityPlayer.inventory.currentItem * 20 - 70, sr.getScaledHeight() - 22, middleScreen + 90, sr.getScaledHeight(), ColorUtils.reAlpha(Color.BLACK,hotbarAlpha).getRGB());
            GuiIngame.drawRect(middleScreen - 91, sr.getScaledHeight() - 24, middleScreen + entityPlayer.inventory.currentItem * 20 - 91, sr.getScaledHeight() - 22, ColorUtils.reAlpha(new Color(255,127,80),hotbarAlpha).getRGB());
            GuiIngame.drawRect(middleScreen + entityPlayer.inventory.currentItem * 20 - 70, sr.getScaledHeight() - 24, middleScreen + 90, sr.getScaledHeight() - 22, ColorUtils.reAlpha(new Color(255,127,80),hotbarAlpha).getRGB());
            GuiIngame.drawRect(middleScreen + entityPlayer.inventory.currentItem * 20 - 91, sr.getScaledHeight() - 20, middleScreen + entityPlayer.inventory.currentItem * 20 - 70, sr.getScaledHeight(), ColorUtils.reAlpha(Color.WHITE,hotbarAlpha).getRGB());
            GuiIngame.drawRect(middleScreen + entityPlayer.inventory.currentItem * 20 - 91, sr.getScaledHeight() - 24, middleScreen + entityPlayer.inventory.currentItem * 20 - 70, sr.getScaledHeight()-20, ColorUtils.reAlpha(new Color(0,245,255),hotbarAlpha).getRGB());

            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();

            for(int j = 0; j < 9; ++j) {
                int k = sr.getScaledWidth() / 2 - 90 + j * 20 + 2;
                int l = sr.getScaledHeight() - 16 - 3;
                this.renderHotbarItem(j, k, l, partialTicks, entityPlayer);
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();

            LiquidBounce.eventManager.callEvent(new Render2DEvent(partialTicks));
            callbackInfo.cancel();
        }
    }

    @Inject(method = "renderTooltip", at = @At("RETURN"))
    private void renderTooltipPost(ScaledResolution sr, float partialTicks, CallbackInfo callbackInfo) {
        if (!ClassUtils.hasClass("net.labymod.api.LabyModAPI")) {
            LiquidBounce.eventManager.callEvent(new Render2DEvent(partialTicks));
        }
    }

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    private void renderPumpkinOverlay(final CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = (AntiBlind) LiquidBounce.moduleManager.getModule(AntiBlind.class);

        if(antiBlind.getState() && antiBlind.getPumpkinEffect().get())
            callbackInfo.cancel();
    }
}