package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import java.awt.Color
import kotlin.math.pow

class GlowRenderSettings(
    isSupported: () -> Boolean,
    defaultScale: Float = 1f,
    defaultRadius: Int = 4,
    defaultFade: Int = 10,
    defaultTargetAlpha: Float = 0f,
) : Configurable("GlowRenderSettings") {
    val renderScale by float("Glow-Renderscale", defaultScale, 0.5f..2f, isSupported = isSupported)
    val radius by int("Glow-Radius", defaultRadius, 1..5, isSupported = isSupported)
    val fade by int("Glow-Fade", defaultFade, 0..30, isSupported = isSupported)
    val targetAlpha by float("Glow-Target-Alpha", defaultTargetAlpha, 0f..1f, isSupported = isSupported)
}

class RenderFilterSettings(
    defaultMaxRenderDistance: Int,
    range: IntRange,
    defaultMaxAngleDifference: Float = 90f,
    includeThruBlocks: Boolean = true,
) : Configurable("RenderFilterSettings") {
    private val maxRenderDistanceValue = int("MaxRenderDistance", defaultMaxRenderDistance, range).onChanged { value ->
        maxRenderDistanceSq = value.toDouble().pow(2)
    }

    val maxRenderDistance by maxRenderDistanceValue
    val onLook by boolean("OnLook", false)
    val maxAngleDifference by float("MaxAngleDifference", defaultMaxAngleDifference.coerceAtLeast(5f), 5.0f..90f) { onLook }
    val thruBlocks by boolean("ThruBlocks", true) { includeThruBlocks }

    private var maxRenderDistanceSq = defaultMaxRenderDistance.toDouble().pow(2)

    fun withinDistance(distanceSquared: Double) = distanceSquared <= maxRenderDistanceSq
}

inline fun renderGlow(
    partialTicks: Float,
    color: Color,
    settings: GlowRenderSettings,
    renderer: () -> Unit,
) {
    GlowShader.startDraw(partialTicks, settings.renderScale)
    renderer()
    GlowShader.stopDraw(color, settings.radius, settings.fade, settings.targetAlpha)
}
