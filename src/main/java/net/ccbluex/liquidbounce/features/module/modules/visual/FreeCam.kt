/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.config.FloatValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3

object FreeCam : Module("FreeCam", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val speed by FloatValue("Speed", 0.8f, 0.1f..2f)

    private val allowCameraInteract by boolean("AllowCameraInteract", true)
        .describe("Let the free camera position interact with blocks.")
    private val allowRotationChange by boolean("AllowRotationChange", true)
        .describe("Allow your rotation to change while in free camera.")

    private val keepSneaking by boolean("KeepSneaking", false)
        .describe("Keep your real player sneaking while flying around.")
    private val lookAt by boolean("LookAt", false)
        .describe("Make your real player look toward the camera.")

    data class PositionPair(var pos: Vec3, var lastPos: Vec3, var extraPos: Vec3 = lastPos) {
        operator fun plusAssign(velocity: Vec3) {
            extraPos = pos
            lastPos = pos
            pos += velocity
        }

        fun interpolate(tickDelta: Float) = Vec3(
            lastPos.xCoord + (pos.xCoord - lastPos.xCoord) * tickDelta,
            lastPos.yCoord + (pos.yCoord - lastPos.yCoord) * tickDelta,
            lastPos.zCoord + (pos.zCoord - lastPos.zCoord) * tickDelta
        )

    }

    override fun onEnable() {
        updatePosition(Vec3_ZERO)
    }

    override fun onDisable() {
        pos = null
        originalPos = null
    }

    val onInputEvent = handler<MovementInputEvent> { event ->
        val speed = this.speed.toDouble()

        val yAxisMovement = when {
            event.originalInput.jump -> 1.0f
            event.originalInput.sneak -> -1.0f
            else -> 0.0f
        }

        val velocity = Vec3_ZERO.apply {
            strafe(speed = speed, moveCheck = !event.originalInput.isMoving)

            this.yCoord = yAxisMovement * speed
        }

        updatePosition(velocity)

        event.originalInput.reset()

        if (keepSneaking) {
            event.originalInput.sneak = true
        }

        if (lookAt) {
            lookRealPlayerAtCamera()
        }
    }

    private fun lookRealPlayerAtCamera() {
        val player = mc.thePlayer ?: return
        val cameraPos = pos?.interpolate(1f) ?: return

        val diff = cameraPos.subtract(player.eyes)
        val dist = Math.sqrt(diff.xCoord * diff.xCoord + diff.zCoord * diff.zCoord)
        if (dist < 1.0E-4) return

        val yaw = (Math.toDegrees(Math.atan2(diff.zCoord, diff.xCoord)) - 90.0).toFloat()
        val pitch = (-Math.toDegrees(Math.atan2(diff.yCoord, dist))).toFloat()

        player.rotationYaw = yaw
        player.rotationPitch = pitch.coerceIn(-90f, 90f)
    }

    private var originalPos: PositionPair? = null
    private var pos: PositionPair? = null

    private fun updatePosition(velocity: Vec3) {
        val player = mc.thePlayer ?: return

        pos = (pos ?: PositionPair(player.currPos, player.currPos)).apply { this += velocity }
    }

    fun useModifiedPosition() {
        val player = mc.thePlayer ?: return

        originalPos = PositionPair(player.currPos, player.prevPos, player.lastTickPos)

        val event = CameraPositionEvent(player.currPos, player.prevPos, player.lastTickPos)
        EventManager.call(event)

        event.result?.run {
            player.setPosAndPrevPos(pos, lastPos, extraPos)
            return
        }

        val data = pos ?: return

        player.setPosAndPrevPos(data.pos, data.lastPos, data.extraPos)
    }

    fun restoreOriginalPosition() {
        val player = mc.thePlayer ?: return

        originalPos?.run { player.setPosAndPrevPos(pos, lastPos, extraPos) }
    }

    fun renderPlayerFromAllPerspectives(entity: EntityLivingBase) =
        handleEvents() && entity == mc.thePlayer || entity.isPlayerSleeping

    fun modifyRaycast(original: Vec3, entity: Entity, tickDelta: Float): Vec3 {
        if (!handleEvents() || entity != mc.thePlayer || !allowCameraInteract) {
            return original
        }

        return pos?.interpolate(tickDelta)?.apply { yCoord += entity.eyeHeight } ?: original
    }

    fun shouldDisableRotations() = handleEvents() && !allowRotationChange


    val onWorldChange = handler<WorldEvent> {
        // Disable when world changed
        state = false
    }

}