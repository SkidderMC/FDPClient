/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getCenterDistance
import net.ccbluex.liquidbounce.utils.block.BlockUtils.searchBlocks
import net.ccbluex.liquidbounce.utils.block.center
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.faceBlock
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.awt.Color
import kotlin.math.roundToInt

/**
 * Extinguish
 *
 * Automatically puts out fire blocks around the player by sending block-break packets.
 */
object Extinguish : Module("Extinguish", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    /**
     * OPTIONS
     */

    private val range by float("Range", 4.5F, 1F..6F)
        .describe("Maximum distance to search for fire blocks.")
    private val throughWalls by boolean("ThroughWalls", false)
        .describe("Allow extinguishing fire hidden behind walls.")
    private val delay by int("Delay", 0, 0..20)
        .describe("Delay between breaking fire blocks in ticks.")

    private val swingMode by choices("SwingMode", arrayOf("Normal", "Packet", "None"), "Normal")
        .describe("How to swing the arm while extinguishing.")

    private val rotations by boolean("Rotations", true)
        .describe("Rotate toward the fire before breaking it.")

    private val esp by boolean("ESP", true).subjective()
        .describe("Render a box around nearby fire blocks.")

    private val options = RotationSettings(this).apply {
        withRequestPriority(RotationPriority.NORMAL)
        immediate = true

        resetTicksValue.excludeWithState()
        withoutKeepRotation()
    }

    /**
     * VALUES
     */

    private val fireBlocks = hashSetOf<BlockPos>()
    private val delayTimer = TickTimer()

    val onUpdate = handler<UpdateEvent> {
        fireBlocks.clear()

        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        delayTimer.update()

        if (!delayTimer.hasTimePassed(delay)) {
            return@handler
        }

        val eyes = player.eyes

        val found = searchBlocks(range.roundToInt() + 1, setOf(Blocks.fire)) { pos, _ ->
            if (getCenterDistance(pos) > range) {
                return@searchBlocks false
            }

            // ThroughWalls: only break fire blocks in sight
            throughWalls || world.rayTraceBlocks(eyes, pos.center, false, true, false)?.blockPos == pos
        }

        if (found.isEmpty()) {
            return@handler
        }

        fireBlocks += found.keys

        // Break the closest fire block first
        val target = found.keys.minByOrNull { getCenterDistance(it) } ?: return@handler

        if (rotations && options.rotationsActive) {
            val rotation = faceBlock(target) ?: return@handler

            setTargetRotation(rotation.rotation, options = options)
        }

        swing()

        // Fire breaks instantly, so a start + stop pair is enough
        sendPackets(
            C07PacketPlayerDigging(START_DESTROY_BLOCK, target, EnumFacing.DOWN),
            C07PacketPlayerDigging(STOP_DESTROY_BLOCK, target, EnumFacing.DOWN)
        )

        mc.playerController.onPlayerDestroyBlock(target, EnumFacing.DOWN)

        delayTimer.reset()
    }

    val onRender3D = handler<Render3DEvent> {
        if (!esp) {
            return@handler
        }

        for (pos in fireBlocks) {
            drawBlockBox(pos, Color.RED, true)
        }
    }

    /**
     * Swing the hand according to the selected [swingMode]
     */
    private fun swing() {
        when (swingMode.lowercase()) {
            "normal" -> mc.thePlayer?.swingItem()
            "packet" -> sendPacket(C0APacketAnimation())
        }
    }
}
