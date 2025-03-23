/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {

    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    private final Gui gui = new Gui();

    final HUDModule hudModule = HUDModule.INSTANCE;

    @Inject(method = "renderPlayerlist", at = @At("HEAD"))
    public void renderPlayerListPre(int p_renderPlayerlist_1_, Scoreboard p_renderPlayerlist_2_, ScoreObjective p_renderPlayerlist_3_, CallbackInfo ci) {
        hudModule.setFlagRenderTabOverlay(true);
    }

    @Inject(method = "renderPlayerlist", at = @At("RETURN"))
    public void renderPlayerListPost(int p_renderPlayerlist_1_, Scoreboard p_renderPlayerlist_2_, ScoreObjective p_renderPlayerlist_3_, CallbackInfo ci) {
        HUDModule.INSTANCE.setFlagRenderTabOverlay(false);
    }

    @Inject(method = "drawPing", at = @At("HEAD"), cancellable = true)
    private void drawPing(int offset, int x, int y, NetworkPlayerInfo info, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();

        if (!HUDModule.INSTANCE.getTabShowPlayerPing()) {
            return;
        }

        int ping = info.getResponseTime();
        int pingIndex = 4;
        if (ping < 0) {
            pingIndex = 5;
        } else if (ping < 150) {
            pingIndex = 0;
        } else if (ping < 300) {
            pingIndex = 1;
        } else if (ping < 600) {
            pingIndex = 2;
        } else {
            pingIndex = 3;
        }

        int color;
        if (ping < 0) {
            color = 0xFFFFFFFF;
        } else if (ping < 150) {
            color = 0xFF00FF00;
        } else if (ping < 300) {
            color = 0xFFFFA500;
        } else if (ping < 600) {
            color = 0xFFFFFF00;
        } else {
            color = 0xFFFF0000;
        }

        boolean noIcon = HUDModule.INSTANCE.getNoIconPing();
        boolean hideTag = HUDModule.INSTANCE.getHidePingTag();
        String pingString = ping + (hideTag ? "" : "ms");

        if (!noIcon) {
            mc.getTextureManager().bindTexture(ICONS);
            gui.drawTexturedModalRect(x + offset - 13, y, 0, 176 + (pingIndex * 8), 10, 8);
            int textX = x + offset - 13 - mc.fontRendererObj.getStringWidth(pingString);
            mc.fontRendererObj.drawStringWithShadow(pingString, textX, y, color);
        } else {
            int textX = x + offset - 8 - (mc.fontRendererObj.getStringWidth(pingString) / 2);
            mc.fontRendererObj.drawStringWithShadow(pingString, textX, y, color);
        }

        ci.cancel();
    }

}