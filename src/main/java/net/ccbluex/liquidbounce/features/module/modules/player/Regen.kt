/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.serverOnGround
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.potion.Potion

object Regen : Module("Regen", Category.PLAYER) {

    private val mode by choices("Mode", arrayOf("Vanilla", "Spartan"), "Vanilla")
    private val speed by int("Speed", 100, 1..100) { mode == "Vanilla" }

    private val delay by int("Delay", 0, 0..10000)
    private val health by int("Health", 18, 0..20)
    private val food by int("Food", 18, 0..20)

    private val noAir by boolean("NoAir", false)
    private val potionEffect by boolean("PotionEffect", false)

    private val timer = MSTimer()

    private var resetTimer = false

    val onUpdate = handler<UpdateEvent> {
        if (resetTimer) {
            mc.timer.timerSpeed = 1F
        } else {
            resetTimer = false
        }

        val thePlayer = mc.thePlayer ?: return@handler

        if (
            !mc.playerController.gameIsSurvivalOrAdventure()
            || noAir && !serverOnGround
            || thePlayer.foodStats.foodLevel <= food
            || !thePlayer.isEntityAlive
            || thePlayer.health >= health
            || (potionEffect && !thePlayer.isPotionActive(Potion.regeneration))
            || !timer.hasTimePassed(delay)
        ) return@handler

        when (mode.lowercase()) {
            "vanilla" -> {
                repeat(speed) {
                    sendPacket(C03PacketPlayer(serverOnGround))
                }
            }

            "spartan" -> {
                if (!thePlayer.isMoving && serverOnGround) {
                    repeat(9) {
                        sendPacket(C03PacketPlayer(serverOnGround))
                    }

                    mc.timer.timerSpeed = 0.45F
                    resetTimer = true
                }
            }
        }

        timer.reset()
    }
}
