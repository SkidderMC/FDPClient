/*
 * ZAVZ Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.player;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.config.impl.FriendsConfig;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.minecraft.scoreboard.ScorePlayerTeam;

import java.util.Arrays;

@ModuleInfo(name = "AutoRole", category = ModuleCategory.PLAYER)
public class AutoRole extends Module {

    private final BoolValue formattingValue = new BoolValue("Formatting", true);

    private static final String[] STAFF_PREFIXES = {
            "[Moderador] ",
            "[MODERADOR] ",
            "[Mod] ",
            "[MOD] ",
            "[Administrador] ",
            "[ADMINISTRADOR] ",
            "[Admin] ",
            "[ADMIN] ",
            "[Coordenador] ",
            "[COORDENADOR] ",
            "[Coord] ",
            "[COORD] ",
            "[Gerente] ",
            "[GERENTE] ",
            "[CEO] ",
            "[Dono] ",
            "[DONO] ",
            "[DIRETOR] ",
            "[DEV] ",
            "[Dev] ",
            "Diretor ",
            "DIRETOR ",
            "Dev ",
            "DEV ",
            "Mod ",
            "MOD ",
            "[Master] ",
            "[MASTER] ",
            "[CEO] ",
            "[TRIAL] "
    };

    private boolean isStaff(final String prefix) {
        return Arrays.asList(STAFF_PREFIXES).contains(ColorUtils.stripColor(prefix));
    }

    @EventTarget
    public void handle(final UpdateEvent event) {
        final FriendsConfig friendManager = FDPClient.fileManager.getFriendsConfig();

        String[] formatCodes = {"§k", "§l", "§m", "§n", "§o"};
        int currentFormatIndex = 0;

        for (final ScorePlayerTeam team : mc.theWorld.getScoreboard().getTeams()) {
            if (this.isStaff(team.getColorPrefix())) {
                for (final String member : team.getMembershipCollection()) {
                    if (!friendManager.isFriend(member)) {
                        friendManager.addFriend(member);

                        String colorPrefix = team.getColorPrefix();
                        if (formattingValue.get() && currentFormatIndex < formatCodes.length) {
                            colorPrefix = formatCodes[currentFormatIndex] + colorPrefix;
                            currentFormatIndex++;
                            if (currentFormatIndex >= formatCodes.length) {
                                currentFormatIndex = 0;
                            }
                        }

                        chat("§7[§d!§7]§7 ADICIONADO: " + colorPrefix + member);
                    }
                }
            }
        }
    }
}