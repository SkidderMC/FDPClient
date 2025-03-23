/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.SmoothStepAnimation
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import org.lwjgl.opengl.GL11.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import java.util.Random

object LineGraphs : Module("LineGlyphs", Category.VISUAL, gameDetecting = false) {

    init {
        state = true
    }

    val slowSpeed by boolean("Slow Speed", false)
    private val glyphCount by int("Glyphs Count", 70, 0..200)

    private val random = Random(93882L)
    private val temp3DVectors = mutableListOf<Vec3>()
    private val glyphVectorGenerators = mutableListOf<GlyphVectorGenerator>()
    private val tessellator: Tessellator = Tessellator.getInstance()

    private val random360X: Int get() = random.nextInt(4) * 90
    private val random360Y: Int get() = (random.nextInt(4) - 2) * 90

    override fun onDisable() {
        super.onDisable()
        glyphVectorGenerators.clear()
        GlyphVectorRenderer.restoreDefaultGlState()
    }

    private fun lineMovementSteps() = intArrayOf(0, 3)
    private fun lineStepsRange() = intArrayOf(7, 12)
    private fun spawnRange() = intArrayOf(6, 24, 0, 12)
    private fun maxGlyphCount() = glyphCount

    private fun Int.mod360(): Int = ((this % 360) + 360) % 360

    private fun getRandom90Rotation(previous: IntArray): IntArray {
        val newA = (previous[0] + if (random.nextBoolean()) 90 else -90).mod360()
        val newB = (previous[1] + if (random.nextBoolean()) 90 else -90).mod360()
        return intArrayOf(newA, newB)
    }

    private fun offsetFromRotation(base: Vec3i, rotation: IntArray, step: Int): Vec3i {
        val yawRad = Math.toRadians(rotation[0].toDouble())
        val pitchRad = Math.toRadians(rotation[1].toDouble())
        val horizontalStep = step * cos(pitchRad)
        val xOffset = (-sin(yawRad) * horizontalStep).toInt()
        val yOffset = (sin(pitchRad) * step).toInt()
        val zOffset = (cos(yawRad) * horizontalStep).toInt()
        return Vec3i(base.x + xOffset, base.y + yOffset, base.z + zOffset)
    }

    private fun calculateMoveAdvance(totalTicks: Int, ticksRemaining: Int, partialTicks: Float): Float {
        val fraction = 1.0f - (ticksRemaining - partialTicks) / totalTicks
        return min(max(fraction, 0f), 1f)
    }

    private fun lerp(t: Float, start: Int, end: Int): Double =
        (start + (end - start) * t).toDouble()

    private fun smoothLerpVectors(vectors: List<Vec3i>, moveAdvance: Float): List<Vec3> {
        temp3DVectors.clear()
        if (vectors.isEmpty()) return temp3DVectors
        vectors.forEachIndexed { i, current ->
            val (x, y, z) = if (i == vectors.lastIndex && i > 0) {
                val previous = vectors[i - 1]
                Triple(lerp(moveAdvance, previous.x, current.x),
                    lerp(moveAdvance, previous.y, current.y),
                    lerp(moveAdvance, previous.z, current.z))
            } else {
                Triple(current.x.toDouble(), current.y.toDouble(), current.z.toDouble())
            }
            temp3DVectors.add(Vec3(x, y, z))
        }
        return temp3DVectors
    }

    private fun randomGlyphSpawnPosition(): Vec3i {
        val range = spawnRange()
        val distance = random.nextInt(range[1] - range[0] + 1) + range[0]
        val player = mc.thePlayer ?: return Vec3i(0, 0, 0)
        val fov = mc.gameSettings.fovSetting
        val baseYaw = player.rotationYaw
        val minYaw = (baseYaw - fov * 0.75).toInt()
        val maxYaw = (baseYaw + fov * 0.75).toInt()
        val randomYaw = random.nextInt(maxYaw - minYaw + 1) + minYaw
        val radYaw = Math.toRadians(randomYaw.toDouble())
        val xOffset = (-(sin(radYaw) * distance)).toInt()
        val yOffset = random.nextInt(range[3] - (-range[2]) + 1) + (-range[2])
        val zOffset = (cos(radYaw) * distance).toInt()
        val renderManager = mc.renderManager ?: return Vec3i(0, 0, 0)
        return Vec3i(
            (renderManager.viewerPosX + xOffset).toInt(),
            (renderManager.viewerPosY + yOffset).toInt(),
            (renderManager.viewerPosZ + zOffset).toInt()
        )
    }

