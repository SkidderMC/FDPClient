/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.other

import me.zywl.fdpclient.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class VerusSmartCritical : CriticalMode("VerusSmart") {
    private var attacks = 0
    override fun onEnable() {
        attacks = 0
    }
    override fun onAttack(event: AttackEvent) {
        attacks++
        if (attacks > 4) {
            attacks = 0

            critical.sendCriticalPacket(yOffset = 0.001, ground = true)
            critical.sendCriticalPacket(ground = false)
        } else {
            critical.antiDesync = false
        }
    }
}