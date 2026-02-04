/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.START_SNEAKING
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.STOP_SNEAKING

object Sneak : Module("Sneak", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN) {

    val mode by choices("Mode", arrayOf("Legit", "Vanilla", "Switch", "MineSecure"), "MineSecure")
    val stopMove by boolean("StopMove", false)

    private var sneaking = false

    val onMotion = handler<MotionEvent> { event ->
        if (stopMove && mc.thePlayer.isMoving) {
            if (sneaking)
                onDisable()
            return@handler
        }

        when (mode.lowercase()) {
            "legit" -> mc.gameSettings.keyBindSneak.pressed = true
            "vanilla" -> {
                if (sneaking)
                    return@handler

                sendPacket(C0BPacketEntityAction(mc.thePlayer, START_SNEAKING))
            }

            "switch" -> {
                when (event.eventState) {
                    EventState.PRE -> {
                        sendPackets(
                            C0BPacketEntityAction(mc.thePlayer, START_SNEAKING),
                            C0BPacketEntityAction(mc.thePlayer, STOP_SNEAKING)
                        )
                    }

                    EventState.POST -> {
                        sendPackets(
                            C0BPacketEntityAction(mc.thePlayer, STOP_SNEAKING),
                            C0BPacketEntityAction(mc.thePlayer, START_SNEAKING)
                        )
                    }

                    else -> {}
                }
            }

            "minesecure" -> {
                if (event.eventState == EventState.PRE)
                    return@handler

                sendPacket(C0BPacketEntityAction(mc.thePlayer, START_SNEAKING))
            }
        }
    }

    val onWorld = handler<WorldEvent> {
        sneaking = false
    }

    override fun onDisable() {
        val player = mc.thePlayer ?: return

        when (mode.lowercase()) {
            "legit" -> {
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
                    mc.gameSettings.keyBindSneak.pressed = false
                }
            }

            "vanilla", "switch", "minesecure" -> sendPacket(C0BPacketEntityAction(player, STOP_SNEAKING))
        }
        sneaking = false
    }
}