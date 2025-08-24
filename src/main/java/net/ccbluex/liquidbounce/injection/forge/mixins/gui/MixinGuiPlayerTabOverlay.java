/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import com.google.common.collect.Ordering;
import net.ccbluex.liquidbounce.features.module.modules.client.TabGUIModule;
import net.ccbluex.liquidbounce.features.module.modules.client.Teams;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.handler.combat.CombatManager;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.IChatComponent;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.ccbluex.liquidbounce.utils.client.MinecraftInstance.mc;
import static net.minecraft.client.renderer.GlStateManager.*;

@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {
    @Shadow private IChatComponent header;
    @Shadow private IChatComponent footer;

    @Unique private IChatComponent fdp$savedHeader;
    @Unique private IChatComponent fdp$savedFooter;

    @Unique
    private static int FDP_TAB_RESERVED_PING = 13;

    @Inject(method = "renderPlayerlist", at = @At("HEAD"))
    public void renderPlayerListPre(int width, Scoreboard scoreboard, ScoreObjective scoreObjective, CallbackInfo ci) {
        TabGUIModule.INSTANCE.setFlagRenderTabOverlay(true);
        fdp$savedHeader = this.header;
        fdp$savedFooter = this.footer;

        if (!TabGUIModule.INSTANCE.getTabDisableHeader()) this.header = null;
        if (!TabGUIModule.INSTANCE.getTabDisableFooter()) this.footer = null;

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
        this.header = fdp$savedHeader;
        this.footer = fdp$savedFooter;

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
            target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;", remap = false))
    private List<NetworkPlayerInfo> redirectSortedCopy(Ordering<NetworkPlayerInfo> ordering, Iterable<NetworkPlayerInfo> iterable) {
        List<NetworkPlayerInfo> list = ordering.sortedCopy(iterable);

        if (mc.thePlayer != null) {
            List<NetworkPlayerInfo> priorityPlayers = new ArrayList<>();
            List<NetworkPlayerInfo> enemyPlayers = new ArrayList<>();
            List<NetworkPlayerInfo> regularPlayers = new ArrayList<>();

            NetworkPlayerInfo self = null;

            for (NetworkPlayerInfo info : list) {
                String playerName = info.getGameProfile().getName();

                if (TabGUIModule.INSTANCE.getTabMoveSelfToTop() && playerName.equals(mc.thePlayer.getName())) {
                    self = info;
                } else if (TabGUIModule.INSTANCE.getTabShowEnemies() && fdp$isFocusedEnemy(playerName)) {
                    enemyPlayers.add(info);
                } else if (TabGUIModule.INSTANCE.getTabShowFriends() && fdp$isFriendOrTeammate(playerName)) {
                    priorityPlayers.add(info);
                } else {
                    regularPlayers.add(info);
                }
            }

            list.clear();
            if (self != null) {
                list.add(self);
            }
            list.addAll(enemyPlayers);
            list.addAll(priorityPlayers);
            list.addAll(regularPlayers);
        }

        return list;
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    private void injectPlayerName(NetworkPlayerInfo info, CallbackInfoReturnable<String> cir) {
        if (mc.thePlayer != null) {
            String playerName = info.getGameProfile().getName();
            ScorePlayerTeam team = info.getPlayerTeam();
            String base = ScorePlayerTeam.formatPlayerName(team, playerName);

            String healthText = "";
            if (TabGUIModule.INSTANCE.getTabShowHealth() && mc.theWorld != null) {
                EntityPlayer targetPlayer = mc.theWorld.getPlayerEntityByName(playerName);
                if (targetPlayer != null) {
                    healthText = fdp$getHealthString(targetPlayer);
                }
            }

            if (TabGUIModule.INSTANCE.getTabMoveSelfToTop() && playerName.equals(mc.thePlayer.getName())) {
                cir.setReturnValue("♛ " + base + healthText);
            } else if (TabGUIModule.INSTANCE.getTabShowEnemies() && fdp$isFocusedEnemy(playerName)) {
                cir.setReturnValue("§c✱ " + base + healthText);
            } else if (TabGUIModule.INSTANCE.getTabShowFriends() && fdp$isFriendOrTeammate(playerName)) {
                cir.setReturnValue("§b♗ " + base + healthText);
            } else if (TabGUIModule.INSTANCE.getTabShowHealth() && !healthText.isEmpty()) {
                cir.setReturnValue(base + healthText);
            }
        }
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/NetHandlerPlayClient;getPlayerInfoMap()Ljava/util/Collection;"))
    private Collection<NetworkPlayerInfo> redirectPlayerInfoMap(NetHandlerPlayClient instance) {
        return instance.getPlayerInfoMap();
    }

    @Inject(method = "drawPing", at = @At("HEAD"), cancellable = true)
    private void drawPing(int offset, int x, int y, NetworkPlayerInfo info, CallbackInfo ci) {
        if (!TabGUIModule.INSTANCE.getTabShowPlayerPing()) return;

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

        if (TabGUIModule.INSTANCE.getPingTextShadow())
            mc.fontRendererObj.drawStringWithShadow(pingString, textX, y, color);
        else
            mc.fontRendererObj.drawString(pingString, textX, y, color);

        ci.cancel();
    }

    @ModifyConstant(method = "renderPlayerlist", constant = @Constant(intValue = 13))
    private int fdp$expandPingReserve(int original) {
        return Math.max(original, FDP_TAB_RESERVED_PING);
    }

    @Unique
    private boolean fdp$isFriendOrTeammate(String playerName) {
        if (FileManager.INSTANCE.getFriendsConfig().isFriend(playerName)) {
            return true;
        }

        if (Teams.INSTANCE.handleEvents() && mc.theWorld != null) {
            EntityPlayer targetPlayer = mc.theWorld.getPlayerEntityByName(playerName);
            if (targetPlayer != null && mc.thePlayer != null) {
                return Teams.INSTANCE.isInYourTeam(targetPlayer);
            }
        }

        return false;
    }

    @Unique
    private boolean fdp$isFocusedEnemy(String playerName) {
        if (mc.theWorld != null) {
            EntityPlayer targetPlayer = mc.theWorld.getPlayerEntityByName(playerName);
            if (targetPlayer != null) {
                return CombatManager.INSTANCE.getFocusedPlayerList().contains(targetPlayer);
            }
        }
        return false;
    }

    @Unique
    private String fdp$getHealthString(EntityPlayer player) {
        if (player == null) return "";

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPercentage = Math.max(0.0f, Math.min(1.0f, health / maxHealth));

        String healthColor;
        if (health <= 0) {
            healthColor = "§4";
        } else if (healthPercentage >= 0.75f) {
            healthColor = "§a";
        } else if (healthPercentage >= 0.5f) {
            healthColor = "§e";
        } else if (healthPercentage >= 0.25f) {
            healthColor = "§6";
        } else {
            healthColor = "§c";
        }

        int healthInt = Math.round(health);
        return " " + healthColor +  healthInt + "HP";
    }
}
