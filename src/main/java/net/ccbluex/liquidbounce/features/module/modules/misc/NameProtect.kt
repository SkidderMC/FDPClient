/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.TextEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.translateAlternateColorCodes
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.TextValue

@ModuleInfo(name = "NameProtect", category = ModuleCategory.MISC)
class NameProtect : Module() {
    val allPlayersValue = BoolValue("AllPlayers", false)
    val skinProtectValue = BoolValue("SkinProtect", true)
    private val fakeNameValue = TextValue("FakeName", "FDPUser")
    private val playerNameValue = TextValue("PlayerName", "SIGMA_HATAR")

    @EventTarget(ignoreCondition = true)
    fun onText(event: TextEvent) {
        val thePlayer = mc.thePlayer

        if (thePlayer == null || event.text!!.contains("§8[§9§l" + LiquidBounce.CLIENT_NAME + "§8] §3")) {
            return
        }

        for (friend in LiquidBounce.fileManager.friendsConfig.friends)
            event.text = StringUtils.replace(event.text, friend.playerName, translateAlternateColorCodes(friend.alias) + "§f")

        if (!state) {
            return
        }
        event.text = StringUtils.replace(event.text, thePlayer.name, translateAlternateColorCodes(fakeNameValue.get()) + "§f")

        if (allPlayersValue.get()) {
            for (playerInfo in mc.netHandler.playerInfoMap)
                event.text = StringUtils.replace(event.text, playerInfo.gameProfile.name, playerNameValue.get())
        }
    }
}