    private fun addOneGlyph() {
        val stepsRange = lineStepsRange()
        val spawnPos = randomGlyphSpawnPosition()
        val minSteps = stepsRange[0]
        val maxSteps = stepsRange[1]
        val steps = random.nextInt(maxSteps - minSteps + 1) + minSteps
        glyphVectorGenerators.add(GlyphVectorGenerator(spawnPos, steps))
    }

    private fun removeExpiredGlyphs() {
        glyphVectorGenerators.removeIf { it.isExpired }
    }

    private fun updateGlyphs() {
        glyphVectorGenerators.forEach { it.update() }
    }

    private fun drawAllGlyphs(partialTicks: Float) {
        if (glyphVectorGenerators.isEmpty()) return
        GlyphVectorRenderer.with3DRendering {
            glyphVectorGenerators.forEachIndexed { index, glyph ->
                GlyphVectorRenderer.renderGlyph(glyph, index + 1, glyph.currentAlpha, partialTicks)
            }
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (!state || mc.thePlayer == null) return@handler

        if (glyphVectorGenerators.size < maxGlyphCount()) {
            addOneGlyph()
        }
        updateGlyphs()
        removeExpiredGlyphs()
    }

    val onRender3D = handler<Render3DEvent> { event ->
        if (!state || mc.thePlayer == null) return@handler
        removeExpiredGlyphs()
        drawAllGlyphs(event.partialTicks)
    }

    private class GlyphVectorGenerator(spawnPos: Vec3i, maxStepsAmount: Int) {
        private var lifeTicks = 80

        val vectorList = mutableListOf<Vec3i>().apply { add(spawnPos) }
        private var currentStepTicks = 0
        private var lastStepTicks = 0
        private var stepsRemaining = maxStepsAmount
        private var lastRotation = intArrayOf(random360X, random360Y)
        private var fadeOut: Double = 1.0
        val animation = SmoothStepAnimation(400, 1.0, Direction.FORWARDS)
        val currentAlpha: Float get() = fadeOut.toFloat()

        init {
            animation.setDirection(Direction.FORWARDS)
        }

        fun update() {
            lifeTicks--
            if (lifeTicks <= 0) {
                animation.setDirection(Direction.BACKWARDS)
                fadeOut = max(fadeOut - 0.05, 0.0)
                return
            }
            if (stepsRemaining == 0) {
                if (currentStepTicks > 0) {
                    currentStepTicks -= if (slowSpeed) 1 else 2
                    if (currentStepTicks < 0) currentStepTicks = 0
                } else {
                    animation.setDirection(Direction.BACKWARDS)
                    fadeOut = max(fadeOut - 0.05, 0.0)
                }
                return
            }
            if (currentStepTicks > 0) {
                currentStepTicks -= if (slowSpeed) 1 else 2
                if (currentStepTicks < 0) currentStepTicks = 0
                return
            }
            lastRotation = getRandom90Rotation(lastRotation)
            val movementSteps = lineMovementSteps()
            val minStep = movementSteps[0]
            val maxStep = movementSteps[1]
            currentStepTicks = random.nextInt(maxStep - minStep + 1) + minStep
            lastStepTicks = currentStepTicks
            val lastPos = vectorList.last()
            vectorList.add(offsetFromRotation(lastPos, lastRotation, currentStepTicks))
            stepsRemaining--
        }

        fun getPositionVectors(partialTicks: Float): List<Vec3> {
            val moveAdvance = calculateMoveAdvance(lastStepTicks, currentStepTicks, partialTicks)
            return smoothLerpVectors(vectorList, moveAdvance)
        }

        val isExpired: Boolean
            get() = animation.finished(Direction.BACKWARDS) && fadeOut <= 0.0
    }

    private object GlyphVectorRenderer {
        fun with3DRendering(render: () -> Unit) {
            glPushAttrib(GL_ALL_ATTRIB_BITS)
            glPushMatrix()
            try {
                val renderManager = mc.renderManager
                val viewerX = renderManager.viewerPosX
                val viewerY = renderManager.viewerPosY
                val viewerZ = renderManager.viewerPosZ
                GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0)
                glEnable(GL_BLEND)
                glLineWidth(1.0f)
                glPointSize(1.0f)
                glEnable(GL_POLYGON_SMOOTH)
                glDisable(GL_TEXTURE_2D)
                mc.entityRenderer.disableLightmap()
                glDisable(GL_LIGHTING)
                glShadeModel(GL_SMOOTH)
                glAlphaFunc(GL_GREATER, 0.003921569f)
                glDisable(GL_CULL_FACE)
                glDepthMask(false)
                glEnable(GL_LINE_SMOOTH)
                glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
                glTranslated(-viewerX, -viewerY, -viewerZ)
                render()
                glTranslated(viewerX, viewerY, viewerZ)
            } finally {
                glPopMatrix()
                glPopAttrib()
            }
        }

        fun calcLineWidth(glyph: GlyphVectorGenerator): Float {
            val renderManager = mc.renderManager
            val cameraPos = Vec3(
                renderManager.renderPosX,
                renderManager.renderPosY,
                renderManager.renderPosZ
            )
            val furthestPoint = glyph.vectorList.maxByOrNull {
                val dx = it.x - cameraPos.xCoord
                val dy = it.y - cameraPos.yCoord
                val dz = it.z - cameraPos.zCoord
                dx * dx + dy * dy + dz * dz
            } ?: return 1f
            val dx = furthestPoint.x - cameraPos.xCoord
            val dy = furthestPoint.y - cameraPos.yCoord
            val dz = furthestPoint.z - cameraPos.zCoord
            val dist = sqrt((dx * dx + dy * dy + dz * dz))
            val factor = 1.0 - dist / 20.0
            return 1.0E-4f + 3.0f * MathHelper.clamp_float(factor.toFloat(), 0f, 1f)
        }

        fun renderGlyph(glyph: GlyphVectorGenerator, colorIndex: Int, alphaPercentage: Float, partialTicks: Float) {
            if (glyph.vectorList.size < 2) return
            val lineWidth = calcLineWidth(glyph)
            glLineWidth(lineWidth)
            val worldRenderer = tessellator.worldRenderer
            worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR)
            var currentColorIndex = colorIndex
            val lineVectors = glyph.getPositionVectors(partialTicks)
            lineVectors.forEach { pos ->
                val color = ClientThemesUtils.getColor(currentColorIndex)
                val r = color.red / 255f
                val g = color.green / 255f
                val b = color.blue / 255f
                worldRenderer.pos(pos.xCoord, pos.yCoord, pos.zCoord)
                    .color(r, g, b, alphaPercentage)
                    .endVertex()
                currentColorIndex += 180
            }
            tessellator.draw()
            glPointSize(lineWidth * 3f)
            worldRenderer.begin(GL_POINTS, DefaultVertexFormats.POSITION_COLOR)
            currentColorIndex = colorIndex
            val pointVectors = glyph.getPositionVectors(partialTicks)
            pointVectors.forEach { pos ->
                val color = ClientThemesUtils.getColor(currentColorIndex)
                val r = color.red / 255f
                val g = color.green / 255f
                val b = color.blue / 255f
                worldRenderer.pos(pos.xCoord, pos.yCoord, pos.zCoord)
                    .color(r, g, b, alphaPercentage)
                    .endVertex()
                currentColorIndex += 180
            }
            tessellator.draw()
        }

        fun restoreDefaultGlState() {
            glPushAttrib(GL_ALL_ATTRIB_BITS)
            try {
                glLineWidth(1.0f)
                glPointSize(1.0f)
                glEnable(GL_TEXTURE_2D)
                glEnable(GL_LIGHTING)
                glEnable(GL_CULL_FACE)
                glDepthMask(true)
                glShadeModel(GL_FLAT)
                glAlphaFunc(GL_GREATER, 0.1f)
                GlStateManager.resetColor()
            } finally {
                glPopAttrib()
            }
        }
    }
}
