/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.FakeLag
import net.ccbluex.liquidbounce.features.module.modules.combat.FastBow
import net.ccbluex.liquidbounce.features.module.modules.other.NoRotateSet
import net.ccbluex.liquidbounce.features.module.modules.client.Rotations
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.client.BlinkUtils
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.rotation
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextDouble
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.rotation.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.simulation.ProjectileSolver
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.*
import javax.vecmath.Vector2f
import kotlin.math.*

object RotationUtils : MinecraftInstance, Listenable {

    private val requestArbiter = RotationRequestArbiter()

    /**
     * Our final rotation point, which [currentRotation] follows.
     */
    private var targetRotation: Rotation? = null

    /**
     * The current rotation that is responsible for aiming at objects, synchronizing movement, etc.
     */
    var currentRotation: Rotation? = null

    /**
     * The last rotation the server knows about. While Blink/FakeLag hold outgoing packets the
     * queued (theoretical) rotation is used, since the server will receive it on flush; otherwise
     * the last rotation that actually left the client, so packets a module drops outright never
     * poison gates that compare against the server's view.
     */
    var serverRotation: Rotation
        get() = if (isFakeLagging) lastRotations[0] else actualServerRotation
        set(value) {
            val previousRotations = lastRotations

            lastRotations = MutableList(MAX_CAPTURE_TICKS) { tick ->
                if (tick == 0) value.copy() else previousRotations[tick - 1].copy()
            }
        }

    /**
     * The last rotation attached to any outgoing rotating packet, even one that was cancelled or
     * is still being held back by Blink/FakeLag.
     */
    val theoreticalServerRotation: Rotation
        get() = lastRotations[0]

    /**
     * The last rotation the server has actually received: set when a rotating packet is truly
     * dispatched to the wire, so cancelled or still-queued packets never advance it.
     */
    @Volatile
    var actualServerRotation = Rotation(0f, 0f)
        private set

    private val isFakeLagging
        get() = BlinkUtils.isBlinking || FakeLag.isLagging

    private const val MAX_CAPTURE_TICKS = 3

    /**
     * Generous safety margin (in ticks) after which the arbiter force-releases an un-refreshed lease.
     * Larger than any ResetTicks setting so it never fires during normal aiming.
     */
    private const val ARBITER_MAX_IDLE_TICKS = 60

    var modifiedInput = MovementInput()

    /**
     * A list that stores the last rotations captured from 0 up to [MAX_CAPTURE_TICKS] previous ticks.
     */
    var lastRotations = MutableList(MAX_CAPTURE_TICKS) { Rotation(0f, 0f) }
        private set

    /**
     * The currently in-use rotation settings, which are used to determine how the rotations will move.
     */
    var activeSettings: RotationSettings? = null

    val activeRequestPriority
        get() = requestArbiter.activeRequest?.priority

    var resetTicks = 0

    /**
     * The entity the active aim is targeting, set by combat modules. Used by DynamicAccel for
     * distance-aware acceleration; null when not aiming at an entity.
     */
    var aimTargetEntity: Entity? = null

    /**
     * Last rotation chosen by [searchCenter]; used to bias spot selection toward the previously aimed
     * point (sticky aim) when a caller opts in.
     */
    private var lastSearchRotation: Rotation? = null

