/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.util.MathHelper

object Derp : Module("Derp", Category.VISUAL, Category.SubCategory.RENDER_SELF) {

    private val yawMode by choices("YawMode", arrayOf("Static", "Offset", "Random", "Jitter", "Spin"), "Random")
        .describe("How the fake yaw rotation is generated.")
    private val staticYaw by float("StaticYaw", 0f, -180f..180f) { yawMode == "Static" }
        .describe("Fixed yaw value in Static mode.")
    private val yawOffset by float("YawOffset", 0f, -180f..180f) { yawMode == "Offset" }
        .describe("Yaw offset added in Offset mode.")
    private val spinSpeed by float("SpinSpeed", 50f, -70f..70f) { yawMode == "Spin" }
        .describe("Yaw spin speed in Spin mode.")
    private val jitterForward by int("JitterForwardTicks", 2, 0..100) { yawMode == "Jitter" }
        .describe("Ticks facing forward in Jitter mode.")
    private val jitterBackward by int("JitterBackwardTicks", 2, 0..100) { yawMode == "Jitter" }
        .describe("Ticks facing backward in Jitter mode.")

    private val pitchMode by choices("PitchMode", arrayOf("Static", "Offset", "Random"), "Random")
        .describe("How the fake pitch rotation is generated.")
    private val staticPitch by float("StaticPitch", -90f, -180f..180f) { pitchMode == "Static" }
        .describe("Fixed pitch value in Static mode.")
    private val pitchOffset by float("PitchOffset", 0f, -180f..180f) { pitchMode == "Offset" }
        .describe("Pitch offset added in Offset mode.")

    private val safePitch by boolean("SafePitch", true)
        .describe("Clamp pitch to a server-safe range.")
    private val notDuringSprint by boolean("NotDuringSprint", true)
        .describe("Disable the effect while sprinting.")

    private val yawGroup = Configurable("Yaw")
    private val pitchGroup = Configurable("Pitch")
    private val generalGroup = Configurable("General")

    init {
        moveValues(yawGroup,
            "YawMode", "StaticYaw", "YawOffset", "SpinSpeed",
            "JitterForwardTicks", "JitterBackwardTicks")
        moveValues(pitchGroup, "PitchMode", "StaticPitch", "PitchOffset")
        moveValues(generalGroup, "SafePitch", "NotDuringSprint")

        addValues(listOf(yawGroup, pitchGroup, generalGroup))
    }
    private var spinYaw = 0f
    private var jitterTick = 0

    override fun onDisable() {
        spinYaw = 0f
        jitterTick = 0
        super.onDisable()
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (notDuringSprint && (mc.gameSettings.keyBindSprint.isKeyDown || player.isSprinting)) {
            return@handler
        }

        val yaw = when (yawMode) {
            "Static" -> staticYaw
            "Offset" -> player.rotationYaw + yawOffset
            "Random" -> randomRange(-180f, 180f)
            "Jitter" -> {
                val forward = jitterForward
                val backward = jitterBackward
                val total = forward + backward
                val result = if (total <= 0 || jitterTick < forward) {
                    player.rotationYaw
                } else {
                    player.rotationYaw + 180f
                }
                jitterTick++
                if (total <= 0 || jitterTick >= total) {
                    jitterTick = 0
                }
                result
            }
            "Spin" -> {
                spinYaw += spinSpeed
                spinYaw
            }
            else -> player.rotationYaw
        }

        var pitch = when (pitchMode) {
            "Static" -> staticPitch
            "Offset" -> player.rotationPitch + pitchOffset
            "Random" -> if (safePitch) randomRange(-90f, 90f) else randomRange(-180f, 180f)
            else -> player.rotationPitch
        }

        if (safePitch) {
            pitch = MathHelper.clamp_float(pitch, -90f, 90f)
        }

        player.rotationYaw = yaw
        player.rotationPitch = pitch
    }

    private fun randomRange(min: Float, max: Float): Float = (min + Math.random().toFloat() * (max - min))
}
