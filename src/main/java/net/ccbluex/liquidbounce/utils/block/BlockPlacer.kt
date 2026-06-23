/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block

import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.pow

interface BlockPlacementEnvironment {
    fun isReplaceable(position: BlockPos): Boolean
    fun canClick(position: BlockPos): Boolean
    fun canReach(hitPosition: Vec3): Boolean
    fun isVisible(neighbor: BlockPos, face: EnumFacing, hitPosition: Vec3): Boolean = true
}

data class BlockPlacementPlan(
    val target: BlockPos,
    val neighbor: BlockPos,
    val face: EnumFacing,
    val hitPosition: Vec3,
    val rotation: Rotation
)

fun interface BlockPlacementAction {
    fun place(plan: BlockPlacementPlan): Boolean
}

/** Separates placement planning from the module-specific inventory and interaction action. */
class BlockPlacer(
    private val environment: BlockPlacementEnvironment,
    private val targetFactory: FaceTargetPositionFactory,
    private val action: BlockPlacementAction
) {
    fun plan(
        target: BlockPos,
        eyes: Vec3,
        currentRotation: Rotation,
        previousTarget: Vec3? = null
    ): BlockPlacementPlan? {
        if (!environment.isReplaceable(target)) return null

        return EnumFacing.values().asSequence().mapNotNull { offset ->
            val neighbor = target.offset(offset)
            val clickedFace = offset.opposite
            if (!environment.canClick(neighbor)) return@mapNotNull null

            val hitPosition = targetFactory.create(neighbor, clickedFace, eyes, previousTarget)
            if (!environment.canReach(hitPosition) || !environment.isVisible(neighbor, clickedFace, hitPosition)) {
                return@mapNotNull null
            }

            BlockPlacementPlan(target, neighbor, clickedFace, hitPosition, rotationTo(eyes, hitPosition))
        }.minByOrNull { candidate -> score(currentRotation, eyes, candidate) }
    }

    fun execute(plan: BlockPlacementPlan): Boolean = action.place(plan)

    fun planAndExecute(
        target: BlockPos,
        eyes: Vec3,
        currentRotation: Rotation,
        previousTarget: Vec3? = null
    ): Boolean = plan(target, eyes, currentRotation, previousTarget)?.let(::execute) == true

    private fun score(current: Rotation, eyes: Vec3, plan: BlockPlacementPlan): Double {
        val yawDifference = MathHelper.wrapAngleTo180_float(plan.rotation.yaw - current.yaw).toDouble()
        val pitchDifference = (plan.rotation.pitch - current.pitch).toDouble()
        return yawDifference.pow(2) + pitchDifference.pow(2) + eyes.squareDistanceTo(plan.hitPosition) * 0.01
    }

    private fun rotationTo(origin: Vec3, target: Vec3): Rotation {
        val difference = target.subtract(origin)
        val horizontal = hypot(difference.xCoord, difference.zCoord)
        val yaw = Math.toDegrees(atan2(difference.zCoord, difference.xCoord)).toFloat() - 90f
        val pitch = -Math.toDegrees(atan2(difference.yCoord, horizontal)).toFloat()
        return Rotation(MathHelper.wrapAngleTo180_float(yaw), pitch.coerceIn(-90f, 90f))
    }
}