    /**
     * Face block
     *
     * @param blockPos target block
     */
    fun faceBlock(
        blockPos: BlockPos?,
        throughWalls: Boolean = true,
        targetUpperFace: Boolean = false,
        hRange: ClosedFloatingPointRange<Double> = 0.0..1.0
    ): VecRotation? {
        val world = mc.theWorld ?: return null
        val player = mc.thePlayer ?: return null

        if (blockPos == null) return null

        val block = blockPos.block ?: return null

        val eyesPos = player.eyes
        val startPos = Vec3(blockPos)

        var visibleVec: VecRotation? = null
        var invisibleVec: VecRotation? = null

        val yRange = if (targetUpperFace) 0.0..0.01 else 0.0..1.0

        for (x in hRange) {
            for (y in yRange) {
                for (z in hRange) {
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

                    if (raycast != null && raycast.blockPos == blockPos && (!targetUpperFace || raycast.sideHit == EnumFacing.UP)) {
                        if (visibleVec == null || rotationDifference(
                                currentVec.rotation, currentRotation
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
                                currentVec.rotation, currentRotation
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
            atan2(posZ, posX).toDegreesF() - 90f, -atan(
                (finalVelocity * finalVelocity - sqrt(
                    finalVelocity * finalVelocity * finalVelocity * finalVelocity - gravityModifier * (gravityModifier * posSqrt * posSqrt + 2 * posY * finalVelocity * finalVelocity)
                )) / (gravityModifier * posSqrt)
            ).toDegreesF()
        )
    }

    /**
     * Drag-aware launch rotation: solves the ballistic arc (gravity plus 0.99 air drag) for the
     * given [launchSpeed] and returns the rotation to fire along, or null when no arc reaches the
     * predicted [target]. Callers fall back to [faceTrajectory] when this returns null.
     */
    fun solveTrajectory(
        target: Entity,
        predict: Boolean,
        predictSize: Float,
        gravity: Double,
        launchSpeed: Double,
    ): Rotation? {
        val player = mc.thePlayer ?: return null
        val lead = if (predict) predictSize.toDouble() else 0.0
        val targetPoint = Vec3(
            target.posX + (target.posX - target.prevPosX) * lead,
            target.entityBoundingBox.minY + (target.entityBoundingBox.minY - target.prevPosY) * lead +
                target.eyeHeight - 0.15,
            target.posZ + (target.posZ - target.prevPosZ) * lead,
        )
        val origin = Vec3(player.posX, player.entityBoundingBox.minY + player.getEyeHeight(), player.posZ)
        val solution = ProjectileSolver(gravity, 0.99).solve(origin, targetPoint, launchSpeed) ?: return null

        val velocity = solution.velocity
        val horizontal = sqrt(velocity.xCoord * velocity.xCoord + velocity.zCoord * velocity.zCoord)
        if (horizontal < 1.0E-6) return null
        return Rotation(
            atan2(-velocity.xCoord, velocity.zCoord).toDegreesF(),
            (-atan2(velocity.yCoord, horizontal)).toDegreesF(),
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
        val eyesPos = fromEntity.eyes.let {
            if (predict) it.addVector(fromEntity.motionX, fromEntity.motionY, fromEntity.motionZ) else it
        }

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
        bb: AxisAlignedBB, distanceBasedSpot: Boolean = false, outborder: Boolean,
        randomization: RandomizationSettings? = null, predict: Boolean,
        lookRange: Float, attackRange: Float, throughWallsRange: Float = 0f,
        bodyPoints: List<String> = listOf("Head", "Feet"), horizontalSearch: ClosedFloatingPointRange<Float> = 0f..1f,
        preferLastPoint: Boolean = false,
    ): Rotation? {
        val scanRange = lookRange.coerceAtLeast(attackRange)

        val max = BodyPoint.fromString(bodyPoints[0]).range.endInclusive
        val min = BodyPoint.fromString(bodyPoints[1]).range.start

        if (outborder) {
            val vec3 = bb.lerpWith(nextDouble(0.5, 1.3), nextDouble(0.9, 1.3), nextDouble(0.5, 1.3))

            return toRotation(vec3, predict).fixedSensitivity()
        }

        val eyes = mc.thePlayer.eyes

        val preferredRotation = lastSearchRotation?.takeIf { preferLastPoint }
            ?: toRotation(getNearestPointBB(eyes, bb), predict).takeIf { distanceBasedSpot }
            ?: currentRotation ?: mc.thePlayer.rotation

        val currRotation = Rotation.ZERO.plus(preferredRotation)

        var attackRotation: Pair<Rotation, Float>? = null
        var lookRotation: Pair<Rotation, Float>? = null

        randomization?.takeIf { it.randomizationChosen }?.run {
            processNextSpot(bb, currRotation, eyes, scanRange.toDouble())
        }

        val (hMin, hMax) = horizontalSearch.start.toDouble() to min(horizontalSearch.endInclusive + 0.01, 1.0)

        for (x in hMin..hMax) {
            for (y in min..max) {
                for (z in hMin..hMax) {
                    val vec = bb.lerpWith(x, y, z)

                    val rotation = toRotation(vec, predict).fixedSensitivity()

                    // Calculate actual hit vec after applying fixed sensitivity to rotation
                    val gcdVec = bb.calculateIntercept(
                        eyes, eyes + getVectorForRotation(rotation) * scanRange.toDouble()
                    )?.hitVec ?: continue

                    val distance = eyes.distanceTo(gcdVec)

                    // Check if vec is in range
                    // Skip if a rotation that is in attack range was already found and the vec is out of attack range
                    if (distance > scanRange || (attackRotation != null && distance > attackRange)) continue

                    // Check if vec is reachable through walls
                    if (!isVisible(gcdVec) && distance > throughWallsRange) continue

                    val rotationWithDiff = rotation to rotationDifference(rotation, currRotation)

                    if (distance <= attackRange) {
                        if (attackRotation == null || rotationWithDiff.second < attackRotation.second) attackRotation =
                            rotationWithDiff
                    } else {
                        if (lookRotation == null || rotationWithDiff.second < lookRotation.second) lookRotation =
                            rotationWithDiff
                    }
                }
            }
        }

        val result = attackRotation?.first ?: lookRotation?.first ?: run {
            val vec = getNearestPointBB(eyes, bb)
            val dist = eyes.distanceTo(vec)

            if (dist <= scanRange && (dist <= throughWallsRange || isVisible(vec))) toRotation(vec, predict)
            else null
        }

        if (preferLastPoint) lastSearchRotation = result

        return result
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
        RotationMath.rotationDifference(a.yaw, a.pitch, b.yaw, b.pitch)

    private fun limitAngleChange(
        currentRotation: Rotation, targetRotation: Rotation, settings: RotationSettings, resetting: Boolean = false
    ): Rotation {
        if (settings.useModernRotations) {
            return ModernRotationEngine.process(currentRotation, targetRotation, settings, resetting)
        }

        val (hSpeed, vSpeed) = if (settings.instant) {
            180f to 180f
        } else settings.horizontalSpeed to settings.verticalSpeed

        return performAngleChange(
            currentRotation,
            targetRotation,
            hSpeed,
            vSpeed,
            !settings.instant && settings.legitimize,
            settings.minRotationDifference,
            settings.minRotationDifferenceResetTiming
        )
    }

    fun performAngleChange(
        currentRotation: Rotation,
        targetRotation: Rotation,
        settings: RotationSettings,
        resetting: Boolean = false,
    ) = limitAngleChange(currentRotation, targetRotation, settings, resetting)

    fun performAngleChange(
        currentRotation: Rotation,
        targetRotation: Rotation,
        hSpeed: Float,
        vSpeed: Float = hSpeed,
        legitimize: Boolean,
        minRotationDiff: Float,
        minRotationDiffResetTiming: String,
    ): Rotation {
        var (yawDiff, pitchDiff) = angleDifferences(targetRotation, currentRotation)

        val rotationDifference = hypot(yawDiff, pitchDiff)

        val isShortStopActive = WaitTickUtils.hasScheduled(this)
        val isNoRotateSetActive = WaitTickUtils.hasScheduled(NoRotateSet)

        if (isNoRotateSetActive) {
            yawDiff = 0F
            pitchDiff = 0F
        } else if (isShortStopActive || activeSettings?.shouldPerformShortStop() == true) {
            if (!isShortStopActive) {
                WaitTickUtils.schedule(activeSettings?.shortStopDuration?.random()?.plus(1) ?: 0, this)
            }

            activeSettings?.resetSimulateShortStopData()

            val yawSlowdown = (0F..0.1F).random()
            val pitchSlowdown = (0F..0.1F).random()

            yawDiff = (yawDiff * yawSlowdown).withGCD()
            pitchDiff = (pitchDiff * pitchSlowdown).withGCD()
        }

        var (straightLineYaw, straightLinePitch) = run {
            var baseYawSpeed = abs(yawDiff safeDiv rotationDifference) * hSpeed
            var basePitchSpeed = abs(pitchDiff safeDiv rotationDifference) * vSpeed

            // Apply imperfect correlation
            if (legitimize) {
                baseYawSpeed *= (0.9F..1.1F).random()
                basePitchSpeed *= (0.9F..1.1F).random()
            }

            baseYawSpeed to basePitchSpeed
        }

        straightLineYaw = yawDiff.coerceIn(-straightLineYaw, straightLineYaw)
        straightLinePitch = pitchDiff.coerceIn(-straightLinePitch, straightLinePitch)

        // Humans usually have some small jitter when moving their mouse from point A to point B.
        // Usually when a rotation axis' difference is prioritized.
        if (rotationDifference > 0F) {
            val yawJitter = (-0.03F..0.03F).random() * straightLineYaw
            val pitchJitter = (-0.02F..0.02F).random() * straightLinePitch

            straightLineYaw += yawJitter
            straightLinePitch += pitchJitter
        }

        val minYaw = nextFloat(min(minRotationDiff, getFixedAngleDelta()), minRotationDiff).withGCD()
        val minPitch = nextFloat(min(minRotationDiff, getFixedAngleDelta()), minRotationDiff).withGCD()

        applySlowDown(straightLineYaw, minYaw, minRotationDiffResetTiming, true, legitimize) {
            straightLineYaw = it
        }

        applySlowDown(straightLinePitch, minPitch, minRotationDiffResetTiming, false, legitimize) {
            straightLinePitch = it
        }

        return currentRotation.plus(Rotation(straightLineYaw, straightLinePitch))
    }

    private fun applySlowDown(
        diff: Float, min: Float, timing: String, yaw: Boolean, applyRealism: Boolean, action: (Float) -> Unit
    ) {
        if (diff == 0f) {
            action(diff)
            return
        }

        val lastTick1 = angleDifferences(serverRotation, lastRotations[1]).let { diffs ->
            if (yaw) diffs.x else diffs.y
        }

        val diffAbs = abs(diff)
        val isSlowingDown = diffAbs <= abs(lastTick1)

        if (diffAbs.withGCD() <= min && (timing == "Always" || timing == "OnSlowDown" && isSlowingDown || timing == "OnStart" && lastTick1 == 0F)) {
            action(0f)
            return
        }

        if (!applyRealism) {
            action(diff)
            return
        }

        val range = when {
            lastTick1 == 0f -> {
                val inc = 0.2f * (diffAbs / 50f).coerceIn(0f, 1f)

                0.1F + inc..0.5F + inc
            }

            else -> 0.3f..0.7f
        }

        val new = (lastTick1..diff).lerpWith(range.random())

        if (abs(new.withGCD()) <= min && isSlowingDown) {
            action(diff)
        } else {
            action(new)
        }
    }

    /**
     * Calculate difference between two angle points
     *
     * @param a angle point
     * @param b angle point
     * @return difference between angle points
     */
    fun angleDifference(a: Float, b: Float) = RotationMath.angleDifference(a, b)

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
        blockReachDistance, rotation.yaw, rotation.pitch
    ) { entity: Entity -> targetEntity == entity } != null

    /**
     * Allows you to check if your enemy is behind a wall
     */
    fun isVisible(vec3: Vec3) = mc.theWorld.rayTraceBlocks(mc.thePlayer.eyes, vec3) == null

    fun isEntityHeightVisible(entity: Entity) = arrayOf(
        entity.hitBox.center.withY(entity.hitBox.maxY), entity.hitBox.center.withY(entity.hitBox.minY)
    ).any { isVisible(it) }

    fun isEntityHeightVisible(entity: TileEntity) = arrayOf(
        entity.renderBoundingBox.center.withY(entity.renderBoundingBox.maxY),
        entity.renderBoundingBox.center.withY(entity.renderBoundingBox.minY)
    ).any { isVisible(it) }

    /**
     * Set your target rotation
     *
     * @param rotation your target rotation
     */
    fun canRequestRotation(options: RotationSettings) =
        requestArbiter.canAcquire(options, options.effectiveRequestPriority)

    fun setTargetRotation(rotation: Rotation, options: RotationSettings, ticks: Int = options.resetTicks): Boolean {
        if (!RotationMath.isValid(rotation.yaw, rotation.pitch)) {
            return false
        }

        if (abs(angleDifference(rotation.yaw, serverRotation.yaw)) > options.maximumRotationDifference) {
            return false
        }

        val requestPriority = options.effectiveRequestPriority

        if (!requestArbiter.canAcquire(options, requestPriority)) {
            return false
        }

        if (!options.applyServerSide) {
            currentRotation?.let {
                mc.thePlayer.rotationYaw = it.yaw
                mc.thePlayer.rotationPitch = it.pitch
            }

            resetRotation()
        }

        requestArbiter.tryAcquire(options, requestPriority)

        targetRotation = rotation.copy()

        val requestedResetTicks = if (options.useModernRotations) options.effectiveResetTicks else ticks
        val resetTicksSupported = if (options.useModernRotations) {
            options.modernTicksUntilResetValue.isSupported()
        } else {
            options.resetTicksValue.isSupported()
        }

        resetTicks = if (!options.applyServerSide || !resetTicksSupported) 1 else requestedResetTicks

        activeSettings = options

        if (options.immediate) {
            update()
        }

        return true
    }

    fun cancelTargetRotation(options: RotationSettings, immediate: Boolean = false): Boolean {
        if (!requestArbiter.release(options)) return false

        targetRotation = null
        resetTicks = 0

        if (immediate || currentRotation == null) {
            resetRotation()
        }

        return true
    }

    fun cancelTargetRotation(owner: Module, immediate: Boolean = false): Boolean {
        val settings = activeSettings?.takeIf { it.moduleOwner === owner } ?: return false

        return cancelTargetRotation(settings, immediate)
    }

    private fun resetRotation(resetHistory: Boolean = false) {
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
        requestArbiter.clear()
        PostRotationExecutor.clear()
        aimTargetEntity = null
        lastSearchRotation = null
        ModernRotationEngine.reset()

        if (resetHistory) {
            val rotation = mc.thePlayer?.rotation ?: Rotation(0f, 0f)
            lastRotations = MutableList(MAX_CAPTURE_TICKS) { rotation.copy() }
            actualServerRotation = rotation.copy()
            modifiedInput = MovementInput()
        }
    }

    /**
     * Called from the network manager the moment a packet is really written to the wire (both the
     * event path and silent sends end up here). Rotating packets advance [actualServerRotation];
     * cancelled or still-queued packets never reach this point.
     */
    fun onPacketDispatched(packet: Packet<*>) {
        if (packet is C03PacketPlayer && packet.rotating) {
            actualServerRotation = packet.rotation
        }
    }

    /**
     * Returns the smallest angle difference possible with a specific sensitivity ("gcd")
     */
    fun getFixedAngleDelta(sensitivity: Float = mc.gameSettings.mouseSensitivity) =
        RotationMath.fixedAngleDelta(sensitivity)

    /**
     * Returns angle that is legitimately accomplishable with player's current sensitivity
     */
    fun getFixedSensitivityAngle(targetAngle: Float, startAngle: Float = 0f, gcd: Float = getFixedAngleDelta()) =
        RotationMath.fixedSensitivityAngle(targetAngle, startAngle, gcd)

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

        return blockPos.block?.collisionRayTrace(
            world, blockPos, eyes, eyes + (getVectorForRotation(rotation) * reach.toDouble())
        )
    }

    fun performRayTrace(blockPos: BlockPos, vec: Vec3, eyes: Vec3 = mc.thePlayer.eyes) =
        mc.theWorld?.let { blockPos.block?.collisionRayTrace(it, blockPos, eyes, vec) }

    fun syncRotations() {
        val player = mc.thePlayer ?: return

        player.prevRotationYaw = player.rotationYaw
        player.prevRotationPitch = player.rotationPitch
        player.renderArmYaw = player.rotationYaw
        player.renderArmPitch = player.rotationPitch
        player.prevRenderArmYaw = player.rotationYaw
        player.prevRenderArmPitch = player.rotationPitch
    }

    private fun update() {
        val settings = activeSettings ?: return
        val player = mc.thePlayer ?: return

        val playerRotation = player.rotation

        val shouldUpdate = !InventoryUtils.serverOpenContainer && !InventoryUtils.serverOpenInventory

        if (!shouldUpdate) {
            return
        }

        val serverRotation = currentRotation ?: serverRotation

        if (resetTicks == 0) {
            if (isDifferenceAcceptableForReset(serverRotation, playerRotation, settings)) {
                resetRotation()
                return
            }

            currentRotation = limitAngleChange(
                serverRotation, playerRotation, settings, resetting = true
            ).fixedSensitivity()
            return
        }

        targetRotation?.let {
            limitAngleChange(serverRotation, it, settings).let { rotation ->
                if (!settings.applyServerSide) {
                    rotation.toPlayer(player)
                } else {
                    currentRotation = rotation.fixedSensitivity()
                    if (settings.useModernRotations && settings.modernMovementCorrection == "ChangeLook") {
                        currentRotation?.toPlayer(player)
                    }
                }
            }
        }

        if (resetTicks > 0) {
            resetTicks--
        }
    }

    private fun isDifferenceAcceptableForReset(
        curr: Rotation, target: Rotation, options: RotationSettings
    ): Boolean {
        if (!options.applyServerSide) return true

        if (options.useModernRotations) {
            return options.modernMovementCorrection == "ChangeLook" ||
                rotationDifference(target, curr) <= options.modernResetThreshold
        }

        if (rotationDifference(target, curr) > options.angleResetDifference) return false

        // We use the last rotation saved 2 ticks ago because we have not updated the currentRotation yet.
        val diffs = angleDifferences(target, curr).abs
        val lastTickDiffs = angleDifferences(curr, lastRotations[1]).abs

        return diffs.x <= lastTickDiffs.x && diffs.y <= lastTickDiffs.y || !options.legitimize
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
        if (current == target) return true

        val smallestAnglePossible = getFixedAngleDelta()

        return rotationDifference(target, current).withGCD() > smallestAnglePossible * multiplier
    }

    val onWorld = handler<WorldEvent>(always = true) {
        resetRotation(resetHistory = true)
    }

    /**
     * Handle rotation update
     */
    val onRotationUpdate = handler<RotationUpdateEvent>(priority = -1) {
        // Backstop so an abandoned lease can never block other modules forever, plus drain any
        // post-rotation tasks scheduled for the next tick.
        requestArbiter.tick(ARBITER_MAX_IDLE_TICKS)
        PostRotationExecutor.tick()

        activeSettings?.let {
            // Was the rotation update immediate? Allow updates the next tick.
            if (it.immediate) {
                it.immediate = false
                return@handler
            }
        }

        update()
    }

    /**
     * Handle strafing
     */
    val onStrafe = handler<StrafeEvent> { event ->
        val data = activeSettings ?: return@handler

        if (data.useModernRotations) {
            val strict = when (data.modernMovementCorrection) {
                "Strict" -> true
                "Silent" -> false
                else -> return@handler
            }

            currentRotation?.let {
                it.applyStrafeToPlayer(event, strict)
                event.cancelEvent()
            }
            return@handler
        }

        if (!data.strafe) {
            return@handler
        }

        currentRotation?.let {
            it.applyStrafeToPlayer(event, data.strict)
            event.cancelEvent()
        }
    }

    /**
     * Mouse correction path applied when the Modern engine runs ChangeLook movement correction.
     */
    val onRotationSet = handler<RotationSetEvent> { event ->
        val data = activeSettings ?: return@handler

        if (!data.useModernRotations || data.modernMovementCorrection != "ChangeLook") {
            return@handler
        }

        val mouseDelta = Rotation(event.yawDiff, -event.pitchDiff)

        currentRotation = currentRotation?.plus(mouseDelta)?.withLimitedPitch()
        targetRotation = targetRotation?.plus(mouseDelta)?.withLimitedPitch()
    }

    /**
     * Handle rotation-packet modification
     */
    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        if (packet !is C03PacketPlayer) {
            return@handler
        }

        val forceQueuedRotation = currentRotation != null && PostRotationExecutor.hasPostMoveTasks

        if (!packet.rotating && !forceQueuedRotation) {
            activeSettings?.resetSimulateShortStopData()
            return@handler
        }

        // An idle C03 normally omits yaw/pitch. A queued post-move action still needs the server to
        // process the requested rotation first, so promote that packet to its rotating form.
        if (forceQueuedRotation) {
            packet.rotating = true
        }

        currentRotation?.let {
            packet.rotation = it
            // The network tail hook releases post-move actions only after this packet actually sends.
            PostRotationExecutor.markRotationPacket(packet)
        }

        val diffs = angleDifferences(packet.rotation, serverRotation)

        if (Rotations.shouldPrintDebug() && currentRotation != null) {
            chat("PREV YAW: ${diffs.x}, PREV PITCH: ${diffs.y}")
        }

        activeSettings?.updateSimulateShortStopData(diffs.x)
    }

    enum class BodyPoint(val rank: Int, val range: ClosedFloatingPointRange<Double>, val displayName: String) {
        HEAD(1, 0.75..0.9, "Head"), BODY(0, 0.5..0.75, "Body"), FEET(-1, 0.1..0.4, "Feet"), UNKNOWN(
            -2, 0.0..0.0, "Unknown"
        );

        companion object {
            fun fromString(point: String): BodyPoint {
                return entries.find { it.name.equals(point, ignoreCase = true) } ?: UNKNOWN
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

    fun calculateYawFromSrcToDst(yaw: Float, srcX: Double, srcZ: Double, dstX: Double, dstZ: Double): Float {
        val xDist = dstX - srcX
        val zDist = dstZ - srcZ
        val var1 = (StrictMath.atan2(zDist, xDist) * 180.0 / Math.PI).toFloat() - 90.0f
        return yaw + MathHelper.wrapAngleTo180_float(var1 - yaw)
    }

    /**
     * Gets rotations entity.
     *
     * @param entity the entity
     * @return the rotations entity
     */
    fun getRotationsEntity(entity: EntityLivingBase): Rotation {
        return getRotations(entity.posX, entity.posY + entity.eyeHeight - 0.4, entity.posZ)
    }

    /**
     * Gets rotations.
     *
     * @param posX the pos x
     * @param posY the pos y
     * @param posZ the pos z
     * @return the rotations
     */
    fun getRotations(posX: Double, posY: Double, posZ: Double): Rotation {
        val player = mc.thePlayer
        val x = posX - player.posX
        val y = posY - (player.posY + player.getEyeHeight().toDouble())
        val z = posZ - player.posZ
        val dist = MathHelper.sqrt_double(x * x + z * z).toDouble()
        val yaw = (atan2(z, x) * 180.0 / 3.141592653589793).toFloat() - 90.0f
        val pitch = (-(atan2(y, dist) * 180.0 / 3.141592653589793)).toFloat()
        return Rotation(yaw, pitch)
    }
}
