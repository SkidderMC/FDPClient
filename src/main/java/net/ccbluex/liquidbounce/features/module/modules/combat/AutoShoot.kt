/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.sendUseItem
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.faceTrajectory
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.rotationDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemEgg
import net.minecraft.item.ItemSnowball
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

/**
 * Automatically charges and releases a held bow (or throws a held snowball/egg) at the
 * nearest valid target. Reuses the existing target search, projectile-trajectory aiming
 * and item-use helpers; it only acts on whatever is already in hand and never switches slots.
 */
object AutoShoot : Module("AutoShoot", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT, gameDetecting = false) {

    private val range by float("Range", 30F, 5F..60F)
        .describe("Max distance to target an enemy.")
    private val throughWalls by boolean("ThroughWalls", false)
        .describe("Allow shooting at targets behind walls.")
    private val throughWallsRange by float("ThroughWallsRange", 12F, 0F..60F) { throughWalls }
        .describe("Max distance to shoot through walls.")

    private val priority by choices("Priority", arrayOf("Distance", "Direction", "Health"), "Distance")
        .describe("How to pick the best target.")

    // Bow handling
    private val shootBow by boolean("Bow", true)
        .describe("Charge and fire a held bow.")
    private val charge by int("Charge", 18, 3..20) { shootBow }
        .describe("Ticks to charge the bow before firing.")
    private val predict by boolean("Predict", true) { shootBow }
        .describe("Lead the shot based on target movement.")
    private val predictSize by float("PredictSize", 2F, 0.1F..5F) { shootBow && predict }
        .describe("Strength of the movement prediction.")

    // Throwable handling
    private val shootThrowable by boolean("Throwable", false)
        .describe("Throw a held snowball or egg.")
    private val throwDelay by int("ThrowDelay", 250, 0..2000) { shootThrowable }
        .describe("Delay between throwable throws.")

    private val aimOffThreshold by float("AimOffThreshold", 4F, 0.5F..20F)
        .describe("Max aim error allowed before releasing a shot.")

    private val dragCorrection by boolean("DragCorrection", false)
        .describe("Solve the launch angle with per-tick air drag for better long-range accuracy.")

    private val requiresKillAura by boolean("RequiresKillAura", false)
        .describe("Only shoot while KillAura has a target.")
    private val notDuringCombat by boolean("NotDuringCombat", false)
        .describe("Hold fire for a moment after taking damage.")
    private val combatTimeout by int("CombatTimeout", 500, 0..3000) { notDuringCombat }
        .describe("Milliseconds after taking damage before shooting resumes.")

    private val options = RotationSettings(this).withRequestPriority(RotationPriority.HIGH)

    private val targetingGroup = Configurable("Targeting")
    private val bowGroup = Configurable("Bow")
    private val throwableGroup = Configurable("Throwable")
    private val aimGroup = Configurable("Aim")

    init {
        moveValues(targetingGroup, "Range", "ThroughWalls", "ThroughWallsRange", "Priority",
            "RequiresKillAura", "NotDuringCombat", "CombatTimeout")
        moveValues(bowGroup, "Bow", "Charge", "Predict", "PredictSize")
        moveValues(throwableGroup, "Throwable", "ThrowDelay")

        options.nestInto(aimGroup)
        moveValues(aimGroup, "AimOffThreshold", "DragCorrection")

        addValues(listOf(targetingGroup, bowGroup, throwableGroup, aimGroup))
    }
    private val throwTimer = MSTimer()
    private val combatTimer = MSTimer()

    private var target: Entity? = null

    override fun onDisable() {
        target = null
        throwTimer.reset()
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (player.hurtTime > 0) combatTimer.reset()
        if (requiresKillAura && !(KillAura.handleEvents() && KillAura.target != null)) return@handler
        if (notDuringCombat && !combatTimer.hasTimePassed(combatTimeout.toLong())) return@handler

        target = null

        val stack = player.heldItem ?: return@handler

        when (stack.item) {
            is ItemBow -> {
                if (!shootBow) return@handler

                val foundTarget = findTarget() ?: return@handler
                target = foundTarget

                aimAt(aimRotationFor(foundTarget))

                // Begin charging the bow if we are not already drawing it.
                if (!player.isUsingItem) {
                    player.sendUseItem(stack)
                    return@handler
                }

                // Release the arrow once charged enough and we are aiming at the target.
                if (player.itemInUseDuration >= charge && isAimingAt(foundTarget)) {
                    player.stopUsingItem()
                    sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                }
            }

            is ItemSnowball, is ItemEgg -> {
                if (!shootThrowable) return@handler

                val foundTarget = findTarget() ?: return@handler
                target = foundTarget

                aimAt(aimRotationFor(foundTarget))

                if (throwTimer.hasTimePassed(throwDelay) && isAimingAt(foundTarget)) {
                    if (player.sendUseItem(stack)) {
                        throwTimer.reset()
                    }
                }
            }

            else -> return@handler
        }
    }

    private fun aimRotationFor(target: Entity): Rotation =
        when (mc.thePlayer?.heldItem?.item) {
            is ItemSnowball, is ItemEgg -> aimRotation(target, gravity = 0.03f, launchSpeed = 1.5, fallbackVelocity = 1.5f)
            else -> aimRotation(target, gravity = 0.05f, launchSpeed = bowLaunchSpeed(), fallbackVelocity = null)
        }

    private fun aimRotation(target: Entity, gravity: Float, launchSpeed: Double, fallbackVelocity: Float?): Rotation {
        if (dragCorrection) {
            solvedRotation(target, gravity.toDouble(), launchSpeed)?.let { return it }
        }
        return faceTrajectory(target, predict, predictSize, gravity = gravity, velocity = fallbackVelocity)
    }

    private fun solvedRotation(target: Entity, gravity: Double, launchSpeed: Double): Rotation? =
        RotationUtils.solveTrajectory(target, predict, predictSize, gravity, launchSpeed)

    /** Vanilla arrow launch speed for the configured release charge (power * 3.0). */
    private fun bowLaunchSpeed(): Double {
        val t = charge / 20f
        val power = ((t * t + t * 2f) / 3f).coerceAtMost(1f)
        return power * 3.0
    }

    private fun aimAt(rotation: Rotation) {
        setTargetRotation(rotation, options = options)
    }

    private fun isAimingAt(target: Entity): Boolean {
        val current = RotationUtils.currentRotation ?: RotationUtils.serverRotation
        return rotationDifference(aimRotationFor(target), current) <= aimOffThreshold
    }

    private fun findTarget(): Entity? {
        val player = mc.thePlayer ?: return null

        return mc.theWorld.loadedEntityList
            .asSequence()
            .filterIsInstance<EntityLivingBase>()
            .filter {
                val distance = player.getDistanceToEntityBox(it)

                isSelected(it, true) && distance <= range &&
                    (throughWalls || player.canEntityBeSeen(it) && distance <= throughWallsRange)
            }.minByOrNull { entity ->
                when (priority.uppercase()) {
                    "DISTANCE" -> player.getDistanceToEntityBox(entity)
                    "DIRECTION" -> rotationDifference(entity).toDouble()
                    "HEALTH" -> entity.health.toDouble()
                    else -> 0.0
                }
            }
    }
}
