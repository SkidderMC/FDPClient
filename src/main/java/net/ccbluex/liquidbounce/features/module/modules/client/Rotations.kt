/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.serverRotation
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue

object Rotations : Module("Rotations", Category.CLIENT, gameDetecting = false, hideModule = false) {

    private val realistic by BoolValue("Realistic", false)
    private val body by BoolValue("Body", true) { !realistic }
    val debugRotations by BoolValue("DebugRotations", false)

    val ghost by BoolValue("Ghost", true)

    val colorRedValue by IntegerValue("R", 0, 0..255) { ghost }
    val colorGreenValue by IntegerValue("G", 160, 0..255) { ghost }
    val colorBlueValue by IntegerValue("B", 255, 0..255) { ghost }
    val alphaValue by IntegerValue("Alpha", 255, 0..255) { ghost }
    val rainbow by BoolValue("RainBow", false) { ghost }

    var prevHeadPitch = 0f
    var headPitch = 0f

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        prevHeadPitch = headPitch
        headPitch = serverRotation.pitch

        if (!shouldRotate() || realistic) {
            return
        }

        thePlayer.rotationYawHead = serverRotation.yaw

        if (body) {
            thePlayer.renderYawOffset = thePlayer.rotationYawHead
        }
    }

    fun lerp(tickDelta: Float, old: Float, new: Float): Float {
        return old + (new - old) * tickDelta
    }

    /**
     * Rotate when current rotation is not null or special modules which do not make use of RotationUtils like Derp are enabled.
     */
    fun shouldRotate() = state || currentRotation != null

    /**
     * Imitate the game's head and body rotation logic
     */
    fun shouldUseRealisticMode() = realistic && shouldRotate()

    /**
     * Which rotation should the module use?
     */
    fun getRotation(useServerRotation: Boolean) = if (useServerRotation) serverRotation else currentRotation
}
