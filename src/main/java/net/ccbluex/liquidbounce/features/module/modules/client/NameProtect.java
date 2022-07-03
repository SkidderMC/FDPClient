/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.TextEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.misc.StringUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.TextValue;
import net.minecraft.client.network.NetworkPlayerInfo;


@ModuleInfo(name = "NameProtect", category = ModuleCategory.CLIENT)
public class NameProtect extends Module {

    private final TextValue fakeNameValue = new TextValue("FakeName", "&cProtected User");
    public final BoolValue allPlayersValue = new BoolValue("AllPlayers", false);
    private final TextValue allplayerNameValue = new TextValue("AllPlayers-FakeName", "&e&lFDP_USER");
    public final BoolValue skinProtectValue = new BoolValue("SkinProtect", true);

    @EventTarget
    public void onText(final TextEvent event) {
        if (mc.thePlayer == null || event.getText().contains("§8[§9§l" + LiquidBounce.CLIENT_NAME + "§8] §3"))
            return;

        if (!getState())
            return;

        event.setText(StringUtils.replace(event.getText(), mc.thePlayer.getName(), fakeNameValue.get().replace("&", "§") + "§f"));

        if (allPlayersValue.get())
            for (final NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap())
                event.setText(StringUtils.replace(event.getText(), playerInfo.getGameProfile().getName(), allplayerNameValue.get().replace("&","§") + "§f"));
    }

}