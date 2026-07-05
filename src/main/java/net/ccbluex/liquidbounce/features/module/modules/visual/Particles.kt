/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.RenderColor.color
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

object Particles : Module("Particles", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val particleSize by float("Size", 1f, 0.5f..2f)
        .describe("Size of each spawned particle.")
    private val minCount by int("MinCount", 2, 1..30)
        .describe("Minimum particles spawned per hit.")
    private val maxCount by int("MaxCount", 10, 1..30)
        .describe("Maximum particles spawned per hit.")
    private val randomRotation by boolean("RandomRotation", true)
        .describe("Give each particle a random rotation.")
    private val shape by choices("Shape", arrayOf("Star", "Heart", "Rhombus", "Point", "Spark", "Line"), "Star")
        .describe("Shape drawn for each particle.")

    private val motion by float("Motion", 15f, 1f..30f)
        .describe("Speed of particle movement.")
    private val bounceX by float("BounceX", 0.8f, 0f..1f)
        .describe("Bounce energy kept on the X axis.")
    private val bounceY by float("BounceY", 0.6f, 0f..1f)
        .describe("Bounce energy kept on the Y axis.")
    private val bounceZ by float("BounceZ", 0.8f, 0f..1f)
        .describe("Bounce energy kept on the Z axis.")
    private val drag by float("Drag", 0.99f, 0f..1f)
        .describe("Air drag applied to particle velocity.")
    private val gravityFactor by float("GravityFactor", 0.8f, 0f..1f)
        .describe("Strength of gravity pulling particles down.")

    private val color by color("Color", Color.RED)
        .describe("Color of the spawned particles.")

    private val appearanceGroup = Configurable("Appearance")
    private val spawnGroup = Configurable("Spawn")
    private val physicsGroup = Configurable("Physics")
    private val bounceGroup = Configurable("Bounce")

    init {
        moveValues(appearanceGroup, "Size", "Shape", "RandomRotation", "Color")
        moveValues(spawnGroup, "MinCount", "MaxCount")
        moveValues(physicsGroup, "Motion", "Drag", "GravityFactor")
        moveValues(bounceGroup, "BounceX", "BounceY", "BounceZ")

        addValues(listOf(
            appearanceGroup, spawnGroup, physicsGroup, bounceGroup,
        ))
    }
    private const val MAX_PARTICLES = 200
    private val particles = mutableListOf<Particle>()
    private var lastSpawn = 0L

    private val gravity: Double
        get() = gravityFactor.toDouble() * 0.03125

    override fun onDisable() {
        particles.clear()
    }

    val onWorld = handler<WorldEvent> {
        particles.clear()
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val camera = player.positionVector.addVector(0.0, player.getEyeHeight().toDouble(), 0.0)
        synchronized(particles) {
            particles.removeIf { particle ->
                if (particle.alpha <= 0f || camera.squareDistanceTo(particle.pos) > 30.0 * 30.0) {
                    true
                } else {
                    particle.update(camera)
                    false
                }
            }
        }
    }

    val onAttack = handler<AttackEvent> { event ->
        val target = event.targetEntity as? EntityLivingBase ?: return@handler
        val now = System.currentTimeMillis()
        if (now - lastSpawn < 230L) return@handler
        lastSpawn = now

        val box = target.entityBoundingBox
        val pos = Vec3(
            (box.minX + box.maxX) / 2.0,
            (box.minY + box.maxY) / 2.0,
            (box.minZ + box.maxZ) / 2.0
        )

        val lo = minOf(minCount, maxCount)
        val hi = maxOf(minCount, maxCount)
        val amount = if (lo == hi) lo else Random.nextInt(lo, hi + 1)

        synchronized(particles) {
            repeat(amount) {
                if (particles.size < MAX_PARTICLES) {
                    particles.add(Particle(pos))
                }
            }
        }
    }

    val onRender3D = handler<Render3DEvent> { event ->
        synchronized(particles) {
            if (particles.isEmpty()) return@handler

            val renderManager = mc.renderManager
            glPushMatrix()
            mc.entityRenderer.disableLightmap()
            glEnable(GL_BLEND)
            tryBlendFuncSeparate(770, 771, 1, 0)
            glDisable(GL_TEXTURE_2D)
            glDisable(GL_LIGHTING)
            glDisable(GL_CULL_FACE)
            glEnable(GL_LINE_SMOOTH)
            glEnable(GL_POINT_SMOOTH)
            glDepthMask(false)
            glShadeModel(GL_SMOOTH)
            glLineWidth(2f)

            for (particle in particles) {
                if (!particle.visible) continue

                val pt = event.partialTicks.toDouble()
                val ix = particle.prevPos.xCoord + (particle.pos.xCoord - particle.prevPos.xCoord) * pt
                val iy = particle.prevPos.yCoord + (particle.pos.yCoord - particle.prevPos.yCoord) * pt
                val iz = particle.prevPos.zCoord + (particle.pos.zCoord - particle.prevPos.zCoord) * pt
                val x = ix - renderManager.renderPosX
                val y = iy - renderManager.renderPosY
                val z = iz - renderManager.renderPosZ

                glPushMatrix()
                glTranslated(x, y, z)
                glRotatef(-renderManager.playerViewY, 0f, 1f, 0f)
                glRotatef(
                    renderManager.playerViewX,
                    if (mc.gameSettings.thirdPersonView == 2) -1f else 1f,
                    0f,
                    0f
                )
                if (randomRotation) glRotatef(particle.rotation, 0f, 0f, 1f)

                val size = particleSize * 0.25f * max(0f, 1f - particle.age() / 12f)
                val alpha = (particle.alpha * (color.alpha / 255f)).coerceIn(0f, 1f)
                color(Color(color.red, color.green, color.blue, (alpha * 255f).toInt().coerceIn(0, 255)).rgb)

                drawShape(size)
                glPopMatrix()
            }

            glLineWidth(1f)
            glPointSize(1f)
            glDepthMask(true)
            glShadeModel(GL_FLAT)
            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_POINT_SMOOTH)
            glEnable(GL_CULL_FACE)
            glEnable(GL_TEXTURE_2D)
            resetColor()
            tryBlendFuncSeparate(770, 771, 1, 0)
            glPopMatrix()
        }
    }

    private fun drawShape(size: Float) {
        when (shape) {
            "Heart" -> {
                glBegin(GL_TRIANGLE_FAN)
                glVertex2f(0f, -size * 0.6f)
                var a = 0
                while (a <= 360) {
                    val t = Math.toRadians(a.toDouble())
                    val hx = (16.0 * sin(t) * sin(t) * sin(t)) / 17.0 * size
                    val hy = -(13.0 * cos(t) - 5.0 * cos(2 * t) - 2.0 * cos(3 * t) - cos(4 * t)) / 17.0 * size
                    glVertex2d(hx, hy)
                    a += 12
                }
                glEnd()
            }
            "Rhombus" -> {
                glBegin(GL_TRIANGLE_FAN)
                glVertex2f(0f, 0f)
                glVertex2f(0f, -size)
                glVertex2f(size, 0f)
                glVertex2f(0f, size)
                glVertex2f(-size, 0f)
                glVertex2f(0f, -size)
                glEnd()
            }
            "Point" -> {
                glPointSize(size * 40f)
                glBegin(GL_POINTS)
                glVertex2f(0f, 0f)
                glEnd()
            }
            "Spark" -> {
                glBegin(GL_LINES)
                for (i in 0 until 6) {
                    val ang = (i * 60.0) * PI / 180.0
                    glVertex2f(0f, 0f)
                    glVertex2d(cos(ang) * size, sin(ang) * size)
                }
                glEnd()
            }
            "Line" -> {
                glBegin(GL_LINES)
                glVertex2f(0f, -size)
                glVertex2f(0f, size)
                glEnd()
            }
            else -> {
                glBegin(GL_TRIANGLE_FAN)
                glVertex2f(0f, 0f)
                var i = 0
                while (i <= 10) {
                    val ang = (i * 36.0 - 90.0) * PI / 180.0
                    val r = if (i % 2 == 0) size.toDouble() else size * 0.4
                    glVertex2d(cos(ang) * r, sin(ang) * r)
                    i++
                }
                glEnd()
            }
        }
    }

    private class Particle(var pos: Vec3) {
        var prevPos = pos
        private var velocity = Vec3(
            (Random.nextDouble() - 0.5) * 0.02,
            0.01 + Random.nextDouble() * 0.01,
            (Random.nextDouble() - 0.5) * 0.02
        )
        var alpha = 1f
        var visible = true
        val rotation = Random.nextFloat() * 360f
        private val spawnTime = System.currentTimeMillis()
        private var collisionTime = -1L

        fun age() = (System.currentTimeMillis() - spawnTime) / 1000f

        private fun collidesAt(x: Double, y: Double, z: Double): Boolean {
            val s = 0.05
            return mc.theWorld.getCollisionBoxes(
                AxisAlignedBB(x - s, y - s, z - s, x + s, y + s, z + s)
            ).isNotEmpty()
        }

        fun update(camera: Vec3) {
            prevPos = pos

            if (collisionTime != -1L) {
                val since = System.currentTimeMillis() - collisionTime
                alpha = max(0f, 1f - since / 3000f)
            }

            val speed = motion.toDouble()
            velocity = velocity.addVector(0.0, -gravity, 0.0)
            var next = pos.addVector(velocity.xCoord * speed, velocity.yCoord, velocity.zCoord * speed)

            if (collidesAt(next.xCoord, next.yCoord, next.zCoord)) {
                if (collisionTime == -1L) collisionTime = System.currentTimeMillis()

                when {
                    collidesAt(pos.xCoord + velocity.xCoord * speed, pos.yCoord, pos.zCoord) ->
                        velocity = Vec3(-velocity.xCoord * bounceX, velocity.yCoord, velocity.zCoord)
                    collidesAt(pos.xCoord, pos.yCoord + velocity.yCoord, pos.zCoord) ->
                        velocity = Vec3(
                            velocity.xCoord * drag,
                            -velocity.yCoord * bounceY,
                            velocity.zCoord * drag
                        )
                    collidesAt(pos.xCoord, pos.yCoord, pos.zCoord + velocity.zCoord * speed) ->
                        velocity = Vec3(velocity.xCoord, velocity.yCoord, -velocity.zCoord * bounceZ)
                }

                next = pos.addVector(velocity.xCoord * speed, velocity.yCoord, velocity.zCoord * speed)
            }

            pos = next
            visible = mc.theWorld.rayTraceBlocks(camera, pos) == null
        }
    }
}
