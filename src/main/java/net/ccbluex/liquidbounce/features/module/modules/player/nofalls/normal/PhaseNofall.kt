package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.normal

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.network.play.client.C03PacketPlayer
import java.util.*
import kotlin.concurrent.schedule

class PhaseNofall : NoFallMode("Phase") {
    private val phaseOffsetValue = IntegerValue("${valuePrefix}PhaseOffset", 1, 0, 5)
    override fun onNoFall(event: UpdateEvent) {
        if (mc.thePlayer.fallDistance > 3 + phaseOffsetValue.get()) {
            val fallPos = FallingPlayer(mc.thePlayer)
                .findCollision(5) ?: return
            if (fallPos.y - mc.thePlayer.motionY / 20.0 < mc.thePlayer.posY) {
                mc.timer.timerSpeed = 0.05f
                Timer().schedule(100L) {
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(fallPos.x.toDouble(), fallPos.y.toDouble(), fallPos.z.toDouble(), true))
                    mc.timer.timerSpeed = 1f
                }
            }
        }
    }
}