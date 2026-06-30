/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.math.geometry.Line
import net.ccbluex.liquidbounce.utils.render.Render3D
import net.ccbluex.liquidbounce.utils.simulation.SimulatedArrow
import net.ccbluex.liquidbounce.utils.simulation.SimulatedPlayerCache
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import java.awt.Color
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Predicts incoming arrows and steps the player out of their path. Detection simulates the local
 * player and every flying arrow forward and raycasts the future hitbox; evasion plans the shortest
 * safe sidestep and only escalates to a sprint turn, a jump and a timer boost when the remaining
 * time is too short. With [evade] off it just draws the threat without moving.
 */
object AutoDodge : Module("AutoDodge", Category.COMBAT, Category.SubCategory.COMBAT_RAGE) {

    // Detection
    private val range by float("Range", 12f, 1f..32f)
        .describe("Maximum distance an arrow is considered for dodging.")
    private val lookahead by int("Lookahead", 40, 5..80)
        .describe("Ticks of flight simulated ahead for both the arrow and the player.")
    private val hitboxExpansion by float("HitboxExpansion", 0.5f, 0f..2f)
        .describe("Extra padding around the player hitbox treated as a threat.")

    // Evasion
    private val evade by boolean("Evade", true)
        .describe("Actually move out of the way. Disable for warnings only.")
    private val allowRotation by boolean("AllowRotationChange", false) { evade }
        .describe("Turn and sprint toward safety when a plain sidestep is too slow.")
    private val allowJump by boolean("AllowJump", true) { evade && allowRotation }
        .describe("Jump for extra momentum while escalating a dodge.")
    private val allowTimer by boolean("AllowTimer", false) { evade }
        .describe("Speed the game timer when a dodge needs more time than available.")
    private val timerSpeed by float("TimerSpeed", 2f, 1f..10f) { evade && allowTimer }
        .describe("Timer speed used while escalating a dodge.")
    private val ignoreInventory by boolean("DodgeWithInventoryOpen", false) { evade }
        .describe("Keep dodging while an inventory or container screen is open.")
    private val ignoreUsingItem by boolean("DodgeWhileUsingItem", false) { evade }
        .describe("Keep dodging while eating, blocking or drawing a bow.")

    // Visuals
    private val showTrajectory by boolean("ShowTrajectory", true)
        .describe("Draw the predicted path of the closest threat.")
    private val showSuggestion by boolean("ShowSuggestion", true)
        .describe("Draw a visual direction away from the trajectory.")
    private val suggestionDistance by float("SuggestionDistance", 1.5f, 0.5f..4f)
        .describe("Length of the visual sidestep suggestion.")
    private val throughWalls by boolean("ThroughWalls", false)
        .describe("Show warnings through terrain.")
    private val lineWidth by float("LineWidth", 2f, 1f..5f)
        .describe("Width of the visual guidance lines.")
    private val trajectoryColor by color("TrajectoryColor", Color(255, 85, 85, 210))
    private val suggestionColor by color("SuggestionColor", Color(85, 255, 120, 230))

    private const val SAFE_DISTANCE = 1.5 * 0.3 + 1.5 * 0.5            // deep danger zone
    private const val SAFE_DISTANCE_WITH_PADDING = 0.3 * 5             // outer danger zone
    private const val STRAFE_SPEED_PER_TICK = 0.11
    private const val SPRINT_SPEED_PER_TICK = 0.13
    private const val TIMER_SPEED_PER_TICK = 0.155
    private const val WALK_SPEED = 0.2155
    private const val SPRINT_SPEED = 0.2806
    private const val JUMP_MOTION = 0.42

    private var timerActive = false

    private val detectionGroup = Configurable("Detection")
    private val evasionGroup = Configurable("Evasion")
    private val visualsGroup = Configurable("Visuals")

    init {
        moveValues(detectionGroup, "Range", "Lookahead", "HitboxExpansion")

        moveValues(evasionGroup,
            "Evade", "AllowRotationChange", "AllowJump", "AllowTimer", "TimerSpeed",
            "DodgeWithInventoryOpen", "DodgeWhileUsingItem")

        moveValues(visualsGroup,
            "ShowTrajectory", "ShowSuggestion", "SuggestionDistance", "ThroughWalls",
            "LineWidth", "TrajectoryColor", "SuggestionColor")

        addValues(listOf(detectionGroup, evasionGroup, visualsGroup))
    }
    private data class HitInfo(val tick: Int, val prevArrowPos: Vec3, val arrowVelocity: Vec3)

