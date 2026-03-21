package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.normal

import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.phaseOffset
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.movement.FallingPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import java.util.Timer
import kotlin.concurrent.schedule

object Phase : NoFallMode("Phase") {
    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (player.fallDistance > 3 + phaseOffset) {
            val fallPos = FallingPlayer(player).findCollision(5)?.pos ?: return

            if (fallPos.y - player.motionY / 20.0 < player.posY) {
                mc.timer.timerSpeed = 0.05f

                Timer().schedule(100L) {
                    sendPacket(
                        C04PacketPlayerPosition(
                            fallPos.x.toDouble(),
                            fallPos.y.toDouble(),
                            fallPos.z.toDouble(),
                            true
                        ),
                        false
                    )
                    mc.timer.timerSpeed = 1f
                }
            }
        }
    }
}
