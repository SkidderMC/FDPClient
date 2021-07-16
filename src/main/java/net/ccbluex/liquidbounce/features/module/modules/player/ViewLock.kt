package net.ccbluex.liquidbounce.features.module.modules.player

import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign


internal object ViewLock : Module(
    name = "ViewLock",
    alias = arrayOf("YawLock", "PitchLock"),
    category = Category.PLAYER,
    description = "Locks your camera view"
) {
    private val page by setting("Page", Page.YAW)

    private val yaw by setting("Yaw", true, { page == Page.YAW })
    private val autoYaw = setting("Auto Yaw", true, { page == Page.YAW && yaw })
    private val disableMouseYaw by setting("Disable Mouse Yaw", false, { page == Page.YAW && yaw && yaw })
    private val specificYaw by setting("Specific Yaw", 180.0f, -180.0f..180.0f, 1.0f, { page == Page.YAW && !autoYaw.value && yaw })
    private val yawSlice = setting("Yaw Slice", 8, 2..32, 1, { page == Page.YAW && autoYaw.value && yaw })

    private val pitch by setting("Pitch", true, { page == Page.PITCH })
    private val autoPitch = setting("Auto Pitch", true, { page == Page.PITCH && pitch })
    private val disableMousePitch by setting("Disable Mouse Pitch", false, { page == Page.PITCH && pitch && pitch })
    private val specificPitch by setting("Specific Pitch", 0.0f, -90.0f..90.0f, 1.0f, { page == Page.PITCH && !autoPitch.value && pitch })
    private val pitchSlice = setting("Pitch Slice", 5, 2..32, 1, { page == Page.PITCH && autoPitch.value && pitch })

    private enum class Page {
        YAW, PITCH
    }

    private var yawSnap = 0
    private var pitchSnap = 0
    private val deltaXQueue = ArrayDeque<Pair<Float, Long>>()
    private val deltaYQueue = ArrayDeque<Pair<Float, Long>>()
    private var pitchSliceAngle = 1.0f
    private var yawSliceAngle = 1.0f

    init {
        onEnable {
            yawSliceAngle = 360.0f / yawSlice.value
            pitchSliceAngle = 180.0f / (pitchSlice.value - 1)
            if (autoYaw.value || autoPitch.value) snapToNext()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener

            if (autoYaw.value || autoPitch.value) {
                snapToSlice()
            }

            if (yaw && !autoYaw.value) {
                player.rotationYaw = specificYaw
            }

            if (pitch && !autoPitch.value) {
                player.rotationPitch = specificPitch
            }
        }
    }

    @JvmStatic
    fun handleTurn(entity: Entity, deltaX: Float, deltaY: Float, ci: CallbackInfo) {
        if (isDisabled) return
        val player = mc.player ?: return
        if (entity != player) return

        val multipliedX = deltaX * 0.15f
        val multipliedY = deltaY * -0.15f

        val yawChange = if (yaw && autoYaw.value) handleDelta(multipliedX, deltaXQueue, yawSliceAngle) else 0
        val pitchChange = if (pitch && autoPitch.value) handleDelta(multipliedY, deltaYQueue, pitchSliceAngle) else 0

        turn(player, multipliedX, multipliedY)
        changeDirection(yawChange, pitchChange)
        player.ridingEntity?.applyOrientationToEntity(player)

        ci.cancel()
    }

    private fun turn(player: EntityPlayerSP, deltaX: Float, deltaY: Float) {
        if (!yaw || !disableMouseYaw) {
            player.prevRotationYaw += deltaX
            player.rotationYaw += deltaX
        }

        if (!pitch || !disableMousePitch) {
            player.prevRotationPitch += deltaY
            player.rotationPitch += deltaY
            player.rotationPitch = player.rotationPitch.coerceIn(-90.0f, 90.0f)
        }
    }

    private fun handleDelta(delta: Float, list: ArrayDeque<Pair<Float, Long>>, slice: Float): Int {
        val currentTime = System.currentTimeMillis()
        list.add(Pair(delta, currentTime))

        val sum = list.sumByDouble { it.first.toDouble() }.toFloat()
        return if (abs(sum) > slice) {
            list.clear()
            sign(sum).toInt()
        } else {
            while (list.first().second < currentTime - 500) {
                list.removeFirstOrNull()
            }
            0
        }
    }

    private fun changeDirection(yawChange: Int, pitchChange: Int) {
        yawSnap = Math.floorMod(yawSnap + yawChange, yawSlice.value)
        pitchSnap = (pitchSnap + pitchChange).coerceIn(0, pitchSlice.value - 1)
        snapToSlice()
    }

    private fun snapToNext() {
        mc.player?.let {
            yawSnap = (it.rotationYaw / yawSliceAngle).roundToInt()
            pitchSnap = ((it.rotationPitch + 90.0f) / pitchSliceAngle).roundToInt()
            snapToSlice()
        }
    }

    private fun snapToSlice() {
        mc.player?.let { player ->
            if (yaw && autoYaw.value) {
                player.rotationYaw = (yawSnap * yawSliceAngle).coerceIn(0f, 360f)
                player.ridingEntity?.let { it.rotationYaw = player.rotationYaw }
            }
            if (pitch && autoPitch.value) {
                player.rotationPitch = (pitchSnap * pitchSliceAngle - 90).coerceIn(-90f, 90f)
            }
        }
    }

    init {
        yawSlice.listeners.add {
            yawSliceAngle = 360.0f / yawSlice.value
            if (isEnabled && autoYaw.value) snapToNext()
        }

        pitchSlice.listeners.add {
            pitchSliceAngle = 180.0f / (pitchSlice.value - 1)
            if (isEnabled && autoPitch.value) snapToNext()
        }

        with({ _: Boolean, it: Boolean -> if (isEnabled && it) snapToNext() }) {
            autoPitch.valueListeners.add(this)
            autoYaw.valueListeners.add(this)
        }
    }
}
