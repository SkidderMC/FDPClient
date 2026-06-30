/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.serverOnGround
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils.isConsumingItem
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.event.handler
import net.minecraft.network.play.client.C03PacketPlayer

object FastUse : Module("FastUse", Category.PLAYER, Category.SubCategory.PLAYER_COUNTER) {

    private val mode by choices("Mode", arrayOf("Instant", "NCP", "AAC", "Custom"), "NCP")
        .describe("Anti-cheat bypass method for fast item use.")

    private val speed by int("Speed", 20, 1..35) { mode == "Instant" || mode == "NCP" }
        .describe("Number of packets sent to speed up use.")
    private val tickCooldown by int("TickCooldown", 0, 0..20, "ticks") { mode == "Instant" || mode == "NCP" }
        .describe("Ticks to wait before speeding up use.")
    private val stopInput by boolean("StopInput", false) { mode == "Instant" || mode == "NCP" }
        .describe("Release the use key once the item is used.")

    private val delay by int("CustomDelay", 0, 0..300) { mode == "Custom" }
        .describe("Delay between custom-mode bursts in ms.")
    private val customSpeed by int("CustomSpeed", 2, 1..35) { mode == "Custom" }
        .describe("Packets per burst in custom mode.")
    private val customTimer by float("CustomTimer", 1.1f, 0.5f..2f) { mode == "Custom" }
        .describe("Game timer speed used in custom mode.")

    private val noMove by boolean("NoMove", false)
        .describe("Stop movement while using an item.")

    init {
        group("General", "Mode")
        group("Instant/NCP", "Speed", "TickCooldown", "StopInput")
        group("Custom", "CustomDelay", "CustomSpeed", "CustomTimer")
        group("Movement", "NoMove")
    }

    private val msTimer = MSTimer()
    private var usedTimer = false
    private var tickCounter = 0


    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }

        if (!isConsumingItem()) {
            msTimer.reset()
            tickCounter = 0
            return@handler
        }

        when (mode.lowercase()) {
            "instant" -> {
                if (tickCounter++ < tickCooldown)
                    return@handler
                tickCounter = 0

                repeat(speed) {
                    sendPacket(C03PacketPlayer(serverOnGround))
                }

                mc.playerController.onStoppedUsingItem(thePlayer)

                if (stopInput)
                    mc.gameSettings.keyBindUseItem.pressed = false
            }

            "ncp" -> if (thePlayer.itemInUseDuration > 14) {
                if (tickCounter++ < tickCooldown)
                    return@handler
                tickCounter = 0

                repeat(speed) {
                    sendPacket(C03PacketPlayer(serverOnGround))
                }

                mc.playerController.onStoppedUsingItem(thePlayer)

                if (stopInput)
                    mc.gameSettings.keyBindUseItem.pressed = false
            }

            "aac" -> {
                mc.timer.timerSpeed = 1.22F
                usedTimer = true
            }

            "custom" -> {
                mc.timer.timerSpeed = customTimer
                usedTimer = true

                if (!msTimer.hasTimePassed(delay))
                    return@handler

                repeat(customSpeed) {
                    sendPacket(C03PacketPlayer(serverOnGround))
                }

                msTimer.reset()
            }
        }
    }


    val onMove = handler<MoveEvent> { event ->
         mc.thePlayer ?: return@handler

        if (!isConsumingItem() || !noMove)
            return@handler

        event.zero()
    }

    override fun onDisable() {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }
    }

    override val tag
        get() = mode
}
