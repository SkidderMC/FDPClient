package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.normal

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer

class MotionCritical : CriticalMode("Motion") {
    private val motionValue =
        ListValue("MotionMode", arrayOf(
            "RedeSkyLowHop", "Hop",
            "Jump", "LowJump",
            "MinemoraTest"
        ),
            "Jump")
    override fun onAttack(event: AttackEvent) {
        when (motionValue.get().lowercase()) {
            "jump" -> mc.thePlayer.motionY = 0.41999998688698
            "lowjump" -> mc.thePlayer.motionY = 0.3425
            "redeskylowhop" -> mc.thePlayer.motionY = 0.35
            "hop" -> {
                mc.thePlayer.motionY = 0.1
                mc.thePlayer.fallDistance = 0.1f
                mc.thePlayer.onGround = false
            }
            "minemoratest" -> {
                mc.timer.timerSpeed = 0.82f
                mc.thePlayer.motionY = 0.124514
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        if(event.packet is C03PacketPlayer) {
            when (motionValue.get().lowercase()) {
                "minemoratest" -> if (!FDPClient.combatManager.inCombat) mc.timer.timerSpeed = 1.00f
            }
        }
    }
}
