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
        .describe("Resolution scale of the glow render pass.")
    val radius by int("Glow-Radius", defaultRadius, 1..5, isSupported = isSupported)
        .describe("Blur radius of the glow effect.")
    val fade by int("Glow-Fade", defaultFade, 0..30, isSupported = isSupported)
        .describe("How far the glow fades out from the edge.")
    val targetAlpha by float("Glow-Target-Alpha", defaultTargetAlpha, 0f..1f, isSupported = isSupported)
        .describe("Opacity of the inner part of the glow.")
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
        .describe("Only render entities you are looking at.")
    val maxAngleDifference by float("MaxAngleDifference", defaultMaxAngleDifference.coerceAtLeast(5f), 5.0f..90f) { onLook }
        .describe("Max angle from your view to count as looked at.")
    val thruBlocks by boolean("ThruBlocks", true) { includeThruBlocks }
        .describe("Render entities hidden behind blocks.")

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