    private data class DodgePlan(
        val direction: Vec3,
        val shouldJump: Boolean,
        val yawChange: Float?,
        val useTimer: Boolean,
    )

    override fun onDisable() = resetTimer()

    val onMove = handler<MoveEvent> { event ->
        if (!evade || !canDodgeNow()) {
            resetTimer()
            return@handler
        }
        val player = mc.thePlayer ?: return@handler
        val hit = inflictedHit() ?: run { resetTimer(); return@handler }
        val plan = planEvasion(hit) ?: run { resetTimer(); return@handler }

        val direction = plan.direction
        val speed = if (plan.yawChange != null && allowRotation) {
            player.rotationYaw = plan.yawChange
            player.isSprinting = true
            SPRINT_SPEED
        } else {
            max(horizontalSpeed(), WALK_SPEED)
        }

        event.x = direction.xCoord * speed
        event.z = direction.zCoord * speed

        if (plan.shouldJump && allowRotation && allowJump && player.onGround) {
            player.motionY = JUMP_MOTION
            event.y = JUMP_MOTION
        }

        if (plan.useTimer && allowTimer) {
            mc.timer.timerSpeed = timerSpeed
            timerActive = true
        } else {
            resetTimer()
        }
    }

    private fun canDodgeNow(): Boolean {
        val player = mc.thePlayer ?: return false
        if (!ignoreInventory && mc.currentScreen is GuiContainer) return false
        if (!ignoreUsingItem && player.isUsingItem) return false
        return true
    }

    private fun resetTimer() {
        if (timerActive) {
            mc.timer.timerSpeed = 1f
            timerActive = false
        }
    }

