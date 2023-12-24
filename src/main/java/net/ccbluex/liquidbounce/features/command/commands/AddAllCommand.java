/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.config.impl.FriendsConfig;
import net.ccbluex.liquidbounce.features.command.Command;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public final class AddAllCommand extends Command {

    public AddAllCommand() {super("addall", new String[]{""});}

    @Override
    public void execute(@NotNull String[] arguments) {
        if (arguments.length == 2) {
            final String tag = ColorUtils.translateAlternateColorCodes(arguments[1]);
            final AtomicInteger count = new AtomicInteger(0);
            final FriendsConfig config = FDPClient.fileManager.getFriendsConfig();
            final boolean presistent = arguments[0].contains("");

            mc.thePlayer.sendQueue.getPlayerInfoMap()
                    .forEach(player -> {
                        final ScorePlayerTeam team = player.getPlayerTeam();

                        assert team != null;

                        if (team != null) {
                            if (ColorUtils.stripColor(team.getColorPrefix()).contains(tag)
                                    || ColorUtils.stripColor(team.getColorSuffix()).contains(tag)) {
                                final String name = player.getGameProfile().getName();

                                config.addFriend(name, String.valueOf(presistent));

                                count.incrementAndGet();
                            }
                        }
                    });

            alert("Were added " + EnumChatFormatting.WHITE + count.get() + EnumChatFormatting.GRAY + "§7 players.");

        } else {
            alert(EnumChatFormatting.GRAY + "Sintax: .addall <tag>");
        }
    }
}