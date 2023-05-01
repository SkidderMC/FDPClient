/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer

@ModuleInfo(name = "AutoL", category = ModuleCategory.MISC)
class AutoL : Module() {
    private val fdpPrefixValue = BoolValue("FDPPrefix", false)
    private val autoReportValue = BoolValue("AutoReport", false)

    private fun getPlayerName(entity: EntityLivingBase): String? {
        if (entity is EntityPlayer) {
            return entity.gameProfile.name
        }
        return null
    }

    @EventTarget
    fun onKilled(event: EntityKilledEvent) {
        val playerName = getPlayerName(event.targetEntity)
        if (playerName !is String) return
        var message = ""
        if (fdpPrefixValue.get()) {
            message += "[FDPClient] "
        }
        message += "$playerName L"
        mc.thePlayer.sendChatMessage(message)
        if (autoReportValue.get()) {
            mc.thePlayer.sendChatMessage("/wdr $playerName Killaura Speed Velocity")
        }
    }
}