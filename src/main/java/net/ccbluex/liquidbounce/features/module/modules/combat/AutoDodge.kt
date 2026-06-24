/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.Render3D
import net.ccbluex.liquidbounce.utils.simulation.SimulatedArrow
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.util.Vec3
import java.awt.Color
import kotlin.math.sqrt

/** Visual-only warning for incoming arrows; it never changes input, motion, or packets. */
object AutoDodge : Module("AutoDodge", Category.COMBAT, Category.SubCategory.COMBAT_RAGE) {

    private val range by float("Range", 5f, 1f..16f)
        .describe("Maximum distance for incoming-arrow warnings.")
    private val lookahead by int("Lookahead", 30, 5..60)
        .describe("Ticks of arrow flight simulated ahead.")
    private val hitRadius by float("HitRadius", 1.2f, 0.5f..3f)
        .describe("Closest approach treated as a threat.")
    private val suggestionDistance by float("SuggestionDistance", 1.5f, 0.5f..4f)
        .describe("Length of the visual sidestep suggestion.")
    private val showTrajectory by boolean("ShowTrajectory", true)
        .describe("Draw the predicted path of the closest threat.")
    private val showSuggestion by boolean("ShowSuggestion", true)
        .describe("Draw a visual direction away from the trajectory.")
    private val throughWalls by boolean("ThroughWalls", false)
        .describe("Show warnings through terrain.")
    private val lineWidth by float("LineWidth", 2f, 1f..5f)
        .describe("Width of the visual guidance lines.")
    private val trajectoryColor by color("TrajectoryColor", Color(255, 85, 85, 210))
    private val suggestionColor by color("SuggestionColor", Color(85, 255, 120, 230))

    val onRender3D = handler<Render3DEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val threat = threats().minByOrNull { player.getDistanceToEntity(it) } ?: return@handler

        if (showTrajectory) {
            Render3D.drawWorldPolyline(
                SimulatedArrow.trace(threat, lookahead),
                trajectoryColor,
                lineWidth,
                throughWalls,
            )
        }

        if (showSuggestion) {
            suggestedDirection(threat)?.let { direction ->
                val partial = event.partialTicks.toDouble()
                val start = Vec3(
                    player.lastTickPosX + (player.posX - player.lastTickPosX) * partial,
                    player.lastTickPosY + (player.posY - player.lastTickPosY) * partial + 0.05,
                    player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partial,
                )
                Render3D.drawWorldLine(
                    start,
                    start.addVector(direction.xCoord * suggestionDistance, 0.0, direction.zCoord * suggestionDistance),
                    suggestionColor,
                    lineWidth + 1f,
                    throughWalls,
                )
            }
        }
    }

    private fun threats(): Sequence<EntityArrow> {
        val player = mc.thePlayer ?: return emptySequence()
        val world = mc.theWorld ?: return emptySequence()
        return world.loadedEntityList.asSequence().filterIsInstance<EntityArrow>().filter { arrow ->
            (arrow.motionX != 0.0 || arrow.motionY != 0.0 || arrow.motionZ != 0.0) &&
                player.getDistanceToEntity(arrow) <= range &&
                isApproaching(arrow) &&
                SimulatedArrow.willHit(arrow, player, lookahead, hitRadius.toDouble())
        }
    }

    private fun suggestedDirection(arrow: EntityArrow): Vec3? {
        val player = mc.thePlayer ?: return null
        val length = sqrt(arrow.motionX * arrow.motionX + arrow.motionZ * arrow.motionZ)
        if (length < 1e-4) return null

        var perpendicularX = -arrow.motionZ / length
        var perpendicularZ = arrow.motionX / length
        val toPlayerX = player.posX - arrow.posX
        val toPlayerZ = player.posZ - arrow.posZ
        if (perpendicularX * toPlayerX + perpendicularZ * toPlayerZ < 0.0) {
            perpendicularX = -perpendicularX
            perpendicularZ = -perpendicularZ
        }
        return Vec3(perpendicularX, 0.0, perpendicularZ)
    }

    private fun isApproaching(arrow: EntityArrow): Boolean {
        val player = mc.thePlayer ?: return false
        val currentX = arrow.posX - player.posX
        val currentY = arrow.posY - player.posY
        val currentZ = arrow.posZ - player.posZ
        val nextX = currentX + arrow.motionX
        val nextY = currentY + arrow.motionY
        val nextZ = currentZ + arrow.motionZ
        return nextX * nextX + nextY * nextY + nextZ * nextZ <
            currentX * currentX + currentY * currentY + currentZ * currentZ
    }
}
