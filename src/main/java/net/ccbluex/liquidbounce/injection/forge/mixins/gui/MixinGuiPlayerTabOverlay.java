/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.features.module.modules.client.TabGUIModule;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.ccbluex.liquidbounce.utils.client.MinecraftInstance.mc;
import static net.minecraft.client.renderer.GlStateManager.*;

@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {

    @Unique
    private static int FDP_TAB_RESERVED_PING = 13;

    @Inject(method = "renderPlayerlist", at = @At("HEAD"))
    public void renderPlayerListPre(int width, Scoreboard scoreboard, ScoreObjective scoreObjective, CallbackInfo ci) {
        TabGUIModule.INSTANCE.setFlagRenderTabOverlay(true);

        if (TabGUIModule.INSTANCE.getTabShowPlayerPing()) {
            boolean showMs = TabGUIModule.INSTANCE.getHidePingTag();
            int max = 13;
            if (mc.thePlayer != null && mc.thePlayer.sendQueue != null) {
                Collection<NetworkPlayerInfo> infos = mc.thePlayer.sendQueue.getPlayerInfoMap();
                for (NetworkPlayerInfo npi : infos) {
                    int p = Math.max(0, npi.getResponseTime());
                    String s = showMs ? (p + "ms") : String.valueOf(p);
                    max = Math.max(max, mc.fontRendererObj.getStringWidth(s) + 4);
                }
            }
            FDP_TAB_RESERVED_PING = max;
        } else {
            FDP_TAB_RESERVED_PING = 13;
        }

        String scaleOption = TabGUIModule.INSTANCE.getTabScale();
        float scaleFactor;
        switch (scaleOption) {
            case "Small":
                scaleFactor = 0.75f;
                break;
            case "Normal":
                scaleFactor = 1.0f;
                break;
            case "Large":
                scaleFactor = 1.25f;
                break;
            case "Extra Large":
                scaleFactor = 1.5f;
                break;
            default:
                scaleFactor = 1.0f;
                break;
        }
        if (scaleFactor != 1.0f) {
            pushMatrix();
            translate((width * (1 - scaleFactor)) / 2.0f, 0, 0);
            scale(scaleFactor, scaleFactor, scaleFactor);
        }
    }

    @Inject(method = "renderPlayerlist", at = @At("RETURN"))
    public void renderPlayerListPost(int width, Scoreboard scoreboard, ScoreObjective scoreObjective, CallbackInfo ci) {
        TabGUIModule.INSTANCE.setFlagRenderTabOverlay(false);

        String scaleOption = TabGUIModule.INSTANCE.getTabScale();
        float scaleFactor;
        switch (scaleOption) {
            case "Small":
                scaleFactor = 0.75f;
                break;
            case "Normal":
                scaleFactor = 1.0f;
                break;
            case "Large":
                scaleFactor = 1.25f;
                break;
            case "Extra Large":
                scaleFactor = 1.5f;
                break;
            default:
                scaleFactor = 1.0f;
                break;
        }
        if (scaleFactor != 1.0f) {
            popMatrix();
        }
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/NetHandlerPlayClient;getPlayerInfoMap()Ljava/util/Collection;"))
    private Collection<NetworkPlayerInfo> redirectPlayerInfoMap(NetHandlerPlayClient instance) {
        Collection<NetworkPlayerInfo> original = instance.getPlayerInfoMap();
        if (TabGUIModule.INSTANCE.getTabMoveSelfToTop()) {
            if (mc.thePlayer != null) {
                List<NetworkPlayerInfo> list = new ArrayList<>(original);
                NetworkPlayerInfo selfInfo = null;
                for (NetworkPlayerInfo info : list) {
                    if (info.getGameProfile().getName().equals(mc.thePlayer.getName())) {
                        selfInfo = info;
                        break;
                    }
                }
                if (selfInfo != null) {
                    list.remove(selfInfo);
                    list.add(0, selfInfo);
                }
                return list;
            }
        }
        return original;
    }

    @Inject(method = "drawPing", at = @At("HEAD"), cancellable = true)
    private void drawPing(int offset, int x, int y, NetworkPlayerInfo info, CallbackInfo ci) {
        if (!TabGUIModule.INSTANCE.getTabShowPlayerPing()) {
            return;
        }

        int ping = info.getResponseTime();

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

        boolean showMsTag = TabGUIModule.INSTANCE.getHidePingTag();
        String pingString = ping + (showMsTag ? "ms" : "");

        int right = x + offset - 1;
        int textX = right - mc.fontRendererObj.getStringWidth(pingString);
        if (TabGUIModule.INSTANCE.getPingTextShadow()) {
            mc.fontRendererObj.drawStringWithShadow(pingString, textX, y, color);
        } else {
            mc.fontRendererObj.drawString(pingString, textX, y, color);
        }

        ci.cancel();
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 13))
    private int fdp$expandPingReserve(int original) {
        return Math.max(original, FDP_TAB_RESERVED_PING);
    }

    @Redirect(method = "renderPlayerlist",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", ordinal = 0))
    private int redirectRenderHeader(FontRenderer fontRenderer, String text, float x, float y, int color) {
        if (TabGUIModule.INSTANCE.getTabDisableHeader()) {
            return 0;
        }
        return fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    @Redirect(method = "renderPlayerlist",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", ordinal = 1))
    private int redirectRenderFooter(FontRenderer fontRenderer, String text, float x, float y, int color) {
        if (TabGUIModule.INSTANCE.getTabDisableFooter()) {
            return 0;
        }
        return fontRenderer.drawStringWithShadow(text, x, y, color);
    }
}