    private fun horizontalSpeed(): Double {
        val player = mc.thePlayer ?: return 0.0
        return sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ)
    }

    /**
     * Simulates the player (with current input) and every flying arrow forward, returning the
     * earliest tick at which an arrow would enter the padded player hitbox.
     */
    private fun inflictedHit(): HitInfo? {
        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        val arrows = world.loadedEntityList.asSequence()
            .filterIsInstance<EntityArrow>()
            .filter { arrow ->
                (arrow.motionX != 0.0 || arrow.motionY != 0.0 || arrow.motionZ != 0.0) &&
                    player.getDistanceToEntity(arrow) <= range
            }
            .toList()
        if (arrows.isEmpty()) return null

        val cache = SimulatedPlayerCache.fromClientPlayer(player.movementInput)
        cache.simulateUntil(lookahead)
        val expansion = hitboxExpansion.toDouble()

        var best: HitInfo? = null
        for (arrow in arrows) {
            val path = SimulatedArrow.trace(arrow, lookahead)
            for (tick in 1..lookahead) {
                val pos = cache.getSnapshotAt(tick).pos
                val box = AxisAlignedBB(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3)
                    .expand(expansion, expansion, expansion)
                    .offset(pos.xCoord, pos.yCoord, pos.zCoord)

                if (box.calculateIntercept(path[tick - 1], path[tick]) != null) {
                    if (best == null || tick < best.tick) {
                        best = HitInfo(tick, path[tick - 1], Vec3(arrow.motionX, arrow.motionY, arrow.motionZ))
                    }
                    break
                }
            }
        }
        return best
    }

    private fun planEvasion(hit: HitInfo): DodgePlan? {
        val player = mc.thePlayer ?: return null
        val velX = hit.arrowVelocity.xCoord
        val velZ = hit.arrowVelocity.zCoord
        if (velX * velX + velZ * velZ < 1.0E-6) return null

        val arrowLine = Line(Vec3(hit.prevArrowPos.xCoord, 0.0, hit.prevArrowPos.zCoord), Vec3(velX, 0.0, velZ))
        val playerPos2d = Vec3(player.posX, 0.0, player.posZ)
        val nearestOnLine = arrowLine.project(playerPos2d)
        val distanceToLine = nearestOnLine.distanceTo(playerPos2d)
        if (distanceToLine > SAFE_DISTANCE_WITH_PADDING) return null

        val optimal = findOptimalDodgePosition(arrowLine, playerPos2d, nearestOnLine) ?: return null
        val relX = optimal.xCoord - playerPos2d.xCoord
        val relZ = optimal.zCoord - playerPos2d.zCoord
        val relLength = sqrt(relX * relX + relZ * relZ)
        if (relLength < 1.0E-4) return null
        val direction = Vec3(relX / relLength, 0.0, relZ / relLength)

        // A plain sidestep already gets us out before impact.
        if (distanceToLine > SAFE_DISTANCE) {
            return DodgePlan(direction, shouldJump = false, yawChange = null, useTimer = false)
        }

        val distanceToTravel = relLength - (SAFE_DISTANCE_WITH_PADDING - SAFE_DISTANCE)
        val ticksToImpact = hit.tick + 1
        if (ticksToImpact > distanceToTravel / STRAFE_SPEED_PER_TICK) {
            return DodgePlan(direction, shouldJump = false, yawChange = null, useTimer = false)
        }

        val useTimer = allowTimer && (distanceToTravel / TIMER_SPEED_PER_TICK) / (ticksToImpact + 1) > 1.6
        if (!allowRotation) {
            return DodgePlan(direction, shouldJump = false, yawChange = null, useTimer = useTimer)
        }

        val yaw = Math.toDegrees(atan2(-direction.xCoord, direction.zCoord)).toFloat()
        val shouldJump = ticksToImpact < distanceToTravel / SPRINT_SPEED_PER_TICK
        return DodgePlan(direction, shouldJump = shouldJump, yawChange = yaw, useTimer = useTimer)
    }

    /**
     * Picks the closer border of the danger zone the player can actually walk to, accounting for
     * the two ticks of free movement it takes to redirect and for blocks in the way.
     */
    private fun findOptimalDodgePosition(arrowLine: Line, playerPos2d: Vec3, nearestOnLine: Vec3): Vec3? {
        val player = mc.thePlayer ?: return null
        val perpendicular = arrowLine.direction.crossProduct(Vec3(0.0, 1.0, 0.0))
        val perpLength = perpendicular.lengthVector()
        if (perpLength < 1.0E-6) return null
        val offsetX = perpendicular.xCoord / perpLength * SAFE_DISTANCE_WITH_PADDING
        val offsetZ = perpendicular.zCoord / perpLength * SAFE_DISTANCE_WITH_PADDING

        val borderLeft = Line(arrowLine.origin.addVector(-offsetX, 0.0, -offsetZ), arrowLine.direction)
        val borderRight = Line(arrowLine.origin.addVector(offsetX, 0.0, offsetZ), arrowLine.direction)

        val afterFreeMovement = playerPos2d.addVector(player.motionX * 2.0, 0.0, player.motionZ * 2.0)
        val left = borderLeft.project(afterFreeMovement)
        val right = borderRight.project(afterFreeMovement)

        val walkableLeft = walkableDistance(nearestOnLine, left)
        val walkableRight = walkableDistance(nearestOnLine, right)
        if (walkableLeft < SAFE_DISTANCE && walkableRight >= SAFE_DISTANCE) return right
        if (walkableRight < SAFE_DISTANCE && walkableLeft >= SAFE_DISTANCE) return left

        return if (left.distanceTo(afterFreeMovement) < right.distanceTo(afterFreeMovement) - 0.05) left else right
    }

    private fun walkableDistance(from: Vec3, to: Vec3): Double {
        val player = mc.thePlayer ?: return 0.0
        val world = mc.theWorld ?: return 0.0
        var worst = Double.MAX_VALUE
        for (height in doubleArrayOf(0.6, 1.6)) {
            val origin = Vec3(from.xCoord, player.posY + height, from.zCoord)
            val target = Vec3(to.xCoord, player.posY + height, to.zCoord)
            val blocked = world.rayTraceBlocks(origin, target)
            val reachable = if (blocked != null) origin.distanceTo(blocked.hitVec) else origin.distanceTo(target)
            if (reachable < worst) worst = reachable
        }
        return worst
    }

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
                SimulatedArrow.willHit(arrow, player, lookahead, (0.3 + hitboxExpansion).toDouble())
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
