/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.TextEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.file.configs.FriendsConfig;
import net.ccbluex.liquidbounce.utils.misc.StringUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.features.value.BoolValue;
import net.ccbluex.liquidbounce.features.value.TextValue;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Objects;

@ModuleInfo(name = "NameProtect", category = ModuleCategory.CLIENT)
public class NameProtect extends Module {
    private final TextValue fakeNameValue = new TextValue("FakeName", "&cProtected User");
    private final TextValue allFakeNameValue = new TextValue("AllPlayersFakeName", "FDP");
    public final BoolValue selfValue = new BoolValue("Yourself", true);
    public final BoolValue tagValue = new BoolValue("Tag", false);
    public final BoolValue allPlayersValue = new BoolValue("AllPlayers", false);
    public final BoolValue nameSpoofValue = new BoolValue("NameSpoof", false);
    public final TextValue customNameValue = new TextValue("CustomName", "");

    @EventTarget
    public void onText(final TextEvent event) {
        if (mc.thePlayer == null || Objects.requireNonNull(event.getText()).contains("§8[§9§l" + FDPClient.CLIENT_NAME + "§8] §3") || event.getText().startsWith("/") || event.getText().startsWith(FDPClient.commandManager.getPrefix() + ""))
            return;

        for (final FriendsConfig.Friend friend : FDPClient.fileManager.getFriendsConfig().getFriends())
            event.setText(StringUtils.replace(event.getText(), friend.getPlayerName(), ColorUtils.translateAlternateColorCodes(friend.getAlias()) + "§f"));

        String playerName = mc.thePlayer.getName();
        if (nameSpoofValue.get() && !customNameValue.get().isEmpty()) {
            playerName = customNameValue.get();
        }

        event.setText(StringUtils.replace(
                event.getText(),
                mc.thePlayer.getName(),
                (selfValue.get() ? (tagValue.get() ? StringUtils.injectAirString(playerName) + " §7(§r" + ColorUtils.translateAlternateColorCodes(fakeNameValue.get() + "§r§7)") : ColorUtils.translateAlternateColorCodes(fakeNameValue.get()) + "§r") : playerName)
        ));

        if (allPlayersValue.get()) {
            for (final NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                String playerInfoName = playerInfo.getGameProfile().getName();
                if (nameSpoofValue.get() && !customNameValue.get().isEmpty()) {
                    playerInfoName = customNameValue.get();
                }
                event.setText(StringUtils.replace(event.getText(), playerInfoName, ColorUtils.translateAlternateColorCodes(allFakeNameValue.get()) + "§f"));
            }
        }
    }
}