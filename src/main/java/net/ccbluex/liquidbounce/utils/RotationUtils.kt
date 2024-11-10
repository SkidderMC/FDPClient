/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.combat.FastBow
import net.ccbluex.liquidbounce.features.module.modules.client.Rotations
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextDouble
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextBoolean
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.minecraft.entity.Entity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.*
import javax.vecmath.Vector2f
import kotlin.math.*

object RotationUtils : MinecraftInstance(), Listenable {

    /**
     * Our final rotation point, which [currentRotation] follows.
     */
    var targetRotation: Rotation? = null

    /**
     * The current rotation that is responsible for aiming at objects, synchronizing movement, etc.
     */
    var currentRotation: Rotation? = null

    /**
     * The last rotation that the server has received.
     */
    var serverRotation: Rotation
        get() = lastRotations[0]
        set(value) {
            lastRotations = lastRotations.toMutableList().apply { set(0, value) }
        }

    private const val MAX_CAPTURE_TICKS = 3

    /**
     * A list that stores the last rotations captured from 0 up to [MAX_CAPTURE_TICKS] previous ticks.
     */
    var lastRotations = MutableList(MAX_CAPTURE_TICKS) { Rotation.ZERO }
        set(value) {
            val updatedList = MutableList(lastRotations.size) { Rotation.ZERO }

            for (tick in 0 until MAX_CAPTURE_TICKS) {
                updatedList[tick] = if (tick == 0) value[0] else field[tick - 1]
            }

            field = updatedList
        }

    /**
     * The currently in-use rotation settings, which are used to determine how the rotations will move.
     */
    var activeSettings: RotationSettings? = null

    var resetTicks = 0

    /**
     * Face block
     *
     * @param blockPos target block
     */
    fun faceBlock(blockPos: BlockPos?, throughWalls: Boolean = true): VecRotation? {
        val world = mc.theWorld ?: return null
        val player = mc.thePlayer ?: return null

        if (blockPos == null)
            return null

        val eyesPos = player.eyes
        val startPos = Vec3(blockPos)

        var visibleVec: VecRotation? = null
        var invisibleVec: VecRotation? = null

        for (x in 0.0..1.0) {
            for (y in 0.0..1.0) {
                for (z in 0.0..1.0) {
                    val block = blockPos.getBlock() ?: return null

                    val posVec = startPos.add(block.lerpWith(x, y, z))

                    val dist = eyesPos.distanceTo(posVec)

                    val (diffX, diffY, diffZ) = posVec - eyesPos
                    val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)

                    val rotation = Rotation(
                        MathHelper.wrapAngleTo180_float(atan2(diffZ, diffX).toDegreesF() - 90f),
                        MathHelper.wrapAngleTo180_float(-atan2(diffY, diffXZ).toDegreesF())
                    ).fixedSensitivity()

                    val rotationVector = getVectorForRotation(rotation)
                    val vector = eyesPos + (rotationVector * dist)

                    val currentVec = VecRotation(posVec, rotation)
                    val raycast = world.rayTraceBlocks(eyesPos, vector, false, true, false)

                    val currentRotation = currentRotation ?: player.rotation

                    if (raycast != null && raycast.blockPos == blockPos) {
                        if (visibleVec == null || rotationDifference(
                                currentVec.rotation,
                                currentRotation
                            ) < rotationDifference(visibleVec.rotation, currentRotation)
                        ) {
                            visibleVec = currentVec
                        }
                    } else if (throughWalls) {
                        val invisibleRaycast = performRaytrace(blockPos, rotation) ?: continue

                        if (invisibleRaycast.blockPos != blockPos) {
                            continue
                        }

                        if (invisibleVec == null || rotationDifference(
                                currentVec.rotation,
                                currentRotation
                            ) < rotationDifference(invisibleVec.rotation, currentRotation)
                        ) {
                            invisibleVec = currentVec
                        }
                    }
                }
            }
        }

