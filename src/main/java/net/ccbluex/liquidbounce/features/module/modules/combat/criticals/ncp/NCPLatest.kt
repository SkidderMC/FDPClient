/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ncp

import me.zywl.fdpclient.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

// By Koitoyuu - 230303
class NCPLatestCritical : CriticalMode("NCPLatest") {
    private var attacked = 0
    override fun onAttack(event: AttackEvent) {
        attacked ++
        if (attacked >= 5) {
            critical.sendCriticalPacket(yOffset = 0.00001058293536, ground = false)
            critical.sendCriticalPacket(yOffset = 0.00000916580235, ground = false)
            critical.sendCriticalPacket(yOffset = 0.00000010371854, ground = false)
            attacked = 0
        }
    }
}
