/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * FastAccel module - reach top movement speed faster than vanilla.
 *
 * Uses Minecraft's ground/air movement physics (reference: SimulatedPlayer) to compute
 * the player's theoretical maximum (equilibrium) speed and interpolates toward it.
 *
 * Boost: 0% = vanilla acceleration, 100% = instant top speed.
 * Does NOT increase maximum speed — only how fast you reach it.
 *
 * The equilibrium speed formula derived from vanilla tick loop:
 *   v_new = (v_old + acceleration) * drag
 *   v_eq  = acceleration * drag / (1 - drag)
 *
 * By boosting v_old toward v_eq before moveFlying adds acceleration, the resulting
 * (v_eq + acceleration) * drag = v_eq — so there is zero overshoot.
 *
 * NOTE: This module makes Illegitimate movement, which is standardly easy to get detected.
 *
 * @author itsakc-me
 */
object FastAccel : Module("FastAccel", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN, gameDetecting = false) {

    // Boost percentage: 0 = vanilla, 100 = instant top speed
    private val boost by float("Boost", 50f, 0f..100f)

    // Conditions
    private val notDamaged by boolean("NotDamaged", false)

    /**
     * Fires inside moveFlying, before vanilla acceleration is applied.
     * We boost motionX/Z toward the equilibrium speed so the player
     * reaches top speed faster without exceeding it.
     */
    val onStrafe = handler<StrafeEvent> {
        val player = mc.thePlayer ?: return@handler

        if (!player.isMoving) return@handler
        if (player.isInLiquid || player.isInWeb || player.isOnLadder) return@handler
        if (player.capabilities.isFlying) return@handler

        // Check conditions
        if (notDamaged && player.hurtTime > 0) return@handler

        val currentSpeed = sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ)
        val maxSpeed = calculateMaxSpeed()

        if (maxSpeed <= 0.0 || currentSpeed >= maxSpeed) return@handler

        val boostFactor = boost / 100.0

        if (currentSpeed < 0.001) {
            // Starting from standstill — set motion in movement input direction
            val dir = MovementUtils.direction
            val targetSpeed = maxSpeed * boostFactor
            player.motionX = -sin(dir) * targetSpeed
            player.motionZ = cos(dir) * targetSpeed
        } else {
            // Scale existing motion toward max speed, preserving current direction
            val targetSpeed = currentSpeed + (maxSpeed - currentSpeed) * boostFactor
            val scale = targetSpeed / currentSpeed
            player.motionX *= scale
            player.motionZ *= scale
        }
    }

    /**
     * Computes the theoretical maximum (equilibrium) horizontal speed
     * for the player's current movement state.
     *
     * Derived from SimulatedPlayer.livingEntitySideMoveEntityWithHeading:
     *   Ground: drag = blockSlipperiness * 0.91
     *           accel = moveSpeed * (0.16277136 / drag^3)
     *   Air:    drag = 0.91
     *           accel = jumpMovementFactor (0.02, +0.006 if sprinting)
     *
     *   v_equilibrium = accel * drag / (1 - drag)
     */
    private fun calculateMaxSpeed(): Double {
        val player = mc.thePlayer ?: return 0.0

        return if (player.onGround) {
            val blockPos = BlockPos(
                MathHelper.floor_double(player.posX),
                MathHelper.floor_double(player.entityBoundingBox.minY) - 1,
                MathHelper.floor_double(player.posZ)
            )
            val slipperiness = player.worldObj.getBlockState(blockPos).block.slipperiness
            val drag = slipperiness * 0.91f
            val accelFactor = 0.16277136f / (drag * drag * drag)

            // Attribute value includes sprint modifier + potion effects
            val moveSpeed = player.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
                .attributeValue.toFloat()

            val acceleration = moveSpeed * accelFactor
            (acceleration * drag / (1.0f - drag)).toDouble()
        } else {
            // Air physics — jumpMovementFactor
            var jmf = 0.02f
            if (player.isSprinting) {
                jmf += 0.02f * 0.3f
            }
            val drag = 0.91f
            (jmf * drag / (1.0f - drag)).toDouble()
        }
    }
}
