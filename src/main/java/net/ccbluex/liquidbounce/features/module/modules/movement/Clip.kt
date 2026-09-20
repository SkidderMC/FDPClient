/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Deliberate, user-controlled clipping utility adapted from LiquidBounce Nextgen.
 *
 * Fancy only accepts a destination after encountering at least one obstructed cell. This prevents
 * the module from behaving like a short-range teleport in open space and keeps its operation
 * deterministic for movement simulators such as Grim.
 */
object ClipModule : Module("Clip", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS) {

    private val modeValue = choices("Mode", arrayOf("Fancy", "Old"), "Fancy")
        .describe("Fancy searches for a safe opening behind a wall; Old applies a configured offset once.")
    private val mode by modeValue

    private val oldHorizontal by float("OldHorizontal", 0f, -10f..10f) { mode == "Old" }
        .describe("Forward offset applied once when Old mode is enabled.")
    private val oldVertical by float("OldVertical", 5f, -10f..10f) { mode == "Old" }
        .describe("Vertical offset applied once when Old mode is enabled.")
    private val oldResetVelocity by boolean("OldResetVelocity", true) { mode == "Old" }
        .describe("Clear motion after applying the Old mode offset.")

    private val fancyHorizontal by int("FancyHorizontal", 0, 0..6) { mode == "Fancy" }
        .describe("Maximum horizontal distance searched for a free two-block-high destination.")
    private val fancyVertical by int("FancyVertical", 5, 0..6) { mode == "Fancy" }
        .describe("Maximum vertical distance searched for a free two-block-high destination.")
    private val requiresStandOn by boolean("RequiresStandOn", true) { mode == "Fancy" }
        .describe("Require a collidable block below the destination, except when clipping upward.")
    private val cooldownTicks by int("CooldownTicks", 5, 1..20) { mode == "Fancy" }
        .describe("Minimum ticks between Fancy clips.")
    private val directionHint by boolean("DirectionHint", true) { mode == "Fancy" }
        .describe("Show available vertical clip directions next to the crosshair.")

    private val possibleVerticalDirections = linkedSetOf<EnumFacing>()
    private var cooldown = 0

    init {
        modeValue.onChanged { selected ->
            if (state && selected == "Old") performOldClip()
        }
        group("Old", "OldHorizontal", "OldVertical", "OldResetVelocity")
        group(
            "Fancy",
            "FancyHorizontal", "FancyVertical", "RequiresStandOn", "CooldownTicks", "DirectionHint"
        )
    }

    override fun onEnable() {
        cooldown = 0
        possibleVerticalDirections.clear()

        if (mode != "Old") return

        performOldClip()
    }

    private fun performOldClip() {
        val player = mc.thePlayer ?: run {
            state = false
            return
        }
        val yaw = Math.toRadians(player.rotationYaw.toDouble())
        val x = -sin(yaw) * oldHorizontal
        val z = cos(yaw) * oldHorizontal

        player.setPosition(player.posX + x, player.posY + oldVertical, player.posZ + z)
        if (oldResetVelocity) {
            player.motionX = 0.0
            player.motionY = 0.0
            player.motionZ = 0.0
        }
        state = false
    }

    override fun onDisable() {
        cooldown = 0
        possibleVerticalDirections.clear()
    }

    val onUpdate = handler<UpdateEvent> {
        if (mode != "Fancy") return@handler

        val player = mc.thePlayer ?: return@handler
        mc.theWorld ?: return@handler
        val origin = BlockPos(player)

        possibleVerticalDirections.clear()
        for (direction in VERTICAL_DIRECTIONS) {
            val destination = ClipPathFinder.findDestination(origin, direction, fancyVertical) { position ->
                isPossibleLocation(position, direction == EnumFacing.UP)
            }
            if (destination != null) possibleVerticalDirections += direction
        }

        if (cooldown > 0) {
            cooldown--
            return@handler
        }

        val direction = requestedDirection() ?: return@handler
        val length = if (direction.axis == EnumFacing.Axis.Y) fancyVertical else fancyHorizontal
        val destination = ClipPathFinder.findDestination(origin, direction, length) { position ->
            val movingUp = direction == EnumFacing.UP
            isPossibleLocation(position, movingUp)
        } ?: return@handler

        // Re-check immediately before moving. A block update can occur between the scan and this
        // assignment; never commit a position which is no longer a valid two-block-high opening.
        if (!isPossibleLocation(destination, direction == EnumFacing.UP)) return@handler

        player.setPosition(destination.x + 0.5, destination.y.toDouble(), destination.z + 0.5)
        cooldown = cooldownTicks
    }

    val onRender2D = handler<Render2DEvent> {
        if (!directionHint || mode != "Fancy" || possibleVerticalDirections.isEmpty()) return@handler

        val hint = buildString {
            append("[ ")
            if (EnumFacing.UP in possibleVerticalDirections) append("^ ")
            if (EnumFacing.DOWN in possibleVerticalDirections) append("v ")
            append(']')
        }
        val resolution = ScaledResolution(mc)
        mc.fontRendererObj.drawStringWithShadow(
            hint,
            resolution.scaledWidth / 2f + 10f,
            resolution.scaledHeight / 2f - mc.fontRendererObj.FONT_HEIGHT / 2f,
            Color.WHITE.rgb
        )
    }

    private fun requestedDirection(): EnumFacing? {
        val player = mc.thePlayer ?: return null
        val settings = mc.gameSettings

        if (player.isCollidedHorizontally) {
            return when {
                settings.keyBindForward.isKeyDown -> player.horizontalFacing
                settings.keyBindBack.isKeyDown -> player.horizontalFacing.opposite
                settings.keyBindLeft.isKeyDown -> player.horizontalFacing.rotateYCCW()
                settings.keyBindRight.isKeyDown -> player.horizontalFacing.rotateY()
                else -> null
            }
        }

        return when {
            settings.keyBindSneak.isKeyDown -> EnumFacing.DOWN
            settings.keyBindJump.isKeyDown -> EnumFacing.UP
            else -> null
        }
    }

    private fun isPossibleLocation(position: BlockPos, movingUp: Boolean): Boolean {
        val world = mc.theWorld ?: return false
        if (!world.isAirBlock(position) || !world.isAirBlock(position.up())) return false
        if (!requiresStandOn || movingUp) return true

        val below = position.down()
        val state = world.getBlockState(below)
        return state.block.getCollisionBoundingBox(world, below, state) != null
    }

    override val tag
        get() = mode

    private val VERTICAL_DIRECTIONS = arrayOf(EnumFacing.UP, EnumFacing.DOWN)
}

internal object ClipPathFinder {
    /** Returns the first valid cell after an obstruction, never a free path in open space. */
    fun findDestination(
        origin: BlockPos,
        direction: EnumFacing,
        length: Int,
        isPossibleLocation: (BlockPos) -> Boolean,
    ): BlockPos? {
        if (length <= 0) return null

        var position = origin
        var wallBetween = false
        repeat(length) {
            position = position.offset(direction)
            if (isPossibleLocation(position)) {
                if (wallBetween) return position
            } else {
                wallBetween = true
            }
        }
        return null
    }
}