        return visibleVec ?: invisibleVec
    }

    /**
     * Face trajectory of arrow by default, can be used for calculating other trajectories (eggs, snowballs)
     * by specifying `gravity` and `velocity` parameters
     *
     * @param target      your enemy
     * @param predict     predict new enemy position
     * @param predictSize predict size of predict
     * @param gravity     how much gravity does the projectile have, arrow by default
     * @param velocity    with what velocity will the projectile be released, velocity for arrow is calculated when null
     */
    fun faceTrajectory(
        target: Entity,
        predict: Boolean,
        predictSize: Float,
        gravity: Float = 0.05f,
        velocity: Float? = null,
    ): Rotation {
        val player = mc.thePlayer

        val posX =
            target.posX + (if (predict) (target.posX - target.prevPosX) * predictSize else .0) - (player.posX + if (predict) player.posX - player.prevPosX else .0)
        val posY =
            target.entityBoundingBox.minY + (if (predict) (target.entityBoundingBox.minY - target.prevPosY) * predictSize else .0) + target.eyeHeight - 0.15 - (player.entityBoundingBox.minY + (if (predict) player.posY - player.prevPosY else .0)) - player.getEyeHeight()
        val posZ =
            target.posZ + (if (predict) (target.posZ - target.prevPosZ) * predictSize else .0) - (player.posZ + if (predict) player.posZ - player.prevPosZ else .0)
        val posSqrt = sqrt(posX * posX + posZ * posZ)

        var finalVelocity = velocity

        if (finalVelocity == null) {
            finalVelocity = if (FastBow.handleEvents()) 1f else player.itemInUseDuration / 20f
            finalVelocity = ((finalVelocity * finalVelocity + finalVelocity * 2) / 3).coerceAtMost(1f)
        }

        val gravityModifier = 0.12f * gravity

        return Rotation(
            atan2(posZ, posX).toDegreesF() - 90f,
            -atan(
                (finalVelocity * finalVelocity - sqrt(
                    finalVelocity * finalVelocity * finalVelocity * finalVelocity - gravityModifier * (gravityModifier * posSqrt * posSqrt + 2 * posY * finalVelocity * finalVelocity)
                )) / (gravityModifier * posSqrt)
            ).toDegreesF()
        )
    }

    /**
     * Translate vec to rotation
     *
     * @param vec     target vec
     * @param predict predict new location of your body
     * @return rotation
     */
    fun toRotation(vec: Vec3, predict: Boolean = false, fromEntity: Entity = mc.thePlayer): Rotation {
        val eyesPos = fromEntity.eyes
        if (predict) eyesPos.addVector(fromEntity.motionX, fromEntity.motionY, fromEntity.motionZ)

        val (diffX, diffY, diffZ) = vec - eyesPos
        return Rotation(
            MathHelper.wrapAngleTo180_float(
                atan2(diffZ, diffX).toDegreesF() - 90f
            ), MathHelper.wrapAngleTo180_float(
                -atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)).toDegreesF()
            )
        )
    }

    /**
     * Search good center
     *
     * @param bb                entity box to search rotation for
     * @param outborder         outborder option
     * @param random            random option
     * @param predict           predict, offsets rotation by player's motion
     * @param lookRange         look range
     * @param attackRange       attack range, rotations in attack range will be prioritized
     * @param throughWallsRange through walls range,
     * @return center
     */
    fun searchCenter(
        bb: AxisAlignedBB, outborder: Boolean, random: Boolean, predict: Boolean,
        lookRange: Float, attackRange: Float, throughWallsRange: Float = 0f,
        bodyPoints: List<String> = listOf("Head", "Feet"), horizontalSearch: ClosedFloatingPointRange<Float> = 0f..1f,
    ): Rotation? {
        val scanRange = lookRange.coerceAtLeast(attackRange)

        val max = BodyPoint.fromString(bodyPoints[0]).range.endInclusive
        val min = BodyPoint.fromString(bodyPoints[1]).range.start

        if (outborder) {
            val vec3 = bb.lerpWith(nextDouble(0.5, 1.3), nextDouble(0.9, 1.3), nextDouble(0.5, 1.3))

            return toRotation(vec3, predict).fixedSensitivity()
        }

        val eyes = mc.thePlayer.eyes

        var currRotation = currentRotation ?: mc.thePlayer.rotation

        var attackRotation: Pair<Rotation, Float>? = null
        var lookRotation: Pair<Rotation, Float>? = null

        if (random) {
            currRotation += Rotation(
                if (Math.random() > 0.25) nextFloat(-15f, 15f) else 0f,
                if (Math.random() > 0.25) nextFloat(-10f, 10f) else 0f
            )
        }

        val (hMin, hMax) = horizontalSearch.start.toDouble() to horizontalSearch.endInclusive.toDouble()

        for (x in hMin..hMax) {
            for (y in min..max) {
                for (z in hMin..hMax) {
                    val vec = bb.lerpWith(x, y, z)

                    val rotation = toRotation(vec, predict).fixedSensitivity()

                    // Calculate actual hit vec after applying fixed sensitivity to rotation
                    val gcdVec = bb.calculateIntercept(
                        eyes,
                        eyes + getVectorForRotation(rotation) * scanRange.toDouble()
                    )?.hitVec ?: continue

                    val distance = eyes.distanceTo(gcdVec)

                    // Check if vec is in range
                    // Skip if a rotation that is in attack range was already found and the vec is out of attack range
                    if (distance > scanRange || (attackRotation != null && distance > attackRange))
                        continue

                    // Check if vec is reachable through walls
                    if (!isVisible(gcdVec) && distance > throughWallsRange)
                        continue

                    val rotationWithDiff = rotation to rotationDifference(rotation, currRotation)

                    if (distance <= attackRange) {
                        if (attackRotation == null || rotationWithDiff.second < attackRotation.second)
                            attackRotation = rotationWithDiff
                    } else {
                        if (lookRotation == null || rotationWithDiff.second < lookRotation.second)
                            lookRotation = rotationWithDiff
                    }
                }
            }
        }

        return attackRotation?.first ?: lookRotation?.first ?: run {
            val vec = getNearestPointBB(eyes, bb)
            val dist = eyes.distanceTo(vec)

            if (dist <= scanRange && (dist <= throughWallsRange || isVisible(vec)))
                toRotation(vec, predict)
            else null
        }
    }

    /**
     * Calculate difference between the client rotation and your entity
     *
     * @param entity your entity
     * @return difference between rotation
     */
    fun rotationDifference(entity: Entity) =
        rotationDifference(toRotation(entity.hitBox.center, true), mc.thePlayer.rotation)

    /**
     * Calculate difference between two rotations
     *
     * @param a rotation
     * @param b rotation
     * @return difference between rotation
     */
    fun rotationDifference(a: Rotation, b: Rotation = serverRotation) =
        hypot(angleDifference(a.yaw, b.yaw), a.pitch - b.pitch)

    private fun limitAngleChange(
        currentRotation: Rotation,
        targetRotation: Rotation,
        settings: RotationSettings
    ): Rotation {
        val (hSpeed, vSpeed) = if (settings.instant) {
            180f to 180f
        } else settings.horizontalSpeed.random() to settings.verticalSpeed.random()

        return performAngleChange(
            currentRotation,
            targetRotation,
            hSpeed,
            vSpeed,
            !settings.instant && settings.legitimize,
            settings.minRotationDifference,
            settings.smootherMode,
        )
    }

    fun performAngleChange(
        currentRotation: Rotation,
        targetRotation: Rotation,
        hSpeed: Float,
        vSpeed: Float = hSpeed,
        legitimize: Boolean,
        minRotationDiff: Float,
        smootherMode: String,
    ): Rotation {
        var (yawDiff, pitchDiff) = angleDifferences(targetRotation, currentRotation)

        val rotationDifference = hypot(yawDiff, pitchDiff)

        val shortStopChance = activeSettings?.shortStopChance ?: 0
        val isShortStopActive = WaitTickUtils.hasScheduled(this)

        if (isShortStopActive || activeSettings?.simulateShortStop == true &&
            shortStopChance > 0 && nextInt(endExclusive = 100) <= shortStopChance
        ) {
            // Use the tick scheduling to our advantage as we can check if short stop is still active.
            if (!isShortStopActive) {
                WaitTickUtils.schedule(activeSettings?.shortStopDuration?.random()?.plus(1) ?: 0, this) {}
            }

            yawDiff = 0f
            pitchDiff = 0f
        }

        val (hFactor, vFactor) = computeFactor(rotationDifference, hSpeed to vSpeed, smootherMode == "Relative")

        var (straightLineYaw, straightLinePitch) =
            abs(yawDiff safeDiv rotationDifference) * hFactor to abs(pitchDiff safeDiv rotationDifference) * vFactor

        straightLineYaw = yawDiff.coerceIn(-straightLineYaw, straightLineYaw)
        straightLinePitch = pitchDiff.coerceIn(-straightLinePitch, straightLinePitch)

        val rotationWithGCD = Rotation(straightLineYaw, straightLinePitch).fixedSensitivity()

        if (abs(rotationWithGCD.yaw) <= nextFloat(min(minRotationDiff, getFixedAngleDelta()), minRotationDiff)) {
            straightLineYaw = 0f
        }

        if (abs(rotationWithGCD.pitch) < nextFloat(min(minRotationDiff, getFixedAngleDelta()), minRotationDiff)) {
            straightLinePitch = 0f
        }

        if (legitimize) {
            applySlowDown(straightLineYaw, true) {
                straightLineYaw = it
            }

            applySlowDown(straightLinePitch, false) {
                straightLinePitch = it
            }
        }

        return Rotation(currentRotation.yaw + straightLineYaw, currentRotation.pitch + straightLinePitch)
    }

    private fun applySlowDown(diff: Float, yaw: Boolean, action: (Float) -> Unit) {
        if (diff == 0f) {
            action(diff)
            return
        }

        var previous = serverRotation

        val lastTickDiffs = lastRotations.slice(1 until lastRotations.size).map { rotation ->
            val difference = angleDifferences(previous, rotation)

            previous = rotation

            if (yaw) difference.x else difference.y
        }

        val (lastTick1, lastTick2) = lastTickDiffs[0] to lastTickDiffs[1]

        val smallestAngleGCD = getFixedAngleDelta() + 2.5F

        when {
            // Slow start
            lastTick1 == 0f -> {
                if ((diff <= smallestAngleGCD || diff > 50f) && nextBoolean())
                    action((lastTick1..diff).lerpWith(nextFloat(0.55f, 0.65f)))
                else action((lastTick1..diff).lerpWith(nextFloat(0f, 0.2f)))
            }

            // Second stage of slow start
            lastTick2 == 0f && abs(lastTick1) <= abs(diff) -> {
                action((lastTick1..diff).lerpWith(nextFloat(0f, 0.4f)))
            }

            // Slow down before direction change
            abs(lastTick2) <= abs(lastTick1) && diff.sign != lastTick1.sign -> {
                val beforeZero = nextFloat(0f, (0f - lastTick1) safeDiv (diff - lastTick1))

                action((lastTick1..diff).lerpWith(beforeZero))
            }

            // Start slow after changing direction
            abs(lastTick2) >= abs(lastTick1) && diff.sign != lastTick1.sign -> {
                action((lastTick1..diff).lerpWith(nextFloat(0f, 0.4f)))
            }
        }
    }

    fun computeFactor(rotationDifference: Float, axis: Pair<Float, Float>, isRelativeChosen: Boolean): Rotation {
        val horizontalDivision = if (isRelativeChosen) nextFloat(120f, 150f) else axis.first
        val verticalDivision = if (isRelativeChosen) nextFloat(120f, 150f) else axis.second

        return Rotation(
            (rotationDifference / horizontalDivision * axis.first).coerceAtMost(180f),
            (rotationDifference / verticalDivision * axis.second).coerceIn(-90f, 90f)
        )
    }

    /**
     * Calculate difference between two angle points
     *
     * @param a angle point
     * @param b angle point
     * @return difference between angle points
     */
    fun angleDifference(a: Float, b: Float) = MathHelper.wrapAngleTo180_float(a - b)

    /**
     * Returns a 2-parameter vector with the calculated angle differences between [target] and [current] rotations
     */
    fun angleDifferences(target: Rotation, current: Rotation) =
        Vector2f(angleDifference(target.yaw, current.yaw), target.pitch - current.pitch)

    /**
     * Calculate rotation to vector
     *
     * @param [yaw] [pitch] your rotation
     * @return target vector
     */
    fun getVectorForRotation(yaw: Float, pitch: Float): Vec3 {
        val yawRad = yaw.toRadians()
        val pitchRad = pitch.toRadians()

        val f = MathHelper.cos(-yawRad - PI.toFloat())
        val f1 = MathHelper.sin(-yawRad - PI.toFloat())
        val f2 = -MathHelper.cos(-pitchRad)
        val f3 = MathHelper.sin(-pitchRad)

        return Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
    }

    fun getVectorForRotation(rotation: Rotation) = getVectorForRotation(rotation.yaw, rotation.pitch)

    /**
     * Returns the inverted yaw angle.
     *
     * @param yaw The original yaw angle in degrees.
     * @return The yaw angle inverted by 180 degrees.
     */
    fun invertYaw(yaw: Float): Float {
        return (yaw + 180) % 360
    }

    /**
     * Allows you to check if your crosshair is over your target entity
     *
     * @param targetEntity       your target entity
     * @param blockReachDistance your reach
     * @return if crosshair is over target
     */
    fun isFaced(targetEntity: Entity, blockReachDistance: Double) =
        raycastEntity(blockReachDistance) { entity: Entity -> targetEntity == entity } != null

    /**
     * Allows you to check if your crosshair is over your target entity
     *
     * @param targetEntity       your target entity
     * @param blockReachDistance your reach
     * @return if crosshair is over target
     */
    fun isRotationFaced(targetEntity: Entity, blockReachDistance: Double, rotation: Rotation) = raycastEntity(
        blockReachDistance,
        rotation.yaw,
        rotation.pitch
    ) { entity: Entity -> targetEntity == entity } != null

    /**
     * Allows you to check if your enemy is behind a wall
     */
    fun isVisible(vec3: Vec3) = mc.theWorld.rayTraceBlocks(mc.thePlayer.eyes, vec3) == null

    /**
     * Set your target rotation
     *
     * @param rotation your target rotation
     */
    fun setTargetRotation(rotation: Rotation, options: RotationSettings, ticks: Int = options.resetTicks) {
        if (rotation.yaw.isNaN() || rotation.pitch.isNaN() || rotation.pitch > 90 || rotation.pitch < -90) {
            return
        }

        if (!options.prioritizeRequest && activeSettings?.prioritizeRequest == true) {
            return
        }

        if (!options.applyServerSide) {
            currentRotation?.let {
                mc.thePlayer.rotationYaw = it.yaw
                mc.thePlayer.rotationPitch = it.pitch
            }

            resetRotation()
        }

        targetRotation = rotation

        resetTicks = if (!options.applyServerSide || !options.resetTicksValue.isSupported()) 1 else ticks

        activeSettings = options

        if (options.immediate) {
            update()
        }
    }

    private fun resetRotation() {
        resetTicks = 0
        currentRotation?.let { (yaw, _) ->
            mc.thePlayer?.let {
                it.rotationYaw = yaw + angleDifference(it.rotationYaw, yaw)
                syncRotations()
            }
        }
        targetRotation = null
        currentRotation = null
        activeSettings = null
    }

    /**
     * Returns the smallest angle difference possible with a specific sensitivity ("gcd")
     */
    fun getFixedAngleDelta(sensitivity: Float = mc.gameSettings.mouseSensitivity) =
        (sensitivity * 0.6f + 0.2f).pow(3) * 1.2f

    /**
     * Returns angle that is legitimately accomplishable with player's current sensitivity
     */
    fun getFixedSensitivityAngle(targetAngle: Float, startAngle: Float = 0f, gcd: Float = getFixedAngleDelta()) =
        startAngle + ((targetAngle - startAngle) / gcd).roundToInt() * gcd

    /**
     * Creates a raytrace even when the target [blockPos] is not visible
     */
    fun performRaytrace(
        blockPos: BlockPos,
        rotation: Rotation,
        reach: Float = mc.playerController.blockReachDistance,
    ): MovingObjectPosition? {
        val world = mc.theWorld ?: return null
        val player = mc.thePlayer ?: return null

        val eyes = player.eyes

        return blockPos.getBlock()?.collisionRayTrace(
            world,
            blockPos,
            eyes,
            eyes + (getVectorForRotation(rotation) * reach.toDouble())
        )
    }

    fun performRayTrace(blockPos: BlockPos, vec: Vec3, eyes: Vec3 = mc.thePlayer.eyes) =
        mc.theWorld?.let { blockPos.getBlock()?.collisionRayTrace(it, blockPos, eyes, vec) }

    fun syncRotations() {
        val player = mc.thePlayer ?: return

        player.prevRotationYaw = player.rotationYaw
        player.prevRotationPitch = player.rotationPitch
        player.renderArmYaw = player.rotationYaw
        player.renderArmPitch = player.rotationPitch
        player.prevRenderArmYaw = player.rotationYaw
        player.prevRotationPitch = player.rotationPitch
    }

    private fun update() {
        val settings = activeSettings ?: return
        val player = mc.thePlayer ?: return

        val playerRotation = player.rotation

        val shouldUpdate = !InventoryUtils.serverOpenContainer && !InventoryUtils.serverOpenInventory

        if (!shouldUpdate) {
            return
        }

        if (resetTicks == 0) {
            val distanceToPlayerRotation = rotationDifference(currentRotation ?: serverRotation, playerRotation)

            if (distanceToPlayerRotation <= settings.angleResetDifference || !settings.applyServerSide) {
                resetRotation()
                return
            }

            currentRotation = limitAngleChange(
                currentRotation ?: serverRotation,
                playerRotation,
                settings
            ).fixedSensitivity()
            return
        }

        targetRotation?.let {
            limitAngleChange(currentRotation ?: serverRotation, it, settings).let { rotation ->
                if (!settings.applyServerSide) {
                    rotation.toPlayer(player)
                } else {
                    currentRotation = rotation.fixedSensitivity()
                }
            }
        }

        if (resetTicks > 0) {
            resetTicks--
        }
    }

    /**
     * Any module that modifies the server packets without using the [currentRotation] should use on module disable.
     */
    fun syncSpecialModuleRotations() {
        serverRotation.let { (yaw, _) ->
            mc.thePlayer?.let {
                it.rotationYaw = yaw + angleDifference(it.rotationYaw, yaw)
                syncRotations()
            }
        }
    }

    /**
     * Checks if the rotation difference is not the same as the smallest GCD angle possible.
     */
    fun canUpdateRotation(current: Rotation, target: Rotation, multiplier: Int = 1): Boolean {
        if (current == target)
            return true

        val smallestAnglePossible = getFixedAngleDelta()

        val gcdRoundedTarget =
            (rotationDifference(target, current) / smallestAnglePossible).roundToInt() * smallestAnglePossible

        return gcdRoundedTarget > smallestAnglePossible * multiplier
    }

    /**
     * Handle rotation update
     */
    @EventTarget(priority = -1)
    fun onRotationUpdate(event: RotationUpdateEvent) {
        activeSettings?.let {
            // Was the rotation update immediate? Allow updates the next tick.
            if (it.immediate) {
                it.immediate = false
                return
            }
        }

        update()
    }

    /**
     * Handle strafing
     */
    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val data = activeSettings ?: return

        if (!data.strafe) {
            return
        }

        currentRotation?.let {
            it.applyStrafeToPlayer(event, data.strict)
            event.cancelEvent()
        }
    }

    /**
     * Handle rotation-packet modification
     */
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet !is C03PacketPlayer || !packet.rotating) {
            return
        }

        currentRotation?.let {
            packet.rotation = it

            val yawDiff = angleDifference(packet.yaw, serverRotation.yaw)
            val pitchDiff = angleDifference(packet.pitch, serverRotation.pitch)

            if (Rotations.debugRotations) {
                chat("PREV YAW: $yawDiff, PREV PITCH: $pitchDiff")
            }
        }
    }

    enum class BodyPoint(val rank: Int, val range: ClosedFloatingPointRange<Double>) {
        HEAD(1, 0.75..0.9),
        BODY(0, 0.5..0.75),
        FEET(-1, 0.1..0.4),
        UNKNOWN(-2, 0.0..0.0);

        companion object {
            fun fromString(point: String): BodyPoint {
                return values().find { it.name.equals(point, ignoreCase = true) } ?: UNKNOWN
            }
        }
    }

    fun coerceBodyPoint(point: BodyPoint, minPoint: BodyPoint, maxPoint: BodyPoint): BodyPoint {
        return when {
            point.rank < minPoint.rank -> minPoint
            point.rank > maxPoint.rank -> maxPoint
            else -> point
        }
    }
}