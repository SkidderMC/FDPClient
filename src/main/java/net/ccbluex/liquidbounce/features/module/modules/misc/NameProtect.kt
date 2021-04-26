/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
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

@ModuleInfo(name = "NameProtect", description = "Changes playernames clientside.", category = ModuleCategory.MISC)
class NameProtect : Module() {
    private val fakeNameValue = TextValue("FakeName", "&cMe")
    private val otherFakeNameValue = TextValue("FakeName", "Guy")
    @JvmField
    val allPlayersValue = BoolValue("AllPlayers", false)
    @JvmField
    val skinProtectValue = BoolValue("SkinProtect", true)

    @EventTarget(ignoreCondition = true)
    fun onText(event: TextEvent) {
        if(!state) return
        
        val text = event.text

        if (mc.thePlayer == null || text == null || text.contains(LiquidBounce.CLIENT_NAME))
            return

        for (friend in LiquidBounce.fileManager.friendsConfig.friends)
            event.text = StringUtils.replace(text, friend.playerName, translateAlternateColorCodes(friend.alias) + "§f")

        event.text = StringUtils.replace(text, mc.thePlayer.name, translateAlternateColorCodes(fakeNameValue.get()) + "§f")

        if (allPlayersValue.get()) for (playerInfo in mc.netHandler.playerInfoMap)
            event.text = StringUtils.replace(text, playerInfo.gameProfile.name, otherFakeNameValue.get())
    }
}