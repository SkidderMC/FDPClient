/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.other.Fucker
import net.ccbluex.liquidbounce.features.module.modules.other.Nuker
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.runTimeTicks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK

object FastBreak : Module("FastBreak", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS) {

    private val mode by choices("Mode", arrayOf("Simple", "Tick"), "Simple")

    // Simple mode only parameter
    private val breakDamage by float("BreakDamage", 0.8F, 0.1F..1F) { mode == "Simple" }

    // Tick mode 1-tick cooldown
    private var lastDigTick = -1L

    override fun onDisable() {
        lastDigTick = -1L
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler
        val controller = mc.playerController ?: return@handler

        if (player.isDead) return@handler

        when (mode.lowercase()) {
            "simple" -> {
                controller.blockHitDelay = 0
                if (controller.curBlockDamageMP > breakDamage)
                    controller.curBlockDamageMP = 1F
                if (Fucker.currentDamage > breakDamage)
                    Fucker.currentDamage = 1F
                if (Nuker.currentDamage > breakDamage)
                    Nuker.currentDamage = 1F
            }

            "tick" -> {
                // Check if the player is currently mining a block (vanilla state)
                if (controller.isHittingBlock) {
                    val pos = controller.currentBlock ?: return@handler
                    val facing = controller.currentBlockSide ?: return@handler

                    // Apply 1-tick delay between packets
                    if (runTimeTicks - lastDigTick >= 2) {
                        sendPackets(
                            C07PacketPlayerDigging(START_DESTROY_BLOCK, pos, facing),
                            C07PacketPlayerDigging(STOP_DESTROY_BLOCK, pos, facing)
                        )
                        lastDigTick = runTimeTicks
                    }
                }
                // Same adjustments for auxiliary modules (unchanged from Simple logic)
                if (Fucker.currentDamage > breakDamage)
                    Fucker.currentDamage = 1F
                if (Nuker.currentDamage > breakDamage)
                    Nuker.currentDamage = 1F
            }
        }
    }
}
