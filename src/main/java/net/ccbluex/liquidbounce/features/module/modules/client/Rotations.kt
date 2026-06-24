/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeCam
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.serverRotation
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.render.Render3D
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import java.awt.Color

object Rotations : Module("Rotations", Category.CLIENT, Category.SubCategory.CLIENT_GENERAL, gameDetecting = false) {

    /**
     * Global rotation engine override. "Per-Module" keeps each module's own Engine choice; "Modern"
     * forces every rotation through the modern engine; "Legacy" forces the classic one. This is how
     * you make the modern engine definitive across the whole client from one place.
     */
    val engine by choices("Engine", arrayOf("Per-Module", "Modern", "Legacy"), "Modern")
        .describe("Which rotation engine modules use globally.")

    private val realistic by boolean("Realistic", true)
        .describe("Mimic the game head and body rotation logic.")
    private val body by boolean("Body", true) { !realistic }
        .describe("Rotate the body along with the head.")

    private val smoothRotations by boolean("SmoothRotations", false)
        .describe("Interpolate rotations for smoother movement.")
    private val smoothingFactor by float("SmoothFactor", 0.15f, 0.1f..0.9f) { smoothRotations }
        .describe("How quickly smoothed rotations reach the target.")

    val ghost by boolean("Ghost", false)
        .describe("Render a ghost showing the server rotation.")

    val color by color("Color", Color(110, 0, 120)) { ghost }
        .describe("Color of the rotation ghost.")

    val debugRotations by boolean("DebugRotations", false)
        .describe("Enable optional rotation diagnostics.")
    private val debugMode by choices("DebugMode", arrayOf("Vector", "Chat", "Both"), "Vector") { debugRotations }
        .describe("Choose visual, chat, or combined rotation diagnostics.")
    private val debugVectorLength by float("DebugVectorLength", 4f, 0.5f..12f, " blocks") {
        debugRotations && debugMode != "Chat"
    }.describe("Length of the server-aim direction vector.")
    private val debugVectorWidth by float("DebugVectorWidth", 2f, 0.5f..5f) {
        debugRotations && debugMode != "Chat"
    }.describe("Width of the server-aim direction vector.")
    private val debugVectorColor by color("DebugVectorColor", Color(80, 190, 255, 220)) {
        debugRotations && debugMode != "Chat"
    }.describe("Color of the server-aim direction vector.")

    var prevHeadPitch = 0f
    var headPitch = 0f

    private var lastRotation: Rotation? = null

    private val specialCases
        get() = arrayListOf(FreeCam.shouldDisableRotations()).any { it }

    val onMotion = handler<MotionEvent> { event ->
        if (event.eventState != EventState.POST)
            return@handler

        val thePlayer = mc.thePlayer ?: return@handler
        val targetRotation = getRotation() ?: serverRotation

        prevHeadPitch = headPitch
        headPitch = targetRotation.pitch

        thePlayer.rotationYawHead = targetRotation.yaw
        if (shouldRotate() && body && !realistic) {
            thePlayer.renderYawOffset = thePlayer.rotationYawHead
        }

        lastRotation = targetRotation
    }

    val onRender3D = handler<Render3DEvent> { event ->
        if (!debugRotations || debugMode == "Chat") return@handler
        val player = mc.thePlayer ?: return@handler
        val rotation = getRotation() ?: return@handler
        val eyes = player.getPositionEyes(event.partialTicks)
        val direction = getVectorForRotation(rotation)
        val end = eyes.addVector(
            direction.xCoord * debugVectorLength,
            direction.yCoord * debugVectorLength,
            direction.zCoord * debugVectorLength
        )
        Render3D.drawWorldLine(eyes, end, debugVectorColor, debugVectorWidth)
    }

    fun shouldPrintDebug(): Boolean = debugRotations && debugMode != "Vector"

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
