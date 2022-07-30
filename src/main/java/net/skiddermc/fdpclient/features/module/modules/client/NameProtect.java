/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.client;

import net.skiddermc.fdpclient.FDPClient;
import net.skiddermc.fdpclient.event.EventTarget;
import net.skiddermc.fdpclient.event.TextEvent;
import net.skiddermc.fdpclient.features.module.Module;
import net.skiddermc.fdpclient.features.module.ModuleCategory;
import net.skiddermc.fdpclient.features.module.ModuleInfo;
import net.skiddermc.fdpclient.file.configs.FriendsConfig;
import net.skiddermc.fdpclient.utils.misc.StringUtils;
import net.skiddermc.fdpclient.utils.render.ColorUtils;
import net.skiddermc.fdpclient.value.BoolValue;
import net.skiddermc.fdpclient.value.TextValue;
import net.minecraft.client.network.NetworkPlayerInfo;

@ModuleInfo(name = "NameProtect", category = ModuleCategory.CLIENT)
public class NameProtect extends Module {

    private final TextValue fakeNameValue = new TextValue("FakeName", "&cProtected User");
    private final TextValue allFakeNameValue = new TextValue("AllPlayersFakeName", "FDP");
    public final BoolValue selfValue = new BoolValue("Yourself", true);
    public final BoolValue tagValue = new BoolValue("Tag", false);
    public final BoolValue allPlayersValue = new BoolValue("AllPlayers", false);

    @EventTarget
    public void onText(final TextEvent event) {
        if (mc.thePlayer == null || event.getText().contains("§8[§9§l" + FDPClient.CLIENT_NAME + "§8] §3") || event.getText().startsWith("/") || event.getText().startsWith(FDPClient.commandManager.getPrefix() + ""))
            return;

        for (final FriendsConfig.Friend friend : FDPClient.fileManager.getFriendsConfig().getFriends())
            event.setText(StringUtils.replace(event.getText(), friend.getPlayerName(), ColorUtils.translateAlternateColorCodes(friend.getAlias()) + "§f"));

        event.setText(StringUtils.replace(
                event.getText(),
                mc.thePlayer.getName(),
                (selfValue.get() ? (tagValue.get() ? StringUtils.injectAirString(mc.thePlayer.getName()) + " §7(§r" + ColorUtils.translateAlternateColorCodes(fakeNameValue.get() + "§r§7)") : ColorUtils.translateAlternateColorCodes(fakeNameValue.get()) + "§r") : mc.thePlayer.getName())
        ));

        if(allPlayersValue.get())
            for(final NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap())
                event.setText(StringUtils.replace(event.getText(), playerInfo.getGameProfile().getName(), ColorUtils.translateAlternateColorCodes(allFakeNameValue.get()) + "§f"));
    }

}