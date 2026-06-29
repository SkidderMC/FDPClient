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

object TimeShift : Module(
    "TimeShift",
    Category.EXPLOIT,
    Category.SubCategory.EXPLOIT_EXTRAS,
    legacyNames = arrayOf("Regen", "Zoot"),
) {

    private val mode by choices("Mode", arrayOf("Vanilla", "Spartan"), "Vanilla")
        .describe("Regen exploit method to use.")
    private val speed by int("Speed", 100, 1..100) { mode == "Vanilla" }
        .describe("Packets sent per tick in vanilla mode.")
    private val vanillaTimer by float("VanillaTimer", 1F, 0.1F..10F) { mode == "Vanilla" }
        .describe("Game timer speed used in vanilla mode.")

    private val spartanPackets by int("SpartanPackets", 9, 1..50) { mode == "Spartan" }
        .describe("Packets sent per burst in Spartan mode.")
    private val spartanTimer by float("SpartanTimer", 0.45F, 0.1F..2F) { mode == "Spartan" }
        .describe("Game timer speed used in Spartan mode.")

    private val delay by int("Delay", 0, 0..10000)
        .describe("Delay between regen bursts in milliseconds.")
    private val health by int("Health", 18, 0..20)
        .describe("Only regen below this health level.")
    private val food by int("Food", 18, 0..20)
        .describe("Only regen above this food level.")

    private val noAir by boolean("NoAir", false)
        .describe("Pause regen while in the air.")
    private val noMove by boolean("NoMove", false)
        .describe("Pause regen while moving.")
    private val pauseOnDamage by boolean("PauseOnDamage", false)
        .describe("Pause regen while taking damage.")
    private val potionEffect by boolean("PotionEffect", false)
        .describe("Only regen while regeneration is active.")

    private val fire by boolean("Fire", false)
        .describe("Also speed regen while on fire.")
    private val badEffects by boolean("BadEffects", false)
        .describe("Also speed regen with bad potion effects.")
    private val maximumSpeed by int("MaximumSpeed", 100, 5..200) { fire || badEffects }
        .describe("Cap on packets sent for fire or bad effects.")

    private val timer = MSTimer()

    private var resetTimer = false

    private val badEffectIds = intArrayOf(
        Potion.poison.id,
        Potion.wither.id,
        Potion.weakness.id,
        Potion.moveSlowdown.id,
        Potion.digSlowdown.id,
        Potion.blindness.id,
        Potion.confusion.id,
        Potion.hunger.id
    )

    val onUpdate = handler<UpdateEvent> {
        if (resetTimer) {
            mc.timer.timerSpeed = 1F
            resetTimer = false
        }

        val thePlayer = mc.thePlayer ?: return@handler

        val lowHealth = thePlayer.health < health
        val onFire = fire && thePlayer.isBurning && !thePlayer.isInWater && !thePlayer.isWet
        val badEffect = badEffects && thePlayer.activePotionEffects.any {
            it.duration > 0 && it.potionID in badEffectIds
        }

        if (
            !mc.playerController.gameIsSurvivalOrAdventure()
            || noAir && !serverOnGround
            || noMove && thePlayer.isMoving
            || pauseOnDamage && thePlayer.hurtTime > 0
            || thePlayer.foodStats.foodLevel <= food
            || !thePlayer.isEntityAlive
            || (!lowHealth && !onFire && !badEffect)
            || (potionEffect && !thePlayer.isPotionActive(Potion.regeneration))
            || !timer.hasTimePassed(delay)
        ) return@handler

        when (mode.lowercase()) {
            "vanilla" -> {
                var tickSpeed = if (lowHealth) speed else 0

                if (onFire) {
                    tickSpeed = maxOf(tickSpeed, 9)
                }

                if (badEffect) {
                    val maxDuration = thePlayer.activePotionEffects
                        .filter { it.duration > 0 && it.potionID in badEffectIds }
                        .maxByOrNull { it.duration }?.duration ?: 0

                    if (maxDuration > 0) {
                        tickSpeed = maxOf(tickSpeed, minOf(maxDuration / 20, maximumSpeed))
                    }
                }

                if (tickSpeed > 0) {
                    if (vanillaTimer != 1F) {
                        mc.timer.timerSpeed = vanillaTimer
                        resetTimer = true
                    }

                    repeat(tickSpeed) {
                        sendPacket(C03PacketPlayer(serverOnGround))
                    }
                }
            }

            "spartan" -> {
                if (!thePlayer.isMoving && serverOnGround) {
                    repeat(spartanPackets) {
                        sendPacket(C03PacketPlayer(serverOnGround))
                    }

                    mc.timer.timerSpeed = spartanTimer
                    resetTimer = true
                }
            }
        }

        timer.reset()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1F
        resetTimer = false
    }
}
