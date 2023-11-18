package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.normal

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.stats.StatList

class FakeCollideCritical : CriticalMode("FakeCollide") {
    override fun onAttack(event: AttackEvent) {
        val motionX: Double
        val motionZ: Double
        if (MovementUtils.isMoving()) {
            motionX = mc.thePlayer.motionX
            motionZ = mc.thePlayer.motionZ
        } else {
            motionX = 0.00
            motionZ = 0.00
        }
        mc.thePlayer.triggerAchievement(StatList.jumpStat)
        critical.sendCriticalPacket(
            xOffset = motionX / 3,
            yOffset = 0.20000004768372,
            zOffset = motionZ / 3,
            ground = false
        )
        critical.sendCriticalPacket(
            xOffset = motionX / 1.5,
            yOffset = 0.12160004615784,
            zOffset = motionZ / 1.5,
            ground = false
        )
    }
}