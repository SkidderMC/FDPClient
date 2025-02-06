/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.Reach
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isFaced
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.performAngleChange
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.rotationDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.searchCenter
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.simulation.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.Entity
import java.util.*
import kotlin.math.atan

object Aimbot : Module("Aimbot", Category.COMBAT) {

    private val range by float("Range", 4.4F, 1F..8F)
    private val horizontalAim by boolean("HorizontalAim", true)
    private val verticalAim by boolean("VerticalAim", true)
    private val legitimize by boolean("Legitimize", true) { horizontalAim || verticalAim }
    private val maxAngleChange by float("MaxAngleChange", 10f, 1F..180F) { horizontalAim || verticalAim }
    private val inViewMaxAngleChange by float("InViewMaxAngleChange", 35f, 1f..180f) { horizontalAim || verticalAim }
    private val generateSpotBasedOnDistance by boolean(
        "GenerateSpotBasedOnDistance", false
    ) { horizontalAim || verticalAim }
    private val predictClientMovement by int("PredictClientMovement", 2, 0..5)
    private val predictEnemyPosition by float("PredictEnemyPosition", 1.5f, -1f..2f)

    private val highestBodyPointToTargetValue = choices(
        "HighestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Head"
    ) {
        verticalAim
    }.onChange { _, new ->
        val newPoint = RotationUtils.BodyPoint.fromString(new)
        val lowestPoint = RotationUtils.BodyPoint.fromString(lowestBodyPointToTarget)
        val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, lowestPoint, RotationUtils.BodyPoint.HEAD)
        coercedPoint.displayName
    }

    private val highestBodyPointToTarget: String by highestBodyPointToTargetValue

    private val lowestBodyPointToTargetValue = choices(
        "LowestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Feet"
    ) {
        verticalAim
    }.onChange { _, new ->
        val newPoint = RotationUtils.BodyPoint.fromString(new)
        val highestPoint = RotationUtils.BodyPoint.fromString(highestBodyPointToTarget)
        val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, RotationUtils.BodyPoint.FEET, highestPoint)
        coercedPoint.displayName
    }

    private val lowestBodyPointToTarget: String by lowestBodyPointToTargetValue

    private val horizontalBodySearchRange by floatRange("HorizontalBodySearchRange", 0f..1f, 0f..1f) { horizontalAim }

    private val minRotationDifference by float("MinRotationDifference", 0f, 0f..2f) { verticalAim || horizontalAim }
    private val minRotationDifferenceResetTiming by choices(
        "MinRotationDifferenceResetTiming", arrayOf("OnStart", "Always"), "OnStart"
    ) { verticalAim || horizontalAim }

    private val fov by float("FOV", 180F, 1F..180F)
    private val lock by boolean("Lock", true) { horizontalAim || verticalAim }
    private val onClick by boolean("OnClick", false) { horizontalAim || verticalAim }
    private val jitter by boolean("Jitter", false)
    private val yawJitterMultiplier by float("JitterYawMultiplier", 1f, 0.1f..2.5f)
    private val pitchJitterMultiplier by float("JitterPitchMultiplier", 1f, 0.1f..2.5f)
    private val center by boolean("Center", false)
    private val headLock by boolean("Headlock", false) { center && lock }
    private val headLockBlockHeight by float("HeadBlockHeight", -1f, -2f..0f) { headLock && center && lock }
    private val breakBlocks by boolean("BreakBlocks", true)

    private val clickTimer = MSTimer()

    val onMotion = handler<MotionEvent> { event ->
        if (event.eventState != EventState.POST) return@handler

        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        // Clicking delay
        if (mc.gameSettings.keyBindAttack.isKeyDown) clickTimer.reset()

        if (onClick && (clickTimer.hasTimePassed(150) || !mc.gameSettings.keyBindAttack.isKeyDown && AutoClicker.handleEvents())) {
            return@handler
        }

        // Search for the best enemy to target
        val entity = world.loadedEntityList.filter {
            Backtrack.runWithNearestTrackedDistance(it) {
                isSelected(
                    it,
                    true
                ) && player.canEntityBeSeen(it) && player.getDistanceToEntityBox(it) <= range && rotationDifference(it) <= fov
            }
        }.minByOrNull { player.getDistanceToEntityBox(it) } ?: return@handler

        // Should it always keep trying to lock on the enemy or just try to assist you?
        if (!lock && isFaced(entity, range.toDouble())) return@handler

        val random = Random()

        if (Backtrack.runWithNearestTrackedDistance(entity) { !findRotation(entity, random) }) return@handler

        // Jitter
        // Some players do jitter on their mouses causing them to shake around. This is trying to simulate this behavior.
        if (jitter) {
            if (random.nextBoolean()) {
                player.fixedSensitivityYaw += ((random.nextGaussian() - 0.5f) * yawJitterMultiplier).toFloat()
            }

            if (random.nextBoolean()) {
                player.fixedSensitivityPitch += ((random.nextGaussian() - 0.5f) * pitchJitterMultiplier).toFloat()
            }
        }
    }

    private fun findRotation(entity: Entity, random: Random): Boolean {
        val player = mc.thePlayer ?: return false

        if (mc.playerController.isHittingBlock && breakBlocks) {
            return false
        }

        val prediction = entity.currPos.subtract(entity.prevPos).times(2 + predictEnemyPosition.toDouble())

        val boundingBox = entity.hitBox.offset(prediction)
        val (currPos, oldPos) = player.currPos to player.prevPos

        val simPlayer = SimulatedPlayer.fromClientPlayer(RotationUtils.modifiedInput)

        simPlayer.rotationYaw = (currentRotation ?: player.rotation).yaw

        repeat(predictClientMovement) {
            simPlayer.tick()
        }

        player.setPosAndPrevPos(simPlayer.pos)

        val playerRotation = player.rotation

        val destinationRotation = if (center) {
            toRotation(boundingBox.center, true)
        } else {
            searchCenter(
                boundingBox,
                generateSpotBasedOnDistance,
                outborder = false,
                predict = true,
                lookRange = range,
                attackRange = if (Reach.handleEvents()) Reach.combatReach else 3f,
                bodyPoints = listOf(highestBodyPointToTarget, lowestBodyPointToTarget),
                horizontalSearch = horizontalBodySearchRange
            )
        }

        if (destinationRotation == null) {
            player.setPosAndPrevPos(currPos, oldPos)
            return false
        }

        // look headLockBlockHeight higher
        if (headLock && center && lock) {
            val distance = player.getDistanceToEntityBox(entity)
            val playerEyeHeight = player.eyeHeight
            val blockHeight = headLockBlockHeight

            // Calculate the pitch offset needed to shift the view one block up
            val pitchOffset = Math.toDegrees(atan((blockHeight + playerEyeHeight) / distance)).toFloat()

            destinationRotation.pitch -= pitchOffset
        }

        // Figure out the best turn speed suitable for the distance and configured turn speed
        val rotationDiff = rotationDifference(playerRotation, destinationRotation)

        // is enemy visible to player on screen. Fov is about to be right with that you can actually see on the screen. Still not 100% accurate, but it is fast check.
        val supposedTurnSpeed = if (rotationDiff < mc.gameSettings.fovSetting) {
            inViewMaxAngleChange
        } else {
            maxAngleChange
        }

        val gaussian = random.nextGaussian()

        val realisticTurnSpeed = rotationDiff * ((supposedTurnSpeed + (gaussian - 0.5)) / 180)

        // Directly access performAngleChange since this module does not use RotationSettings
        val rotation = performAngleChange(
            player.rotation,
            destinationRotation,
            realisticTurnSpeed.toFloat(),
            legitimize = legitimize,
            minRotationDiff = minRotationDifference,
            minRotationDiffResetTiming = minRotationDifferenceResetTiming,
        )

        rotation.toPlayer(player, horizontalAim, verticalAim)

        player.setPosAndPrevPos(currPos, oldPos)

        return true
    }
}