/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/**
 * Zooms the camera in while enabled by lowering the field of view. Bind it to a key and toggle it.
 * The real FOV is saved on enable and restored on disable so it never persists.
 * Speed and DurationFactor control how the FOV transitions; max speed snaps instantly.
 */
object Zoom : Module("Zoom", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val zoom by float("Zoom", 3f, 1.5f..10f)
    private val speed by float("Speed", 100f, 1f..100f)
    private val durationFactor by float("DurationFactor", 1f, 0.1f..5f, "x")

    private var savedFov = -1f
    private var currentFov = -1f
    private var lastTime = 0L

    override fun onEnable() {
        savedFov = mc.gameSettings.fovSetting
        currentFov = savedFov
        lastTime = System.currentTimeMillis()
        super.onEnable()
    }

    val onUpdate = handler<UpdateEvent> {
        if (savedFov <= 0f) {
            savedFov = mc.gameSettings.fovSetting
            currentFov = savedFov
        }

        val now = System.currentTimeMillis()
        val delta = (now - lastTime).coerceAtLeast(0L)
        lastTime = now

        val target = savedFov / zoom

        // Max speed snaps instantly, reproducing the original behavior.
        if (speed >= 100f && durationFactor <= 1f) {
            currentFov = target
        } else {
            val step = (speed * (delta / 50f)) / durationFactor.coerceAtLeast(0.001f)
            currentFov = approach(currentFov, target, step)
        }

        mc.gameSettings.fovSetting = currentFov
    }

    override fun onDisable() {
        if (savedFov > 0f) mc.gameSettings.fovSetting = savedFov
        savedFov = -1f
        currentFov = -1f
        super.onDisable()
    }

    private fun approach(current: Float, target: Float, step: Float): Float {
        if (step >= 100f) return target
        val diff = target - current
        val move = diff * (step / 100f).coerceIn(0f, 1f)
        return if (kotlin.math.abs(diff) <= 0.01f) target else current + move
    }
}
