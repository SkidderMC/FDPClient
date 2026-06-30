/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColor
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.random.Random

object DamageParticle : Module("DamageParticle", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {

    private val aliveTicks by int("AliveTicks", 50, 10..50)
        .describe("How many ticks a particle stays visible.")
    private val size by int("Size", 3, 1..7)
        .describe("Size of the damage text particles.")
    private val offsetDistance by float("OffsetDistance", 0.5f, 0f..2f)
        .describe("Random spawn offset from the entity.")
    private val randomRotation by boolean("RandomRotation", false)
        .describe("Spawn particles at random rotations.")
    private val riseSpeed by float("RiseSpeed", 0.012f, 0f..0.05f)
        .describe("Vertical movement applied to each particle per tick.")
    private val fade by boolean("Fade", true)
        .describe("Fade particles during the final half of their lifetime.")
    private val throughWalls by boolean("ThroughWalls", false)
        .describe("Render particles through terrain.")
    private val colorMode by choices("ColourType", arrayOf("Damage", "Custom", "Client"), "Damage")
        .describe("How the particle text is colored.")
    private val customColor by color("Color", Color.WHITE) { colorMode == "Custom" }
        .describe("Custom color for the particle text.")
    private val shadowMode by choices("Shadow", arrayOf("Normal", "Default", "Vanilla", "Outline", "None"), "Outline") { colorMode != "Damage" }
        .describe("Shadow style for the particle text.")

    private val particleGroup = Configurable("Particle")
    private val motionGroup = Configurable("Motion")
    private val coloringGroup = Configurable("Coloring")

    init {
        moveValues(particleGroup,
            "AliveTicks", "Size", "OffsetDistance", "RandomRotation", "ThroughWalls")

        moveValues(motionGroup,
            "RiseSpeed", "Fade")

        moveValues(coloringGroup,
            "ColourType", "Color", "Shadow")

        addValues(listOf(particleGroup, motionGroup, coloringGroup))
    }

    private fun moveValues(group: Configurable, vararg names: String) {
        for (name in names) {
            values.filter { it.matchesKey(name) }.forEach(group::addValue)
        }
    }

    private const val MAX_PARTICLES = 100
    private const val MAX_HEALTH_DATA_SIZE = 200

    private val healthData = mutableMapOf<Int, Float>()
    private val particles = mutableListOf<SingleParticle>()

    val onUpdate = handler<UpdateEvent> {
        synchronized(particles) {
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity !is EntityLivingBase || !EntityUtils.isSelected(entity, true)) continue

                val lastHealth = healthData.put(entity.entityId, entity.health) ?: continue
                if (lastHealth == entity.health) continue

                val kind = if (lastHealth > entity.health) ParticleKind.DAMAGE else ParticleKind.HEAL
                val prefix = if (colorMode == "Client") {
                    if (kind == ParticleKind.DAMAGE) "-" else "+"
                } else {
                    "\u2665"
                }
                val amount = BigDecimal.valueOf(abs((lastHealth - entity.health).toDouble()))
                    .setScale(1, RoundingMode.HALF_UP)
                    .toPlainString()

                if (particles.size >= MAX_PARTICLES) particles.removeAt(0)
                val spread = offsetDistance.toDouble()
                particles += SingleParticle(
                    str = prefix + amount,
                    kind = kind,
                    posX = entity.posX + randomOffset(spread),
                    posY = entity.entityBoundingBox.minY + entity.height * 0.5,
                    posZ = entity.posZ + randomOffset(spread),
                    roll = if (randomRotation) Random.nextInt(360).toFloat() else 0f,
                )
            }

            particles.removeAll { particle -> ++particle.ticks > aliveTicks }

            if (healthData.size > MAX_HEALTH_DATA_SIZE) {
                val activeEntityIds = mc.theWorld.loadedEntityList
                    .filterIsInstance<EntityLivingBase>()
                    .mapTo(HashSet()) { it.entityId }
                healthData.keys.retainAll(activeEntityIds)
            }
        }
    }

    val onRender3D = handler<Render3DEvent> { event ->
        synchronized(particles) {
            val renderManager = mc.renderManager
            val particleSize = size * 0.01

            glPushAttrib(GL_ENABLE_BIT or GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_POLYGON_BIT)
            try {
                if (throughWalls) glDisable(GL_DEPTH_TEST)
                glDepthMask(false)
                enablePolygonOffset()
                doPolygonOffset(1f, -1500000f)

                for (particle in particles) {
                    val age = particle.ticks + event.partialTicks
                    val x = particle.posX - renderManager.renderPosX
                    val y = particle.posY + age * riseSpeed - renderManager.renderPosY
                    val z = particle.posZ - renderManager.renderPosZ

                    glPushMatrix()
                    try {
                        translate(x.toFloat(), y.toFloat(), z.toFloat())
                        rotate(-renderManager.playerViewY, 0f, 1f, 0f)
                        rotate(renderManager.playerViewX, if (mc.gameSettings.thirdPersonView == 2) -1f else 1f, 0f, 0f)
                        if (particle.roll != 0f) rotate(particle.roll, 0f, 0f, 1f)
                        scale(-particleSize, -particleSize, particleSize)
                        drawParticle(particle, age)
                    } finally {
                        glPopMatrix()
                    }
                }
            } finally {
                disablePolygonOffset()
                resetColor()
                glPopAttrib()
            }
        }
    }

    val onWorld = handler<WorldEvent> { clearState() }

    override fun onDisable() = clearState()

    private fun drawParticle(particle: SingleParticle, age: Float) {
        val font = mc.fontRendererObj
        val textWidth = font.getStringWidth(particle.str)
        val textHeight = font.FONT_HEIGHT - 1
        val alpha = if (!fade || age <= aliveTicks * 0.5f) {
            255
        } else {
            (255f * ((aliveTicks - age) / (aliveTicks * 0.5f))).toInt().coerceIn(0, 255)
        }
        val baseColor = when (colorMode) {
            "Client" -> getColor(1)
            "Custom" -> customColor
            else -> if (particle.kind == ParticleKind.DAMAGE) Color(255, 85, 85) else Color(85, 255, 85)
        }
        val textColor = Color(baseColor.red, baseColor.green, baseColor.blue, alpha).rgb

        if (colorMode != "Damage") {
            val shadowAlpha = (alpha * 0.6f).toInt()
            val shadowColor = Color(0, 0, 0, shadowAlpha).rgb
            val shadowOffset = 0.5f
            when (shadowMode) {
                "Normal" -> font.drawString(particle.str, -(textWidth / 2) + 1, -textHeight + 1, shadowColor)
                "Vanilla" -> font.drawString(particle.str, -(textWidth / 2) + 1, -textHeight + 1, Color(20, 20, 20, shadowAlpha).rgb)
                "Default" -> font.drawString(particle.str, -(textWidth / 2) + shadowOffset, -textHeight + shadowOffset, shadowColor, false)
                "Outline" -> {
                    font.drawString(particle.str, -(textWidth / 2) + shadowOffset, -textHeight + shadowOffset, shadowColor, false)
                    font.drawString(particle.str, -(textWidth / 2) - shadowOffset, -textHeight - shadowOffset, shadowColor, false)
                    font.drawString(particle.str, -(textWidth / 2) + shadowOffset, -textHeight - shadowOffset, shadowColor, false)
                    font.drawString(particle.str, -(textWidth / 2) - shadowOffset, -textHeight + shadowOffset, shadowColor, false)
                }
            }
        }
        font.drawString(particle.str, (-(textWidth / 2)).toFloat(), (-textHeight).toFloat(), textColor, false)
    }

    private fun randomOffset(spread: Double): Double = if (spread == 0.0) 0.0 else Random.nextDouble(-spread, spread)

    private fun clearState() = synchronized(particles) {
        particles.clear()
        healthData.clear()
    }

    private enum class ParticleKind { DAMAGE, HEAL }

    private class SingleParticle(
        val str: String,
        val kind: ParticleKind,
        val posX: Double,
        val posY: Double,
        val posZ: Double,
        val roll: Float,
    ) {
        var ticks = 0
    }
}
