/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeCam
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.serverRotation
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue

object Rotations : Module("Rotations", Category.CLIENT, gameDetecting = false, hideModule = false) {

    private val realistic by BoolValue("Realistic", true)
    private val body by BoolValue("Body", true) { !realistic }

    private val smoothRotations by BoolValue("SmoothRotations", false)
    private val smoothingFactor by FloatValue("SmoothFactor", 0.15f, 0.1f..0.9f) { smoothRotations }

    val ghost by BoolValue("Ghost", true)

    val colorRedValue by IntegerValue("R", 0, 0..255) { ghost }
    val colorGreenValue by IntegerValue("G", 160, 0..255) { ghost }
    val colorBlueValue by IntegerValue("B", 255, 0..255) { ghost }
    val alphaValue by IntegerValue("Alpha", 255, 0..255) { ghost }
    val rainbow by BoolValue("RainBow", false) { ghost }

    val debugRotations by BoolValue("DebugRotations", false)

    var prevHeadPitch = 0f
    var headPitch = 0f

    private var lastRotation: Rotation? = null

    private val specialCases
        get() = arrayListOf(FreeCam.shouldDisableRotations()).any { it }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState != EventState.POST)
            return

        val thePlayer = mc.thePlayer ?: return
        val targetRotation = getRotation() ?: serverRotation

        prevHeadPitch = headPitch
        headPitch = targetRotation.pitch

        thePlayer.rotationYawHead = targetRotation.yaw
        if (shouldRotate() && body && !realistic) {
            thePlayer.renderYawOffset = thePlayer.rotationYawHead
        }

        lastRotation = targetRotation
    }

    fun lerp(tickDelta: Float, old: Float, new: Float): Float {
        return old + (new - old) * tickDelta
    }

    /**
     * Rotate when current rotation is not null.
     */
    fun shouldRotate() = state && (specialCases || currentRotation != null)

    /**
     * Smooth out rotations between two points
     */
    private fun smoothRotation(from: Rotation, to: Rotation): Rotation {
        val diffYaw = to.yaw - from.yaw
        val diffPitch = to.pitch - from.pitch

        val smoothedYaw = from.yaw + diffYaw * smoothingFactor
        val smoothedPitch = from.pitch + diffPitch * smoothingFactor

        return Rotation(smoothedYaw, smoothedPitch)
    }

    /**
     * Imitate the game's head and body rotation logic
     */
    fun shouldUseRealisticMode() = realistic && shouldRotate()

    /**
     * Which rotation should the module use?
     */
    fun getRotation(): Rotation? {
        val currRotation = if (specialCases) serverRotation else currentRotation

        return if (smoothRotations && currRotation != null) {
            smoothRotation(lastRotation ?: return currRotation, currRotation)
        } else {
            currRotation
        }
    }
}